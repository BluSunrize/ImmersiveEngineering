/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.recipes.builder;

import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.api.excavator.MineralMix.BiomeTagPredicate;
import com.google.common.collect.ImmutableSet;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.ArrayList;
import java.util.List;

public class MineralMixBuilder extends IERecipeBuilder<MineralMixBuilder>
{
	private final List<StackWithChance> outputs = new ArrayList<>();
	private final List<StackWithChance> spoils = new ArrayList<>();
	private int weight;
	private float failChance;
	private List<BiomeTagPredicate> biomeTagPredicates = new ArrayList<>();
	private Block background = Blocks.STONE;

	private MineralMixBuilder()
	{
	}

	public static MineralMixBuilder builder()
	{
		return new MineralMixBuilder();
	}

	@SafeVarargs
	public final MineralMixBuilder biomeCondition(TagKey<Biome>... tags)
	{
		// normal Set.of results in varying order of elements during datagen
		this.biomeTagPredicates.add(new BiomeTagPredicate(ImmutableSet.copyOf(tags)));
		return this;
	}

	public MineralMixBuilder dimensionOverworld()
	{
		this.biomeTagPredicates.add(new BiomeTagPredicate(BiomeTags.IS_OVERWORLD));
		return this;
	}

	public MineralMixBuilder dimensionNether()
	{
		this.biomeTagPredicates.add(new BiomeTagPredicate(BiomeTags.IS_NETHER));
		return this;
	}

	public MineralMixBuilder background(Block background)
	{
		this.background = background;
		return this;
	}

	public MineralMixBuilder spoil(ItemLike output, float weight)
	{
		this.spoils.add(new StackWithChance(new ItemStack(output), weight));
		return this;
	}

	public MineralMixBuilder ore(ItemLike output, float weight)
	{
		this.outputs.add(new StackWithChance(new ItemStack(output), weight));
		return this;
	}

	public MineralMixBuilder ore(TagKey<Item> output, float weight, ICondition... conditions)
	{
		this.outputs.add(new StackWithChance(new TagOutput(output), weight, conditions));
		return this;
	}

	public MineralMixBuilder weight(int weight)
	{
		this.weight = weight;
		return this;
	}

	public MineralMixBuilder failchance(float failchance)
	{
		this.failChance = failchance;
		return this;
	}

	public MineralMixBuilder addOverworldSpoils()
	{
		return spoil(Items.GRAVEL, 0.2f)
				.spoil(Items.COBBLESTONE, 0.5f)
				.spoil(Items.COBBLED_DEEPSLATE, 0.3f);
	}

	public MineralMixBuilder addSoilSpoils()
	{
		return spoil(Items.GRAVEL, 0.6f)
				.spoil(Items.COBBLESTONE, 0.3f)
				.spoil(Items.COARSE_DIRT, 0.1f);
	}

	public MineralMixBuilder addSeabedSpoils()
	{
		return spoil(Items.SANDSTONE, 0.6f)
				.spoil(Items.GRAVEL, 0.3f)
				.spoil(Items.SAND, 0.1f);
	}

	public MineralMixBuilder addNetherSpoils()
	{
		return spoil(Items.NETHERRACK, 0.5f)
				.spoil(Blocks.BASALT, 0.3f)
				.spoil(Blocks.GRAVEL, 0.2f);
	}

	public void build(RecipeOutput out, ResourceLocation name)
	{
		MineralMix recipe = new MineralMix(
				outputs, spoils, weight, failChance, biomeTagPredicates, background
		);
		out.accept(name, recipe, null, getConditions());
	}
}
