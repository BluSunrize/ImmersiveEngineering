package blusunrize.immersiveengineering.common.util.compat.computercraft;

import blusunrize.immersiveengineering.api.energy.DieselHandler.RefineryRecipe;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRefinery;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidTank;

public class PeripheralRefinery extends IEPeripheral
{
	public static final String[] cmds = {"getInputFluid", "getOutputTank", "getRecipe", "setEnabled", "isValidRecipe", "getEmptyCanisters", "getFilledCanisters", "getMaxEnergyStored", "getEnergyStored"};
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
			return null;
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
			return saveFluidStack(write.getFluid(), new Object[2], 0);
		case 1://Output
			return saveFluidStack(te.tank2.getFluid(), new Object[2], 0);
		case 2://recipe
			Object[] ret = new Object[7];
			RefineryRecipe ref = te.getRecipe();
			if (ref==null)
				throw new LuaException("The recipe of the refinery is invalid");
			saveFluidStack(ref.input0, ret, 0);
			saveFluidStack(ref.input1, ret, 2);
			saveFluidStack(ref.output, ret, 4);
			return ret;
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
				return saveStack(te.inventory[1], new Object[3]);
			case 1:
				return saveStack(te.inventory[3], new Object[3]);
			case 2:
				return saveStack(te.inventory[4], new Object[3]);
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
				return saveStack(te.inventory[0], new Object[3]);
			case 1:
				return saveStack(te.inventory[2], new Object[3]);
			case 2:
				return saveStack(te.inventory[5], new Object[3]);
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
		TileEntityRefinery te = (TileEntityRefinery) getTileEntity(TileEntityRefinery.class);
		if (te==null)
			return;
		te.computerControlled = true;
		te.computerOn = true;
	}

	@Override
	public void detach(IComputerAccess computer)
	{
		TileEntityRefinery te = (TileEntityRefinery) getTileEntity(TileEntityRefinery.class);
		if (te==null)
			return;
		te.computerControlled = false;
	}
	@Override
	protected TileEntity getTileEntity(Class<? extends TileEntity> type)
	{
		TileEntityRefinery te = (TileEntityRefinery) super.getTileEntity(type);
		return te==null?null:te.master();
	}
}
