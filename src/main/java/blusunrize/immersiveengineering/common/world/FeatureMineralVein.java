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
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.stream.IntStream;

class FeatureMineralVein extends Feature<NoneFeatureConfiguration>
{
	public static HashMultimap<ResourceKey<Level>, ChunkPos> veinGeneratedChunks = HashMultimap.create();

	public FeatureMineralVein()
	{
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override
	public boolean place(@Nonnull WorldGenLevel worldIn, @Nonnull ChunkGenerator generator, @Nonnull Random random,
							@Nonnull BlockPos pos, @Nonnull NoneFeatureConfiguration config)
	{
		if(ExcavatorHandler.noiseGenerator==null)
			ExcavatorHandler.noiseGenerator = new PerlinSimplexNoise(
					new WorldgenRandom(worldIn.getSeed()),
					IntStream.of(0)
			);

		ResourceKey<Level> dimension = worldIn.getLevel().dimension();
		ChunkAccess chunk = worldIn.getChunk(pos);
		if(!veinGeneratedChunks.containsEntry(dimension, chunk.getPos()))
		{
			veinGeneratedChunks.put(dimension, chunk.getPos());
			ExcavatorHandler.generatePotentialVein(worldIn.getLevel(), chunk.getPos(), random);
			return true;
		}
		return false;
	}
}
