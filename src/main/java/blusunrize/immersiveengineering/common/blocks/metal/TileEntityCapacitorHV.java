package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.Config.IEConfig;

public class TileEntityCapacitorHV extends TileEntityCapacitorMV
{
	@Override
	public int getMaxStorage()
	{
		return IEConfig.Machines.capacitorHV_storage;
	}
	@Override
	public int getMaxInput()
	{
		return IEConfig.Machines.capacitorHV_input;
	}
	@Override
	public int getMaxOutput()
	{
		return IEConfig.Machines.capacitorHV_output;
	}

}