/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items.upgrades;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.upgrade.IUpgrade;
import blusunrize.immersiveengineering.api.tool.upgrade.UpgradeData;
import blusunrize.immersiveengineering.common.items.IEBaseItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

public class ToolUpgradeItem extends IEBaseItem implements IUpgrade
{
	private final ToolUpgrade type;

	public ToolUpgradeItem(ToolUpgrade type)
	{
		super(new Properties().stacksTo(1));
		this.type = type;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag)
	{
		list.add(Component.translatable(Lib.DESC_FLAVOUR+BuiltInRegistries.ITEM.getKey(this).getPath()).withStyle(ChatFormatting.GRAY));
	}

	@Override
	public int getMaxStackSize(ItemStack stack)
	{
		return type.stackSize;
	}

	@Override
	public Set<String> getUpgradeTypes(ItemStack upgrade)
	{
		return type.toolset;
	}

	@Override
	public boolean canApplyUpgrades(UpgradeData target, ItemStack upgrade)
	{
		BiPredicate<ItemStack, UpgradeData> check = type.applyCheck;
		if(check!=null)
			return check.test(upgrade, target);
		return true;
	}

	@Override
	public UpgradeData applyUpgrades(UpgradeData base, ItemStack upgrade)
	{
		return type.function.apply(upgrade, base);
	}
}
