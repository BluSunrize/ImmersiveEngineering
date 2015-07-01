package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.Config;

public class TileEntityCapacitorHV extends TileEntityCapacitorMV
{
	@Override
	public int getMaxStorage()
	{
		return Config.getInt("capacitorHV_storage");
	}
	@Override
	public int getMaxInput()
	{
		return Config.getInt("capacitorHV_input");
	}
	@Override
	public int getMaxOutput()
	{
		return Config.getInt("capacitorHV_output");
	}

}