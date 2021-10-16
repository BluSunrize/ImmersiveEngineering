/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.blocks.ticking;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

public interface IEServerTickableBE extends IETickableBase
{
	void tickServer();

	static <T extends BlockEntity>BlockEntityTicker<T> makeTicker() {
		return (level, pos, state, blockEntity) -> {
			IEServerTickableBE tickable = (IEServerTickableBE) blockEntity;
			if (tickable.canTickAny())
				tickable.tickServer();
		};
	}
}
