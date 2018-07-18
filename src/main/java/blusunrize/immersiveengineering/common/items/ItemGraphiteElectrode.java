/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemGraphiteElectrode extends ItemIEBase
{
	public static int electrodeMaxDamage;

	public ItemGraphiteElectrode()
	{
		super("graphite_electrode", 16);
		electrodeMaxDamage = IEConfig.Machines.arcfurnace_electrodeDamage;
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag)
	{
		float integrity = 100-(float)getDurabilityForDisplay(stack)*100f;
		list.add(String.format("%s %.2f %%", I18n.format(Lib.DESC_INFO+"electrodeIntegrity"), integrity));
		if(super.getDamage(stack)!=0)
			list.add("This item is deprecated. Hold it in your inventory to update it.");
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity ent, int slot, boolean hand)
	{
		if(ent instanceof EntityPlayer)
			if(super.getDamage(stack)!=0)
			{
				ItemStack fixed = new ItemStack(this);
				ItemNBTHelper.setInt(fixed, "graphDmg", stack.getItemDamage());
				((EntityPlayer)ent).inventory.setInventorySlotContents(slot, fixed);
			}
	}

	@Override
	public boolean isEnchantable(ItemStack stack)
	{
		return false;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack)
	{
		return ItemNBTHelper.getInt(stack, "graphDmg")/(double)electrodeMaxDamage;
	}

	@Override
	public int getMaxDamage(ItemStack stack)
	{
		return electrodeMaxDamage;
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
		ItemNBTHelper.setInt(stack, "graphDmg", damage);
	}
}