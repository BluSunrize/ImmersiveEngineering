/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.crafting.IIEBufferRecipeSerializer;
import blusunrize.immersiveengineering.api.utils.IEPacketBuffer;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//TODO better name
public class CompressedShapedRecipe extends ShapedRecipe
{
	public CompressedShapedRecipe(ResourceLocation idIn, String groupIn, int recipeWidthIn, int recipeHeightIn,
								  NonNullList<Ingredient> recipeItemsIn, ItemStack recipeOutputIn)
	{
		super(idIn, groupIn, recipeWidthIn, recipeHeightIn, recipeItemsIn, recipeOutputIn);
	}

	@Nonnull
	@Override
	public IRecipeSerializer<?> getSerializer()
	{
		return RecipeSerializers.COMPRESSED_SHAPED_SERIALIZER.get();
	}

	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IIEBufferRecipeSerializer<CompressedShapedRecipe>
	{
		@Nonnull
		@Override
		public CompressedShapedRecipe read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json)
		{
			ShapedRecipe base = IRecipeSerializer.CRAFTING_SHAPED.read(recipeId, json);
			return wrap(base);
		}

		@Nullable
		@Override
		public CompressedShapedRecipe read(@Nonnull ResourceLocation recipeId, @Nonnull IEPacketBuffer buffer)
		{
			int width = buffer.readVarInt();
			int height = buffer.readVarInt();
			String group = buffer.readString(32767);
			NonNullList<Ingredient> ingredients = NonNullList.withSize(width*height, Ingredient.EMPTY);

			List<Ingredient> known = new ArrayList<>();
			for(int i = 0; i < ingredients.size(); ++i)
			{
				int knownIndex = buffer.readVarInt()-1;
				Ingredient nextIngredient;
				if(knownIndex >= 0)
				{
					nextIngredient = known.get(knownIndex);
				}
				else
				{
					List<ItemStack> matching = buffer.readList(PacketBuffer::readItemStack);
					nextIngredient = Ingredient.fromStacks(matching.toArray(new ItemStack[0]));
					known.add(nextIngredient);
				}
				ingredients.set(i, nextIngredient);
			}

			ItemStack output = buffer.readItemStack();
			return new CompressedShapedRecipe(recipeId, group, width, height, ingredients, output);
		}

		@Override
		public void write(@Nonnull IEPacketBuffer buffer, @Nonnull CompressedShapedRecipe recipe)
		{
			buffer.writeVarInt(recipe.getRecipeWidth());
			buffer.writeVarInt(recipe.getRecipeHeight());
			buffer.writeString(recipe.getGroup());

			List<ItemStack[]> knownIngredients = new ArrayList<>();
			for(Ingredient ingredient : recipe.getIngredients())
			{
				ItemStack[] matching = ingredient.getMatchingStacks();
				int matchedId = -1;
				for(int i = 0; i < knownIngredients.size(); ++i)
				{
					ItemStack[] candidate = knownIngredients.get(i);
					if(candidate.length!=matching.length)
						continue;
					boolean allMatch = true;
					for(int j = 0; j < candidate.length; ++j)
						if(!ItemStack.areItemStacksEqual(candidate[j], matching[j]))
						{
							allMatch = false;
							break;
						}
					if(allMatch)
					{
						matchedId = i;
						break;
					}
				}
				if(matchedId >= 0)
				{
					// + 1: 0 means new ingredient, -1 is large when encoded as a VarInt
					buffer.writeVarInt(matchedId+1);
				}
				else
				{
					buffer.writeVarInt(0);
					buffer.writeList(Arrays.asList(matching), (stack, buf) -> buf.writeItemStack(stack));
					knownIngredients.add(matching);
				}
			}

			buffer.writeItemStack(recipe.getRecipeOutput());
		}

		private CompressedShapedRecipe wrap(ShapedRecipe base)
		{
			return new CompressedShapedRecipe(
					base.getId(), base.getGroup(), base.getRecipeWidth(), base.getRecipeHeight(), base.getIngredients(),
					base.getRecipeOutput()
			);
		}
	}
}
