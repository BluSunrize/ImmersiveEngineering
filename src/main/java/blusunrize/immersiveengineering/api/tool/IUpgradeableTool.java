/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

import java.util.function.Supplier;

/**
 * @author BluSunrize - 27.10.2015
 * <p>
 * Upgradeable tools like Drill and Revolver implement this.<br>
 * Since this is an interface, upgrade- and inventory-management need to be handled by the item implementing this
 */
public interface IUpgradeableTool
{
	/**
	 * @return an NBTTagCompound containing the upgrades as keys and their values<br>
	 * Examples include "speed" for the mining speed of the drill or "bullets" for extended magazines on the revolver
	 */
	CompoundNBT getUpgrades(ItemStack stack);

	void clearUpgrades(ItemStack stack);

	void finishUpgradeRecalculation(ItemStack stack);

	/**
	 * Iterate through the stored items and apply upgrades. For an example implementation, see ItemUpgradeableTool in the IE source
	 */
	void recalculateUpgrades(ItemStack stack, World w, PlayerEntity player);

	/**
	 * @return false to prevent this item from being removed from the workbench. Used by blueprints for example.
	 */
	boolean canTakeFromWorkbench(ItemStack stack);

	void removeFromWorkbench(PlayerEntity player, ItemStack stack);

	boolean canModify(ItemStack stack);

	/**
	 * @return an array of Slots to display in the workbench when this item is placed in it
	 */
	Slot[] getWorkbenchSlots(Container container, ItemStack stack, Supplier<World> getWorld, Supplier<PlayerEntity> getPlayer);

}