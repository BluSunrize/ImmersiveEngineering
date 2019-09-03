/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.common.items.ItemRevolver;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper.ShapedPrimer;

public class RecipeRevolverAssembly extends RecipeShapedIngredient
{
	public RecipeRevolverAssembly(ResourceLocation group, ItemStack result, Object... recipe)
	{
		super(group, result, wrapIngredients(recipe));
	}

	public RecipeRevolverAssembly(ResourceLocation group, ItemStack result, ShapedPrimer primer)
	{
		super(group, result, primer);
	}

	private static Object[] wrapIngredients(Object... recipe)
	{
		Object[] out = new Object[recipe.length];
		for(int i = 0; i < recipe.length; i++)
			if(recipe[i] instanceof IngredientStack)
				out[i] = new IngredientIngrStack((IngredientStack)recipe[i]);
			else
				out[i] = recipe[i];
		return out;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting matrix)
	{
		if(nbtCopyTargetSlot!=null)
		{
			ItemStack out = output.copy();
			NBTTagCompound tag = new NBTTagCompound();
			for(int targetSlot : nbtCopyTargetSlot)
			{
				ItemStack s = matrix.getStackInSlot(targetSlot);
				if(!s.isEmpty()&&s.hasTagCompound())
				{
					NBTTagCompound perks = ItemNBTHelper.getTagCompound(s, "perks");
					for(String key : perks.getKeySet())
						if(perks.getTagId(key)==6) //Double
						{
							ItemRevolver.RevolverPerk perk = ItemRevolver.RevolverPerk.get(key);
							if(!tag.hasKey(key))
								tag.setDouble(key, perks.getDouble(key));
							else
								tag.setDouble(key, perk.concat(tag.getDouble(key), perks.getDouble(key)));
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