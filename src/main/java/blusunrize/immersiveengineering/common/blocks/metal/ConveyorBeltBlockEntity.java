/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.IConveyorBlockEntity;
import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorBelt;
import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorType;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.util.ResettableCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Supplier;

@EventBusSubscriber(modid = Lib.MODID, bus = Bus.MOD)
public class ConveyorBeltBlockEntity<T extends IConveyorBelt> extends IEBaseBlockEntity
		implements IStateBasedDirectional, ICollisionBounds, ISelectionBounds, IHammerInteraction,
		IPlayerInteraction, IConveyorBlockEntity<T>, IEServerTickableBE
{
	public static final Map<IConveyorType<?>, Supplier<BlockEntityType<?>>> BE_TYPES = new Reference2ObjectOpenHashMap<>();

	private final T conveyorBeltSubtype;

	public ConveyorBeltBlockEntity(IConveyorType<T> type, BlockPos pos, BlockState state)
	{
		super(Preconditions.checkNotNull(ConveyorHandler.getBEType(type), "Not BE type for "+type), pos, state);
		conveyorBeltSubtype = ConveyorHandler.getConveyor(type, this);
	}

	@Override
	@Nullable
	public T getConveyorInstance()
	{
		return conveyorBeltSubtype;
	}

	@Override
	public void onEntityCollision(Level world, Entity entity)
	{
		if(this.conveyorBeltSubtype!=null)
			this.conveyorBeltSubtype.onEntityCollision(entity);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		if(nbt.contains("conveyorBeltSubtypeNBT", Tag.TAG_COMPOUND))
			conveyorBeltSubtype.readConveyorNBT(nbt.getCompound("conveyorBeltSubtypeNBT"));

		if(descPacket&&level!=null)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		if(conveyorBeltSubtype!=null)
			nbt.put("conveyorBeltSubtypeNBT", conveyorBeltSubtype.writeConveyorNBT());
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return ConveyorBlock.FACING;
	}

	@Override
	public Direction getFacing()
	{
		return IStateBasedDirectional.super.getFacing();
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL_QUADRANT;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return !entity.isShiftKeyDown();
	}

	@Override
	public void afterRotation(Direction oldDir, Direction newDir)
	{
		if(this.conveyorBeltSubtype!=null)
			this.conveyorBeltSubtype.afterRotation(oldDir, newDir);
	}

	@Override
	public void tickServer()
	{
		if(this.conveyorBeltSubtype!=null)
			this.conveyorBeltSubtype.tickServer();
	}

	@Override
	public boolean hammerUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		if(player.isShiftKeyDown()&&conveyorBeltSubtype!=null&&conveyorBeltSubtype.changeConveyorDirection())
		{
			if(!level.isClientSide)
			{
				this.setChanged();
				this.markContainingBlockForUpdate(null);
				level.blockEvent(getBlockPos(), this.getBlockState().getBlock(), 0, 0);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(conveyorBeltSubtype!=null)
		{
			boolean update;
			if(conveyorBeltSubtype.getType().canBeDyed()&&Utils.isDye(heldItem))
			{
				DyeColor dye = Utils.getDye(heldItem);
				update = dye!=null&&conveyorBeltSubtype.setDyeColour(dye);
			}
			else
				update = conveyorBeltSubtype.playerInteraction(player, hand, heldItem, hitX, hitY, hitZ, side);
			if(update)
			{
				this.setChanged();
				this.markContainingBlockForUpdate(null);
				level.blockEvent(getBlockPos(), this.getBlockState().getBlock(), 0, 0);
				return true;
			}
		}
		return false;
	}

	private static final VoxelShape COLISIONBB =
			Shapes.box(0, 0, 0, 1, .125F, 1);

	@Override
	public VoxelShape getCollisionShape(CollisionContext ctx)
	{
		if(conveyorBeltSubtype!=null)
			return conveyorBeltSubtype.getCollisionShape();
		return COLISIONBB;
	}

	@Override
	public VoxelShape getSelectionShape(@Nullable CollisionContext ctx)
	{
		if(conveyorBeltSubtype!=null)
			return conveyorBeltSubtype.getSelectionShape();
		return COLISIONBB;
	}

	private final ResettableCapability<IItemHandler> insertionCap = registerCapability(new ConveyorInventoryHandler(this));

	@Nonnull
	@Override
	public <T2> LazyOptional<T2> getCapability(@Nonnull Capability<T2> cap, @Nullable Direction side)
	{
		if(cap==Capabilities.ITEM_HANDLER)
			return insertionCap.cast();
		return super.getCapability(cap, side);
	}

	// Make public
	@Override
	public boolean isRSPowered()
	{
		return super.isRSPowered();
	}

	@SubscribeEvent
	public static void registerConveyorBEsAndBlocks(NewRegistryEvent ev)
	{
		for(IConveyorType<?> type : ConveyorHandler.getConveyorTypes())
		{
			RegistryObject<BlockEntityType<?>> beType = IEBlockEntities.REGISTER.register(
					type.getId().getPath(), () -> new BlockEntityType<>(
							(pos, state) -> new ConveyorBeltBlockEntity<>(type, pos, state),
							ImmutableSet.of(ConveyorHandler.getBlock(type)), null
					));
			BE_TYPES.put(type, beType);
		}
		MetalDevices.initConveyors();
	}

	public static class ConveyorInventoryHandler implements IItemHandlerModifiable
	{
		ConveyorBeltBlockEntity conveyor;

		public ConveyorInventoryHandler(ConveyorBeltBlockEntity conveyor)
		{
			this.conveyor = conveyor;
		}

		@Override
		public int getSlots()
		{
			return 1;
		}

		@Override
		public ItemStack getStackInSlot(int slot)
		{
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			if(conveyor.getConveyorInstance().isBlocked())
				return stack;
			if(!simulate)
			{
				ItemEntity entity = new ItemEntity(conveyor.getLevelNonnull(), conveyor.getBlockPos().getX()+.5, conveyor.getBlockPos().getY()+.1875, conveyor.getBlockPos().getZ()+.5, stack.copy());
				entity.setDeltaMovement(Vec3.ZERO);
				conveyor.getLevelNonnull().addFreshEntity(entity);
				if(conveyor.conveyorBeltSubtype!=null)
					conveyor.conveyorBeltSubtype.onItemDeployed(entity);
			}
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot)
		{
			return 64;
		}

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack)
		{
			return true;
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack)
		{
		}
	}
}