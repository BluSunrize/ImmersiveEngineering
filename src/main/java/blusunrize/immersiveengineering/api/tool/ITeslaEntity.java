package blusunrize.immersiveengineering.api.tool;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTeslaCoil;

public interface ITeslaEntity
{
	void onHit(TileEntityTeslaCoil te, boolean lowPower);
}
