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
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class GraphiteElectrodeItem extends IEBaseItem
{
	public GraphiteElectrodeItem()
	{
		super("graphite_electrode", new Properties().maxStackSize(16));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
	{
		float integrity = 100-(float)getDurabilityForDisplay(stack)*100f;
		list.add(new TranslationTextComponent(Lib.DESC_INFO+"electrodeIntegrity", String.format("%.2f", integrity)));
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
	public double getDurabilityForDisplay(ItemStack stack)
	{
		return ItemNBTHelper.getInt(stack, "graphDmg")/(double)IEServerConfig.MACHINES.arcfurnace_electrodeDamage.getOr(0);
	}

	@Override
	public int getMaxDamage(ItemStack stack)
	{
		return IEServerConfig.MACHINES.arcfurnace_electrodeDamage.getOrDefault();
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