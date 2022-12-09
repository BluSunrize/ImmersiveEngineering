/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.energy.ThermoelectricSource;
import blusunrize.immersiveengineering.common.network.PacketUtils;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static blusunrize.immersiveengineering.api.crafting.builders.ThermoelectricSourceBuilder.*;

public class ThermoelectricSourceSerializer extends IERecipeSerializer<ThermoelectricSource>
{
	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(MetalDevices.THERMOELECTRIC_GEN);
	}

	@Override
	public ThermoelectricSource readFromJson(ResourceLocation recipeId, JsonObject json, IContext context)
	{
		int temperature = json.get(TEMPERATURE_KEY).getAsInt();
		if(json.has(SINGLE_BLOCK_KEY))
		{
			ResourceLocation blockName = new ResourceLocation(json.get(SINGLE_BLOCK_KEY).getAsString());
			Block singleBlock = Preconditions.checkNotNull(ForgeRegistries.BLOCKS.getValue(blockName));
			return new ThermoelectricSource(recipeId, ImmutableList.of(singleBlock), temperature);
		}
		else
		{
			ResourceLocation tagName = new ResourceLocation(json.get(BLOCK_TAG_KEY).getAsString());
			TagKey<Block> tag = TagKey.create(Registries.BLOCK, tagName);
			return new ThermoelectricSource(recipeId, tag, temperature);
		}
	}

	@Nullable
	@Override
	public ThermoelectricSource fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer)
	{
		List<Block> blocks = PacketUtils.readList(buffer, buf -> buf.readRegistryIdUnsafe(ForgeRegistries.BLOCKS));
		int temperature = buffer.readInt();
		return new ThermoelectricSource(recipeId, blocks, temperature);
	}

	@Override
	public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull ThermoelectricSource recipe)
	{
		PacketUtils.writeList(
				buffer, recipe.getMatchingBlocks(), (b, buf) -> buf.writeRegistryIdUnsafe(ForgeRegistries.BLOCKS, b)
		);
		buffer.writeInt(recipe.getTemperature());
	}
}
