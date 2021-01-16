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
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nonnull;

public class RevolverAssemblyRecipe extends TurnAndCopyRecipe
{
	public RevolverAssemblyRecipe(ResourceLocation id, String group, int width, int height, NonNullList<Ingredient> ingr, ItemStack output)
	{
		super(id, group, width, height, ingr, output);
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(@Nonnull CraftingInventory matrix)
	{
		if(nbtCopyTargetSlot!=null)
		{
			ItemStack out = getRecipeOutput().copy();
			CompoundNBT tag = new CompoundNBT();
			for(int targetSlot : nbtCopyTargetSlot)
			{
				ItemStack s = matrix.getStackInSlot(targetSlot);
				if(!s.isEmpty()&&s.hasTag())
				{
					CompoundNBT perks = ItemNBTHelper.getTagCompound(s, "perks");
					for(String key : perks.keySet())
						if(perks.getTagId(key)==NBT.TAG_DOUBLE)
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
			return super.getCraftingResult(matrix);
	}
}