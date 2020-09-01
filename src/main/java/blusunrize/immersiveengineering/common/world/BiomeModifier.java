/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.world;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.gen.feature.ConfiguredFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

// Mostly based on the same class from Applied Energistics 2, but using ATs instead of Mixins
public class BiomeModifier
{
	private final Biome biome;

	public BiomeModifier(Biome biome)
	{
		this.biome = biome;
	}

	public void addFeature(Decoration stage, ConfiguredFeature<?, ?> newFeature)
	{
		final int index = stage.ordinal();
		List<List<Supplier<ConfiguredFeature<?, ?>>>> allFeatures = new ArrayList<>(biome.func_242440_e().field_242484_f);
		while(allFeatures.size() <= index)
			allFeatures.add(new ArrayList<>());
		List<Supplier<ConfiguredFeature<?, ?>>> oreGen = new ArrayList<>(allFeatures.get(index));
		oreGen.add(() -> newFeature);
		allFeatures.set(index, oreGen);
		biome.func_242440_e().field_242484_f = allFeatures;
	}
}
