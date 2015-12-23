package blusunrize.immersiveengineering.common.util.compat.computercraft;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityDieselGenerator;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PeripheralDieselGenerator extends IEPeripheral
{
	public static String[] cmds = {"setEnabled", "isActive", "getTankInfo"};
	public PeripheralDieselGenerator(World w, int _x, int _y, int _z)
	{
		super(w, _x, _y, _z);
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
		TileEntityDieselGenerator te = (TileEntityDieselGenerator) getTileEntity(TileEntityDieselGenerator.class);
		if (te==null)
			return null;
		switch (method)
		{
		case 0://set enabled
			if (arguments.length!=1||!(arguments[0] instanceof Boolean))
				throw new LuaException("Wrong amount of arguments, needs one boolean");
			boolean param = (boolean)arguments[0];
			te.computerActivated = param;
			return null;
		case 1://is active
			return new Object[]{te.active};
		case 2://tank
			return new Object[]{saveFluidTank(te.tank)};
		}
		return null;
	}
	@Override
	public void attach(IComputerAccess computer)
	{
		TileEntityDieselGenerator te = (TileEntityDieselGenerator) getTileEntity(TileEntityDieselGenerator.class);
		if (te==null)
			return;
		te.computerControlled = true;
		te.computerActivated = true;
	}

	@Override
	public void detach(IComputerAccess computer)
	{
		TileEntityDieselGenerator te = (TileEntityDieselGenerator) getTileEntity(TileEntityDieselGenerator.class);
		if (te==null)
			return;
		te.computerControlled = false;
	}
	@Override
	protected TileEntity getTileEntity(Class<? extends TileEntity> type)
	{
		TileEntityDieselGenerator te = (TileEntityDieselGenerator) super.getTileEntity(type);
		return te==null?null:te.master();
	}
}
