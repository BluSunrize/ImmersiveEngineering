package blusunrize.immersiveengineering.common.util.compat.computercraft;

import static blusunrize.immersiveengineering.common.util.Utils.saveStack;

import java.util.Map;

import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityArcFurnace;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.items.ItemGraphiteElectrode;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class PeripheralArcFurnace extends IEPeripheral
{
	public static final String[] cmds = {"setEnabled", "isActive", "getInputStack", "getOutputStack", "getAdditiveStack", "getSlagStack", "hasElectrodes", "getElectrode", "getMaxEnergyStored", "getEnergyStored"};
	public PeripheralArcFurnace(World w, BlockPos pos)
	{
		super(w, pos);
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
			return new Object[]{te.shouldRenderAsActive()};
		case 2://get input stack (0-11)
			if (arguments.length!=1||!(arguments[0] instanceof Integer)&&!(arguments[0] instanceof Double))
				throw new LuaException("Wrong amount of arguments, needs one integer");
			int slot = (int)(double)arguments[0];
			if (slot<1||slot>12)
				throw new LuaException("Input slots are 1-12");
			Map<String, Object> ret = saveStack(te.inventory[slot-1]);

			mainLoop:
				for (MultiblockProcess<ArcFurnaceRecipe> p:te.processQueue)
					for (int i:((MultiblockProcessInMachine<ArcFurnaceRecipe>)p).getInputSlots())
						if (i==slot-1)
						{
							ret.put("progress", p.processTick);
							ret.put("maxProgress", p.maxTicks);
							break mainLoop;
						}
			return new Object[]{ret};
		case 3://get Output stack (16-21)
			if (arguments.length!=1||!(arguments[0] instanceof Integer)&&!(arguments[0] instanceof Double))
				throw new LuaException("Wrong amount of arguments, needs one integer");
			slot = (int)(double)arguments[0];
			if (slot<1||slot>6)
				throw new LuaException("Output slots are 1-6");
			return new Object[]{saveStack(te.inventory[slot+15])};
		case 4://get additives (12-15)
			if (arguments.length!=1||!(arguments[0] instanceof Integer)&&!(arguments[0] instanceof Double))
				throw new LuaException("Wrong amount of arguments, needs one integer");
			slot = (int)(double)arguments[0];
			if (slot<1||slot>4)
				throw new LuaException("Additive slots are 1-4");
			return new Object[]{saveStack(te.inventory[slot+11])};
		case 5://getSlag
			return new Object[]{saveStack(te.inventory[22])};
		case 6://hasElectrodes
			return new Object[]{te.hasElectrodes()};
		case 7: //getElectrode
			if (arguments.length!=1||!(arguments[0] instanceof Integer)&&!(arguments[0] instanceof Double))
				throw new LuaException("Wrong amount of arguments, needs one integer");
			slot = (int)(double)arguments[0];
			if (slot<1||slot>3)
				throw new LuaException("Electrode slots are 1-3");
			ItemStack stack = te.inventory[slot+22];
			Map<String, Object> map = saveStack(stack);
			if (stack!=null&&stack.getItem() instanceof ItemGraphiteElectrode)
				map.put("damage", ItemNBTHelper.getInt(stack, "graphDmg"));
			return new Object[]{map};
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
		TileEntityArcFurnace te = (TileEntityArcFurnace) w.getTileEntity(pos);
		if (te!=null)
		{
			te.controllingComputers++;
			te.computerOn = true;
		}
	}

	@Override
	public void detach(IComputerAccess computer)
	{
		TileEntityArcFurnace te = (TileEntityArcFurnace) w.getTileEntity(pos);
		if (te!=null)
			te.controllingComputers--;
	}

}
