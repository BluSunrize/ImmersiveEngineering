/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.excavator;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author BluSunrize - 03.06.2015
 * <p>
 * The Handler for the Excavator. Generates veins upon world gen, stores them, and keeps a cache of queried positions
 */
public class ExcavatorHandler
{
	private static final Multimap<ResourceKey<Level>, MineralVein> MINERAL_VEIN_LIST = ArrayListMultimap.create();
	// Only access when synchronized on MINERAL_VEIN_LIST
	private static final Map<Pair<ResourceKey<Level>, ColumnPos>, MineralWorldInfo> MINERAL_INFO_CACHE = new HashMap<>();
	static final SetRestrictedField<Runnable> MARK_SAVE_DATA_DIRTY = SetRestrictedField.common();
	public static int mineralVeinYield = 0;
	public static double initialVeinDepletion = 0;
	public static double mineralNoiseThreshold = 0;
	public static PerlinSimplexNoise noiseGenerator;

	@Nullable
	public static MineralVein getRandomMineral(Level world, BlockPos pos)
	{
		if(world.isClientSide)
			return null;
		MineralWorldInfo info = getMineralWorldInfo(world, pos);
		return info.getMineralVein(ApiUtils.RANDOM);
	}

	// Always call "resetCache" after modifying the map returned here!
	public static Multimap<ResourceKey<Level>, MineralVein> getMineralVeinList()
	{
		return MINERAL_VEIN_LIST;
	}

	public static MineralWorldInfo getMineralWorldInfo(Level world, BlockPos pos)
	{
		return getMineralWorldInfo(world, new ColumnPos(pos.getX(), pos.getZ()));
	}

	public static MineralWorldInfo getMineralWorldInfo(Level world, ColumnPos columnPos)
	{
		if(world.isClientSide)
			return null;
		ResourceKey<Level> dimension = world.dimension();
		Pair<ResourceKey<Level>, ColumnPos> cacheKey = Pair.of(dimension, columnPos);
		synchronized(MINERAL_VEIN_LIST)
		{
			MineralWorldInfo worldInfo = MINERAL_INFO_CACHE.get(cacheKey);
			if(worldInfo==null)
			{
				List<Pair<MineralVein, Double>> inVeins = new ArrayList<>();
				double totalSaturation = 0;
				// Iterate all known veins
				for(MineralVein vein : MINERAL_VEIN_LIST.get(dimension))
				{
					// Use longs here to avoid overflow issues (#4468)
					// With longs we can handle distances up to roughly 2**31 * sqrt(2), much larger than the maximum
					// distance in an MC world (6*10**6*sqrt(2))
					long dX = vein.getPos().x()-columnPos.x();
					long dZ = vein.getPos().z()-columnPos.z();
					long d = dX*dX+dZ*dZ;
					double rSq = vein.getRadius()*vein.getRadius();
					if(d < rSq)
					{
						double saturation = 1-(d/rSq);
						inVeins.add(Pair.of(vein, saturation));
						totalSaturation += saturation;
					}
				}
				final double finalTotalSaturation = totalSaturation;
				worldInfo = new MineralWorldInfo(inVeins.stream()
						.map(pair -> Pair.of(pair.getFirst(), (int)(pair.getSecond()/finalTotalSaturation*1000)))
						.filter(p -> p.getFirst().getMineral(world)!=null)
						.collect(Collectors.toList())
				);
				MINERAL_INFO_CACHE.put(cacheKey, worldInfo);
			}
			return worldInfo;
		}
	}

	public static List<MineralVein> findVeinsForVillager(
			Level world, BlockPos villagerPos, long radius, List<Long> excludedPositions
	)
	{
		if(world.isClientSide)
			return Collections.emptyList();
		long radiusSq = radius*radius;
		ResourceKey<Level> dimension = world.dimension();
		List<MineralVein> foundVeins;
		synchronized(MINERAL_VEIN_LIST)
		{
			// filter maps already sold, then fiter by radius, finally order by weight
			foundVeins = MINERAL_VEIN_LIST.get(dimension).stream()
					.filter(vein -> !excludedPositions.contains(vein.getPos().toLong()))
					.filter(vein -> {
						long dX = vein.getPos().x()-villagerPos.getX();
						long dZ = vein.getPos().z()-villagerPos.getZ();
						long d = dX*dX+dZ*dZ;
						return d < radiusSq;
					}).sorted(Comparator.comparingInt(o -> o.getMineral(world).weight))
					.collect(Collectors.toList());
		}
		return foundVeins;
	}

	public static void generatePotentialVein(Level world, WorldGenLevel worldGenLevel, ChunkPos chunkpos, RandomSource rand)
	{
		int xStart = chunkpos.getMinBlockX();
		int zStart = chunkpos.getMinBlockZ();
		double d0 = 0.0625D;
		ColumnPos pos = null;
		double maxNoise = 0;

		// Find highest noise value in chunk
		for(int xx = 0; xx < 16; ++xx)
			for(int zz = 0; zz < 16; ++zz)
			{
				double noise = noiseGenerator.getValue((xStart+xx)*d0, (zStart+zz)*d0, true)*0.55D;
				// Vanilla Perlin noise scales to 0.55, so we un-scale it
				double chance = Math.abs(noise)/.55;
				if(chance > mineralNoiseThreshold&&chance > maxNoise)
				{
					pos = new ColumnPos(xStart+xx, zStart+zz);
					maxNoise = chance;
				}
			}

		if(pos!=null)
			synchronized(MINERAL_VEIN_LIST)
			{
				ColumnPos finalPos = pos;
				int radius = 12+rand.nextInt(32);
				int radiusSq = radius*radius;
				boolean crossover = MINERAL_VEIN_LIST.get(world.dimension()).stream().anyMatch(vein -> {
					// Use longs to prevent overflow
					long dX = vein.getPos().x()-finalPos.x();
					long dZ = vein.getPos().z()-finalPos.z();
					long dSq = dX*dX+dZ*dZ;
					return dSq < vein.getRadius()*vein.getRadius()||dSq < radiusSq;
				});
				if(!crossover)
				{
					RecipeHolder<MineralMix> mineralMix = null;
					Set<Holder<Biome>> biomes = new HashSet<>();
					BiomeManager biomeManager = worldGenLevel.getBiomeManager();
					int surfaceHeight = worldGenLevel.getHeight(Types.WORLD_SURFACE_WG, finalPos.x(), finalPos.z());
					for(int i = worldGenLevel.getMinBuildHeight(); i <= surfaceHeight; i++)
						biomes.add(biomeManager.getNoiseBiomeAtPosition(pos.x(), i, pos.z()));
					MineralSelection selection = new MineralSelection(world, biomes);
					if(selection.getTotalWeight() > 0)
					{
						int weight = selection.getRandomWeight(rand);
						for(RecipeHolder<MineralMix> e : selection.getMinerals())
						{
							weight -= e.value().weight;
							if(weight < 0)
							{
								mineralMix = e;
								break;
							}
						}
					}
					if(mineralMix!=null)
					{
						MineralVein vein = new MineralVein(pos, mineralMix.id(), radius);
						// generate initial depletion
						if(initialVeinDepletion > 0)
							vein.setDepletion((int)(mineralVeinYield*(rand.nextDouble()*initialVeinDepletion)));
						addVein(world.dimension(), vein);
						MARK_SAVE_DATA_DIRTY.get().run();
					}
				}
			}
	}

	public static void addVein(ResourceKey<Level> dimension, MineralVein vein)
	{
		synchronized(MINERAL_VEIN_LIST)
		{
			MINERAL_VEIN_LIST.put(dimension, vein);
			resetCache();
		}
	}

	public static void resetCache()
	{
		synchronized(MINERAL_VEIN_LIST)
		{
			MINERAL_INFO_CACHE.clear();
		}
	}

	public static void setSetDirtyCallback(Runnable setDirty)
	{
		MARK_SAVE_DATA_DIRTY.setValue(setDirty);
	}

	public static class MineralSelection
	{
		private final int totalWeight;
		private final Set<RecipeHolder<MineralMix>> validMinerals;

		public MineralSelection(Level world, Set<Holder<Biome>> biomes)
		{
			int weight = 0;
			this.validMinerals = new HashSet<>();
			for(RecipeHolder<MineralMix> e : MineralMix.RECIPES.getRecipes(world))
				if(biomes.stream().anyMatch(biome -> e.value().validBiome(biome)))
				{
					validMinerals.add(e);
					weight += e.value().weight;
				}
			this.totalWeight = weight;
		}

		public int getTotalWeight()
		{
			return this.totalWeight;
		}

		public int getRandomWeight(RandomSource random)
		{
			return random.nextInt(this.totalWeight);
		}

		public Set<RecipeHolder<MineralMix>> getMinerals()
		{
			return this.validMinerals;
		}
	}
}