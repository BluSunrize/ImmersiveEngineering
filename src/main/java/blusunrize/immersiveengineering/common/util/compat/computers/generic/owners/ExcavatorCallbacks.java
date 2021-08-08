/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.common.blocks.metal.ExcavatorBlockEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.PoweredMBCallbacks;

public class ExcavatorCallbacks extends MultiblockCallbackOwner<ExcavatorBlockEntity>
{
	public ExcavatorCallbacks()
	{
		super(ExcavatorBlockEntity.class, "exavator");
		addAdditional(PoweredMBCallbacks.INSTANCE);
	}
}
