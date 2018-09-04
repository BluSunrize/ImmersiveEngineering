/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.*;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TileEntityEnergyMeter extends TileEntityImmersiveConnectable implements ITickable, IDirectionalTile, IHasDummyBlocks, IAdvancedCollisionBounds, IAdvancedSelectionBounds, IPlayerInteraction, IComparatorOverride
{
	public EnumFacing facing = EnumFacing.NORTH;
	public double lastEnergyPassed = 0;
	public final ArrayList<Double> lastPackets = new ArrayList<>(25);
	public boolean lower = true;
	private int compVal = -1;

	@Override
	protected boolean canTakeLV()
	{
		return true;
	}

	@Override
	protected boolean canTakeMV()
	{
		return true;
	}

	@Override
	protected boolean canTakeHV()
	{
		return true;
	}

	@Override
	protected boolean isRelay()
	{
		return true;
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(!heldItem.isEmpty()&&heldItem.getItem() instanceof IWireCoil)
			return false;
		int transfer = getAveragePower();
		int packets = lastPackets.size();
		if(lower)
		{
			TileEntity above = world.getTileEntity(getPos().add(0, 1, 0));
			if(above instanceof TileEntityEnergyMeter)
				packets = ((TileEntityEnergyMeter)above).lastPackets.size();
		}
		String transferred = "0";
		if(transfer > 0)
			transferred = Utils.formatDouble(transfer, "0.###");
		ChatUtils.sendServerNoSpamMessages(player, new TextComponentTranslation(Lib.CHAT_INFO+"energyTransfered", packets, transferred));
		return true;
	}

	@Override
	public void update()
	{
		ApiUtils.checkForNeedlessTicking(this);
		if(lower||world.isRemote)
			return;
		if(((world.getTotalWorldTime()&31)==(pos.toLong()&31)||compVal < 0))
			updateComparatorValues();
		//Yes, this might tick in between different connectors sending power, but since this is a block for statistical evaluation over a tick, that is irrelevant.
		lastPackets.add(lastEnergyPassed);
		if(lastPackets.size() > 20)
			lastPackets.remove(0);
		lastEnergyPassed = 0;
	}

	@Override
	public boolean canConnect()
	{
		return true;
	}

	@Override
	public void onEnergyPassthrough(double amount)
	{
		lastEnergyPassed += amount;
	}

	@Override
	public boolean canConnectCable(WireType cableType, TargetingInfo target, Vec3i offset)
	{
		if(lower)
		{
			TileEntity above = world.getTileEntity(getPos().add(0, 1, 0));
			if(above instanceof TileEntityEnergyMeter)
				return ((TileEntityEnergyMeter)above).canConnectCable(cableType, target, offset);
			return false;
		}
		return super.canConnectCable(cableType, target, offset);
	}

	@Override
	public void connectCable(WireType cableType, TargetingInfo target, IImmersiveConnectable other)
	{
		if(lower)
		{
			TileEntity above = world.getTileEntity(getPos().add(0, 1, 0));
			if(above instanceof TileEntityEnergyMeter)
				((TileEntityEnergyMeter)above).connectCable(cableType, target, other);
		}
		else
			super.connectCable(cableType, target, other);
	}

	@Override
	public BlockPos getConnectionMaster(WireType cableType, TargetingInfo target)
	{
		if(lower)
			return pos.up();
		else
			return pos;
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", facing.ordinal());
		nbt.setBoolean("dummy", lower);
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = EnumFacing.values()[nbt.getInteger("facing")];
		lower = nbt.getBoolean("dummy");
	}

	@Override
	public Vec3d getConnectionOffset(Connection con)
	{
		int xDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)?con.end.getX()-getPos().getX(): (con.end.equals(Utils.toCC(this))&&con.start!=null)?con.start.getX()-getPos().getX(): 0;
		int zDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)?con.end.getZ()-getPos().getZ(): (con.end.equals(Utils.toCC(this))&&con.start!=null)?con.start.getZ()-getPos().getZ(): 0;
		if(facing.getAxis()==Axis.X)
			return new Vec3d(.5, .4375, zDif > 0?.8125: .1875);
		else
			return new Vec3d(xDif > 0?.8125: .1875, .4375, .5);
	}

	@Override
	public boolean isDummy()
	{
		return !lower;
	}

	@Override
	public boolean isLogicDummy()
	{
		return lower;
	}

	@Override
	public void placeDummies(BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		world.setBlockState(pos.add(0, 1, 0), state);
		((TileEntityEnergyMeter)world.getTileEntity(pos.add(0, 1, 0))).lower = false;
		((TileEntityEnergyMeter)world.getTileEntity(pos.add(0, 1, 0))).facing = this.facing;
	}

	@Override
	public void breakDummies(BlockPos pos, IBlockState state)
	{
		for(int i = 0; i <= 1; i++)
			if(world.getTileEntity(getPos().add(0, !lower?-1: 0, 0).add(0, i, 0)) instanceof TileEntityEnergyMeter)
				world.setBlockToAir(getPos().add(0, !lower?-1: 0, 0).add(0, i, 0));
	}

	public int getAveragePower()
	{
		TileEntityEnergyMeter te = this;
		if(te.lower)
		{
			TileEntity tmp = world.getTileEntity(getPos().add(0, 1, 0));
			if(!(tmp instanceof TileEntityEnergyMeter))
				return -1;
			te = (TileEntityEnergyMeter)tmp;
		}
		if(te.lastPackets.size()==0)
			return 0;
		double sum = 0;
		synchronized(te.lastPackets)
		{
			for(double transfer : te.lastPackets)
				sum += transfer;
		}
		return (int)Math.round(sum/te.lastPackets.size());
	}

	@Override
	public float[] getBlockBounds()
	{
//		return new float[]{0,0,0,1,1,1};
		return null;
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(.1875f, -.625f, .1875f, .8125f, .8125f, .8125f).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		if(lower)
		{
			list.set(0, list.get(0).offset(0, 1, 0));
			list.add(new AxisAlignedBB(0, 0, 0, 1, .375f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		return list;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, ArrayList<AxisAlignedBB> list)
	{
		return false;
	}

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds()
	{
		return getAdvancedSelectionBounds();
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
		return 2;
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

	private void updateComparatorValues()
	{
		int oldVal = compVal;
		int maxTrans = 0;
		Set<Connection> conns = ImmersiveNetHandler.INSTANCE.getConnections(world, pos);
		if(conns==null)
			compVal = 0;
		else
		{
			for(Connection c : conns)
				maxTrans += c.cableType.getTransferRate();
			maxTrans /= 2;
			double val = getAveragePower()/(double)maxTrans;
			compVal = (int)Math.ceil(15*val);
			TileEntity te = world.getTileEntity(pos.down());
			if(te instanceof TileEntityEnergyMeter)
				((TileEntityEnergyMeter)te).compVal = compVal;
		}
		if(oldVal!=compVal)
		{
			world.updateComparatorOutputLevel(pos, getBlockType());
			world.updateComparatorOutputLevel(pos.down(), getBlockType());
		}
	}

	@Override
	public int getComparatorInputOverride()
	{
		return compVal;
	}

	@Override
	public boolean moveConnectionTo(Connection c, BlockPos newEnd)
	{
		return true;
	}
}