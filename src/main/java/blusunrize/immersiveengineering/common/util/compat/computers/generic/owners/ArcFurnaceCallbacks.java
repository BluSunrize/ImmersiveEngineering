/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.common.blocks.metal.ArcFurnaceBlockEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.InventoryCallbacks;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.PoweredMBCallbacks;
import net.minecraft.world.item.ItemStack;

public class ArcFurnaceCallbacks extends MultiblockCallbackOwner<ArcFurnaceBlockEntity>
{
	public ArcFurnaceCallbacks()
	{
		super(ArcFurnaceBlockEntity.class, "arc_furnace");
		addAdditional(PoweredMBCallbacks.INSTANCE);
		addAdditional(new InventoryCallbacks<>(
				ArcFurnaceBlockEntity::getInventory,
				ArcFurnaceBlockEntity.FIRST_ELECTRODE_SLOT, ArcFurnaceBlockEntity.ELECTRODE_COUNT, "electrode"
		));
		addAdditional(new InventoryCallbacks<>(
				ArcFurnaceBlockEntity::getInventory,
				ArcFurnaceBlockEntity.FIRST_OUT_SLOT, ArcFurnaceBlockEntity.OUT_SLOT_COUNT, "output"
		));
		addAdditional(new InventoryCallbacks<>(
				ArcFurnaceBlockEntity::getInventory,
				ArcFurnaceBlockEntity.FIRST_IN_SLOT, ArcFurnaceBlockEntity.IN_SLOT_COUNT, "input"
		));
		addAdditional(new InventoryCallbacks<>(
				ArcFurnaceBlockEntity::getInventory,
				ArcFurnaceBlockEntity.FIRST_ADDITIVE_SLOT, ArcFurnaceBlockEntity.ADDITIVE_SLOT_COUNT, "additive"
		));
	}

	@ComputerCallable
	public ItemStack getSlag(CallbackEnvironment<ArcFurnaceBlockEntity> env)
	{
		return env.getObject().getInventory().get(ArcFurnaceBlockEntity.SLAG_SLOT);
	}
}
