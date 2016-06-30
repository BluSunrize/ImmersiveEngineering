package blusunrize.immersiveengineering.common.util.compat.computercraft;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCapacitorLV;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class PeripheralCapacitor extends IEPeripheral
{
	public static final String[] cmds = {"getEnergyStored", "getMaxEnergyStored"};
	String type;

	public PeripheralCapacitor(World w, BlockPos pos, String type)
	{
		super(w, pos);
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
			return new Object[]{te.getEnergyStored(EnumFacing.DOWN)};
		else
			return new Object[]{te.getMaxEnergyStored(EnumFacing.EAST)};
	}

	@Override
	public void attach(IComputerAccess computer)
	{}

	@Override
	public void detach(IComputerAccess computer)
	{}

}
