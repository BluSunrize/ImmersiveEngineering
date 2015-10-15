package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.api.energy.WireType;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.TargetingInfo;

public class TileEntityConnectorStructural extends TileEntityConnectorLV
{
	public float rotation = 0;

	@Override
	protected boolean canTakeMV()
	{
		return false;
	}
	@Override
	protected boolean canTakeLV()
	{
		return false;
	}
	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setFloat("rotation", rotation);
	}
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		rotation = nbt.getFloat("rotation");
	}

	@Override
	public Vec3 getConnectionOffset(Connection con)
	{
		ForgeDirection fd = ForgeDirection.getOrientation(facing).getOpposite();
		double conRadius = .03125;
		return Vec3.createVectorHelper(.5+fd.offsetX*(-.125-conRadius), .5+fd.offsetY*(-.125-conRadius), .5+fd.offsetZ*(-.125-conRadius));
	}

	@Override
	int getRenderRadiusIncrease()
	{
		return WireType.STRUCTURE_STEEL.getMaxLength();
	}

	@Override
	public int getMaxInput()
	{
		return 0;
	}
	@Override
	public int getMaxOutput()
	{
		return 0;
	}

	@Override
	public boolean canConnectCable(WireType cableType, TargetingInfo target)
	{
		if(cableType!=WireType.STRUCTURE_ROPE && cableType!=WireType.STRUCTURE_STEEL)
			return false;
		return limitType==null||limitType==cableType;
	}
	@Override
	public boolean isEnergyOutput()
	{
		return false;
	}
	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return false;
	}
	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive,boolean simulate)
	{
		return 0;
	}
	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		return 0;
	}
	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		return 0;
	}
}