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
import blusunrize.immersiveengineering.api.utils.codec.DualCodec;
import blusunrize.immersiveengineering.api.utils.codec.DualCodecs;
import blusunrize.immersiveengineering.api.utils.codec.DualMapCodec;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.mojang.datafixers.util.Function6;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Set;

public class MineralMixSerializer extends IERecipeSerializer<MineralMix>
{
	public static final DualCodec<ByteBuf, BiomeTagPredicate> BIOME_TAG_PREDICATE_CODECS = DualCodecs.tag(Registries.BIOME)
			.setOf()
			.map(BiomeTagPredicate::new, BiomeTagPredicate::tags);
	private static final Function6<List<StackWithChance>, List<StackWithChance>, Integer, Float, Set<BiomeTagPredicate>, Block, MineralMix>
			FROM_CODEC_DATA = (ores, spoils, weight, failChance, biomes, background) -> {
		double finalTotalChance = ores.stream().mapToDouble(StackWithChance::chance).sum();
		ores = ores.stream().map(stack -> stack.recalculate(finalTotalChance)).toList();
		double finalSpoilChance = spoils.stream().mapToDouble(StackWithChance::chance).sum();
		spoils = spoils.stream().map(stack -> stack.recalculate(finalSpoilChance)).toList();
		return new MineralMix(ores, spoils, weight, failChance, biomes, background);
	};

	private static final DualMapCodec<RegistryFriendlyByteBuf, MineralMix> CODECS = DualMapCodec.composite(
			CHANCE_LIST_CODECS.fieldOf("ores"), r -> r.outputs,
			CHANCE_LIST_CODECS.fieldOf("spoils"), r -> r.spoils,
			DualCodecs.INT.fieldOf("weight"), r -> r.weight,
			DualCodecs.FLOAT.optionalFieldOf("fail_chance", 0f), r -> r.failChance,
			BIOME_TAG_PREDICATE_CODECS.setOf().fieldOf("biome_predicates"), r -> r.biomeTagPredicates,
			DualCodecs.registry(BuiltInRegistries.BLOCK).optionalFieldOf("sample_background", Blocks.STONE), r -> r.background,
			FROM_CODEC_DATA
	);

	@Override
	protected DualMapCodec<RegistryFriendlyByteBuf, MineralMix> codecs()
	{
		return CODECS;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.CRUSHER.iconStack();
	}
}
