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
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ShapedNBTBuilder extends ShapedRecipeBuilder
{
	private final CompoundTag nbt;

	public ShapedNBTBuilder(ItemStack stack)
	{
		super(RecipeCategory.MISC, stack.getItem(), stack.getCount());
		this.nbt = stack.getTag();
	}

	@Override
	public void save(@Nonnull Consumer<FinishedRecipe> out, @Nonnull ResourceLocation name)
	{
		super.save(base -> out.accept(new FinishedRecipe()
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
			public ResourceLocation getId()
			{
				return base.getId();
			}

			@Nonnull
			@Override
			public RecipeSerializer<?> getType()
			{
				return base.getType();
			}

			@Nullable
			@Override
			public JsonObject serializeAdvancement()
			{
				return base.serializeAdvancement();
			}

			@Nullable
			@Override
			public ResourceLocation getAdvancementId()
			{
				return base.getAdvancementId();
			}
		}), name);
	}
}
