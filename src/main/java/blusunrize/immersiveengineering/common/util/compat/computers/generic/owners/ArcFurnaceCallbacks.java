/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.arcfurnace.ArcFurnaceLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.arcfurnace.ArcFurnaceLogic.State;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.InventoryCallbacks;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.MBEnergyCallbacks;
import net.minecraft.world.item.ItemStack;

public class ArcFurnaceCallbacks extends Callback<State>
{
	public ArcFurnaceCallbacks()
	{
		addAdditional(InventoryCallbacks.fromHandler(
				State::getInventory,
				ArcFurnaceLogic.FIRST_ELECTRODE_SLOT, ArcFurnaceLogic.ELECTRODE_COUNT, "electrode"
		));
		addAdditional(InventoryCallbacks.fromHandler(
				State::getInventory,
				ArcFurnaceLogic.FIRST_OUT_SLOT, ArcFurnaceLogic.OUT_SLOT_COUNT, "output"
		));
		addAdditional(InventoryCallbacks.fromHandler(
				State::getInventory,
				ArcFurnaceLogic.FIRST_IN_SLOT, ArcFurnaceLogic.IN_SLOT_COUNT, "input"
		));
		addAdditional(InventoryCallbacks.fromHandler(
				State::getInventory,
				ArcFurnaceLogic.FIRST_ADDITIVE_SLOT, ArcFurnaceLogic.ADDITIVE_SLOT_COUNT, "additive"
		));
		addAdditional(MBEnergyCallbacks.INSTANCE, State::getEnergy);
	}

	@ComputerCallable
	public boolean isRunning(CallbackEnvironment<State> env)
	{
		return env.object().isClientActive();
	}

	@ComputerCallable
	public ItemStack getSlag(CallbackEnvironment<State> env)
	{
		return env.object().getInventory().getStackInSlot(ArcFurnaceLogic.SLAG_SLOT);
	}
}
