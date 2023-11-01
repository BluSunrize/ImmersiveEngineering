/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.data.recipebuilder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ShapedNBTBuilder extends ShapedRecipeBuilder
{
	private final CompoundTag nbt;

	public ShapedNBTBuilder(ItemStack stack)
	{
		super(RecipeCategory.MISC, stack.getItem(), stack.getCount());
		this.nbt = stack.getTag();
	}

	@Override
	public void save(@Nonnull RecipeOutput out, @Nonnull ResourceLocation name)
	{
		super.save(new WrappingRecipeOutput(out, base -> out.accept(new FinishedRecipe()
		{
			@Override
			public void serializeRecipeData(@Nonnull JsonObject jsonOut)
			{
				base.serializeRecipeData(jsonOut);
				if(nbt!=null)
				{
					// Uhh, is this how you're supposed to turn NBT into JSON?
					JsonElement json = NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, nbt);
					jsonOut.getAsJsonObject("result").add("nbt", json);
				}
			}

			@Nonnull
			@Override
			public ResourceLocation id()
			{
				return base.id();
			}

			@Nonnull
			@Override
			public RecipeSerializer<?> type()
			{
				return base.type();
			}

			@Nullable
			@Override
			public JsonObject serializedAdvancement()
			{
				return base.serializedAdvancement();
			}

			@Nullable
			@Override
			public AdvancementHolder advancement()
			{
				return base.advancement();
			}
		})), name);
	}
}
