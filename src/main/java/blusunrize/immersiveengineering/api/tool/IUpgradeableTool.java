package blusunrize.immersiveengineering.api.tool;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author BluSunrize - 27.10.2015
 *
 * Upgradeable tools like Drill and Revolver implement this.<br>
 * Since this is an interface, upgrade- and inventory-management need to be handled by the item implementing this 
 */
public interface IUpgradeableTool extends IInternalStorageItem
{
	/**
	 * @return an NBTTagCompound containing the upgrades as keys and their values<br>
	 * Examples include "speed" for the mining speed of the drill or "bullets" for extended magazines on the revolver
	 */
	public NBTTagCompound getUpgrades(ItemStack stack);
	
	public void clearUpgrades(ItemStack stack);
	
	/**
	 * Iterate through the stored items and apply upgrades. For an example implementation, see ItemUpgradeableTool in the IE source
	 */
	public void recalculateUpgrades(ItemStack stack);

	/**
	 * @return false to prevent this item from being removed from the workbench. Used by blueprints for example.
	 */
	public boolean canTakeFromWorkbench(ItemStack stack);
	
	public void removeFromWorkbench(EntityPlayer player, ItemStack stack);
	
	public abstract boolean canModify(ItemStack stack);
	
	/**
	 * @return an array of Slots to display in the workbench when this item is placed in it
	 */
	public abstract Slot[] getWorkbenchSlots(Container container, ItemStack stack, IInventory invItem);

}