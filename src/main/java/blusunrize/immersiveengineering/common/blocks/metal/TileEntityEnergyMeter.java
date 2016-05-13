package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ITickable;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class TileEntityEnergyMeter extends TileEntityImmersiveConnectable implements ITickable, IDirectionalTile, IHasDummyBlocks, IAdvancedCollisionBounds,IAdvancedSelectionBounds, IPlayerInteraction, IComparatorOverride
{
	public EnumFacing facing = EnumFacing.NORTH;
	public int lastEnergyPassed = 0;
	public ArrayList<Integer> lastPackets = new ArrayList<Integer>(25);
	public boolean dummy=true;
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
	public boolean interact(EnumFacing side, EntityPlayer player, float hitX, float hitY, float hitZ)
	{
		int transfer = getAveragePower();
		int packets = lastPackets.size();
		if(dummy)
		{
			TileEntity above = worldObj.getTileEntity(getPos().add(0,1,0));
			if(above instanceof TileEntityEnergyMeter)
				packets = ((TileEntityEnergyMeter)above).lastPackets.size();
		}
		String transferred = "0";
		if(transfer>0)
			transferred = Utils.formatDouble(transfer, "0.###");
		ChatUtils.sendServerNoSpamMessages(player, new ChatComponentTranslation(Lib.CHAT_INFO+"energyTransfered",packets,transferred));
		return true;
	}

	@Override
	public void update()
	{
		if (!worldObj.isRemote&&((worldObj.getTotalWorldTime()&31)==(pos.toLong()&31)||compVal<0))
			updateComparatorValues();
		if(dummy || worldObj.isRemote)
			return;
		//Yes, this might tick in between different connectors sending power, but since this is a block for statistical evaluation over a tick, that is irrelevant.
		lastPackets.add(lastEnergyPassed);
		if(lastPackets.size()>20)
			lastPackets.remove(0);
		lastEnergyPassed = 0;
	}

	@Override
	public boolean canConnect()
	{
		return !dummy;
	}

	@Override
	public void onEnergyPassthrough(int amount)
	{
		lastEnergyPassed += amount;
	}

	@Override
	public boolean canConnectCable(WireType cableType, TargetingInfo target)
	{
		if(dummy)
		{
			TileEntity above = worldObj.getTileEntity(getPos().add(0,1,0));
			if(above instanceof TileEntityEnergyMeter)
				return ((TileEntityEnergyMeter)above).canConnectCable(cableType, target);
			return false;
		}
		return super.canConnectCable(cableType, target);
	}
	@Override
	public void connectCable(WireType cableType, TargetingInfo target)
	{
		if(dummy)
		{
			TileEntity above = worldObj.getTileEntity(getPos().add(0,1,0));
			if(above instanceof TileEntityEnergyMeter)
				((TileEntityEnergyMeter)above).connectCable(cableType, target);
		}
		else
			super.connectCable(cableType, target);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", facing.ordinal());
		nbt.setBoolean("dummy", dummy);
	}
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = EnumFacing.values()[nbt.getInteger("facing")];
		dummy = nbt.getBoolean("dummy");
	}

	@Override
	public Vec3 getRaytraceOffset(IImmersiveConnectable link)
	{
		int xDif = ((TileEntity)link).getPos().getX()-getPos().getX();
		int zDif = ((TileEntity)link).getPos().getZ()-getPos().getZ();
		if(facing.getAxis()==Axis.X)
			return new Vec3(.5,.4375,zDif>0?.8125:.1875);
		else
			return new Vec3(xDif>0?.8125:.1875,.4375,.5);
	}
	@Override
	public Vec3 getConnectionOffset(Connection con)
	{
		int xDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)? con.end.getX()-getPos().getX(): (con.end.equals(Utils.toCC(this))&& con.start!=null)?con.start.getX()-getPos().getX(): 0;
		int zDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)? con.end.getZ()-getPos().getZ(): (con.end.equals(Utils.toCC(this))&& con.start!=null)?con.start.getZ()-getPos().getZ(): 0;
		if(facing.getAxis()==Axis.X)
			return new Vec3(.5,.4375,zDif>0?.8125:.1875);
		else
			return new Vec3(xDif>0?.8125:.1875,.4375,.5);
	}

	@Override
	public boolean isDummy()
	{
		return !dummy;
	}
	@Override
	public void placeDummies(BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		worldObj.setBlockState(pos.add(0,1,0), state);
		((TileEntityEnergyMeter)worldObj.getTileEntity(pos.add(0,1,0))).dummy = false;
		((TileEntityEnergyMeter)worldObj.getTileEntity(pos.add(0,1,0))).facing = this.facing;
	}
	@Override
	public void breakDummies(BlockPos pos, IBlockState state)
	{
		for(int i=0; i<=1; i++)
			if(worldObj.getTileEntity(getPos().add(0,!dummy?-1:0,0).add(0,i,0)) instanceof TileEntityEnergyMeter)
				worldObj.setBlockToAir(getPos().add(0,!dummy?-1:0,0).add(0,i,0));
	}

	public int getAveragePower()
	{
		TileEntityEnergyMeter te = this;
		if(te.dummy)
		{
			TileEntity tmp = worldObj.getTileEntity(getPos().add(0,1,0));
			if(!(tmp instanceof TileEntityEnergyMeter))
				return -1;
			te = (TileEntityEnergyMeter) tmp;
		}
		if(te.lastPackets.size()==0)
			return 0;
		int sum = 0;
		synchronized (te.lastPackets)
		{
			for(int transfer: te.lastPackets)
				sum += transfer;
		}
		return sum/te.lastPackets.size();
	}

	@Override
	public float[] getBlockBounds()
	{
		return null;
	}
	@Override
	public float[] getSpecialCollisionBounds()
	{
		return null;
	}
	@Override
	public float[] getSpecialSelectionBounds()
	{
		return null;
	}
	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(.1875f,-.625f,.1875f, .8125f,.8125f,.8125f).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
		if(dummy)
		{
			list.set(0, list.get(0).offset(0,1,0));
			list.add(new AxisAlignedBB(0,0,0, 1,.375f,1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
		}
		return list;
	}
	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, MovingObjectPosition mop, ArrayList<AxisAlignedBB> list)
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
	private void updateComparatorValues()
	{
		int oldVal = compVal;
		int maxTrans = 0;
		Set<Connection> conns = ImmersiveNetHandler.INSTANCE.getConnections(worldObj, dummy?pos.up():pos);
		if (conns==null)
		{
			compVal = 0;
			return;
		}
		for (Connection c:conns)
			maxTrans+=c.cableType.getTransferRate();
		maxTrans/=2;
		double val = getAveragePower()/(double)maxTrans;
		compVal = (int) Math.ceil(15*val);
		if (oldVal!=compVal)
			worldObj.updateComparatorOutputLevel(pos, getBlockType());
	}
	@Override
	public int getComparatorInputOverride()
	{
		return compVal;
	}
}