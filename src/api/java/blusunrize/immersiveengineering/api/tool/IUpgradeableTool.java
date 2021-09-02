/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

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
	CompoundTag getUpgrades(ItemStack stack);

	void clearUpgrades(ItemStack stack);

	void finishUpgradeRecalculation(ItemStack stack);

	/**
	 * Iterate through the stored items and apply upgrades. For an example implementation, see ItemUpgradeableTool in the IE source
	 */
	void recalculateUpgrades(ItemStack stack, Level w, Player player);

	/**
	 * Called when an upgrade is removed, BEFORE recalculateUpgrades is called
	 *
	 * @return the upgrade removed
	 */
	default ItemStack removeUpgrade(ItemStack stack, Player player, ItemStack upgrade)
	{
		return upgrade;
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