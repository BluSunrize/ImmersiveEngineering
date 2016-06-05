package blusunrize.immersiveengineering.api.tool;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTeslaCoil;

public interface ITeslaEntity
{
	public void onHit(TileEntityTeslaCoil te, boolean lowPower);
}
