package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.api.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.WireType;

public class TileEntityConnectorMV extends TileEntityConnectorLV
{
	@Override
	protected boolean canTakeMV()
	{
		return true;
	}
	@Override
	protected boolean canTakeLV()
	{
		return false;
	}
	
	@Override
	public Vec3 getConnectionOffset(Connection con)
	{
		ForgeDirection fd = ForgeDirection.getOrientation(facing).getOpposite();
		double conRadius = con.cableType==WireType.STEEL?.03125:.015625;
		return Vec3.createVectorHelper(.5+fd.offsetX*(.0625-conRadius), .5+fd.offsetY*(.0625-conRadius), .5+fd.offsetZ*(.0625-conRadius));
	}
	
	@Override
	public int getMaxInput()
	{
		return WireType.ELECTRUM.getTransferRate();
	}
	@Override
	public int getMaxOutput()
	{
		return WireType.ELECTRUM.getTransferRate();
	}
}