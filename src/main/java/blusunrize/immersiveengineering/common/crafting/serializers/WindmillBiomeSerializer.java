/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.energy.WindmillBiome;
import blusunrize.immersiveengineering.common.network.PacketUtils;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDevices;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static blusunrize.immersiveengineering.api.crafting.builders.WindmillBiomeBuilder.*;

public class WindmillBiomeSerializer extends IERecipeSerializer<WindmillBiome>
{
	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(WoodenDevices.WINDMILL);
	}

	@Override
	public WindmillBiome readFromJson(ResourceLocation recipeId, JsonObject json, IContext context)
	{
		int temperature = json.get(MODIFIER_KEY).getAsInt();
		if(json.has(SINGLE_BIOME_KEY))
		{
			ResourceLocation biomeName = new ResourceLocation(json.get(SINGLE_BIOME_KEY).getAsString());
			Biome singleBiome = Preconditions.checkNotNull(ForgeRegistries.BIOMES.getValue(biomeName));
			return new WindmillBiome(recipeId, ImmutableList.of(singleBiome), temperature);
		}
		else
		{
			ResourceLocation tagName = new ResourceLocation(json.get(BIOME_TAG_KEY).getAsString());
			TagKey<Biome> tag = TagKey.create(Registries.BIOME, tagName);
			return new WindmillBiome(recipeId, tag, temperature);
		}
	}

	@Nullable
	@Override
	public WindmillBiome fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer)
	{
		boolean isTags = buffer.readBoolean();
		if(isTags)
		{
			ResourceLocation tagName = buffer.readResourceLocation();
			TagKey<Biome> tag = TagKey.create(Registries.BIOME, tagName);
			return new WindmillBiome(recipeId, tag, buffer.readFloat());
		}
		else
		{
			List<Biome> biomes = PacketUtils.readList(buffer, buf -> buf.readRegistryIdUnsafe(ForgeRegistries.BIOMES));
			return new WindmillBiome(recipeId, biomes, buffer.readFloat());
		}
	}

	@Override
	public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull WindmillBiome recipe)
	{
		if(recipe.biomes.isLeft())
		{
			buffer.writeBoolean(true);
			buffer.writeResourceLocation(recipe.biomes.leftNonnull().location());
		}
		else
		{
			buffer.writeBoolean(false);
			PacketUtils.writeList(buffer, recipe.biomes.rightNonnull(), (b, buf) -> buf.writeRegistryIdUnsafe(ForgeRegistries.BIOMES, b));
		}
		buffer.writeFloat(recipe.getModifier());
	}
}
