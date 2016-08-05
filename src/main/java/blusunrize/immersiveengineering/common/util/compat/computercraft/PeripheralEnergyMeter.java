package blusunrize.immersiveengineering.common.util.compat.computercraft;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityEnergyMeter;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class PeripheralEnergyMeter extends IEPeripheral {
	public PeripheralEnergyMeter(World w, BlockPos pos)
	{
		super(w, pos);
	}

	final static String[] cmds = {"getAvgEnergy"};
	@Override
	public String getType()
	{
		return "IE:currentTransformer";
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
		if (method==0)
		{
			TileEntityEnergyMeter te = getTileEntity(TileEntityEnergyMeter.class);
			if (te!=null)
				return new Object[]{te.getAveragePower()};
			else
				throw new LuaException("The current transformer was removed");
		}
		return null;
	}

	@Override
	public void attach(IComputerAccess computer) {}

	@Override
	public void detach(IComputerAccess computer) {}

	@Override
	public boolean equals(IPeripheral other)
	{
		if (!(other instanceof PeripheralEnergyMeter))
			return false;
		PeripheralEnergyMeter otherPer = (PeripheralEnergyMeter) other;
		return w==otherPer.w&&pos.equals(otherPer.pos);
	}

}
