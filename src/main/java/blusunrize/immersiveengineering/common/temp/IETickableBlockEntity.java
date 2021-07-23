/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.temp;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;

public interface IETickableBlockEntity extends TickableBlockEntity, IEClientTickableBE, IEServerTickableBE, IECommonTickableBE
{
	@Override
	default void tick() {
		if (!canTickAny())
			return;
		tickCommon();
		if (((BlockEntity) this).getLevel().isClientSide)
			tickClient();
		else
			tickServer();
	}

	default boolean canTickAny() {
		return true;
	}
}
