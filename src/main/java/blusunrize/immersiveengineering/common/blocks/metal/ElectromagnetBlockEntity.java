/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IScrewdriverInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.ResettableCapability;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ElectromagnetBlockEntity extends IEBaseBlockEntity implements IEServerTickableBE, IStateBasedDirectional, IScrewdriverInteraction
{
	public MutableEnergyStorage energyStorage = new MutableEnergyStorage(32000);
	private final ResettableCapability<IEnergyStorage> energyCap = registerEnergyInput(energyStorage);
	public boolean redstoneControlInverted = false;

	public static final int MAGNET_CONSUMPTION = 32;

	public ElectromagnetBlockEntity(BlockEntityType<? extends ElectromagnetBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	public ElectromagnetBlockEntity(BlockPos pos, BlockState state)
	{
		this(IEBlockEntities.ELECTROMAGNET.get(), pos, state);
	}

	@Override
	public void tickServer()
	{
		if(isRSPowered()==redstoneControlInverted&&energyStorage.extractEnergy(MAGNET_CONSUMPTION, false) >= MAGNET_CONSUMPTION)
		{
			final int radius = 6;
			Direction facing = getFacing();
			Vec3 sourcePos = getBlockPos().relative(facing).getCenter().add(0, .25, 0);
			AABB area;
			if(getFacing().getAxis()!=Axis.Y)
			{
				BlockPos bottomClose = getBlockPos().relative(facing.getCounterClockWise(), radius).below(radius);
				BlockPos topFar = getBlockPos().relative(facing, radius).relative(facing.getClockWise(), radius).above(radius);
				area = new AABB(bottomClose, topFar);
			}
			else
			{
				BlockPos bottomClose = getBlockPos().offset(-radius, 0, -radius);
				BlockPos topFar = getBlockPos().offset(radius, 0, radius).relative(facing, radius);
				area = new AABB(bottomClose, topFar);
			}
			// perform attraction
			List<ItemEntity> items = performMagnetAttraction(getLevelNonnull(), area, 0.75, sourcePos, Vec3.ZERO, getBlockPos().toShortString(), () -> true);
			// stop items from being attracted when they get close enough
			items.forEach(itemEntity -> {
				if(itemEntity.distanceToSqr(sourcePos) <= 1)
					itemEntity.getPersistentData().putInt(Lib.MAGNET_TIME_NBT, itemEntity.tickCount);
			});
		}
	}

	/**
	 * Used to acquire a predicate that checks all the basics (pickup delay, on a conveyor) but also compares to the unique key of the source of magnetism
	 */
	public static final LoadingCache<String, Predicate<ItemEntity>> MAGNET_SOURCE_PREDICATE = CacheBuilder.newBuilder()
			.maximumSize(100)
			.expireAfterAccess(20, TimeUnit.MINUTES)
			.build(CacheLoader.from(key -> itemEntity -> {
				// if on pickup delay, ignore
				if(itemEntity.hasPickUpDelay())
					return false;
				// only allow grabbing items that have gone 40 ticks without being near a magnet
				int lastMagnetized = itemEntity.getPersistentData().getInt(Lib.MAGNET_TIME_NBT);
				if(lastMagnetized > 0&&itemEntity.tickCount-lastMagnetized < 40)
					return false;
				// check if already being pulled by a different magnet
				String nbtSource = itemEntity.getPersistentData().getString(Lib.MAGNET_SOURCE_NBT);
				if(!nbtSource.isEmpty()&&!nbtSource.equals(key))
					return false;
				// check if NBT blacklisted (e.g.: on a conveyor)
				return !itemEntity.getPersistentData().contains(Lib.MAGNET_PREVENT_NBT);
			}));

	public static List<ItemEntity> performMagnetAttraction(Level world, AABB targetArea, double minimumDistance, Vec3 sourcePosition, Vec3 sourceMovement, String sourceName, Supplier<Boolean> hasConsumedEnergy)
	{
		// Inspired by similar code in Mekanism
		List<ItemEntity> items = world.getEntitiesOfClass(ItemEntity.class, targetArea, MAGNET_SOURCE_PREDICATE.getUnchecked(sourceName));
		for(ItemEntity itemEntity : items)
		{
			Vec3 dist = sourcePosition.subtract(itemEntity.position());
			if(dist.length() > minimumDistance&&hasConsumedEnergy.get())
			{
				if(!itemEntity.getPersistentData().contains(Lib.MAGNET_SOURCE_NBT))
				{
					// play sound when being initially moved
					itemEntity.playSound(IESounds.electromagnet.get(), (float)(.125+world.getRandom().nextDouble()*.25), 1);
					// mark the source of magnetism
					itemEntity.getPersistentData().putString(Lib.MAGNET_SOURCE_NBT, sourceName);
				}
				// figure out relative movement needed to move it to the source position
				Vec3 movementRequired = new Vec3(Math.min(dist.x, 1), Math.min(dist.y, 1), Math.min(dist.z, 1)).subtract(sourceMovement);
				itemEntity.setDeltaMovement(movementRequired.scale(0.2));
			}
		}
		return items;
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		redstoneControlInverted = nbt.getBoolean("redstoneInverted");
		EnergyHelper.deserializeFrom(energyStorage, nbt);
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		nbt.putBoolean("redstoneInverted", redstoneControlInverted);
		EnergyHelper.serializeTo(energyStorage, nbt);
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
	{
		if(cap==ForgeCapabilities.ENERGY&&(side==null||side!=getFacing()))
			return energyCap.cast();
		return super.getCapability(cap, side);
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.PISTON_INVERTED;
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_ALL;
	}

	@Override
	public InteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		if(player.isShiftKeyDown()&&!getLevelNonnull().isClientSide)
		{
			redstoneControlInverted = !redstoneControlInverted;
			player.displayClientMessage(
					Component.translatable(Lib.CHAT_INFO+"rsControl."+(redstoneControlInverted?"invertedOn": "invertedOff")),
					true
			);
			setChanged();
			this.markContainingBlockForUpdate(null);
		}
		return InteractionResult.SUCCESS;
	}
}
