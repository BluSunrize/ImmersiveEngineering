package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

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
//	@Override
//	public boolean canUpdate()
//	{
//		return false;
//	}

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
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT && worldObj!=null)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public Vec3d getConnectionOffset(Connection con)
	{
		EnumFacing side = facing.getOpposite();
		double conRadius = .03125;
		return new Vec3d(.5+side.getFrontOffsetX()*(-.125-conRadius), .5+side.getFrontOffsetY()*(-.125-conRadius), .5+side.getFrontOffsetZ()*(-.125-conRadius));
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
//	@Override
//	public boolean canConnectEnergy(ForgeDirection from)
//	{
//		return false;
//	}
//	@Override
//	public int receiveEnergy(ForgeDirection from, int maxReceive,boolean simulate)
//	{
//		return 0;
//	}
//	@Override
//	public int getEnergyStored(ForgeDirection from)
//	{
//		return 0;
//	}
//	@Override
//	public int getMaxEnergyStored(ForgeDirection from)
//	{
//		return 0;
//	}
}