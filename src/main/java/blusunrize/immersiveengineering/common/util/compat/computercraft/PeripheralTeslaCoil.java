package blusunrize.immersiveengineering.common.util.compat.computercraft;

import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTeslaCoil;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class PeripheralTeslaCoil extends IEPeripheral {

	final static String[] cmds = {"isActive", "setRSMode", "setPowerMode"};
	public PeripheralTeslaCoil(World w, BlockPos pos)
	{
		super(w, pos);
	}
	@Override
	public String getType()
	{
		return "IE:teslaCoil";
	}

	@Override
	public String[] getMethodNames()
	{
		return cmds;
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments)
			throws LuaException, InterruptedException
	{
		TileEntityTeslaCoil te = getTileEntity(TileEntityTeslaCoil.class);
		if (te==null)
			throw new LuaException("The tesla coil was removed");
		switch (method)
		{
		case 0://isActive
			int energyDrain = Config.getInt("teslacoil_consumption");
			if (te.lowPower)
				energyDrain/=2;
			return new Object[]{te.canRun(energyDrain)};
		case 1://set RS mode
			if (arguments.length!=1||!(arguments[0] instanceof Boolean))
				throw new LuaException("Wrong amount of arguments, needs one boolean");
			te.redstoneControlInverted = (boolean) arguments[0];
			break;
		case 2:
			if (arguments.length!=1||!(arguments[0] instanceof Boolean))
				throw new LuaException("Wrong amount of arguments, needs one boolean");
			energyDrain = Config.getInt("teslacoil_consumption");
			if (te.lowPower)
				energyDrain/=2;
			if (te.canRun(energyDrain))
				throw new LuaException("Can't switch power mode on an active coil");
			te.lowPower = !(boolean) arguments[0];
		}
		return null;
	}

	@Override
	public void attach(IComputerAccess computer) {}

	@Override
	public void detach(IComputerAccess computer) {}

	@Override
	public boolean equals(IPeripheral other)
	{
		if (!(other instanceof PeripheralTeslaCoil))
			return false;
		PeripheralTeslaCoil otherPer = (PeripheralTeslaCoil) other;
		return w==otherPer.w&&pos.equals(otherPer.pos);
	}

}
