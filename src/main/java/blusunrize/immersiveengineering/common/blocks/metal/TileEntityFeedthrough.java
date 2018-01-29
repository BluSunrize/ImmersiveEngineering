package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.*;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;
import static blusunrize.immersiveengineering.api.energy.wires.WireApi.INFOS;
import static blusunrize.immersiveengineering.api.energy.wires.WireApi.registerFeedthroughForWiretype;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_Connector.*;

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
	private BlockPos connPositive = null;
	private boolean hasNegative = false;

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setString(WIRE, reference.getUniqueName());
		if (connPositive!=null)
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
		if (nbt.hasKey(POSITIVE_CON_X))
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
		return offset.getX()*facing.getFrontOffsetX()+
				offset.getY()*facing.getFrontOffsetY()+
				offset.getZ()*facing.getFrontOffsetZ()>0;
	}

	@Override
	public Vec3d getConnectionOffset(ImmersiveNetHandler.Connection con, TargetingInfo target, Vec3i offsetLink)
	{
		return getOffset(isPositive(offsetLink));
	}

	private Vec3d getOffset(boolean positive)
	{
		double l = INFOS.get(reference).connOffset;
		int factor = positive?1:-1;
		return new Vec3d(.5+(.5+l)*facing.getFrontOffsetX()*factor, .5+(.5+l)*facing.getFrontOffsetY()*factor,
				.5+(.5+l)*facing.getFrontOffsetZ()*factor);
	}

	@Override
	public boolean canConnectCable(WireType cableType, TargetingInfo target, Vec3i offset)
	{
		if (!WireApi.canMix(reference, cableType))
			return false;
		boolean positive = isPositive(offset);
		if (positive)
			return connPositive==null;
		else
			return !hasNegative;
	}

	@Override
	public void connectCable(WireType cableType, TargetingInfo target, IImmersiveConnectable other, @Nullable Vec3i offset)
	{
		if (offset!=null)
		{
			if (isPositive(offset))
				connPositive = ApiUtils.toBlockPos(other);
			else
				hasNegative = true;
		}
	}

	@Override
	public void removeCable(ImmersiveNetHandler.Connection connection)
	{
		if (connection==null)
		{
			connPositive = null;
			hasNegative = false;
		}
		else
		{
			if (connection.end.equals(connPositive)||connection.start.equals(connPositive))
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
	public ItemStack getTileDrop(@Nullable EntityPlayer player, IBlockState state)
	{
		ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
		stack.setTagInfo(WIRE, new NBTTagString(reference.getUniqueName()));
		NBTTagCompound stateNbt = new NBTTagCompound();
		Utils.stateToNBT(stateNbt, stateForMiddle);
		stack.setTagInfo(MIDDLE_STATE, stateNbt);
		return stack;
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
		for (int i = -1;i<=1;i+=2)
		{
			BlockPos tmp = pos.offset(facing, i);
			world.setBlockState(tmp, state);
			TileEntity te = world.getTileEntity(tmp);
			if (te instanceof TileEntityFeedthrough)
			{
				((TileEntityFeedthrough) te).facing = facing;
				((TileEntityFeedthrough) te).offset = i;
				((TileEntityFeedthrough) te).reference = reference;
				((TileEntityFeedthrough) te).stateForMiddle = stateForMiddle;
				world.checkLight(tmp);
			}
		}
	}

	@Override
	public void breakDummies(BlockPos pos, IBlockState state)
	{
		for (int i = -1-offset;i<=1-offset;i++)
			world.setBlockToAir(pos.offset(facing, i));
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
		if (offset==0)
			return FULL_BLOCK;
		if (aabb==null)
		{
			float[] tmp = {
					5F/16, 0, 5F/16,
					11F/16, (float) INFOS.get(reference).connLength, 11F/16
			};
			aabb = Utils.rotateToFacing(tmp, offset>0?facing:facing.getOpposite());
		}
		return aabb;
	}

	@Override
	public Object[] getCacheData()
	{
		return new Object[] {
				stateForMiddle, reference, facing
		};
	}
}
