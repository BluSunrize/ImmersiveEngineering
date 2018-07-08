/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

//TODO the bug this worked around has been fixed in Forge, remove this in 1.13
public class IngredientFactoryStackableNBT implements IIngredientFactory
{
	@Nonnull
	@Override
	public Ingredient parse(JsonContext context, JsonObject json)
	{
		return new IngredientStackableNBT(CraftingHelper.getItemStack(json, context));
	}

	public static class IngredientStackableNBT extends Ingredient
	{
		@Nonnull
		private final ItemStack stack;

		public IngredientStackableNBT(@Nonnull ItemStack match)
		{
			super(match);
			stack = match;
		}

		@Override
		public boolean apply(@Nullable ItemStack input)
		{
			if(input==null||!super.apply(input))
				return false;
			Optional<NBTTagCompound> tag1 = Optional.ofNullable(stack.getTagCompound());
			Optional<NBTTagCompound> tag2 = Optional.ofNullable(input.getTagCompound());
			return tag1.equals(tag2);
		}

		@Override
		public boolean isSimple()
		{
			return false;
		}
	}
}
