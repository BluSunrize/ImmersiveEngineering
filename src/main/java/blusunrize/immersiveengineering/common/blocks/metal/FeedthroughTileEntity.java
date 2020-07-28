package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static blusunrize.immersiveengineering.api.wires.WireApi.INFOS;

public class FeedthroughTileEntity extends ImmersiveConnectableTileEntity implements ITileDrop,
		IPropertyPassthrough, IBlockBounds, ICacheData, IStateBasedDirectional
{
	public static TileEntityType<FeedthroughTileEntity> TYPE;

	public static final String WIRE = "wire";
	private static final String OFFSET = "offset";
	public static final String MIDDLE_STATE = "middle";

	@Nonnull
	public WireType reference = WireType.COPPER;
	@Nonnull
	public BlockState stateForMiddle = Blocks.DIRT.getDefaultState();
	public int offset = 0;
	public boolean currentlyDisassembling = false;

	public FeedthroughTileEntity()
	{
		super(TYPE);
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.putString(WIRE, reference.getUniqueName());
		nbt.putInt(OFFSET, offset);
		CompoundNBT stateNbt = NBTUtil.writeBlockState(stateForMiddle);
		nbt.put(MIDDLE_STATE, stateNbt);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		reference = WireType.getValue(nbt.getString(WIRE));
		offset = nbt.getInt(OFFSET);
		stateForMiddle = NBTUtil.readBlockState(nbt.getCompound(MIDDLE_STATE));
	}

	@Override
	public Vector3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		double l = INFOS.get(reference).connOffset;
		int factor;
		if(here.equals(getPositivePoint()))
			factor = 1;
		else
			factor = -1;
		return new Vector3d(.5+(.5+l)*getFacing().getXOffset()*factor, .5+(.5+l)*getFacing().getYOffset()*factor,
				.5+(.5+l)*getFacing().getZOffset()*factor);
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vector3i offset)
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
		return ImmutableSet.of(pos.offset(getFacing(), 1), pos.offset(getFacing(), -1));
	}

	@Override
	public BlockPos getConnectionMaster(WireType cableType, TargetingInfo target)
	{
		return pos.offset(getFacing(), -offset);
	}

	@Override
	public List<ItemStack> getTileDrops(LootContext context)
	{
		WireApi.FeedthroughModelInfo info = INFOS.get(reference);
		if(offset==0)
		{
			LootContext.Builder builder = new LootContext.Builder(context.getWorld())
					.withNullableParameter(LootParameters.TOOL, context.get(LootParameters.TOOL))
					.withNullableParameter(LootParameters.THIS_ENTITY, context.get(LootParameters.THIS_ENTITY))
					.withNullableParameter(LootParameters.POSITION, context.get(LootParameters.POSITION));
			return Utils.getDrops(stateForMiddle, builder);
		}
		else
		{
			return NonNullList.from(ItemStack.EMPTY,
					new ItemStack(info.conn.get().getBlock(), 1));
		}
	}

	@Override
	public ItemStack getPickBlock(@Nullable PlayerEntity player, BlockState state, RayTraceResult rayRes)
	{
		if(offset==0)
			return Utils.getPickBlock(stateForMiddle, rayRes, player);
		return ITileDrop.super.getPickBlock(player, state, rayRes);
	}

	@Override
	public void readOnPlacement(@Nullable LivingEntity placer, ItemStack stack)
	{
		reference = WireType.getValue(ItemNBTHelper.getString(stack, WIRE));
		stateForMiddle = NBTUtil.readBlockState(ItemNBTHelper.getTagCompound(stack, MIDDLE_STATE));
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
	public boolean canHammerRotate(Direction side, Vector3d hit, LivingEntity entity)
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
	public VoxelShape getBlockBounds(@Nullable ISelectionContext ctx)
	{
		if(offset==0)
			return VoxelShapes.fullCube();
		if(aabb==null)
		{
			float[] tmp = {
					5F/16, 0, 5F/16,
					11F/16, (float)INFOS.get(reference).connLength, 11F/16
			};
			tmp = Utils.rotateToFacing(tmp, offset > 0?getFacing(): getFacing().getOpposite());
			aabb = VoxelShapes.create(tmp[0], tmp[1], tmp[2], tmp[3], tmp[4], tmp[5]);
		}
		return aabb;
	}

	@Override
	public Object[] getCacheData()
	{
		return new Object[]{
				stateForMiddle, reference, getFacing()
		};
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==253)
		{
			checkLight();
			return true;
		}
		return super.receiveClientEvent(id, arg);
	}

	@Override
	public Collection<ConnectionPoint> getConnectionPoints()
	{
		return ImmutableList.of(getNegativePoint(), getPositivePoint());
	}

	//pos.add(facing.getOpposite().getDirectionVec())
	public ConnectionPoint getNegativePoint()
	{
		return new ConnectionPoint(pos, getIndexForOffset(-1));
	}

	//pos.add(facing.getDirectionVec())
	public ConnectionPoint getPositivePoint()
	{
		return new ConnectionPoint(pos, getIndexForOffset(1));
	}

	@Override
	public EnumProperty<Direction> getFacingProperty()
	{
		return IEProperties.FACING_ALL;
	}

	@Override
	public Iterable<? extends Connection> getInternalConnections()
	{
		return ImmutableList.of(new Connection(pos, 0, 1));
	}

	@Nullable
	@Override
	public ConnectionPoint getTargetedPoint(TargetingInfo info, Vector3i offset)
	{
		if(offset.equals(getFacing().getDirectionVec()))
			return getPositivePoint();
		else if(offset.equals(getFacing().getOpposite().getDirectionVec()))
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
		public final Int2IntFunction colorMultiplier;

		public FeedthroughData(BlockState baseState, WireType wire, Direction facing, int offset, Int2IntFunction colorMultiplier)
		{
			this.baseState = baseState;
			this.wire = wire;
			this.facing = facing;
			this.offset = offset;
			this.colorMultiplier = colorMultiplier;
		}
	}
}
