package blusunrize.immersiveengineering.common.util.compat.computercraft;

import java.util.Map;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityArcFurnace;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PeripheralArcFurnace extends IEPeripheral
{
	public static final String[] cmds = {"setEnabled", "isActive", "getInputStack", "getOutputStack", "getAdditiveStack", "getSlagStack", "hasElectrodes", "getElectrode", "getMaxEnergyStored", "getEnergyStored"};
	public PeripheralArcFurnace(World w, int _x, int _y, int _z)
	{
		super(w, _x, _y, _z);
	}

	@Override
	public String getType()
	{
		return "IE:arcFurnace";
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
		TileEntityArcFurnace te = (TileEntityArcFurnace) getTileEntity(TileEntityArcFurnace.class);
		if (te==null)
			throw new LuaException("The arc furnace was removed");
		switch (method)
		{
		case 0: //set RS simulation
			if (arguments.length!=1||!(arguments[0] instanceof Boolean))
				throw new LuaException("Wrong amount of arguments, needs one boolean");
			boolean param = (boolean)arguments[0];
			te.computerOn = param;
			return null;
		case 1: //get active
			return new Object[]{te.active};
		case 2://get input stack (0-11)
			if (arguments.length!=1||!(arguments[0] instanceof Integer)&&!(arguments[0] instanceof Double))
				throw new LuaException("Wrong amount of arguments, needs one integer");
			int slot = (int)(double)arguments[0];
			if (slot<0||slot>11)
				throw new LuaException("Input slots are 0-11");
			Map<String, Object> ret = saveStack(te.getStackInSlot(slot));
			ret.put("progress", te.process[slot]);
			ret.put("maxProgress", te.processMax[slot]);
			return new Object[]{ret};
		case 3://get Output stack (16-21)
			if (arguments.length!=1||!(arguments[0] instanceof Integer)&&!(arguments[0] instanceof Double))
				throw new LuaException("Wrong amount of arguments, needs one integer");
			slot = (int)(double)arguments[0];
			if (slot<0||slot>5)
				throw new LuaException("Output slots are 0-5");
			return new Object[]{saveStack(te.getStackInSlot(slot+16))};
		case 4://get additives (12-15)
			if (arguments.length!=1||!(arguments[0] instanceof Integer)&&!(arguments[0] instanceof Double))
				throw new LuaException("Wrong amount of arguments, needs one integer");
			slot = (int)(double)arguments[0];
			if (slot<0||slot>3)
				throw new LuaException("Additive slots are 0-3");
			return new Object[]{saveStack(te.getStackInSlot(slot+12))};
		case 5://getSlag
			return new Object[]{saveStack(te.getStackInSlot(22))};
		case 6://hasElectrodes
			return new Object[]{te.electrodes[0]&&te.electrodes[1]&&te.electrodes[2]};
		case 7: //getElectrode
			if (arguments.length!=1||!(arguments[0] instanceof Integer)&&!(arguments[0] instanceof Double))
				throw new LuaException("Wrong amount of arguments, needs one integer");
			slot = (int)(double)arguments[0];
			if (slot<0||slot>2)
				throw new LuaException("Output slots are 0-2");
			return new Object[]{saveStack(te.getStackInSlot(slot+23))};
		case 8: //max energy
			return new Object[]{te.energyStorage.getMaxEnergyStored()};
		case 9:
			return new Object[]{te.energyStorage.getEnergyStored()};
		}
		return null;
	}

	@Override
	public void attach(IComputerAccess computer)
	{
		TileEntityArcFurnace te = (TileEntityArcFurnace) w.getTileEntity(x, y, z);
		if (te!=null)
		{
			te.computerControlled = true;
			te.computerOn = true;
		}
	}

	@Override
	public void detach(IComputerAccess computer)
	{
		TileEntityArcFurnace te = (TileEntityArcFurnace) w.getTileEntity(x, y, z);
		if (te!=null)
			te.computerControlled = false;
	}
	
	@Override
	protected TileEntity getTileEntity(Class<? extends TileEntity> type)
	{
		TileEntityArcFurnace te = (TileEntityArcFurnace) super.getTileEntity(type);
		return te;
	}

}
