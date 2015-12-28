package blusunrize.immersiveengineering.common.util.compat.computercraft;

import java.util.Map;

import blusunrize.immersiveengineering.api.energy.DieselHandler.RefineryRecipe;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRefinery;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidTank;

public class PeripheralRefinery extends IEPeripheral
{
	public static final String[] cmds = {"getInputFluid", "getOutputTank", "getRecipe", "setEnabled", "isValidRecipe", "getEmptyCanisters", "getFullCanisters", "getMaxEnergyStored", "getEnergyStored"};
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
			if (arguments.length!=1||!(arguments[0] instanceof Double))
				throw new LuaException("Wrong amount of arguments, needs one integer");
			int tank = (int)(double)arguments[0];
			if (tank<0||tank>1)
				throw new LuaException("Only tanks 0 and 1 are available");
			FluidTank write;
			if (tank==0)
				write = te.tank0;
			else
				write = te.tank1;
			return new Object[]{saveFluidTank(write)};
		case 1://Output
			return new Object[]{saveFluidTank(te.tank2)};
		case 2://recipe
			RefineryRecipe ref = te.getRecipe();
			if (ref==null)
				throw new LuaException("The recipe of the refinery is invalid");
			Map<String, Object> in1 = saveFluidStack(ref.input0);
			Map<String, Object> in2 = saveFluidStack(ref.input1);
			Map<String, Object> out = saveFluidStack(ref.output);
			return new Object[]{in1, in2, out};
		case 3://setEnabled
			if (arguments.length!=1||!(arguments[0] instanceof Boolean))
				throw new LuaException("Wrong amount of arguments, needs one boolean");
			boolean param = (boolean)arguments[0];
			te.computerOn = param;
			return null;
		case 4://isValid
			return new Object[]{te.getRecipe()!=null};
		case 5://Empty canisters
			if (arguments.length!=1||!(arguments[0] instanceof Double))
				throw new LuaException("Wrong amount of arguments, needs one integer");
			int id = (int)(double)arguments[0];
			if (id>2||id<0)
				throw new LuaException("Empty canisters can be requested for tanks 0, 1 and 2");
			switch (id)
			{
			case 0:
				return new Object[]{saveStack(te.inventory[1])};
			case 1:
				return new Object[]{saveStack(te.inventory[3])};
			case 2:
				return new Object[]{saveStack(te.inventory[4])};
			}
		case 6://full canisters
			if (arguments.length!=1||!(arguments[0] instanceof Double))
				throw new LuaException("Wrong amount of arguments, needs one integer");
			id = (int)(double)arguments[0];
			if (id>2||id<0)
				throw new LuaException("Empty canisters can be requested for tanks 0, 1 and 2");
			switch (id)
			{
			case 0:
				return new Object[]{saveStack(te.inventory[0])};
			case 1:
				return new Object[]{saveStack(te.inventory[2])};
			case 2:
				return new Object[]{saveStack(te.inventory[5])};
			}
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
