/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.common.crafting.fluidaware.TurnAndCopyRecipe;
import blusunrize.immersiveengineering.common.items.RevolverItem;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;

public class RevolverAssemblyRecipe extends TurnAndCopyRecipe
{
	public RevolverAssemblyRecipe(ResourceLocation id, String group, int width, int height, NonNullList<Ingredient> ingr, ItemStack output)
	{
		super(id, group, width, height, ingr, output, CraftingBookCategory.EQUIPMENT);
	}

	@Nonnull
	@Override
	public ItemStack assemble(@Nonnull CraftingContainer matrix, RegistryAccess access)
	{
		if(nbtCopyTargetSlot!=null)
		{
			ItemStack out = getResultItem(access).copy();
			CompoundTag tag = new CompoundTag();
			for(int targetSlot : nbtCopyTargetSlot)
			{
				ItemStack s = matrix.getItem(targetSlot);
				if(!s.isEmpty()&&s.hasTag())
				{
					CompoundTag perks = ItemNBTHelper.getTagCompound(s, "perks");
					for(String key : perks.getAllKeys())
						if(perks.getTagType(key)==Tag.TAG_DOUBLE)
						{
							RevolverItem.RevolverPerk perk = RevolverItem.RevolverPerk.get(key);
							if(!tag.contains(key))
								tag.putDouble(key, perks.getDouble(key));
							else
								tag.putDouble(key, perk.concat(tag.getDouble(key), perks.getDouble(key)));
						}
				}
			}
			if(!tag.isEmpty())
				ItemNBTHelper.setTagCompound(out, "perks", tag);
			return out;
		}
		else
			return super.assemble(matrix, access);
	}
}