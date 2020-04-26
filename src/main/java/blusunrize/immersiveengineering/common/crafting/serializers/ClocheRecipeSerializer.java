/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.ClocheRecipe;
import blusunrize.immersiveengineering.api.crafting.ClocheRecipe.ClocheRenderFunction;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDevices;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ClocheRecipeSerializer extends IERecipeSerializer<ClocheRecipe>
{
	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(MetalDevices.cloche);
	}

	@Override
	public ClocheRecipe readFromJson(ResourceLocation recipeId, JsonObject json)
	{
		JsonArray results = json.getAsJsonArray("results");

		List<ItemStack> outputs = new ArrayList<>(results.size());
		for(int i = 0; i < results.size(); i++)
			outputs.add(readOutput(results.get(i).getAsJsonObject()));

		Ingredient seed = Ingredient.deserialize(json.getAsJsonObject("input"));
		Ingredient soil = Ingredient.deserialize(json.getAsJsonObject("soil"));
		int time = JSONUtils.getInt(json, "time");

		JsonObject render = JSONUtils.getJsonObject(json, "render");
		String renderType = JSONUtils.getString(render, "type");
		Block renderBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(JSONUtils.getString(render, "block")));

		Function<Block, ClocheRenderFunction> renderFactory = "crop".equals(renderType)?ClocheRecipe.RENDER_FUNCTION_CROP:
				"stack".equals(renderType)?ClocheRecipe.RENDER_FUNCTION_STACK:
						"stem".equals(renderType)?ClocheRecipe.RENDER_FUNCTION_STEM:
								ClocheRecipe.RENDER_FUNCTION_GENERIC;
		return new ClocheRecipe(recipeId, outputs, seed, soil, time, renderFactory.apply(renderBlock));
	}

	@Nullable
	@Override
	public ClocheRecipe read(ResourceLocation recipeId, PacketBuffer buffer)
	{
		int outputCount = buffer.readInt();
		List<ItemStack> outputs = new ArrayList<>(outputCount);
		for(int i = 0; i < outputCount; i++)
			outputs.add(buffer.readItemStack());
		Ingredient seed = Ingredient.read(buffer);
		Ingredient soil = Ingredient.read(buffer);
		int time = buffer.readInt();
		return new ClocheRecipe(recipeId, outputs, seed, soil, time, ClocheRecipe.RENDER_FUNCTION_GENERIC.apply(Blocks.STONE));
	}

	@Override
	public void write(PacketBuffer buffer, ClocheRecipe recipe)
	{
		buffer.writeInt(recipe.outputs.size());
		for(ItemStack stack : recipe.outputs)
			buffer.writeItemStack(stack);
		recipe.seed.write(buffer);
		recipe.soil.write(buffer);
		buffer.writeInt(recipe.time);
	}
}
