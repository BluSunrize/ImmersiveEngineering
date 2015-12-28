package blusunrize.immersiveengineering.common.util.compat.computercraft;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFloodlight;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.world.World;

public class PeripheralFloodlight extends IEPeripheral
{

	public PeripheralFloodlight(World w, int _x, int _y, int _z)
	{
		super(w, _x, _y, _z);
	}

	@Override
	public String getType()
	{
		return "IE:floodlight";
	}

	@Override
	public String[] getMethodNames()
	{
		return new String[]{"turnAroundXZ", "turnAroundY", "canTurn", "waitForTimeout", "setEnabled", "isActive", "getEnergyStored", "getMaxEnergyStored"};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments)
			throws LuaException, InterruptedException
	{
		TileEntityFloodlight te = (TileEntityFloodlight) getTileEntity(TileEntityFloodlight.class);
		if(te==null)
			throw new LuaException("The floodlight was removed");
		switch (method)
		{
		case 0://turn X
			if (arguments.length!=1||!(arguments[0] instanceof Boolean))
				throw new LuaException("Wrong amount of arguments, needs one boolean");
			if (!te.canComputerTurn())
				throw new LuaException("The floodlight can not turn again yet");
			boolean param = (boolean)arguments[0];
			te.turnX(param, true);
			return null;
		case 1://turn Y
			if (arguments.length!=1||!(arguments[0] instanceof Boolean))
				throw new LuaException("Wrong amount of arguments, needs one boolean");
			if (!te.canComputerTurn())
				throw new LuaException("The floodlight can not turn again yet");
			param = (boolean)arguments[0];
			te.turnY(param, true);
			return null;
		case 2://can turn
			return new Object[]{te.canComputerTurn()};
		case 3://wait
			synchronized (te)
			{
				while (!te.canComputerTurn())
					te.wait();
			}
			return null;
		case 4://setEnabled
			if (arguments.length!=1||!(arguments[0] instanceof Boolean))
				throw new LuaException("Wrong amount of arguments, needs one boolean");
			te.computerOn = (boolean)arguments[0];
			return null;
		case 5: //is active
			return new Object[]{te.active};
		case 6://stored energy
			return new Object[]{te.energyStorage};
		case 7://max energy stored
			return new Object[]{60};
		}
		return null;
	}

	@Override
	public void attach(IComputerAccess computer)
	{
		TileEntityFloodlight te = (TileEntityFloodlight) w.getTileEntity(x, y, z);
		if(te==null)
			return;
		te.computerControlled = true;
		te.computerOn = true;
	}

	@Override
	public void detach(IComputerAccess computer)
	{
		TileEntityFloodlight te = (TileEntityFloodlight) w.getTileEntity(x, y, z);
		if(te==null)
			return;
		te.computerControlled = false;
	}

}
