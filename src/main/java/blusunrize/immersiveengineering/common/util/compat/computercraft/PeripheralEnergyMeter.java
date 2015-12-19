package blusunrize.immersiveengineering.common.util.compat.computercraft;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityEnergyMeter;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PeripheralEnergyMeter implements IPeripheral {
	final static String[] cmds = {"getAvgEnergy"};
	World w;
	int x, y, z;
	public PeripheralEnergyMeter(World world, int _x, int _y, int _z) {
		w = world;
		x = _x;
		y = _y;
		z = _z;
	}
	@Override
	public String getType() {
		return "IE:CurrentTrafo";
	}

	@Override
	public String[] getMethodNames() {
		return cmds;
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments)
			throws LuaException, InterruptedException {
		if (method<0||method>=cmds.length)
			return null;
		
		if (method==0)
		{
			TileEntity te = w.getTileEntity(x, y, z);
			return new Object[]{(te instanceof TileEntityEnergyMeter)?((TileEntityEnergyMeter)te).getAveragePower():-1};
		}
		return null;
	}

	@Override
	public void attach(IComputerAccess computer) {
		
	}

	@Override
	public void detach(IComputerAccess computer) {
		
	}

	@Override
	public boolean equals(IPeripheral other) {
		if (!(other instanceof PeripheralEnergyMeter))
			return false;
		PeripheralEnergyMeter otherPer = (PeripheralEnergyMeter) other;
		return w==otherPer.w&&x==otherPer.x&&y==otherPer.y&&z==otherPer.z;
	}

}
