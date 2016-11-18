package blusunrize.immersiveengineering.common.blocks.metal;

import javax.annotation.Nonnull;

import blusunrize.immersiveengineering.api.energy.IRotationAcceptor;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxConnection;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import cofh.api.energy.IEnergyConnection;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class TileEntityDynamo extends TileEntityIEBase implements IFluxConnection,IEnergyConnection, IDirectionalTile, IRotationAcceptor
{
	public EnumFacing facing = EnumFacing.NORTH;
	
	@Override
	public void inputRotation(double rotation, @Nonnull EnumFacing side)
	{
		if(side!=this.facing.getOpposite())
			return;
		int output = (int) (IEConfig.Machines.dynamo_output * rotation);
		for(EnumFacing fd : EnumFacing.VALUES)
		{
			TileEntity te = worldObj.getTileEntity(getPos().offset(fd));
			if(te instanceof IFluxReceiver)
				output -= ((IFluxReceiver)te).receiveEnergy(fd.getOpposite(), output, false);
		}
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
		return true;
	}
	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return true;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		facing = EnumFacing.getFront(nbt.getInteger("facing"));
//		if(descPacket && worldObj!=null)
//			worldObj.markBlockForUpdate(getPos());
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("facing", facing.ordinal());
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from)
	{
		return true;
	}
}