/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.common.blocks.metal.RefineryTileEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.PoweredMBCallbacks;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.SingleItemCallback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.TankCallbacks;

public class RefineryCallbacks extends MultiblockCallbackOwner<RefineryTileEntity>
{
	public RefineryCallbacks()
	{
		super(RefineryTileEntity.class, "refinery");
		addAdditional(PoweredMBCallbacks.INSTANCE);
		addAdditional(new TankCallbacks<>(te -> te.tanks[0], "left input"));
		addAdditional(new TankCallbacks<>(te -> te.tanks[1], "right input"));
		addAdditional(new TankCallbacks<>(te -> te.tanks[2], "output"));
		addAdditional(new SingleItemCallback<>(te -> te.inventory, 0, "left filled canisters"));
		addAdditional(new SingleItemCallback<>(te -> te.inventory, 1, "left empty canisters"));
		addAdditional(new SingleItemCallback<>(te -> te.inventory, 2, "right filled canisters"));
		addAdditional(new SingleItemCallback<>(te -> te.inventory, 3, "right empty canisters"));
		addAdditional(new SingleItemCallback<>(te -> te.inventory, 4, "empty output canisters"));
		addAdditional(new SingleItemCallback<>(te -> te.inventory, 3, "filled output canisters"));
	}
}
