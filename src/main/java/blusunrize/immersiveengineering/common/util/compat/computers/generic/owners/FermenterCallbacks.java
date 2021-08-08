/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.common.blocks.metal.FermenterBlockEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.InventoryCallbacks;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.PoweredMBCallbacks;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.SingleItemCallback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.TankCallbacks;

public class FermenterCallbacks extends MultiblockCallbackOwner<FermenterBlockEntity>
{
	public FermenterCallbacks()
	{
		super(FermenterBlockEntity.class, "fermenter");
		addAdditional(PoweredMBCallbacks.INSTANCE);
		addAdditional(new TankCallbacks<>(te -> te.tanks[0], ""));
		addAdditional(new InventoryCallbacks<>(te -> te.inventory, 0, 8, "input"));
		addAdditional(new SingleItemCallback<>(te -> te.inventory, 9, "empty canisters"));
		addAdditional(new SingleItemCallback<>(te -> te.inventory, 10, "filled canisters"));
	}
}
