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
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static blusunrize.immersiveengineering.api.crafting.builders.WindmillBiomeBuilder.*;

public class WindmillBiomeSerializer extends IERecipeSerializer<WindmillBiome>
{
	public static final Codec<WindmillBiome> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Codec.FLOAT.fieldOf(MODIFIER_KEY).forGetter(r -> r.modifier),
			TagKey.codec(Registries.BIOME).optionalFieldOf(BIOME_TAG_KEY).forGetter(r -> r.biomes.leftOptional()),
			ForgeRegistries.BIOMES.getCodec().listOf().optionalFieldOf(SINGLE_BIOME_KEY).forGetter(r -> r.biomes.rightOptional())
	).apply(inst, (temperature, tag, fixedBiomes) -> {
		Preconditions.checkState(tag.isPresent()!=fixedBiomes.isPresent());
		if(tag.isPresent())
			return new WindmillBiome(tag.get(), temperature);
		else
			return new WindmillBiome(fixedBiomes.get(), temperature);
	}));

	@Override
	public Codec<WindmillBiome> codec()
	{
		return CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(WoodenDevices.WINDMILL);
	}

	@Nullable
	@Override
	public WindmillBiome fromNetwork(@Nonnull FriendlyByteBuf buffer)
	{
		boolean isTags = buffer.readBoolean();
		if(isTags)
		{
			ResourceLocation tagName = buffer.readResourceLocation();
			TagKey<Biome> tag = TagKey.create(Registries.BIOME, tagName);
			return new WindmillBiome(tag, buffer.readFloat());
		}
		else
		{
			List<Biome> biomes = PacketUtils.readList(buffer, buf -> buf.readRegistryIdUnsafe(ForgeRegistries.BIOMES));
			return new WindmillBiome(biomes, buffer.readFloat());
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
