package blusunrize.immersiveengineering.common.util.compat.computercraft;

import java.util.Map;

import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal.MultiblockProcessInWorld;
import blusunrize.immersiveengineering.common.util.Utils;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class PeripheralCrusher extends IEPeripheral
{
	public static final String[] cmds = {"getQueueLength", "setEnabled", "isActive", "getInputStack", "getMaxEnergyStored", "getEnergyStored"};
	public PeripheralCrusher(World w, BlockPos pos)
	{
		super(w, pos);
	}

	@Override
	public String getType()
	{
		return "IE:crusher";
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
		TileEntityCrusher te = getTileEntity(TileEntityCrusher.class);
		if (te==null)
			throw new LuaException("The crusher was removed");
		switch (method)
		{
		case 0: //queue length
			synchronized (te.inputs)
			{
				return new Object[]{te.processQueue.size()};
			}
		case 1: //set RS simulation
			if (arguments.length!=1||!(arguments[0] instanceof Boolean))
				throw new LuaException("Wrong amount of arguments, needs one boolean");
			boolean param = (boolean)arguments[0];
			te.computerOn = param;
			return null;
		case 2: //get active
			return new Object[]{te.shouldRenderAsActive()};
		case 3: //get queue element
			if (arguments.length!=1||!(arguments[0] instanceof Double))
				throw new LuaException("Wrong amount of arguments, needs one integer");
			int id = (int) (double)arguments[0];
			if (id<1||te.processQueue.size()<id)
				throw new LuaException("The requested place in the queue does not exist");
			MultiblockProcessInWorld<CrusherRecipe> process = ((MultiblockProcessInWorld<CrusherRecipe>)te.processQueue.get(id-1));
			Map<String, Object> ret = Utils.saveStack(process.inputItem);
			ret.put("progress", process.processTick);
			ret.put("maxProgress", process.maxTicks);
			return new Object[]{ret};
		case 4://max energy
			return new Object[]{te.energyStorage.getMaxEnergyStored()};
		case 5://current energy
			return new Object[]{te.energyStorage.getEnergyStored()};
		}
		return null;
	}

	@Override
	public void attach(IComputerAccess computer)
	{
		TileEntityCrusher te = (TileEntityCrusher) w.getTileEntity(pos);
		if (te==null)
			return;
		te.controllingComputers++;
		te.computerOn = true;
	}

	@Override
	public void detach(IComputerAccess computer)
	{
		TileEntityCrusher te = (TileEntityCrusher) w.getTileEntity(pos);
		if (te==null)
			return;
		te.controllingComputers--;
	}
}
