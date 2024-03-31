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
import blusunrize.immersiveengineering.common.network.PacketUtils;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MineralMixSerializer extends IERecipeSerializer<MineralMix>
{
	public static final Codec<BiomeTagPredicate> BIOME_TAG_PREDICATE_CODEC = NeoForgeExtraCodecs.setOf(TagKey.codec(Registries.BIOME)).xmap(BiomeTagPredicate::new, BiomeTagPredicate::tags);

	private static final Codec<MineralMix> CODEC = RecordCodecBuilder.create(
			inst -> inst.group(
					CHANCE_LIST.fieldOf("ores").forGetter(r -> r.outputs),
					CHANCE_LIST.fieldOf("spoils").forGetter(r -> r.spoils),
					Codec.INT.fieldOf("weight").forGetter(r -> r.weight),
					ExtraCodecs.strictOptionalField(Codec.FLOAT, "fail_chance", 0f).forGetter(r -> r.failChance),
					NeoForgeExtraCodecs.setOf(BIOME_TAG_PREDICATE_CODEC).fieldOf("biome_predicates").forGetter(r -> r.biomeTagPredicates),
					ExtraCodecs.strictOptionalField(BuiltInRegistries.BLOCK.byNameCodec(), "sample_background", Blocks.STONE).forGetter(r -> r.background)
			).apply(inst, (ores, spoils, weight, failChance, biomes, background) -> {
				double finalTotalChance = ores.stream().mapToDouble(StackWithChance::chance).sum();
				ores = ores.stream().map(stack -> stack.recalculate(finalTotalChance)).toList();
				double finalSpoilChance = spoils.stream().mapToDouble(StackWithChance::chance).sum();
				spoils = spoils.stream().map(stack -> stack.recalculate(finalSpoilChance)).toList();
				return new MineralMix(ores, spoils, weight, failChance, biomes, background);
			})
	);

	@Override
	public Codec<MineralMix> codec()
	{
		return CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.CRUSHER.iconStack();
	}

	@Nullable
	@Override
	public MineralMix fromNetwork(FriendlyByteBuf buffer)
	{
		List<StackWithChance> outputs = PacketUtils.readList(buffer, StackWithChance::read);
		List<StackWithChance> spoils = PacketUtils.readList(buffer, StackWithChance::read);
		int weight = buffer.readInt();
		float failChance = buffer.readFloat();
		int totalPredicates = buffer.readInt();
		List<BiomeTagPredicate> biomes = new ArrayList<>(totalPredicates);
		for(int i = 0; i < totalPredicates; i++)
		{
			int count = buffer.readInt();
			Set<TagKey<Biome>> tags = new HashSet<>(count);
			for(int j = 0; j < count; j++)
				tags.add(TagKey.create(Registries.BIOME, buffer.readResourceLocation()));
			biomes.add(new BiomeTagPredicate(tags));
		}
		Block bg = PacketUtils.readRegistryElement(buffer, BuiltInRegistries.BLOCK);
		return new MineralMix(outputs, spoils, weight, failChance, biomes, bg);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, MineralMix recipe)
	{
		PacketUtils.writeList(buffer, recipe.outputs, StackWithChance::write);
		PacketUtils.writeList(buffer, recipe.spoils, StackWithChance::write);
		buffer.writeInt(recipe.weight);
		buffer.writeFloat(recipe.failChance);
		buffer.writeInt(recipe.biomeTagPredicates.size());
		for(BiomeTagPredicate biomes : recipe.biomeTagPredicates)
		{
			buffer.writeInt(biomes.tags().size());
			for(TagKey<Biome> tag : biomes.tags())
				buffer.writeResourceLocation(tag.location());
		}
		PacketUtils.writeRegistryElement(buffer, BuiltInRegistries.BLOCK, recipe.background);
	}

}
