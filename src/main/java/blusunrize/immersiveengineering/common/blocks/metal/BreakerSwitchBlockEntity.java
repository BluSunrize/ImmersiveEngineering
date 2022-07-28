/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.api.utils.shapes.CachedVoxelShapes;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.generic.ConnectorBlock;
import blusunrize.immersiveengineering.common.blocks.generic.ImmersiveConnectableBlockEntity;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

import static blusunrize.immersiveengineering.api.wires.WireType.HV_CATEGORY;

public class BreakerSwitchBlockEntity extends ImmersiveConnectableBlockEntity implements IBlockBounds, IAdvancedDirectionalBE,
		IActiveState, IHammerInteraction, IScrewdriverInteraction, IPlayerInteraction, IRedstoneOutput, IStateBasedDirectional
{
	public static final int LEFT_INDEX = 0;
	public static final int RIGHT_INDEX = 1;
	public int rotation = 0;
	public int wires = 0;
	public boolean inverted = false;

	public BreakerSwitchBlockEntity(BlockPos pos, BlockState state)
	{
		this(IEBlockEntities.BREAKER_SWITCH.get(), pos, state);
	}

	public BreakerSwitchBlockEntity(BlockEntityType<? extends BreakerSwitchBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	@Nullable
	@Override
	public ConnectionPoint getTargetedPoint(TargetingInfo info, Vec3i offset)
	{
		Matrix4 mat = new Matrix4()
				.setIdentity()
				.translate(.5, .5, 0)
				.rotate(-Math.PI/2*rotation, 0, 0, 1)
				.translate(-.5, -.5, 0)
				.multiply(Matrix4.inverseFacing(getFacing()));
		Vec3 transformedHit = mat.apply(new Vec3(info.hitX, info.hitY, info.hitZ));
		return new ConnectionPoint(worldPosition, transformedHit.x > 0.5?RIGHT_INDEX: LEFT_INDEX);
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset)
	{
		if(HV_CATEGORY.equals(cableType.getCategory())&&!canTakeHV())
			return false;
		for(ConnectionPoint cp : getConnectionPoints())
			for(Connection c : globalNet.getLocalNet(cp).getConnections(cp))
				if(!c.isInternal()&&(cp.equals(target)||!cableType.getCategory().equals(c.type.getCategory())))
					return false;
		return true;
	}

	protected boolean canTakeHV()
	{
		return false;
	}

	@Override
	public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget)
	{
		wires++;
	}

	@Override
	public void removeCable(Connection connection, ConnectionPoint attachedPoint)
	{
		WireType type = connection!=null?connection.type: null;
		if(type==null)
			wires = 0;
		else
			wires--;
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.putInt("rotation", rotation);
		nbt.putInt("wires", wires);
		nbt.putBoolean("inverted", inverted);
	}

	@Override
	public void readCustomNBT(@Nonnull CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		rotation = nbt.getInt("rotation");
		wires = nbt.getInt("wires");
		inverted = nbt.getBoolean("inverted");
	}

	@Override
	public Vec3 getConnectionOffset(ConnectionPoint here, ConnectionPoint other, WireType type)
	{
		Matrix4 mat = new Matrix4(getFacing());
		mat.translate(.5, .5, 0).rotate(Math.PI/2*rotation, 0, 0, 1).translate(-.5, -.5, 0);
		boolean isLeft = here.index()==LEFT_INDEX;
		return mat.apply(new Vec3(isLeft?.25: .75, .5, .125));
	}

	@Override
	public boolean hammerUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		rotation = (rotation+3)%4;
		for(ConnectionPoint cp : getConnectionPoints())
			for(Connection c : getLocalNet(cp.index()).getConnections(cp))
				if(!c.isInternal())
					globalNet.updateCatenaryData(c);
		setChanged();
		markContainingBlockForUpdate(getBlockState());
		return true;
	}

	@Override
	public InteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		final boolean oldPassing = allowEnergyToPass();
		inverted = !inverted;
		if(!level.isClientSide)
		{
			player.displayClientMessage(
					Component.translatable(Lib.CHAT_INFO+"rsSignal."+(inverted?"invertedOn": "invertedOff")), true
			);
			notifyNeighbours();
			if(oldPassing!=allowEnergyToPass())
				updateConductivity();
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public boolean interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(!Utils.isHammer(heldItem))
		{
			boolean active = !getIsActive();
			setActive(active);
			level.playSound(null, getBlockPos(), IESounds.direSwitch.get(), SoundSource.BLOCKS, 2.5F, 1);
			level.blockEvent(getBlockPos(), getBlockState().getBlock(), active?1: 0, 0);
			notifyNeighbours();
			updateConductivity();
			return true;
		}
		else
			return false;
	}

	protected void updateConductivity()
	{
		if(allowEnergyToPass())
			globalNet.addConnection(new Connection(worldPosition, LEFT_INDEX, RIGHT_INDEX));
		else
			globalNet.removeConnection(new Connection(worldPosition, LEFT_INDEX, RIGHT_INDEX));
	}

	public void notifyNeighbours()
	{
		setChanged();
		level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
		for(Direction f : DirectionUtils.VALUES)
			level.updateNeighborsAt(getBlockPos().relative(f), getBlockState().getBlock());
	}

	@Override
	public boolean triggerEvent(int id, int arg)
	{
		if(super.triggerEvent(id, arg))
			return true;
		this.markContainingBlockForUpdate(null);
		return true;
	}


	@Override
	public Property<Direction> getFacingProperty()
	{
		return ConnectorBlock.DEFAULT_FACING_PROP;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.SIDE_CLICKED;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return true;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return false;
	}

	private static final CachedVoxelShapes<Pair<Direction, Integer>> SHAPES = new CachedVoxelShapes<>(pair -> {
		Vec3 start = new Vec3(.25, .1875, 0);
		Vec3 end = new Vec3(.75, .8125, .5);
		Matrix4 mat = new Matrix4(pair.getFirst());
		mat.translate(.5, .5, 0).rotate(Math.PI/2*pair.getSecond(), 0, 0, 1).translate(-.5, -.5, 0);
		start = mat.apply(start);
		end = mat.apply(end);
		return ImmutableList.of(new AABB(start, end));
	});

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return SHAPES.get(Pair.of(getFacing(), rotation));
	}

	@Override
	public int getWeakRSOutput(@Nonnull Direction side)
	{
		return (getIsActive()^inverted)?15: 0;
	}

	@Override
	public int getStrongRSOutput(@Nonnull Direction side)
	{
		return side.getOpposite()==getFacing()&&(getIsActive()^inverted)?15: 0;
	}

	@Override
	public boolean canConnectRedstone(@Nonnull Direction side)
	{
		return true;
	}

	@Override
	public void onDirectionalPlacement(Direction side, float hitX, float hitY, float hitZ, LivingEntity placer)
	{
		Direction f = Direction.SOUTH;
		int rotationSign = -1;
		if(side.getAxis()==Axis.Y)
		{
			float xFromMid = hitX-.5f;
			float zFromMid = hitZ-.5f;
			float max = Math.max(Math.abs(xFromMid), Math.abs(zFromMid));
			if(max==Math.abs(xFromMid))
				f = xFromMid < 0?Direction.WEST: Direction.EAST;
			else
				f = zFromMid < 0?Direction.NORTH: Direction.SOUTH;
			if(side==Direction.DOWN)
			{
				f = f.getOpposite();
				rotationSign = 1;
			}
		}
		rotation = Direction.NORTH.get2DDataValue()+rotationSign*f.get2DDataValue();
		rotation = (rotation+4)%4;
	}

	@Override
	public Collection<ConnectionPoint> getConnectionPoints()
	{
		return ImmutableList.of(new ConnectionPoint(worldPosition, LEFT_INDEX), new ConnectionPoint(worldPosition, RIGHT_INDEX));
	}

	@Override
	public Iterable<? extends Connection> getInternalConnections()
	{
		if(allowEnergyToPass())
			return ImmutableList.of(new Connection(worldPosition, LEFT_INDEX, RIGHT_INDEX));
		else
			return ImmutableList.of();
	}

	protected boolean allowEnergyToPass()
	{
		return getIsActive();
	}
}