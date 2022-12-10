/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.world;

import blusunrize.immersiveengineering.api.excavator.ExcavatorHandler;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;

import javax.annotation.Nonnull;

public class FeatureMineralVein extends Feature<NoneFeatureConfiguration>
{
	public static HashMultimap<ResourceKey<Level>, ChunkPos> veinGeneratedChunks = HashMultimap.create();

	public FeatureMineralVein()
	{
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override
	public boolean place(@Nonnull FeaturePlaceContext<NoneFeatureConfiguration> ctx)
	{
		if(ExcavatorHandler.noiseGenerator==null)
			ExcavatorHandler.noiseGenerator = new PerlinSimplexNoise(
					//TODO? new WorldgenRandom(ctx.level().getSeed()),
                    new WorldgenRandom(new LegacyRandomSource(1309L)),
					ImmutableList.of(0)
			);

		ServerLevel realLevel = ctx.level().getLevel();
		ResourceKey<Level> dimension = realLevel.dimension();
		ChunkPos chunkPos = new ChunkPos(ctx.origin());
		if(!veinGeneratedChunks.containsEntry(dimension, chunkPos))
		{
			veinGeneratedChunks.put(dimension, chunkPos);
			ExcavatorHandler.generatePotentialVein(realLevel, chunkPos, ctx.random());
			return true;
		}
		return false;
	}
}
