package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.Config.IEConfig;

public class TileEntityCapacitorMV extends TileEntityCapacitorLV
{
	@Override
	public int getMaxStorage()
	{
		return IEConfig.Machines.capacitorMV_storage;
	}
	@Override
	public int getMaxInput()
	{
		return IEConfig.Machines.capacitorMV_input;
	}
	@Override
	public int getMaxOutput()
	{
		return IEConfig.Machines.capacitorMV_output;
	}

}