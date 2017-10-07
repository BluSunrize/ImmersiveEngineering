/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import net.minecraft.tileentity.TileEntity;

public interface ITeslaEntity
{
	void onHit(TileEntity teslaCoil, boolean lowPower);
}
