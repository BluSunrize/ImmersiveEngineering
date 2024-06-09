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
import blusunrize.immersiveengineering.api.excavator.MineralMix.BiomeTagPredicate;
import blusunrize.immersiveengineering.api.utils.IECodecs;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.mojang.datafixers.util.Function6;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MineralMixSerializer extends IERecipeSerializer<MineralMix>
{
	public static final Codec<BiomeTagPredicate> BIOME_TAG_PREDICATE_CODEC = NeoForgeExtraCodecs.setOf(TagKey.codec(Registries.BIOME))
			.xmap(BiomeTagPredicate::new, BiomeTagPredicate::tags);
	public static final StreamCodec<ByteBuf, BiomeTagPredicate> BIOME_TAG_PREDICATE_STREAM_CODEC = IECodecs.tagCodec(Registries.BIOME)
			.apply(ByteBufCodecs.<ByteBuf, TagKey<Biome>, Set<TagKey<Biome>>>collection($ -> new HashSet<>()))
			.map(BiomeTagPredicate::new, BiomeTagPredicate::tags);
	private static final Function6<List<StackWithChance>, List<StackWithChance>, Integer, Float, Set<BiomeTagPredicate>, Block, MineralMix>
			FROM_CODEC_DATA = (ores, spoils, weight, failChance, biomes, background) -> {
		double finalTotalChance = ores.stream().mapToDouble(StackWithChance::chance).sum();
		ores = ores.stream().map(stack -> stack.recalculate(finalTotalChance)).toList();
		double finalSpoilChance = spoils.stream().mapToDouble(StackWithChance::chance).sum();
		spoils = spoils.stream().map(stack -> stack.recalculate(finalSpoilChance)).toList();
		return new MineralMix(ores, spoils, weight, failChance, biomes, background);
	};

	private static final MapCodec<MineralMix> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			CHANCE_LIST_CODEC.fieldOf("ores").forGetter(r -> r.outputs),
			CHANCE_LIST_CODEC.fieldOf("spoils").forGetter(r -> r.spoils),
			Codec.INT.fieldOf("weight").forGetter(r -> r.weight),
			Codec.FLOAT.optionalFieldOf("fail_chance", 0f).forGetter(r -> r.failChance),
			NeoForgeExtraCodecs.setOf(BIOME_TAG_PREDICATE_CODEC).fieldOf("biome_predicates").forGetter(r -> r.biomeTagPredicates),
			BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("sample_background", Blocks.STONE).forGetter(r -> r.background)
	).apply(inst, FROM_CODEC_DATA));
	private static final StreamCodec<RegistryFriendlyByteBuf, MineralMix> STREAM_CODEC = StreamCodec.composite(
			StackWithChance.STREAM_LIST, r -> r.outputs,
			StackWithChance.STREAM_LIST, r -> r.spoils,
			ByteBufCodecs.INT, r -> r.weight,
			ByteBufCodecs.FLOAT, r -> r.failChance,
			BIOME_TAG_PREDICATE_STREAM_CODEC.apply(ByteBufCodecs.collection($ -> new HashSet<>())), r -> r.biomeTagPredicates,
			ByteBufCodecs.registry(Registries.BLOCK), r -> r.background,
			FROM_CODEC_DATA
	);

	@Override
	public MapCodec<MineralMix> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, MineralMix> streamCodec()
	{
		return STREAM_CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.CRUSHER.iconStack();
	}
}
