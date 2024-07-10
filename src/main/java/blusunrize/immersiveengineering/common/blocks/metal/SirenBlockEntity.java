/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.redstone.IRedstoneConnector;
import blusunrize.immersiveengineering.api.wires.redstone.RedstoneNetworkHandler;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IScrewdriverInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.generic.ImmersiveConnectableBlockEntity;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.IESounds;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class SirenBlockEntity extends ImmersiveConnectableBlockEntity
		implements IEServerTickableBE, IStateBasedDirectional, IScrewdriverInteraction,
		IBlockBounds, IRedstoneConnector
{
	private final double RADIUS = 48;
	boolean active = false;
	int activeTicks = 0;

	public DyeColor redstoneChannel = DyeColor.WHITE;

	public SirenBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.SIREN.get(), pos, state);
	}

	@Override
	public void tickServer()
	{
		if(active)
		{
			if(activeTicks%160==0) // sound refreshes after 8 seconds
				getLevelNonnull().playSound(null, getPosition(), IESounds.siren.value(), SoundSource.BLOCKS, 4, 1);
			if(activeTicks%80==0) // entities update after 4 seconds
			{
				AABB aabb = new AABB(getPosition()).inflate(RADIUS);
				List<LivingEntity> nearbyEntities = getLevelNonnull().getEntitiesOfClass(LivingEntity.class, aabb, entity -> entity.isAlive()&&!entity.isRemoved());
				nearbyEntities.forEach(entity -> {
					// hear the bell
					entity.getBrain().setMemory(MemoryModuleType.HEARD_BELL_TIME, getLevelNonnull().getGameTime());
					// make raiders glow
					if(entity.getType().is(EntityTypeTags.RAIDERS))
						entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80));
				});
			}
			activeTicks++;
		}
		else if(activeTicks > 0)
			activeTicks = 0;
	}

	@Override
	public void onChange(ConnectionPoint cp, RedstoneNetworkHandler handler)
	{
		if(!level.isClientSide&&SafeChunkUtils.isChunkSafe(level, worldPosition))
		{
			final boolean oldActive = this.active;
			this.active = handler.getValue(this.redstoneChannel.getId()) > 0;
			if(this.active!=oldActive)
				this.markContainingBlockForUpdate(null);
		}
	}


	@Override
	public void updateInput(byte[] signals, ConnectionPoint cp)
	{
	}

	@Override
	public InteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
//		if(level.isClientSide)
//			ImmersiveEngineering.proxy.openTileScreen(Lib.GUIID_RedstoneStateCell, this);
		return InteractionResult.SUCCESS;
	}

	@Override
	public void receiveMessageFromClient(CompoundTag message)
	{
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.putInt("redstoneChannel", redstoneChannel.getId());
		nbt.putBoolean("active", active);
	}

	@Override
	public void readCustomNBT(@Nonnull CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		redstoneChannel = DyeColor.byId(nbt.getInt("redstoneChannel"));
		active = nbt.getBoolean("active");
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset)
	{
		return WireType.REDSTONE_CATEGORY.equals(cableType.getCategory());
	}

	@Override
	public Vec3 getConnectionOffset(ConnectionPoint here, ConnectionPoint other, WireType type)
	{
		Direction side = getFacing();
		double conRadius = type.getRenderDiameter()/2;
		return new Vec3(.5+side.getStepX()*(.25-conRadius), .75, .5+side.getStepZ()*(.25-conRadius));
	}

	private static final Map<Direction, VoxelShape> SHAPES = Util.make(
			new EnumMap<>(Direction.class), map -> {
				final float wMin = .3125f;
				final float wMax = .6875f;
				map.put(Direction.NORTH, Shapes.box(wMin, wMin, 0, wMax, wMax, .875));
				map.put(Direction.SOUTH, Shapes.box(wMin, wMin, .125, wMax, wMax, 1));
				map.put(Direction.WEST, Shapes.box(0, wMin, wMin, .875, wMax, wMax));
				map.put(Direction.EAST, Shapes.box(.125, wMin, wMin, 1, wMax, wMax));
				map.put(Direction.DOWN, Shapes.box(wMin, 0, wMin, wMax, .875, wMax));
				map.put(Direction.UP, Shapes.box(wMin, .125, wMin, wMax, 1, wMax));
			}
	);

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return Shapes.block();
//		return VoxelShape. SHAPES.get(getFacing());
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL_PREFER_SIDE;
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
	}

//
//	@Override
//	public Component[] getOverlayText(Player player, HitResult mop, boolean hammer)
//	{
//		return null;
//	}

}