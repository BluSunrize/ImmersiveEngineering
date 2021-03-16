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
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.PerlinNoiseGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.stream.IntStream;

class FeatureMineralVein extends Feature<NoFeatureConfig>
{
	public static HashMultimap<RegistryKey<World>, ChunkPos> veinGeneratedChunks = HashMultimap.create();

	public FeatureMineralVein()
	{
		super(NoFeatureConfig.CODEC);
	}

	@Override
	public boolean generate(@Nonnull ISeedReader worldIn, @Nonnull ChunkGenerator generator, @Nonnull Random random,
							@Nonnull BlockPos pos, @Nonnull NoFeatureConfig config)
	{
		if(ExcavatorHandler.noiseGenerator==null)
			ExcavatorHandler.noiseGenerator = new PerlinNoiseGenerator(
					new SharedSeedRandom(worldIn.getSeed()),
					IntStream.of(0)
			);

		RegistryKey<World> dimension = worldIn.getWorld().getDimensionKey();
		IChunk chunk = worldIn.getChunk(pos);
		if(!veinGeneratedChunks.containsEntry(dimension, chunk.getPos()))
		{
			veinGeneratedChunks.put(dimension, chunk.getPos());
			ExcavatorHandler.generatePotentialVein(worldIn.getWorld(), chunk.getPos(), random);
			return true;
		}
		return false;
	}
}
