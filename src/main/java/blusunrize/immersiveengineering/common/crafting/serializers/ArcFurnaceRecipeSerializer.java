/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.crafting.ArcRecyclingRecipe;
import blusunrize.immersiveengineering.common.network.PacketUtils;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArcFurnaceRecipeSerializer extends IERecipeSerializer<ArcFurnaceRecipe>
{
	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.ARC_FURNACE.iconStack();
	}

	@Override
	public ArcFurnaceRecipe readFromJson(ResourceLocation recipeId, JsonObject json, IContext context)
	{
		JsonArray results = json.getAsJsonArray("results");
		List<Lazy<ItemStack>> outputs = new ArrayList<>();
		for(int i = 0; i < results.size(); i++)
			outputs.add(readOutput(results.get(i)));

		IngredientWithSize input = IngredientWithSize.deserialize(json.get("input"));

		JsonArray additives = json.getAsJsonArray("additives");
		IngredientWithSize[] ingredients = new IngredientWithSize[additives.size()];
		for(int i = 0; i < additives.size(); i++)
			ingredients[i] = IngredientWithSize.deserialize(additives.get(i));

		Lazy<ItemStack> slag = IESerializableRecipe.LAZY_EMPTY;
		if(json.has("slag"))
			slag = readOutput(json.get("slag"));

		int time = GsonHelper.getAsInt(json, "time");
		int energy = GsonHelper.getAsInt(json, "energy");
		JsonArray array = json.getAsJsonArray("secondaries");
		List<StackWithChance> secondaries = new ArrayList<>();
		if(array!=null)
			for(int i = 0; i < array.size(); i++)
			{
				StackWithChance secondary = readConditionalStackWithChance(array.get(i), context);
				if(secondary!=null)
					secondaries.add(secondary);
			}
		return IEServerConfig.MACHINES.arcFurnaceConfig.apply(
				new ArcFurnaceRecipe(recipeId, outputs, slag, secondaries, time, energy, input, ingredients)
		);
	}

	@Nullable
	@Override
	public ArcFurnaceRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
	{
		List<Lazy<ItemStack>> outputs = PacketUtils.readList(buffer, IERecipeSerializer::readLazyStack);
		List<StackWithChance> secondaries = PacketUtils.readList(buffer, StackWithChance::read);
		IngredientWithSize input = IngredientWithSize.read(buffer);
		IngredientWithSize[] additives = PacketUtils.readList(buffer, IngredientWithSize::read)
				.toArray(new IngredientWithSize[0]);
		Lazy<ItemStack> slag = readLazyStack(buffer);
		int time = buffer.readInt();
		int energy = buffer.readInt();
		if(!buffer.readBoolean())
			return new ArcFurnaceRecipe(recipeId, outputs, slag, secondaries, time, energy, input, additives);
		else
		{
			final int numOutputs = buffer.readVarInt();
			List<Pair<Lazy<ItemStack>, Double>> recyclingOutputs = new ArrayList<>(numOutputs);
			for(int i = 0; i < numOutputs; ++i)
				recyclingOutputs.add(Pair.of(readLazyStack(buffer), buffer.readDouble()));
			return new ArcRecyclingRecipe(
					recipeId, () -> Minecraft.getInstance().getConnection().registryAccess(), recyclingOutputs, input, time, energy
			);
		}
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, ArcFurnaceRecipe recipe)
	{
		PacketUtils.writeListReverse(buffer, recipe.output.get(), FriendlyByteBuf::writeItem);
		PacketUtils.writeList(buffer, recipe.secondaryOutputs, StackWithChance::write);
		recipe.input.write(buffer);
		PacketUtils.writeList(buffer, Arrays.asList(recipe.additives), IngredientWithSize::write);
		buffer.writeItem(recipe.slag.get());
		buffer.writeInt(recipe.getTotalProcessTime());
		buffer.writeInt(recipe.getTotalProcessEnergy());
		buffer.writeBoolean(recipe instanceof ArcRecyclingRecipe);
		if(recipe instanceof ArcRecyclingRecipe recyclingRecipe)
		{
			List<Pair<Lazy<ItemStack>, Double>> outputs = recyclingRecipe.getOutputs();
			buffer.writeVarInt(outputs.size());
			for(Pair<Lazy<ItemStack>, Double> e : outputs)
			{
				buffer.writeItem(e.getFirst().get());
				buffer.writeDouble(e.getSecond());
			}
		}
	}
}
