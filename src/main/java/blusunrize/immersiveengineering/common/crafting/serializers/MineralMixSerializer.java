/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.common.register.IEBlocks.Multiblocks;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MineralMixSerializer extends IERecipeSerializer<MineralMix>
{
	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Multiblocks.CRUSHER);
	}

	@Override
	public MineralMix readFromJson(ResourceLocation recipeId, JsonObject json)
	{
		JsonArray array = json.getAsJsonArray("ores");
		List<StackWithChance> tempOres = new ArrayList<>();
		float totalChance = 0;
		for(int i = 0; i < array.size(); i++)
		{
			JsonObject element = array.get(i).getAsJsonObject();
			if(CraftingHelper.processConditions(element, "conditions"))
			{
				Lazy<ItemStack> stack = readOutput(element.get("output"));
				float chance = GsonHelper.getAsFloat(element, "chance");
				totalChance += chance;
				tempOres.add(new StackWithChance(stack, chance));
			}
		}
		float finalTotalChance = totalChance;
		StackWithChance[] ores = tempOres.stream().map(stack -> stack.recalculate(finalTotalChance)).toArray(StackWithChance[]::new);

		int weight = GsonHelper.getAsInt(json, "weight");
		float failChance = GsonHelper.getAsFloat(json, "fail_chance", 0);
		array = json.getAsJsonArray("dimensions");
		List<ResourceKey<Level>> dimensions = new ArrayList<>();
		for(int i = 0; i < array.size(); i++)
			dimensions.add(ResourceKey.create(
					Registry.DIMENSION_REGISTRY,
					new ResourceLocation(array.get(i).getAsString())
			));
		ResourceLocation rl = new ResourceLocation(GsonHelper.getAsString(json, "sample_background", "minecraft:stone"));
		Block b = ForgeRegistries.BLOCKS.getValue(rl);
		if(b==Blocks.AIR)
			b = Blocks.STONE;
		return new MineralMix(recipeId, ores, weight, failChance, dimensions, b);
	}

	@Nullable
	@Override
	public MineralMix fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
	{
		int count = buffer.readInt();
		StackWithChance[] outputs = new StackWithChance[count];
		for(int i = 0; i < count; i++)
			outputs[i] = StackWithChance.read(buffer);
		int weight = buffer.readInt();
		float failChance = buffer.readFloat();
		count = buffer.readInt();
		List<ResourceKey<Level>> dimensions = new ArrayList<>();
		for(int i = 0; i < count; i++)
			dimensions.add(ResourceKey.create(
					Registry.DIMENSION_REGISTRY,
					buffer.readResourceLocation()
			));
		Block bg = ForgeRegistries.BLOCKS.getValue(buffer.readResourceLocation());
		return new MineralMix(recipeId, outputs, weight, failChance, dimensions, bg);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, MineralMix recipe)
	{
		buffer.writeInt(recipe.outputs.length);
		for(StackWithChance secondaryOutput : recipe.outputs)
			secondaryOutput.write(buffer);
		buffer.writeInt(recipe.weight);
		buffer.writeFloat(recipe.failChance);
		buffer.writeInt(recipe.dimensions.size());
		for(ResourceKey<Level> dimension : recipe.dimensions)
			buffer.writeResourceLocation(dimension.location());
		buffer.writeResourceLocation(ForgeRegistries.BLOCKS.getKey(recipe.background));
	}
}
