package blusunrize.immersiveengineering.api.tool;

import java.util.HashMap;
import java.util.Set;

import net.minecraft.item.ItemStack;

/**
 * @author BluSunrize - 29.05.2015
 *
 * Upgrades for the drill (and possibly other items) are handled by this interface
 */
public interface IUpgrade
{
	public static enum UpgradeType
	{
		DRILL,
		REVOLVER,
		SKYHOOK;
	}
	
	/**
	 * @return the upgrade types this item provides
	 * Returns a set so an item can be used for multiple items
	 */
	public Set<UpgradeType> getUpgradeTypes(ItemStack upgrade);
	
	/**
	 * @return whether the upgrade can be applied to the parsed target item
	 * This should fired after comparing UpradeTypes, so you don't have to account for that
	 */
	public boolean canApplyUpgrades(ItemStack target, ItemStack upgrade);
	
	/**
	 * Applies the modifications to a HashMap. Do <b>NOT</b> apply upgrades to the target directly<br>
	 * Valid modifications you can apply are Byte, byte[], Boolean, Integer, int[], Float, Double, String
	 */
	public void applyUpgrades(ItemStack target, ItemStack upgrade, HashMap<String, Object> modifications);
}