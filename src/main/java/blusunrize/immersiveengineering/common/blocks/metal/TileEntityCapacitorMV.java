package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.Config;

public class TileEntityCapacitorMV extends TileEntityCapacitorLV
{
	@Override
	public int getMaxStorage()
	{
		return Config.getInt("capacitorMV_storage");
	}
	@Override
	public int getMaxInput()
	{
		return Config.getInt("capacitorMV_input");
	}
	@Override
	public int getMaxOutput()
	{
		return Config.getInt("capacitorMV_output");
	}

}