package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.*;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

import static blusunrize.immersiveengineering.api.energy.wires.WireApi.INFOS;

public class TileEntityFeedthrough extends TileEntityImmersiveConnectable implements ITileDrop, IDirectionalTile,
		IHasDummyBlocks, IPropertyPassthrough, IBlockBounds, ICacheData
{
	public static final String WIRE = "wire";
	private static final String POSITIVE_CON_X = "posConnX";
	private static final String POSITIVE_CON_Y = "posConnY";
	private static final String POSITIVE_CON_Z = "posConnZ";
	private static final String HAS_NEGATIVE = "hasNeg";
	private static final String FACING = "facing";
	private static final String OFFSET = "offset";
	public static final String MIDDLE_STATE = "middle";

	@Nonnull
	public WireType reference = WireType.COPPER;
	@Nonnull
	public IBlockState stateForMiddle = Blocks.DIRT.getDefaultState();
	@Nonnull
	EnumFacing facing = EnumFacing.NORTH;
	public int offset = 0;
	@Nullable
	public BlockPos connPositive = null;
	public boolean hasNegative = false;
	private boolean formed = true;

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setString(WIRE, reference.getUniqueName());
		if(connPositive!=null)
		{
			nbt.setInteger(POSITIVE_CON_X, connPositive.getX());
			nbt.setInteger(POSITIVE_CON_Y, connPositive.getY());
			nbt.setInteger(POSITIVE_CON_Z, connPositive.getZ());
		}
		nbt.setBoolean(HAS_NEGATIVE, hasNegative);
		nbt.setInteger(FACING, facing.getIndex());
		nbt.setInteger(OFFSET, offset);
		NBTTagCompound stateNbt = new NBTTagCompound();
		Utils.stateToNBT(stateNbt, stateForMiddle);
		nbt.setTag(MIDDLE_STATE, stateNbt);
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		reference = WireType.getValue(nbt.getString(WIRE));
		if(nbt.hasKey(POSITIVE_CON_X))
			connPositive = new BlockPos(
					nbt.getInteger(POSITIVE_CON_X),
					nbt.getInteger(POSITIVE_CON_Y),
					nbt.getInteger(POSITIVE_CON_Z));
		hasNegative = nbt.getBoolean(HAS_NEGATIVE);
		facing = EnumFacing.VALUES[nbt.getInteger(FACING)];
		offset = nbt.getInteger(OFFSET);
		stateForMiddle = Utils.stateFromNBT(nbt.getCompoundTag(MIDDLE_STATE));
	}

	@Override
	public Vec3d getConnectionOffset(ImmersiveNetHandler.Connection con)
	{
		return getOffset(con.start.equals(connPositive)||con.end.equals(connPositive));
	}

	private boolean isPositive(Vec3i offset)
	{
		return offset.getX()*facing.getXOffset()+
				offset.getY()*facing.getYOffset()+
				offset.getZ()*facing.getZOffset() > 0;
	}

	@Override
	public Vec3d getConnectionOffset(ImmersiveNetHandler.Connection con, TargetingInfo target, Vec3i offsetLink)
	{
		return getOffset(isPositive(offsetLink));
	}

	private Vec3d getOffset(boolean positive)
	{
		double l = INFOS.get(reference).connOffset;
		int factor = positive?1: -1;
		return new Vec3d(.5+(.5+l)*facing.getXOffset()*factor, .5+(.5+l)*facing.getYOffset()*factor,
				.5+(.5+l)*facing.getZOffset()*factor);
	}

	@Override
	public boolean canConnectCable(WireType cableType, TargetingInfo target, Vec3i offset)
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
	public void connectCable(WireType cableType, TargetingInfo target, IImmersiveConnectable other, @Nullable Vec3i offset)
	{
		if(offset!=null)
		{
			if(isPositive(offset))
				connPositive = ApiUtils.toBlockPos(other);
			else
				hasNegative = true;
		}
	}

	@Override
	public void removeCable(ImmersiveNetHandler.Connection connection)
	{
		if(connection==null)
		{
			connPositive = null;
			hasNegative = false;
		}
		else
		{
			if(connection.end.equals(connPositive)||connection.start.equals(connPositive))
				connPositive = null;
			else
				hasNegative = false;
		}
	}

	@Override
	public WireType getCableLimiter(TargetingInfo target)
	{
		return reference;
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
	public NonNullList<ItemStack> getTileDrops(@Nullable EntityPlayer player, IBlockState state)
	{
		WireApi.FeedthroughModelInfo info = INFOS.get(reference);
		if(info.canReplace())
		{
			if(offset==0)
				return Utils.getDrops(stateForMiddle);
			else
			{
				assert info.conn!=null;//If it's marked as replaceable it should have a state to replace with
				return NonNullList.from(ItemStack.EMPTY,
						new ItemStack(info.conn.getBlock(), 1, info.conn.getBlock().getMetaFromState(info.conn)));
			}
		}
		else
		{
			ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
			stack.setTagInfo(WIRE, new NBTTagString(reference.getUniqueName()));
			NBTTagCompound stateNbt = new NBTTagCompound();
			Utils.stateToNBT(stateNbt, stateForMiddle);
			stack.setTagInfo(MIDDLE_STATE, stateNbt);
			return NonNullList.from(ItemStack.EMPTY, stack);
		}
	}

	@Override
	public ItemStack getPickBlock(@Nullable EntityPlayer player, IBlockState state, RayTraceResult rayRes)
	{
		WireApi.FeedthroughModelInfo info = INFOS.get(reference);
		if(info.canReplace()&&offset==0)
		{
			//getPickBlock needs a proper World, not an IBlockAccess, which is hard to emulate quickly.
			// "world, pos" won't have anything remotely like the state this expects, I hope it won't notice.
			try
			{
				return stateForMiddle.getBlock().getPickBlock(stateForMiddle, rayRes, world, pos, player);
			} catch(Exception x)// We can't predict what is going to happen with weird inputs. The block is mostly inert, so it shouldn't be too bad.
			{
			}                   // No output as WAILA etc call this every tick (every frame?)
		}
		return getTileDrop(player, state);
	}

	@Override
	public void readOnPlacement(@Nullable EntityLivingBase placer, ItemStack stack)
	{
		reference = WireType.getValue(ItemNBTHelper.getString(stack, WIRE));
		stateForMiddle = Utils.stateFromNBT(ItemNBTHelper.getTagCompound(stack, MIDDLE_STATE));
	}

	@Override
	public EnumFacing getFacing()
	{
		return facing;
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 1;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(EnumFacing axis)
	{
		return false;
	}

	//Called after setFacing
	@Override
	public void placeDummies(BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		for(int i = -1; i <= 1; i += 2)
		{
			BlockPos tmp = pos.offset(facing, i);
			world.setBlockState(tmp, state);
			TileEntity te = world.getTileEntity(tmp);
			if(te instanceof TileEntityFeedthrough)
			{
				((TileEntityFeedthrough)te).facing = facing;
				((TileEntityFeedthrough)te).offset = i;
				((TileEntityFeedthrough)te).reference = reference;
				((TileEntityFeedthrough)te).stateForMiddle = stateForMiddle;
				world.checkLight(tmp);
			}
		}
	}

	@Override
	public void breakDummies(BlockPos pos, IBlockState state)
	{
		if(!formed)
			return;
		TileEntityFeedthrough master;
		BlockPos masterPos = pos.offset(facing, -offset);
		{
			TileEntity tmp = world.getTileEntity(masterPos);
			if(tmp instanceof TileEntityFeedthrough)
				master = (TileEntityFeedthrough)tmp;
			else
				master = null;
		}
		disassembleBlock(-1);
		disassembleBlock(1);
		Set<Connection> conns = ImmersiveNetHandler.INSTANCE.getConnections(world, masterPos);
		if(conns!=null)
		{
			if(master!=null)
				for(Connection c : conns)
				{
					BlockPos newPos = null;
					if(c.end.equals(master.connPositive))
					{
						if(offset!=1)
							newPos = masterPos.offset(facing);
					}
					else if(offset!=-1)
						newPos = masterPos.offset(facing, -1);
					if(newPos!=null)
					{
						Connection reverse = ImmersiveNetHandler.INSTANCE.getReverseConnection(world.provider.getDimension(), c);
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
						}
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
		if(!info.canReplace())
			world.setBlockToAir(replacePos);
		else if(toBreak!=offset)
		{
			TileEntity te = world.getTileEntity(replacePos);
			if(te instanceof TileEntityFeedthrough)
				((TileEntityFeedthrough)te).formed = false;
			IBlockState newState = Blocks.AIR.getDefaultState();
			switch(toBreak)
			{
				case -1:
					newState = info.conn.withProperty(IEProperties.FACING_ALL, facing);
					break;
				case 0:
					newState = stateForMiddle;
					break;
				case 1:
					newState = info.conn.withProperty(IEProperties.FACING_ALL, facing.getOpposite());
					break;
			}
			world.setBlockState(replacePos, newState);//TODO move wires properly

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
	public float getDamageAmount(Entity e, ImmersiveNetHandler.Connection c)
	{
		return INFOS.get(reference).postProcessDmg.apply(super.getDamageAmount(e, c));
	}

	@Override
	protected float getBaseDamage(ImmersiveNetHandler.Connection c)
	{
		return INFOS.get(reference).dmgPerEnergy;
	}

	@Override
	protected float getMaxDamage(ImmersiveNetHandler.Connection c)
	{
		return INFOS.get(reference).maxDmg;
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==253)
		{
			world.checkLight(pos);
			return true;
		}
		return super.receiveClientEvent(id, arg);
	}

	@Override
	public boolean moveConnectionTo(Connection c, BlockPos newEnd)
	{
		if(c.end.equals(connPositive))
			connPositive = newEnd;
		return true;
	}
}
