package blusunrize.immersiveengineering.common.util.compat.computercraft;
import static blusunrize.immersiveengineering.common.util.Utils.saveFluidStack;
import static blusunrize.immersiveengineering.common.util.Utils.saveFluidTank;
import static blusunrize.immersiveengineering.common.util.Utils.saveStack;

import java.util.HashMap;

import blusunrize.immersiveengineering.api.energy.DieselHandler.RefineryRecipe;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRefinery;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.world.World;

public class PeripheralRefinery extends IEPeripheral
{
	public static final String[] cmds = {"getInputFluidTanks", "getOutputTank", "getRecipe", "setEnabled", "isValidRecipe", "getEmptyCannisters", "getFullCannisters", "getMaxEnergyStored", "getEnergyStored"};
	public PeripheralRefinery(World w, int _x, int _y, int _z)
	{
		super(w, _x, _y, _z);
	}

	@Override
	public String getType()
	{
		return "IE:refinery";
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

		TileEntityRefinery te = (TileEntityRefinery) getTileEntity(TileEntityRefinery.class);
		if (te==null)
			throw new LuaException("The refinery was removed");
		switch (method)
		{
		case 0://getFluid
			HashMap<String, Object> ret = new HashMap<>(2);
			ret.put("input1", saveFluidTank(te.tank0));
			ret.put("input2", saveFluidTank(te.tank1));
			return new Object[]{ret};
		case 1://Output
			return new Object[]{saveFluidTank(te.tank2)};
		case 2://recipe
			RefineryRecipe ref = te.getRecipe(false);
			if (ref==null)
				throw new LuaException("The recipe of the refinery is invalid");
			ret = new HashMap<>(3);
			ret.put("input1", saveFluidStack(ref.input0));
			ret.put("input2", saveFluidStack(ref.input1));
			ret.put("output", saveFluidStack(ref.output));
			return new Object[]{ret};
		case 3://setEnabled
			if (arguments.length!=1||!(arguments[0] instanceof Boolean))
				throw new LuaException("Wrong amount of arguments, needs one boolean");
			boolean param = (boolean)arguments[0];
			te.computerOn = param;
			return null;
		case 4://isValid
			return new Object[]{te.getRecipe(false)!=null};
		case 5://Empty cannisters
			ret = new HashMap<>(3);
			ret.put("input1", saveStack(te.inventory[1]));
			ret.put("input2", saveStack(te.inventory[3]));
			ret.put("output", saveStack(te.inventory[4]));
			return new Object[]{ret};
		case 6://full cannisters
			ret = new HashMap<>(3);
			ret.put("input1", saveStack(te.inventory[0]));
			ret.put("input2", saveStack(te.inventory[2]));
			ret.put("output", saveStack(te.inventory[5]));
			return new Object[]{ret};
		case 7://max energy
			return new Object[]{te.energyStorage.getMaxEnergyStored()};
		case 8://current energy
			return new Object[]{te.energyStorage.getEnergyStored()};
		}
		return null;
	}

	@Override
	public void attach(IComputerAccess computer)
	{
		TileEntityRefinery te = (TileEntityRefinery) w.getTileEntity(x, y, z);
		if (te==null)
			return;
		te.computerControlled = true;
		te.computerOn = true;
	}

	@Override
	public void detach(IComputerAccess computer)
	{
		TileEntityRefinery te = (TileEntityRefinery) w.getTileEntity(x, y, z);
		if (te==null)
			return;
		te.computerControlled = false;
	}
}
