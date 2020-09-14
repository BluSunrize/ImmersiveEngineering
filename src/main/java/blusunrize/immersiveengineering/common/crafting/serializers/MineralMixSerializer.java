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
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MineralMixSerializer extends IERecipeSerializer<MineralMix>
{
	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Multiblocks.crusher);
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
				ItemStack stack = readOutput(element.get("output"));
				float chance = JSONUtils.getFloat(element, "chance");
				totalChance += chance;
				tempOres.add(new StackWithChance(stack, chance));
			}
		}
		float finalTotalChance = totalChance;
		StackWithChance[] ores = tempOres.stream().map(stack -> stack.recalculate(finalTotalChance)).toArray(StackWithChance[]::new);

		int weight = JSONUtils.getInt(json, "weight");
		float failChance = JSONUtils.getFloat(json, "fail_chance", 0);
		array = json.getAsJsonArray("dimensions");
		List<RegistryKey<DimensionType>> dimensions = new ArrayList<>();
		for(int i = 0; i < array.size(); i++)
			dimensions.add(RegistryKey.func_240903_a_(
					Registry.DIMENSION_TYPE_KEY,
					new ResourceLocation(array.get(i).getAsString())
			));
		ResourceLocation rl = new ResourceLocation(JSONUtils.getString(json, "sample_background", "minecraft:stone"));
		Block b = ForgeRegistries.BLOCKS.getValue(rl);
		if(b==Blocks.AIR)
			b = Blocks.STONE;
		return new MineralMix(recipeId, ores, weight, failChance, dimensions, b);
	}

	@Nullable
	@Override
	public MineralMix read(ResourceLocation recipeId, PacketBuffer buffer)
	{
		int count = buffer.readInt();
		StackWithChance[] outputs = new StackWithChance[count];
		for(int i = 0; i < count; i++)
			outputs[i] = StackWithChance.read(buffer);
		int weight = buffer.readInt();
		float failChance = buffer.readFloat();
		count = buffer.readInt();
		List<RegistryKey<DimensionType>> dimensions = new ArrayList<>();
		for(int i = 0; i < count; i++)
			dimensions.add(RegistryKey.func_240903_a_(
					Registry.DIMENSION_TYPE_KEY,
					buffer.readResourceLocation()
			));
		Block bg = ForgeRegistries.BLOCKS.getValue(buffer.readResourceLocation());
		return new MineralMix(recipeId, outputs, weight, failChance, dimensions, bg);
	}

	@Override
	public void write(PacketBuffer buffer, MineralMix recipe)
	{
		buffer.writeInt(recipe.outputs.length);
		for(StackWithChance secondaryOutput : recipe.outputs)
			secondaryOutput.write(buffer);
		buffer.writeInt(recipe.weight);
		buffer.writeFloat(recipe.failChance);
		buffer.writeInt(recipe.dimensions.size());
		for(RegistryKey<DimensionType> dimension : recipe.dimensions)
			buffer.writeResourceLocation(dimension.func_240901_a_());
		buffer.writeResourceLocation(ForgeRegistries.BLOCKS.getKey(recipe.background));
	}
}
