package blusunrize.immersiveengineering.common.util.compat.computercraft;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public abstract class IEPeripheral implements IPeripheral {
	World w;
	int x, y, z;
	public IEPeripheral(World w, int _x, int _y, int _z)
	{
		this.w = w;
		x = _x;
		y = _y;
		z = _z;
	}
	protected TileEntity getTileEntity(Class<? extends TileEntity> type) {
		TileEntity te = w.getTileEntity(x, y, z);
		if (te.getClass().equals(type))
			return te;
		return null;
	}
}
