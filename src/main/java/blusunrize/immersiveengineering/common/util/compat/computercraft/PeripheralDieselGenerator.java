package blusunrize.immersiveengineering.common.util.compat.computercraft;

import static blusunrize.immersiveengineering.common.util.Utils.saveFluidTank;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityDieselGenerator;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class PeripheralDieselGenerator extends IEPeripheral
{
	public static String[] cmds = {"setEnabled", "isActive", "getTankInfo"};
	public PeripheralDieselGenerator(World w, BlockPos pos)
	{
		super(w, pos);
	}

	@Override
	public String getType()
	{
		return "IE:dieselGenerator";
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
		TileEntityDieselGenerator te = getTileEntity(TileEntityDieselGenerator.class);
		if (te==null)
			throw new LuaException("The diesel generator was removed");
		switch (method)
		{
		case 0://set enabled
			if (arguments.length!=1||!(arguments[0] instanceof Boolean))
				throw new LuaException("Wrong amount of arguments, needs one boolean");
			boolean param = (boolean)arguments[0];
			te.computerOn = param;
			return null;
		case 1://is active
			return new Object[]{te.active};
		case 2://tank
			return new Object[]{saveFluidTank(te.tanks[0])};
		}
		return null;
	}
	@Override
	public void attach(IComputerAccess computer)
	{
		TileEntityDieselGenerator te = (TileEntityDieselGenerator) w.getTileEntity(pos);
		if (te==null)
			return;
		te.controllingComputers++;
		te.computerOn = true;
	}

	@Override
	public void detach(IComputerAccess computer)
	{
		TileEntityDieselGenerator te = (TileEntityDieselGenerator) w.getTileEntity(pos);
		if (te==null)
			return;
		te.computerOn = false;
	}
}
