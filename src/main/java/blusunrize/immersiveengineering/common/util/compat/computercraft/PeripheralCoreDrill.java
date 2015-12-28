package blusunrize.immersiveengineering.common.util.compat.computercraft;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySampleDrill;
import blusunrize.immersiveengineering.common.util.IELogger;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.world.World;

public class PeripheralCoreDrill extends IEPeripheral
{
	public static final String[] cmds = {"getSampleProgress", "isSamplingFinished", "getVeinUnlocalizedName", "getVeinLocalizedName", "getVeinIntegrity", "getEnergyStored", "getMaxEnergyStored"};
	public PeripheralCoreDrill(World w, int _x, int _y, int _z)
	{
		super(w, _x, _y, _z);
	}

	@Override
	public String getType()
	{
		return "IE:sampleDrill";
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
		TileEntitySampleDrill te = (TileEntitySampleDrill) getTileEntity(TileEntitySampleDrill.class);
		IELogger.info(te);
		if (te==null)
			throw new LuaException("The sample drill was removed");
		switch (method)
		{
		case 0://sample progress
			return new Object[]{te.getSampleProgress()};
		case 1://is finished
			return new Object[]{te.isSamplingFinished()};
		case 2://name unloc
			if (te.isSamplingFinished())
				return new Object[]{te.getVeinUnlocalizedName()};
			throw new LuaException("The sample drill has not finished getting its sample yet");
		case 3://name local
			if (te.isSamplingFinished())
				return new Object[]{te.getVeinLocalizedName()};
			throw new LuaException("The sample drill has not finished getting its sample yet");
		case 4://integrity
			if (te.isSamplingFinished())
				return new Object[]{te.getVeinIntegrity()};
			throw new LuaException("The sample drill has not finished getting its sample yet");
		case 5://energy stored
			return new Object[]{te.energyStorage.getEnergyStored()};
		case 6://energy max stored
			return new Object[]{te.energyStorage.getMaxEnergyStored()};
		}
		return null;
	}

	@Override
	public void attach(IComputerAccess computer)
	{}

	@Override
	public void detach(IComputerAccess computer)
	{}

}
