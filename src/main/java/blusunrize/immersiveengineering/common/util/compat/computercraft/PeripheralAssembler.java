package blusunrize.immersiveengineering.common.util.compat.computercraft;

import static blusunrize.immersiveengineering.common.util.Utils.saveFluidTank;
import static blusunrize.immersiveengineering.common.util.Utils.saveStack;

import java.util.ArrayList;
import java.util.HashMap;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityAssembler;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class PeripheralAssembler extends IEPeripheral {
	public static final String[] cmds = {"hasIngredients", "setEnabled", "getRecipe", "isValidRecipe", "getTank", "getMaxEnergyStored", "getEnergyStored", "getStackInSlot", "getBufferStack"};
	public PeripheralAssembler(World w, int _x, int _y, int _z) {
		super(w, _x, _y, _z);
	}

	@Override
	public String getType() {
		return "IE:assembler";
	}

	@Override
	public String[] getMethodNames() {
		return cmds;
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments)
			throws LuaException, InterruptedException {
		TileEntityAssembler te = (TileEntityAssembler) getTileEntity(TileEntityAssembler.class);
		if (te==null)
			throw new LuaException("The assembler was removed");
		switch (method)
		{
		case 0://has ingredients
			if (arguments.length!=1||!(arguments[0] instanceof Double))
				throw new LuaException("Wrong amount of arguments, needs one integer");
			int recipe = (int)(double)arguments[0];
			if (recipe>3||recipe<1)
				throw new LuaException("Only recipes 1-3 are available");
			if (te.patterns[recipe-1].inv[9]==null)
				throw new LuaException("The requested recipe is invalid");
			ArrayList<ItemStack> queryList = new ArrayList<>();
			for(ItemStack stack : te.inventory)
				if(stack!=null)
					queryList.add(stack.copy());
			return new Object[]{te.hasIngredients(te.patterns[recipe-1], queryList)};
		case 1://setEnabled
			if (arguments.length!=2||!(arguments[1] instanceof Boolean)||!(arguments[0] instanceof Double))
				throw new LuaException("Wrong amount of arguments, needs one integer and one boolean");
			recipe = (int)(double)arguments[0];
			boolean active = (boolean) arguments[1];
			if (recipe>3||recipe<1)
				throw new LuaException("Only recipes 1-3 are available");
			te.computerOn[recipe-1] = active;
			return null;
		case 2://getRecipe
			if (arguments.length!=1||!(arguments[0] instanceof Integer)&&!(arguments[0] instanceof Double))
				throw new LuaException("Wrong amount of arguments, needs one integer");
			recipe = (int)(double)arguments[0];
			if (recipe>3||recipe<1)
				throw new LuaException("Only recipes 1-3 are available");
			HashMap<String, Object> ret = new HashMap<>();
			for (int i = 0;i<9;i++)
				ret.put("in"+(i+1), saveStack(te.patterns[recipe-1].inv[i]));
			ret.put("out", saveStack(te.patterns[recipe-1].inv[9]));
			return new Object[]{ret};
		case 3://is valid
			if (arguments.length!=1||!(arguments[0] instanceof Double))
				throw new LuaException("Wrong amount of arguments, needs one integer");
			recipe = (int)(double)arguments[0];
			if (recipe>3||recipe<1)
				throw new LuaException("Only recipes 1-3 are available");
			return new Object[]{te.patterns[recipe-1].inv[9]!=null};
		case 4://get tank
			if (arguments.length!=1||!(arguments[0] instanceof Double))
				throw new LuaException("Wrong amount of arguments, needs one integer");
			int tank = (int)(double)arguments[0];
			if (tank>3||tank<1)
				throw new LuaException("Only tanks 1-3 are available");
			return new Object[]{saveFluidTank(te.tanks[tank-1])};
		case 5://max energy
			return new Object[]{te.energyStorage.getMaxEnergyStored()};
		case 6://curr energy
			return new Object[]{te.energyStorage.getEnergyStored()};
		case 7://get stack
			if (arguments.length!=1||!(arguments[0] instanceof Double))
				throw new LuaException("Wrong amount of arguments, needs one integer");
			int slot = (int)(double)arguments[0];
			if (slot>18||slot<1)
				throw new LuaException("Only slots 1-18 are available");
			return new Object[]{saveStack(te.getStackInSlot(slot-1))};
		case 8://buffer slot
			if (arguments.length!=1||!(arguments[0] instanceof Integer)&&!(arguments[0] instanceof Double))
				throw new LuaException("Wrong amount of arguments, needs one integer");
			recipe = (int)(double)arguments[0];
			if (recipe>3||recipe<1)
				throw new LuaException("Only recipes 1-3 are available");
			return new Object[]{saveStack(te.inventory[17+recipe])};
		}
		return null;
	}

	@Override
	public void attach(IComputerAccess computer) {
		TileEntityAssembler te = (TileEntityAssembler) getTileEntity(TileEntityAssembler.class);
		if (te==null)
			return;
		te.computerControlled = true;
		te.computerOn[0] = true;
		te.computerOn[1] = true;
		te.computerOn[2] = true;
	}

	@Override
	public void detach(IComputerAccess computer) {
		TileEntityAssembler te = (TileEntityAssembler) getTileEntity(TileEntityAssembler.class);
		if (te==null)
			return;
		te.computerControlled = false;
	}

}
