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
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;
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
		return IEMultiblockLogic.CRUSHER.iconStack();
	}

	@Override
	public MineralMix readFromJson(ResourceLocation recipeId, JsonObject json, IContext context)
	{
		JsonArray array = json.getAsJsonArray("ores");
		List<StackWithChance> temp = new ArrayList<>();
		float totalChance = 0;
		for(int i = 0; i < array.size(); i++)
		{
			JsonObject element = array.get(i).getAsJsonObject();
			if(CraftingHelper.processConditions(element, "conditions", context))
			{
				Lazy<ItemStack> stack = readOutput(element.get("output"));
				float chance = GsonHelper.getAsFloat(element, "chance");
				totalChance += chance;
				temp.add(new StackWithChance(stack, chance));
			}
		}
		float finalTotalChance = totalChance;
		StackWithChance[] ores = temp.stream().map(stack -> stack.recalculate(finalTotalChance)).toArray(StackWithChance[]::new);

		array = json.getAsJsonArray("spoils");
		temp.clear();
		float totalSpoilChance = 0;
		if(array!=null)
			for(int i = 0; i < array.size(); i++)
			{
				JsonObject element = array.get(i).getAsJsonObject();
				if(CraftingHelper.processConditions(element, "conditions", context))
				{
					Lazy<ItemStack> stack = readOutput(element.get("output"));
					float chance = GsonHelper.getAsFloat(element, "chance");
					totalSpoilChance += chance;
					temp.add(new StackWithChance(stack, chance));
				}
			}
		float finalTotalSpoilChance = totalSpoilChance;
		StackWithChance[] spoils = temp.stream().map(stack -> stack.recalculate(finalTotalSpoilChance)).toArray(StackWithChance[]::new);

		int weight = GsonHelper.getAsInt(json, "weight");
		float failChance = GsonHelper.getAsFloat(json, "fail_chance", 0);
		array = json.getAsJsonArray("dimensions");
		List<ResourceKey<Level>> dimensions = new ArrayList<>();
		for(int i = 0; i < array.size(); i++)
			dimensions.add(ResourceKey.create(
					Registries.DIMENSION,
					new ResourceLocation(array.get(i).getAsString())
			));
		ResourceLocation rl = new ResourceLocation(GsonHelper.getAsString(json, "sample_background", "minecraft:stone"));
		Block b = ForgeRegistries.BLOCKS.getValue(rl);
		if(b==Blocks.AIR)
			b = Blocks.STONE;
		return new MineralMix(recipeId, ores, spoils, weight, failChance, dimensions, b);
	}

	@Nullable
	@Override
	public MineralMix fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
	{
		int count = buffer.readInt();
		StackWithChance[] outputs = new StackWithChance[count];
		for(int i = 0; i < count; i++)
			outputs[i] = StackWithChance.read(buffer);
		count = buffer.readInt();
		StackWithChance[] spoils = new StackWithChance[count];
		for(int i = 0; i < count; i++)
			spoils[i] = StackWithChance.read(buffer);
		int weight = buffer.readInt();
		float failChance = buffer.readFloat();
		count = buffer.readInt();
		List<ResourceKey<Level>> dimensions = new ArrayList<>();
		for(int i = 0; i < count; i++)
			dimensions.add(ResourceKey.create(
					Registries.DIMENSION,
					buffer.readResourceLocation()
			));
		Block bg = ForgeRegistries.BLOCKS.getValue(buffer.readResourceLocation());
		return new MineralMix(recipeId, outputs, spoils, weight, failChance, dimensions, bg);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, MineralMix recipe)
	{
		buffer.writeInt(recipe.outputs.length);
		for(StackWithChance secondaryOutput : recipe.outputs)
			secondaryOutput.write(buffer);
		buffer.writeInt(recipe.spoils.length);
		for(StackWithChance spoils : recipe.spoils)
			spoils.write(buffer);
		buffer.writeInt(recipe.weight);
		buffer.writeFloat(recipe.failChance);
		buffer.writeInt(recipe.dimensions.size());
		for(ResourceKey<Level> dimension : recipe.dimensions)
			buffer.writeResourceLocation(dimension.location());
		buffer.writeResourceLocation(ForgeRegistries.BLOCKS.getKey(recipe.background));
	}
}
