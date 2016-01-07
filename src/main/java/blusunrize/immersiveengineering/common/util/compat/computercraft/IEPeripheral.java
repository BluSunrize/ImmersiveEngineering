package blusunrize.immersiveengineering.common.util.compat.computercraft;

import blusunrize.immersiveengineering.common.EventHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public abstract class IEPeripheral implements IPeripheral
{
	World w;
	int x, y, z;
	public IEPeripheral(World w, int _x, int _y, int _z)
	{
		this.w = w;
		x = _x;
		y = _y;
		z = _z;
	}
	protected TileEntity getTileEntity(Class<? extends TileEntity> type)
	{
		boolean usePipeline = FMLCommonHandler.instance().getEffectiveSide()!=Side.SERVER;
		TileEntity te = usePipeline?EventHandler.requestTE(w, x, y, z):w.getTileEntity(x, y, z);
		if (te!=null&&te.getClass().equals(type))
			return te;
		return null;
	}
	@Override
	public boolean equals(IPeripheral other)
	{
		if (!(other instanceof IEPeripheral))
			return false;
		IEPeripheral otherPer = (IEPeripheral) other;
		return w==otherPer.w&&x==otherPer.x&&y==otherPer.y&&z==otherPer.z;
	}
}
