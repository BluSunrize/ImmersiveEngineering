/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPropertyPassthrough;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.blocks.generic.ImmersiveConnectableTileEntity;
import blusunrize.immersiveengineering.common.register.IETileTypes;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static blusunrize.immersiveengineering.api.wires.WireApi.INFOS;

public class FeedthroughTileEntity extends ImmersiveConnectableTileEntity implements ITileDrop,
		IPropertyPassthrough, IBlockBounds, IStateBasedDirectional
{
	public static final String WIRE = "wire";
	private static final String OFFSET = "offset";
	public static final String MIDDLE_STATE = "middle";

	@Nonnull
	public WireType reference = WireType.COPPER;
	@Nonnull
	public BlockState stateForMiddle = Blocks.DIRT.defaultBlockState();
	public int offset = 0;
	public boolean currentlyDisassembling = false;

	public FeedthroughTileEntity(BlockPos pos, BlockState state)
	{
		super(IETileTypes.FEEDTHROUGH.get(), pos, state);
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.putString(WIRE, reference.getUniqueName());
		nbt.putInt(OFFSET, offset);
		CompoundTag stateNbt = NbtUtils.writeBlockState(stateForMiddle);
		nbt.put(MIDDLE_STATE, stateNbt);
	}

	@Override
	public void readCustomNBT(@Nonnull CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		reference = WireType.getValue(nbt.getString(WIRE));
		offset = nbt.getInt(OFFSET);
		stateForMiddle = NbtUtils.readBlockState(nbt.getCompound(MIDDLE_STATE));
	}

	@Override
	public Vec3 getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		double l = INFOS.get(reference).connOffset;
		int factor;
		if(here.equals(getPositivePoint()))
			factor = 1;
		else
			factor = -1;
		return new Vec3(.5+(.5+l)*getFacing().getStepX()*factor, .5+(.5+l)*getFacing().getStepY()*factor,
				.5+(.5+l)*getFacing().getStepZ()*factor);
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset)
	{
		if(!WireApi.canMix(reference, cableType))
			return false;
		Collection<Connection> existing = globalNet.getLocalNet(target).getConnections(target);
		for(Connection c : existing)
			if(!c.isInternal())
				return false;
		return true;
	}

	@Override
	public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget)
	{

	}

	@Override
	public void removeCable(Connection connection, ConnectionPoint attachedPoint)
	{

	}

	@Override
	public Set<BlockPos> getIgnored(IImmersiveConnectable other)
	{
		return ImmutableSet.of(worldPosition.relative(getFacing(), 1), worldPosition.relative(getFacing(), -1));
	}

	@Override
	public BlockPos getConnectionMaster(WireType cableType, TargetingInfo target)
	{
		return worldPosition.relative(getFacing(), -offset);
	}

	@Override
	public List<ItemStack> getTileDrops(LootContext context)
	{
		WireApi.FeedthroughModelInfo info = INFOS.get(reference);
		if(offset==0)
		{
			LootContext.Builder builder = new LootContext.Builder(context.getLevel())
					.withOptionalParameter(LootContextParams.TOOL, context.getParamOrNull(LootContextParams.TOOL))
					.withOptionalParameter(LootContextParams.THIS_ENTITY, context.getParamOrNull(LootContextParams.THIS_ENTITY))
					.withOptionalParameter(LootContextParams.ORIGIN, context.getParamOrNull(LootContextParams.ORIGIN));
			return Utils.getDrops(stateForMiddle, builder);
		}
		else
		{
			return NonNullList.of(ItemStack.EMPTY,
					new ItemStack(info.conn.get().getBlock(), 1));
		}
	}

	@Override
	public ItemStack getPickBlock(@Nullable Player player, BlockState state, HitResult rayRes)
	{
		if(offset==0)
			return Utils.getPickBlock(stateForMiddle, rayRes, player);
		return ITileDrop.super.getPickBlock(player, state, rayRes);
	}

	@Override
	public void readOnPlacement(@Nullable LivingEntity placer, ItemStack stack)
	{
		reference = WireType.getValue(ItemNBTHelper.getString(stack, WIRE));
		stateForMiddle = NbtUtils.readBlockState(ItemNBTHelper.getTagCompound(stack, MIDDLE_STATE));
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.PISTON_LIKE;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return false;
	}

	private VoxelShape aabb;

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		if(offset==0)
			return Shapes.block();
		if(aabb==null)
		{
			float[] tmp = {
					5F/16, 0, 5F/16,
					11F/16, (float)INFOS.get(reference).connLength, 11F/16
			};
			tmp = Utils.rotateToFacing(tmp, offset > 0?getFacing(): getFacing().getOpposite());
			aabb = Shapes.box(tmp[0], tmp[1], tmp[2], tmp[3], tmp[4], tmp[5]);
		}
		return aabb;
	}

	@Override
	public boolean triggerEvent(int id, int arg)
	{
		if(id==253)
		{
			checkLight();
			return true;
		}
		return super.triggerEvent(id, arg);
	}

	@Override
	public Collection<ConnectionPoint> getConnectionPoints()
	{
		return ImmutableList.of(getNegativePoint(), getPositivePoint());
	}

	//pos.add(facing.getOpposite().getDirectionVec())
	public ConnectionPoint getNegativePoint()
	{
		return new ConnectionPoint(worldPosition, getIndexForOffset(-1));
	}

	//pos.add(facing.getDirectionVec())
	public ConnectionPoint getPositivePoint()
	{
		return new ConnectionPoint(worldPosition, getIndexForOffset(1));
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_ALL;
	}

	@Override
	public Iterable<? extends Connection> getInternalConnections()
	{
		return ImmutableList.of(new Connection(worldPosition, 0, 1));
	}

	@Nullable
	@Override
	public ConnectionPoint getTargetedPoint(TargetingInfo info, Vec3i offset)
	{
		if(offset.equals(getFacing().getNormal()))
			return getPositivePoint();
		else if(offset.equals(getFacing().getOpposite().getNormal()))
			return getNegativePoint();
		else
			return null;
	}

	public static int getIndexForOffset(int offset)
	{
		if(offset==-1)
			return 1;
		else if(offset==1)
			return 0;
		else
			return -1;
	}

	public static class FeedthroughData
	{
		public final BlockState baseState;
		public final WireType wire;
		public final Direction facing;
		public final int offset;
		public final int colorMultiplier;

		public FeedthroughData(BlockState baseState, WireType wire, Direction facing, int offset, int colorMultiplier)
		{
			this.baseState = baseState;
			this.wire = wire;
			this.facing = facing;
			this.offset = offset;
			this.colorMultiplier = colorMultiplier;
		}
	}
}
