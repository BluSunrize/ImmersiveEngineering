/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.IUpgrade;
import blusunrize.immersiveengineering.api.tool.IUpgradeableTool;
import blusunrize.immersiveengineering.api.tool.ToolUpgrades;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class ItemToolUpgrade extends ItemIEBase implements IUpgrade
{
	private ToolUpgrades type;
	public ItemToolUpgrade(ToolUpgrades type)
	{
		super("toolupgrade_"+type.name().toLowerCase(), 1);
		this.type = type;
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag)
	{
		String[] flavour = ImmersiveEngineering.proxy.splitStringOnWidth(I18n.format(Lib.DESC_FLAVOUR +
				"toolupgrade." + type.name().toLowerCase()), 200);
		list.addAll(Arrays.asList(flavour));
	}

	@Override
	public int getItemStackLimit(ItemStack stack)
	{
		return ToolUpgrades.get(stack.getMetadata()).stackSize;
	}

	@Override
	public Set<String> getUpgradeTypes(ItemStack upgrade)
	{
		return ToolUpgrades.get(upgrade.getMetadata()).toolset;
	}

	@Override
	public boolean canApplyUpgrades(ItemStack target, ItemStack upgrade)
	{
		BiPredicate<ItemStack, ItemStack> check = ToolUpgrades.get(upgrade.getMetadata()).applyCheck;
		if (check != null && target.getItem() instanceof IUpgradeableTool)
			return check.test(target, upgrade);
		return true;
	}

	@Override
	public void applyUpgrades(ItemStack target, ItemStack upgrade, NBTTagCompound modifications)
	{
		ToolUpgrades.get(upgrade.getMetadata()).function.accept(upgrade, modifications);
	}

}
