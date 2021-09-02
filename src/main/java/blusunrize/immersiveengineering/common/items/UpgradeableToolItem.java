/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.tool.IUpgrade;
import blusunrize.immersiveengineering.api.tool.IUpgradeableTool;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

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
	public CompoundTag getUpgrades(ItemStack stack)
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
	public void recalculateUpgrades(ItemStack stack, Level w, Player player)
	{
		if(w.isClientSide)
			return;
		clearUpgrades(stack);
		LazyOptional<IItemHandler> lazyInv = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		lazyInv.ifPresent(inv->
		{
			CompoundTag upgradeTag = getUpgradeBase(stack).copy();
			for(int i = 0; i < inv.getSlots(); i++)
			{
				ItemStack u = inv.getStackInSlot(i);
				if(!u.isEmpty()&&u.getItem() instanceof IUpgrade)
				{
					IUpgrade upg = (IUpgrade)u.getItem();
					if(upg.getUpgradeTypes(u).contains(upgradeType)&&upg.canApplyUpgrades(stack, u))
						upg.applyUpgrades(stack, u, upgradeTag);
				}
			}
			ItemNBTHelper.setTagCompound(stack, "upgrades", upgradeTag);
			finishUpgradeRecalculation(stack);
		});
	}

	public CompoundTag getUpgradeBase(ItemStack stack)
	{
		return new CompoundTag();
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