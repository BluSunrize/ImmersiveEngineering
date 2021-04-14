/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.common.blocks.metal.SawmillTileEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.PoweredMBCallbacks;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.SingleItemCallback;

public class SawmillCallbacks extends MultiblockCallbackOwner<SawmillTileEntity>
{
	public SawmillCallbacks()
	{
		super(SawmillTileEntity.class, "sawmill");
		addAdditional(PoweredMBCallbacks.INSTANCE);
		addAdditional(new SingleItemCallback<>(te -> te.sawblade, "sawblade"));
	}
}
