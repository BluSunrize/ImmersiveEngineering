package blusunrize.immersiveengineering.common.util.compat.computercraft;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCapacitorLV;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class PeripheralCapacitor extends IEPeripheral
{
	public static final String[] cmds = {"getEnergyStored", "getMaxEnergyStored"};
	String type;

	public PeripheralCapacitor(World w, int _x, int _y, int _z, String type)
	{
		super(w, _x, _y, _z);
		this.type = type;
	}

	@Override
	public String getType()
	{
		return "IE:"+type+"Capacitor";
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
		TileEntityCapacitorLV te = (TileEntityCapacitorLV) getTileEntity(TileEntityCapacitorLV.class);
		if (te==null)
			throw new LuaException("The capacitor was removed");
		if (method==0) //energy stored
			return new Object[]{te.getEnergyStored(ForgeDirection.DOWN)};
		else
			return new Object[]{te.getMaxEnergyStored(ForgeDirection.EAST)};
	}

	@Override
	public void attach(IComputerAccess computer)
	{}

	@Override
	public void detach(IComputerAccess computer)
	{}

}
