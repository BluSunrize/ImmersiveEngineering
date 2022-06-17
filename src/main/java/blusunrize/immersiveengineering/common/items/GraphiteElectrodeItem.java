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
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class GraphiteElectrodeItem extends IEBaseItem
{
	public GraphiteElectrodeItem()
	{
		super(new Properties().stacksTo(16));
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		float integrity = getRelativeBarWidth(stack)*100f;
		list.add(Component.translatable(Lib.DESC_INFO+"electrodeIntegrity", String.format("%.2f", integrity)));
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
	public int getBarWidth(@Nonnull ItemStack stack)
	{
		return Math.round(MAX_BAR_WIDTH*getRelativeBarWidth(stack));
	}

	private float getRelativeBarWidth(@Nonnull ItemStack stack)
	{
		return 1-ItemNBTHelper.getInt(stack, "graphDmg")/(float)IEServerConfig.getOrDefault(IEServerConfig.MACHINES.arcfurnace_electrodeDamage);
	}

	@Override
	public int getMaxDamage(ItemStack stack)
	{
		return IEServerConfig.getOrDefault(IEServerConfig.MACHINES.arcfurnace_electrodeDamage);
	}

	@Override
	public boolean isDamaged(ItemStack stack)
	{
		return ItemNBTHelper.getInt(stack, "graphDmg") > 0;
	}

	@Override
	public int getDamage(ItemStack stack)
	{
		return ItemNBTHelper.getInt(stack, "graphDmg");
	}

	@Override
	public void setDamage(ItemStack stack, int damage)
	{
		ItemNBTHelper.putInt(stack, "graphDmg", damage);
	}
}