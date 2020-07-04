/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.excavator;

import blusunrize.immersiveengineering.common.IESaveData;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ArrayListMultimap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.INoiseGenerator;
import org.apache.commons.lang3.tuple.Pair;

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
	private static final ArrayListMultimap<DimensionType, MineralVein> MINERAL_VEIN_LIST = ArrayListMultimap.create();
	private static final HashMap<ColumnPos, MineralWorldInfo> MINERAL_INFO_CACHE = new HashMap<>();
	public static int mineralVeinYield = 0;
	public static double initialVeinDepletion = 0;
	public static double mineralNoiseThreshold = 0;
	public static INoiseGenerator noiseGenerator;

	@Nullable
	public static MineralVein getRandomMineral(World world, BlockPos pos)
	{
		if(world.isRemote)
			return null;
		MineralWorldInfo info = getMineralWorldInfo(world, pos);
		return info.getMineralVein(Utils.RAND);
	}

	public static ArrayListMultimap<DimensionType, MineralVein> getMineralVeinList()
	{
		return MINERAL_VEIN_LIST;
	}


	public static MineralWorldInfo getMineralWorldInfo(World world, BlockPos pos)
	{
		return getMineralWorldInfo(world, new ColumnPos(pos));
	}

	public static MineralWorldInfo getMineralWorldInfo(World world, ColumnPos columnPos)
	{
		if(world.isRemote)
			return null;
		MineralWorldInfo worldInfo = MINERAL_INFO_CACHE.get(columnPos);
		if(worldInfo==null)
		{
			List<Pair<MineralVein, Double>> inVeins = new ArrayList<>();
			double totalSaturation = 0;
			MineralMix mix = null;
			// Iterate all known veins
			for(MineralVein vein : MINERAL_VEIN_LIST.get(world.getDimension().getType()))
			{
				int dX = vein.getPos().x-columnPos.x;
				int dZ = vein.getPos().z-columnPos.z;
				int d = dX*dX+dZ*dZ;
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
					.map(pair -> Pair.of(pair.getLeft(), (int)(pair.getRight()/finalTotalSaturation*1000)))
					.collect(Collectors.toList())
			);
			MINERAL_INFO_CACHE.put(columnPos, worldInfo);
		}
		return worldInfo;
	}

	public static void generatePotentialVein(DimensionType dimension, ChunkPos chunkpos, Random rand)
	{
		int xStart = chunkpos.getXStart();
		int zStart = chunkpos.getZStart();
		double d0 = 0.0625D;
		boolean containsVein = false;
		ColumnPos pos = null;
		iteratechunk:
		for(int xx = 0; xx < 16; ++xx)
			for(int zz = 0; zz < 16; ++zz)
			{
				double noise = noiseGenerator.noiseAt((xStart+xx)*d0, (zStart+zz)*d0, d0, xx*d0);
				double chance = Math.abs(noise)/.55;
				if(chance > mineralNoiseThreshold)
				{
					containsVein = true;
					pos = new ColumnPos(xStart+xx, zStart+zz);
					break iteratechunk;
				}
			}
		if(containsVein)
		{
			ColumnPos finalPos = pos;
			int radius = 12+rand.nextInt(32);
			int radiusSq = radius*radius;
			boolean crossover = MINERAL_VEIN_LIST.get(dimension).stream().anyMatch(vein -> {
				int dx = vein.getPos().x-finalPos.x;
				int dz = vein.getPos().z-finalPos.z;
				return dx*dx+dz*dz < vein.getRadius()*vein.getRadius()||dx*dx+dz*dz < radiusSq;
			});
			if(!crossover)
			{
				MineralMix mineralMix = null;
				MineralSelection selection = new MineralSelection(dimension, 2);
				if(selection.getTotalWeight() > 0)
				{
					int weight = selection.getRandomWeight(rand);
					for(MineralMix e : selection.getMinerals())
					{
						weight -= e.weight;
						if(weight < 0)
						{
							mineralMix = e;
							break;
						}
					}
				}
				if(mineralMix!=null)
				{
					MineralVein vein = new MineralVein(pos, mineralMix, radius);
					// generate initial depletion
					if(initialVeinDepletion > 0)
						vein.setDepletion((int)(mineralVeinYield*(rand.nextDouble()*initialVeinDepletion)));
					MINERAL_VEIN_LIST.put(dimension, vein);
					IESaveData.setDirty();
				}
			}
		}
	}

	public static class MineralSelection
	{
		private final int totalWeight;
		private final Set<MineralMix> validMinerals;

		public MineralSelection(DimensionType dimension, int radius)
		{
			Set<MineralMix> surrounding = new HashSet<>();
//			for(int xx = -radius; xx <= radius; xx++)
//				for(int zz = -radius; zz <= radius; zz++)
//					if(xx!=0||zz!=0)
//					{
//						DimensionChunkCoords offset = chunkCoords.withOffset(xx, zz);
//						MineralWorldInfo worldInfo = mineralCache.get(offset);
//						if(worldInfo!=null&&worldInfo.mineral!=null)
//							surrounding.add(worldInfo.mineral);
//					}

			int weight = 0;
			this.validMinerals = new HashSet<>();
			for(MineralMix e : MineralMix.mineralList.values())
				if(e.validDimension(dimension)&&!surrounding.contains(e))
				{
					validMinerals.add(e);
					weight += e.weight;
				}
			this.totalWeight = weight;
		}

		public int getTotalWeight()
		{
			return this.totalWeight;
		}

		public int getRandomWeight(Random random)
		{
			return Math.abs(random.nextInt()%this.totalWeight);
		}

		public Set<MineralMix> getMinerals()
		{
			return this.validMinerals;
		}
	}
}