package blusunrize.immersiveengineering.common.util.compat.computercraft;
import static blusunrize.immersiveengineering.common.util.Utils.saveFluidStack;
import static blusunrize.immersiveengineering.common.util.Utils.saveFluidTank;
import static blusunrize.immersiveengineering.common.util.Utils.saveStack;

import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySqueezer;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class PeripheralSqueezer extends IEPeripheral
{
	public static final String[] cmds = {"getRecipe", "getInputStack", "getOutputStack", "getFluid", "getEmptyCannisters", "getFilledCannisters", "getEnergyStored", "getMaxEnergyStored", "isActive", "setEnabled"};
	public PeripheralSqueezer(World w, BlockPos pos)
	{
		super(w, pos);
	}

	@Override
	public String getType()
	{
		return "IE:squeezer";
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
		TileEntitySqueezer te = (TileEntitySqueezer) getTileEntity(TileEntitySqueezer.class);
		if (te==null)
			throw new LuaException("The squeezer was removed");
		switch (method)
		{
		case 0://recipe
			if (arguments.length!=1||!(arguments[0] instanceof Double))
				throw new LuaException("Wrong amount of arguments, needs one integer");
			int slot = (int) (double)arguments[0];
			if (slot<1||slot>8)
				throw new LuaException("Input slots are numbers 1-8");
			SqueezerRecipe recipe = SqueezerRecipe.findRecipe(te.inventory[slot-1]);
			if (recipe!=null)
				return new Object[]{saveStack(recipe.input.getExampleStack()), saveStack(recipe.itemOutput), saveFluidStack(recipe.fluidOutput), recipe.getTotalProcessTime()};
			else
				return new Object[]{"No recipe found"};
		case 1://Input stack
			if (arguments.length!=1||!(arguments[0] instanceof Double))
				throw new LuaException("Wrong amount of arguments, needs one integer");
			slot = (int) (double)arguments[0];
			if (slot<1||slot>8)
				throw new LuaException("Input slots are numbers 1-8");
			return new Object[]{saveStack(te.inventory[slot-1])};
		case 2://output item stack
			return new Object[]{saveStack(te.inventory[8])};
		case 3://fluid tank
			return new Object[]{saveFluidTank(te.tanks[0])};
		case 4://empty cannisters
			return new Object[]{saveStack(te.inventory[9])};
		case 5://full cannisters
			return new Object[]{saveStack(te.inventory[10])};
		case 6://energy stored
			return new Object[]{te.energyStorage.getEnergyStored()};
		case 7://max energy stored
			return new Object[]{te.energyStorage.getMaxEnergyStored()};
		case 8://isActive
			return new Object[]{te.shouldRenderAsActive()};
		case 9://setEnabled
			if (arguments.length!=1||!(arguments[0] instanceof Boolean))
				throw new LuaException("Wrong amount of arguments, needs one boolean");
			te.computerOn = (Boolean)arguments[0];
		}
		return null;
	}

	@Override
	public void attach(IComputerAccess computer)
	{
		TileEntitySqueezer te = (TileEntitySqueezer) w.getTileEntity(pos);
		if (te==null)
			return;
		te.controllingComputers++;
		te.computerOn = true;
	}

	@Override
	public void detach(IComputerAccess computer)
	{
		TileEntitySqueezer te = (TileEntitySqueezer) w.getTileEntity(pos);
		if (te==null)
			return;
		te.computerOn = false;
	}
}
