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
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class RevolverpartItem extends IEBaseItem
{
	public RevolverpartItem(String name)
	{
		super(name, new Properties().stacksTo(1));
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
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		CompoundTag perks = ItemNBTHelper.getTagCompound(stack, "perks");
		for(String key : perks.getAllKeys())
		{
			RevolverItem.RevolverPerk perk = RevolverItem.RevolverPerk.get(key);
			if(perk!=null)
				list.add(new TextComponent("  ").append(perk.getDisplayString(perks.getDouble(key))));
		}
	}
}