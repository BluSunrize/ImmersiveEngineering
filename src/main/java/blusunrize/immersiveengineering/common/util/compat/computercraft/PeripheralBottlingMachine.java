package blusunrize.immersiveengineering.common.util.compat.computercraft;

import java.util.Map;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBottlingMachine;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PeripheralBottlingMachine extends IEPeripheral {
	public static final String[] cmds = {"getFluid", "getEmptyCannister", "getEmptyCannisterCount", "getFilledCannister", "getFilledCannisterCount", "getEnergyStored", "getMaxEnergyStored", "setEnabled"};
	public PeripheralBottlingMachine(World w, int _x, int _y, int _z)
	{
		super(w, _x, _y, _z);
	}

	@Override
	public String getType()
	{
		return "IE:bottlingMachine";
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
		TileEntityBottlingMachine te = (TileEntityBottlingMachine) getTileEntity(TileEntityBottlingMachine.class);
		if (te==null)
			throw new LuaException("The bottling machine was removed");
		switch (method)
		{
		case 0://tank
			return new Object[]{saveFluidTank(te.tank)};
		case 1://get empty cannister
			if (arguments.length!=1||!(arguments[0] instanceof Double))
				throw new LuaException("Wrong amount of arguments, needs one integer");
			int param = (int)(double) arguments[0];
			if (param<0||param>4)
				throw new LuaException("Only 0-4 are valid cannister positions");
			int id = te.getEmptyCannister(param);
			Map<String, Object> map = saveStack(te.inventory[id]);
			map.put("process", te.process[id]);
			return new Object[]{map};
		case 2://empty count
			return new Object[]{te.getEmptyCount()};
		case 3://get filled cannister
			if (arguments.length!=1||!(arguments[0] instanceof Double))
				throw new LuaException("Wrong amount of arguments, needs one integer");
			param = (int)(double) arguments[0];
			if (param<0||param>4)
				throw new LuaException("Only 0-4 are valid cannister positions");
			id = te.getFilledCannister(param);
			map = saveStack(te.inventory[id]);
			map.put("process", te.process[id]);
			return new Object[]{map};
		case 4://filled count
			return new Object[]{te.getFilledCount()};
		case 5://energy stored
			return new Object[]{te.energyStorage.getEnergyStored()};
		case 6://max energy stored
			return new Object[]{te.energyStorage.getMaxEnergyStored()};
		case 7://set active
			if (arguments.length!=1||!(arguments[0] instanceof Boolean))
				throw new LuaException("Wrong amount of arguments, needs one boolean");
			boolean active = (boolean) arguments[0];
			te.computerOn = active;
			return null;
		}
		return null;
	}

	@Override
	public void attach(IComputerAccess computer)
	{
		TileEntity te = w.getTileEntity(x, y, z);
		if (!(te instanceof TileEntityBottlingMachine))
			return;
		((TileEntityBottlingMachine)te).computerControlled = true;
		((TileEntityBottlingMachine)te).computerOn = true;
	}

	@Override
	public void detach(IComputerAccess computer)
	{
		TileEntity te = w.getTileEntity(x, y, z);
		if (!(te instanceof TileEntityBottlingMachine))
			return;
		((TileEntityBottlingMachine)te).computerControlled = false;
	}

}
