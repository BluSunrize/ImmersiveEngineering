/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class RevolverpartItem extends IEBaseItem
{
	public RevolverpartItem(String name)
	{
		super(name, new Properties().maxStackSize(1));
	}

	@Nonnull
	@Override
	public ITextComponent getDisplayName(ItemStack stack)
	{
		ITextComponent name = super.getDisplayName(stack);
		if(ItemNBTHelper.hasKey(stack, "perks"))
			return RevolverItem.RevolverPerk.getFormattedName(name, ItemNBTHelper.getTagCompound(stack, "perks"));
		return name;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
	{
		CompoundNBT perks = ItemNBTHelper.getTagCompound(stack, "perks");
		for(String key : perks.keySet())
		{
			RevolverItem.RevolverPerk perk = RevolverItem.RevolverPerk.get(key);
			if(perk!=null)
				list.add(new StringTextComponent("  ").appendSibling(perk.getDisplayString(perks.getDouble(key))));
		}
	}
}