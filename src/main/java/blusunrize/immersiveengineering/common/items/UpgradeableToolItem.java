/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.tool.upgrade.IUpgrade;
import blusunrize.immersiveengineering.api.tool.upgrade.IUpgradeableTool;
import blusunrize.immersiveengineering.api.tool.upgrade.UpgradeData;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.IItemHandler;

public abstract class UpgradeableToolItem extends InternalStorageItem implements IUpgradeableTool
{
	private String upgradeType;

	public UpgradeableToolItem(Item.Properties props, String upgradeType)
	{
		super(props);
		this.upgradeType = upgradeType;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
	{
		return !ItemStack.matches(oldStack, newStack);
	}

	@Override
	public final UpgradeData getUpgrades(ItemStack stack)
	{
		return getUpgradesStatic(stack);
	}

	public static UpgradeData getUpgradesStatic(ItemStack stack)
	{
		return stack.getOrDefault(IEDataComponents.UPGRADE_DATA, UpgradeData.EMPTY);
	}

	@Override
	public void clearUpgrades(ItemStack stack)
	{
		stack.remove(IEDataComponents.UPGRADE_DATA);
	}

	@Override
	public void finishUpgradeRecalculation(ItemStack stack, RegistryAccess registries)
	{
	}

	@Override
	public void recalculateUpgrades(ItemStack stack, Level w, Player player)
	{
		if(w.isClientSide)
			return;
		clearUpgrades(stack);
		IItemHandler inv = stack.getCapability(ItemHandler.ITEM);
		if(inv!=null)
		{
			var upgrades = getUpgradeBase(stack);
			for(int i = 0; i < inv.getSlots(); i++)
			{
				ItemStack u = inv.getStackInSlot(i);
				if(!u.isEmpty()&&u.getItem() instanceof IUpgrade upg)
				{
					if(upg.getUpgradeTypes(u).contains(upgradeType)&&upg.canApplyUpgrades(upgrades, u))
						upgrades = upg.applyUpgrades(upgrades, u);
				}
			}
			stack.set(IEDataComponents.UPGRADE_DATA, upgrades);
			finishUpgradeRecalculation(stack, w.registryAccess());
		}
	}

	public UpgradeData getUpgradeBase(ItemStack stack)
	{
		return UpgradeData.EMPTY;
	}

	@Override
	public boolean canTakeFromWorkbench(ItemStack stack)
	{
		return true;
	}

	@Override
	public void removeFromWorkbench(Player player, ItemStack stack)
	{
	}
}