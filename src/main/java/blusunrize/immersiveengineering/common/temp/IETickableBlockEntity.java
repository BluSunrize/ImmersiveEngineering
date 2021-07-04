/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.temp;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;

public interface IETickableBlockEntity extends ITickableTileEntity, IEClientTickableBE, IEServerTickableBE, IECommonTickableBE
{
	@Override
	default void tick() {
		if (!canTickAny())
			return;
		tickCommon();
		if (((TileEntity) this).getWorld().isRemote)
			tickClient();
		else
			tickServer();
	}

	default boolean canTickAny() {
		return true;
	}
}
