package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.storage.loot.LootContext.Builder;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static blusunrize.immersiveengineering.api.wires.WireApi.INFOS;

public class FeedthroughTileEntity extends ImmersiveConnectableTileEntity implements ITileDrop, IDirectionalTile,
		IHasDummyBlocks, IPropertyPassthrough, IBlockBounds, ICacheData
{
	public static TileEntityType<FeedthroughTileEntity> TYPE;

	public static final String WIRE = "wire";
	private static final String HAS_NEGATIVE = "hasNeg";
	private static final String FACING = "facing";
	private static final String POSITIVE = "positive";
	private static final String OFFSET = "offset";
	public static final String MIDDLE_STATE = "middle";

	@Nonnull
	public WireType reference = WireType.COPPER;
	@Nonnull
	public BlockState stateForMiddle = Blocks.DIRT.getDefaultState();
	@Nonnull
	Direction facing = Direction.NORTH;
	public int offset = 0;
	@Nullable
	public ConnectionPoint connPositive = null;
	public boolean hasNegative = false;
	private boolean formed = true;

	public FeedthroughTileEntity()
	{
		super(TYPE);
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.putString(WIRE, reference.getUniqueName());
		if(connPositive!=null)
			nbt.put(POSITIVE, connPositive.createTag());
		nbt.putBoolean(HAS_NEGATIVE, hasNegative);
		nbt.putInt(FACING, facing.getIndex());
		nbt.putInt(OFFSET, offset);
		CompoundNBT stateNbt = NBTUtil.writeBlockState(stateForMiddle);
		nbt.put(MIDDLE_STATE, stateNbt);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		reference = WireType.getValue(nbt.getString(WIRE));
		if(nbt.contains(POSITIVE, NBT.TAG_COMPOUND))
			connPositive = new ConnectionPoint(nbt.getCompound(POSITIVE));
		hasNegative = nbt.getBoolean(HAS_NEGATIVE);
		facing = Direction.VALUES[nbt.getInt(FACING)];
		offset = nbt.getInt(OFFSET);
		stateForMiddle = NBTUtil.readBlockState(nbt.getCompound(MIDDLE_STATE));
	}

	@Override
	public Vec3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		return getOffset(con.isEnd(connPositive));
	}

	private boolean isPositive(Vec3i offset)
	{
		return offset.getX()*facing.getXOffset()+
				offset.getY()*facing.getYOffset()+
				offset.getZ()*facing.getZOffset() > 0;
	}

	private Vec3d getOffset(boolean positive)
	{
		double l = INFOS.get(reference).connOffset;
		int factor = positive?1: -1;
		return new Vec3d(.5+(.5+l)*facing.getXOffset()*factor, .5+(.5+l)*facing.getYOffset()*factor,
				.5+(.5+l)*facing.getZOffset()*factor);
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset)
	{
		if(!WireApi.canMix(reference, cableType))
			return false;
		boolean positive = isPositive(offset);
		if(positive)
			return connPositive==null;
		else
			return !hasNegative;
	}

	@Override
	public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget)
	{
		if(target.getIndex() > 0)
			connPositive = otherTarget;
		else
			hasNegative = true;
	}

	@Override
	public void removeCable(Connection connection)
	{
		if(connection==null)
		{
			connPositive = null;
			hasNegative = false;
		}
		else
		{
			if(connection.isEnd(connPositive))
				connPositive = null;
			else
				hasNegative = false;
		}
	}

	@Override
	public Set<BlockPos> getIgnored(IImmersiveConnectable other)
	{
		return ImmutableSet.of(pos.offset(facing, 1), pos.offset(facing, -1));
	}

	@Override
	public BlockPos getConnectionMaster(WireType cableType, TargetingInfo target)
	{
		return pos.offset(facing, -offset);
	}

	@Override
	public List<ItemStack> getTileDrops(Builder context)
	{
		WireApi.FeedthroughModelInfo info = INFOS.get(reference);
		if(offset==0)
			return Utils.getDrops(stateForMiddle, context);
		else
		{
			return NonNullList.from(ItemStack.EMPTY,
					new ItemStack(info.conn.getBlock(), 1));
		}
	}

	@Override
	public ItemStack getPickBlock(@Nullable PlayerEntity player, BlockState state, RayTraceResult rayRes)
	{
		WireApi.FeedthroughModelInfo info = INFOS.get(reference);
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
	public Direction getFacing()
	{
		return facing;
	}

	@Override
	public void setFacing(Direction facing)
	{
		this.facing = facing;
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
	public boolean canHammerRotate(Direction side, float hitX, float hitY, float hitZ, LivingEntity entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return false;
	}

	//Called after setFacing
	@Override
	public void placeDummies(BlockItemUseContext ctx, BlockState state)
	{
		for(int i = -1; i <= 1; i += 2)
		{
			BlockPos tmp = pos.offset(facing, i);
			world.setBlockState(tmp, state);
			TileEntity te = world.getTileEntity(tmp);
			if(te instanceof FeedthroughTileEntity)
			{
				((FeedthroughTileEntity)te).facing = facing;
				((FeedthroughTileEntity)te).offset = i;
				((FeedthroughTileEntity)te).reference = reference;
				((FeedthroughTileEntity)te).stateForMiddle = stateForMiddle;
				checkLight(tmp);
			}
		}
	}

	@Override
	public void breakDummies(BlockPos pos, BlockState state)
	{
		if(!formed)
			return;
		FeedthroughTileEntity master;
		BlockPos masterPos = pos.offset(facing, -offset);
		{
			TileEntity tmp = world.getTileEntity(masterPos);
			if(tmp instanceof FeedthroughTileEntity)
				master = (FeedthroughTileEntity)tmp;
			else
				master = null;
		}
		disassembleBlock(-1);
		disassembleBlock(1);
		Collection<Connection> conns = globalNet.getLocalNet(masterPos).getConnections(masterPos);
		if(conns!=null)
		{
			if(master!=null)
				for(Connection c : conns)
				{
					BlockPos newPos = null;
					if(master.connPositive!=null&&c.isPositiveEnd(master.connPositive))
					{
						if(offset!=1)
							newPos = masterPos.offset(facing);
					}
					else if(offset!=-1)
						newPos = masterPos.offset(facing, -1);
					if(newPos!=null)
					{
						/*TODO
						ImmersiveNetHandler.Connection reverse = ImmersiveNetHandler.INSTANCE.getReverseConnection(world.provider.getDimension(), c);
						ApiUtils.moveConnectionEnd(reverse, newPos, world);
						IImmersiveConnectable connector = ApiUtils.toIIC(newPos, world);
						IImmersiveConnectable otherEnd = ApiUtils.toIIC(reverse.start, world);
						if(connector!=null)
						{
							try
							{
								//TODO clean this up in 1.13
								connector.connectCable(reverse.cableType, null, otherEnd);
							} catch(Exception x)
							{
								IELogger.logger.info("Failed to fully move connection", x);
							}
						}*/
					}
				}
		}
		disassembleBlock(0);
	}

	private void disassembleBlock(int toBreak)
	{
		WireApi.FeedthroughModelInfo info = INFOS.get(reference);
		int offsetLocal = toBreak-offset;
		BlockPos replacePos = pos.offset(facing, offsetLocal);
		if(toBreak!=offset)
		{
			TileEntity te = getWorldNonnull().getTileEntity(replacePos);
			if(te instanceof FeedthroughTileEntity)
				((FeedthroughTileEntity)te).formed = false;
			BlockState newState = Blocks.AIR.getDefaultState();
			switch(toBreak)
			{
				case -1:
					newState = info.conn.with(IEProperties.FACING_ALL, facing);
					break;
				case 0:
					newState = stateForMiddle;
					break;
				case 1:
					newState = info.conn.with(IEProperties.FACING_ALL, facing.getOpposite());
					break;
			}
			getWorldNonnull().setBlockState(replacePos, newState);//TODO move wires properly

		}
	}

	@Override
	public boolean isDummy()
	{
		return false;//Every block has a model
	}

	private static float[] FULL_BLOCK = {0, 0, 0, 1, 1, 1};
	private float[] aabb;

	@Override
	public float[] getBlockBounds()
	{
		if(offset==0)
			return FULL_BLOCK;
		if(aabb==null)
		{
			float[] tmp = {
					5F/16, 0, 5F/16,
					11F/16, (float)INFOS.get(reference).connLength, 11F/16
			};
			aabb = Utils.rotateToFacing(tmp, offset > 0?facing: facing.getOpposite());
		}
		return aabb;
	}

	@Override
	public Object[] getCacheData()
	{
		return new Object[]{
				stateForMiddle, reference, facing
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
