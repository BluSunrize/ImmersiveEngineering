/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class GraphiteElectrodeItem extends IEBaseItem
{
	public GraphiteElectrodeItem()
	{
		super(new Properties().stacksTo(16).component(DataComponents.MAX_DAMAGE, 10));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag)
	{
		float integrity = 1-getDamage(stack)/(float)getMaxDamage(stack);
		list.add(Component.translatable(Lib.DESC_INFO+"electrodeIntegrity", String.format("%.2f", 100*integrity)).withStyle(ChatFormatting.GRAY));
	}

	@Override
	public boolean isEnchantable(ItemStack stack)
	{
		return false;
	}

	@Override
	public boolean isDamageable(ItemStack stack)
	{
		return true;
	}

	@Override
	public int getMaxDamage(ItemStack stack)
	{
		return IEServerConfig.getOrDefault(IEServerConfig.MACHINES.arcfurnace_electrodeDamage);
	}
}