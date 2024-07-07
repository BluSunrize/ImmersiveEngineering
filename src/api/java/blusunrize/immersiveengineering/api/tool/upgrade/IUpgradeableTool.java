/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool.upgrade;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

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
	UpgradeData getUpgrades(ItemStack stack);

	void clearUpgrades(ItemStack stack);

	void finishUpgradeRecalculation(ItemStack stack, RegistryAccess registries);

	/**
	 * Iterate through the stored items and apply upgrades. For an example implementation, see ItemUpgradeableTool in the IE source
	 */
	void recalculateUpgrades(ItemStack stack, Level w, Player player);

	/**
	 * Adds information that should be stored in the upgrade if it is removed to the given upgrade stack.
	 * Note: This will be called even if the upgrade is not going to be removed!
	 */
	default ItemStack getUpgradeAfterRemoval(ItemStack stack, ItemStack upgrade)
	{
		return upgrade;
	}

	/**
	 * Called when an upgrade is removed, BEFORE recalculateUpgrades is called
	 */
	default void removeUpgrade(ItemStack stack, Player player, ItemStack upgrade)
	{
	}

	/**
	 * @return false to prevent this item from being removed from the workbench. Used by blueprints for example.
	 */
	boolean canTakeFromWorkbench(ItemStack stack);

	void removeFromWorkbench(Player player, ItemStack stack);

	boolean canModify(ItemStack stack);

	/**
	 * @return an array of Slots to display in the workbench when this item is placed in it
	 */
	Slot[] getWorkbenchSlots(AbstractContainerMenu container, ItemStack stack, Level level, Supplier<Player> getPlayer, IItemHandler toolInventory);

}