/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.world;

import blusunrize.immersiveengineering.common.register.IEBiomeModifiers;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo.BiomeInfo.Builder;

public record AddGlobalFeatureBiomeModifier(Holder<PlacedFeature> feature, Decoration step) implements BiomeModifier
{
	@Override
	public void modify(Holder<Biome> biome, Phase phase, Builder builder)
	{
		if(phase==Phase.ADD)
			builder.getGenerationSettings().addFeature(this.step, this.feature);
	}

	@Override
	public Codec<? extends BiomeModifier> codec()
	{
		return IEBiomeModifiers.ADD_GLOBAL.get();
	}
}
