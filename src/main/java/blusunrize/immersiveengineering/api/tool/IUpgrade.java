/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Set;

/**
 * @author BluSunrize - 29.05.2015
 * <p>
 * Upgrades for the drill (and possibly other items) are handled by this interface
 */
public interface IUpgrade
{
	/**
	 * @return the upgrade types this item provides
	 * Returns a set so an item can be used for multiple items
	 */
	Set<String> getUpgradeTypes(ItemStack upgrade);

	/**
	 * @return whether the upgrade can be applied to the parsed target item
	 * This should fired after comparing UpradeTypes, so you don't have to account for that
	 */
	boolean canApplyUpgrades(ItemStack target, ItemStack upgrade);

	/**
	 * Applies the modifications to a HashMap. Do <b>NOT</b> apply upgrades to the target directly<br>
	 * Valid modifications you can apply are Byte, byte[], Boolean, Integer, int[], Float, Double, String
	 */
	void applyUpgrades(ItemStack target, ItemStack upgrade, NBTTagCompound modifications);
}