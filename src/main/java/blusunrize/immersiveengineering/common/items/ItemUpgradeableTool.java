package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.tool.IUpgrade;
import blusunrize.immersiveengineering.api.tool.IUpgradeableTool;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import java.util.HashMap;

public abstract class ItemUpgradeableTool extends ItemInternalStorage implements IUpgradeableTool
{
	String upgradeType;

	public ItemUpgradeableTool(String name, int stackSize, String upgradeType, String... subNames)
	{
		super(name, stackSize, subNames);
		this.upgradeType=upgradeType;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
	{
		return !OreDictionary.itemMatches(oldStack, newStack, true);
	}

	@Override
	public NBTTagCompound getUpgrades(ItemStack stack)
	{
		return ItemNBTHelper.getTagCompound(stack, "upgrades");
	}
	@Override
	public void clearUpgrades(ItemStack stack)
	{
		ItemNBTHelper.remove(stack, "upgrades");
	}
	@Override
	public void finishUpgradeRecalculation(ItemStack stack)
	{
	}

	@Override
	public void recalculateUpgrades(ItemStack stack)
	{
		clearUpgrades(stack);
		ItemStack[] inv = getContainedItems(stack);
		HashMap<String, Object> map = new HashMap<String, Object>();
		for(int i=0; i<inv.length; i++)//start at 1, 0 is the drill
		{
			ItemStack u = inv[i];
			if(u!=null && u.getItem() instanceof IUpgrade)
			{
				IUpgrade upg = (IUpgrade)u.getItem();
				if(upg.getUpgradeTypes(u).contains(upgradeType) && upg.canApplyUpgrades(stack, u))
					upg.applyUpgrades(stack, u, map);
			}
		}
		NBTTagCompound upgradeTag = getUpgradeBase(stack).copy();
		for(String key : map.keySet())
		{
			Object o = map.get(key);
			if(o instanceof Byte)
				upgradeTag.setByte(key, (Byte)o);
			else if(o instanceof byte[])
				upgradeTag.setByteArray(key, (byte[])o);
			else if(o instanceof Boolean)
				upgradeTag.setBoolean(key, (Boolean)o);
			else if(o instanceof Integer)
				upgradeTag.setInteger(key, (Integer)o);
			else if(o instanceof int[])
				upgradeTag.setIntArray(key, (int[])o);
			else if(o instanceof Float)
				upgradeTag.setFloat(key, (Float)o);
			else if(o instanceof Double)
				upgradeTag.setDouble(key, (Double)o);
			else if(o instanceof String)
				upgradeTag.setString(key, (String)o);
		}
		ItemNBTHelper.setTagCompound(stack, "upgrades", upgradeTag);
		finishUpgradeRecalculation(stack);
	}
	public NBTTagCompound getUpgradeBase(ItemStack stack)
	{
		return new NBTTagCompound();
	}
	@Override
	public boolean canTakeFromWorkbench(ItemStack stack)
	{
		return true;
	}
	@Override
	public void removeFromWorkbench(EntityPlayer player, ItemStack stack)
	{
	}

	@Override
	public abstract boolean canModify(ItemStack stack);
	@Override
	public abstract Slot[] getWorkbenchSlots(Container container, ItemStack stack, IInventory invItem);
}