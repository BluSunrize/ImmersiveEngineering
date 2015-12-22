package blusunrize.immersiveengineering.common.util.compat.computercraft;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityExcavator;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PeripheralExcavator extends IEPeripheral
{
public static final String[] cmds = {"isActive", "setEnabled", "getEnergyStored", "getMaxEnergyStored"};
	public PeripheralExcavator(World w, int _x, int _y, int _z)
	{
		super(w, _x, _y, _z);
	}

	@Override
	public String getType()
	{
		return "IE:excavator";
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
		TileEntityExcavator te = (TileEntityExcavator) getTileEntity(TileEntityExcavator.class);
		if (te==null)
			return null;
		switch (method)
		{
		case 0://isActive
			return new Object[]{te.active};
		case 1://setEnabled
			if (arguments.length!=1||!(arguments[0] instanceof Boolean))
				throw new LuaException("Wrong amount of arguments, needs one boolean");
			boolean on = (boolean)arguments[0];
			te.computerOn = on;
			return null;
		case 2: //stored energy
			return new Object[]{te.energyStorage.getEnergyStored()};
		case 3: //max energy
			return new Object[]{te.energyStorage.getMaxEnergyStored()};
		}
		return null;
	}

	@Override
	public void attach(IComputerAccess computer)
	{
		TileEntityExcavator te = (TileEntityExcavator) getTileEntity(TileEntityExcavator.class);
		if (te==null)
			return;
		te.computerControlled = true;
		te.computerOn = true;
	}

	@Override
	public void detach(IComputerAccess computer)
	{
		TileEntityExcavator te = (TileEntityExcavator) getTileEntity(TileEntityExcavator.class);
		if (te==null)
			return;
		te.computerControlled = false;
	}

	@Override
	public boolean equals(IPeripheral other)
	{
		if (!(other instanceof PeripheralExcavator))
			return false;
		PeripheralExcavator otherPer = (PeripheralExcavator) other;
		return w==otherPer.w&&x==otherPer.x&&y==otherPer.y&&z==otherPer.z;
	}
	@Override
	protected TileEntity getTileEntity(Class<? extends TileEntity> type)
	{
		TileEntityExcavator te = (TileEntityExcavator) super.getTileEntity(type);
		return te==null?null:te.master();
	}
}
