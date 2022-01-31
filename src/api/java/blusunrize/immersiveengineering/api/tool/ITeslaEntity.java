/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface ITeslaEntity
{
	void onHit(BlockEntity teslaCoil, boolean lowPower);
}
