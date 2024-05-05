/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nonnull;
import java.util.List;

public class RevolverpartItem extends IEBaseItem
{
	public RevolverpartItem()
	{
		super(new Properties().stacksTo(1));
	}

	@Nonnull
	@Override
	public Component getName(ItemStack stack)
	{
		Component name = super.getName(stack);
		if(ItemNBTHelper.hasKey(stack, "perks"))
			return RevolverItem.RevolverPerk.getFormattedName(name, ItemNBTHelper.getTagCompound(stack, "perks"));
		return name;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag)
	{
		CompoundTag perks = ItemNBTHelper.getTagCompound(stack, "perks");
		for(String key : perks.getAllKeys())
		{
			RevolverItem.RevolverPerk perk = RevolverItem.RevolverPerk.get(key);
			if(perk!=null)
				list.add(Component.literal("  ").append(perk.getDisplayString(perks.getDouble(key))));
		}
	}
}