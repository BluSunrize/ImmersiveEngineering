package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.energy.IRotationAcceptor;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxConnector;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityDynamo extends TileEntityIEBase implements IIEInternalFluxConnector, IDirectionalTile, IRotationAcceptor
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

	@Nonnull
	@Override
	public SideConfig getEnergySideConfig(@Nullable EnumFacing facing)
	{
		return SideConfig.INPUT;
	}
	@Override
	public boolean canConnectEnergy(EnumFacing from)
	{
		return true;
	}
	IEForgeEnergyWrapper wrapper = new IEForgeEnergyWrapper(this,null);
	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing)
	{
		return wrapper;
	}
}