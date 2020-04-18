/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.world;

import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.collect.ArrayListMultimap;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig.FillerBlockType;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.*;
import java.util.Map.Entry;

@EventBusSubscriber
public class IEWorldGen
{
	public static Map<String, ConfiguredFeature<?>> features = new HashMap<>();
	public static Map<String, ConfiguredFeature<?>> retroFeatures = new HashMap<>();
	public static List<ResourceLocation> oreDimBlacklist = new ArrayList<>();
	public static Map<String, Boolean> retrogenMap = new HashMap<>();

	public static void addOreGen(String name, BlockState state, int maxVeinSize, int minY, int maxY, int chunkOccurence)
	{
		OreFeatureConfig cfg = new OreFeatureConfig(FillerBlockType.NATURAL_STONE, state, maxVeinSize);
		ConfiguredFeature<?> feature = Biome.createDecoratedFeature(Feature.ORE, cfg, Placement.COUNT_RANGE,
				new CountRangeConfig(chunkOccurence, minY, minY, maxY));
		for(Biome biome : Biome.BIOMES)
			biome.addFeature(Decoration.UNDERGROUND_ORES, feature);
		features.put(name, feature);

		ConfiguredFeature<?> retroFeature = Biome.createDecoratedFeature(IEContent.ORE_RETROGEN, cfg, Placement.COUNT_RANGE,
				new CountRangeConfig(chunkOccurence, minY, minY, maxY));
		retroFeatures.put(name, retroFeature);
	}

	public void generateOres(Random random, int chunkX, int chunkZ, World world, boolean newGeneration)
	{
		if(!oreDimBlacklist.contains(world.getDimension().getType().getRegistryName()))
		{
			if(newGeneration)
			{
				for(Entry<String, ConfiguredFeature<?>> gen : features.entrySet())
				{

					gen.getValue().place(world, world.getChunkProvider().getChunkGenerator(), random, new BlockPos(16*chunkX, 0, 16*chunkZ));
				}
			}
			else
			{
				for(Entry<String, ConfiguredFeature<?>> gen : retroFeatures.entrySet())
				{
					if(retrogenMap.containsKey("retrogen_"+gen.getKey())&&retrogenMap.get("retrogen_"+gen.getKey()))
					{
						gen.getValue().place(world, world.getChunkProvider().getChunkGenerator(), random, new BlockPos(16*chunkX, 0, 16*chunkZ));
					}
				}
			}
		}

	}

	@SubscribeEvent
	public void chunkSave(ChunkDataEvent.Save event)
	{
		CompoundNBT nbt = new CompoundNBT();
		event.getData().put("ImmersiveEngineering", nbt);
		nbt.putBoolean(IEConfig.ORES.retrogen_key.get(), true);
	}

	@SubscribeEvent
	public void chunkLoad(ChunkDataEvent.Load event)
	{
		if(event.getChunk().getStatus()!=ChunkStatus.FULL)
		{
			return;
		}
		DimensionType dimension = event.getWorld().getDimension().getType();
		if(!event.getData().getCompound("ImmersiveEngineering").contains(IEConfig.ORES.retrogen_key.get())&&
				retrogenMap.size() > 0)
		{
			if(IEConfig.ORES.retrogen_log_flagChunk.get())
				IELogger.info("Chunk "+event.getChunk().getPos()+" has been flagged for Ore RetroGeneration by IE.");
			retrogenChunks.put(dimension, event.getChunk().getPos());
		}
	}

	public static ArrayListMultimap<DimensionType, ChunkPos> retrogenChunks = ArrayListMultimap.create();

	@SubscribeEvent
	public void serverWorldTick(TickEvent.WorldTickEvent event)
	{
		if(event.side==LogicalSide.CLIENT||event.phase==TickEvent.Phase.START)
			return;
		DimensionType dimension = event.world.getDimension().getType();
		int counter = 0;
		List<ChunkPos> chunks = retrogenChunks.get(dimension);

		if(chunks!=null&&chunks.size() > 0)
			for(int i = 0; i < 2; i++)
			{
				chunks = retrogenChunks.get(dimension);
				if(chunks==null||chunks.size() <= 0)
					break;
				counter++;
				ChunkPos loc = chunks.get(0);
				if(event.world.chunkExists(loc.x, loc.z))
				{
					long worldSeed = event.world.getSeed();
					Random fmlRandom = new Random(worldSeed);
					long xSeed = (fmlRandom.nextLong() >> 3);
					long zSeed = (fmlRandom.nextLong() >> 3);
					fmlRandom.setSeed(xSeed*loc.x+zSeed*loc.z^worldSeed);
					this.generateOres(fmlRandom, loc.x, loc.z, event.world, false);
					chunks.remove(0);
				}
				else
				{
					chunks.remove(0);
				}
			}
		if(counter > 0&&IEConfig.ORES.retrogen_log_remaining.get())
			IELogger.info("Retrogen was performed on "+counter+" Chunks, "+Math.max(0, chunks.size())+" chunks remaining");
	}
}