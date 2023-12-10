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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class WindmillBiomeSerializer extends IERecipeSerializer<WindmillBiome>
{
	public static final Codec<WindmillBiome> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Codec.FLOAT.fieldOf("modifier").forGetter(r -> r.modifier),
			ExtraCodecs.strictOptionalField(TagKey.codec(Registries.BIOME), "biomeTag").forGetter(r -> r.biomes.leftOptional()),
			ExtraCodecs.strictOptionalField(ResourceKey.codec(Registries.BIOME).listOf(), "singleBiome").forGetter(r -> r.biomes.rightOptional())
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
			List<ResourceKey<Biome>> biomes = PacketUtils.readList(
					buffer, buf -> buf.readResourceKey(Registries.BIOME)
			);
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
			PacketUtils.writeList(buffer, recipe.biomes.rightNonnull(), (b, buf) -> buf.writeResourceKey(b));
		}
		buffer.writeFloat(recipe.getModifier());
	}
}
