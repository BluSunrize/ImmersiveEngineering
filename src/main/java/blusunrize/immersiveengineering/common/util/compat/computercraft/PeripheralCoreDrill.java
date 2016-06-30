package blusunrize.immersiveengineering.common.util.compat.computercraft;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySampleDrill;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class PeripheralCoreDrill extends IEPeripheral
{
	public static final String[] cmds = {"getSampleProgress", "isSamplingFinished", "getVeinUnlocalizedName", "getVeinExpectedYield", "getEnergyStored", "getMaxEnergyStored", "reset"};
	public PeripheralCoreDrill(World w, BlockPos pos)
	{
		super(w, pos);
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
				return new Object[]{te.getVein()};
			throw new LuaException("The sample drill has not finished getting its sample yet");
		case 3://integrity
			if (te.isSamplingFinished())
				return new Object[]{te.getExpectedVeinYield()};
			throw new LuaException("The sample drill has not finished getting its sample yet");
		case 4://energy stored
			return new Object[]{te.energyStorage.getEnergyStored()};
		case 5://energy max stored
			return new Object[]{te.energyStorage.getMaxEnergyStored()};
		case 6://reset
			te.process = 0;
			te.active = true;
			te.sample = null;
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
