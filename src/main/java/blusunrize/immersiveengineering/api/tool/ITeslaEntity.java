package blusunrize.immersiveengineering.api.tool;

import net.minecraft.tileentity.TileEntity;

public interface ITeslaEntity
{
	void onHit(TileEntity teslaCoil, boolean lowPower);
}
