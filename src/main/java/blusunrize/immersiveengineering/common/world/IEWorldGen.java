/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.world;

import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.collect.ArrayListMultimap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.gen.feature.CompositeFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.MinableConfig;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

@EventBusSubscriber
public class IEWorldGen
{
	public static Map<String, CompositeFeature<MinableConfig, CountRangeConfig>> features = new HashMap<>();
	//TODO public static ArrayList<Integer> oreDimBlacklist = new ArrayList();
	public static Map<String, Boolean> retrogenMap = new HashMap<>();

	public static void addOreGen(String name, IBlockState state, int maxVeinSize, int minY, int maxY, int chunkOccurence, int weight)
	{
		MinableConfig cfg = new MinableConfig(MinableConfig.IS_ROCK, state, maxVeinSize);
		CompositeFeature<MinableConfig, CountRangeConfig> feature = Biome.createCompositeFeature(Feature.MINABLE, cfg, Biome.COUNT_RANGE,
				new CountRangeConfig(chunkOccurence, minY, maxY, minY));
		for(Biome biome : Biome.BIOMES)
			biome.addFeature(Decoration.UNDERGROUND_ORES, feature);
		features.put(name, feature);
	}

	public void generateOres(Random random, int chunkX, int chunkZ, World world, boolean newGeneration)
	{
		//if(!oreDimBlacklist.contains(world.provider.getDimension()))
		for(Entry<String, CompositeFeature<MinableConfig, CountRangeConfig>> gen : features.entrySet())
			if(newGeneration||retrogenMap.get("retrogen_"+gen.getKey()))
				gen.getValue().func_212245_a(world, world.getChunkProvider().getChunkGenerator(),
						random, new BlockPos(16*chunkX, 0, 16*chunkZ),
						null);
	}

	@SubscribeEvent
	public void chunkSave(ChunkDataEvent.Save event)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		event.getData().setTag("ImmersiveEngineering", nbt);
		nbt.setBoolean(IEConfig.Ores.retrogen_key, true);
	}

	@SubscribeEvent
	public void chunkLoad(ChunkDataEvent.Load event)
	{
		DimensionType dimension = event.getWorld().getDimension().getType();
		if((!event.getData().getCompound("ImmersiveEngineering").hasKey(IEConfig.Ores.retrogen_key))&&(IEConfig.Ores.retrogen_copper||IEConfig.Ores.retrogen_bauxite||IEConfig.Ores.retrogen_lead||IEConfig.Ores.retrogen_silver||IEConfig.Ores.retrogen_nickel||IEConfig.Ores.retrogen_uranium))
		{
			if(IEConfig.Ores.retrogen_log_flagChunk)
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
				long worldSeed = event.world.getSeed();
				Random fmlRandom = new Random(worldSeed);
				long xSeed = (fmlRandom.nextLong() >> 3);
				long zSeed = (fmlRandom.nextLong() >> 3);
				fmlRandom.setSeed(xSeed*loc.x+zSeed*loc.z^worldSeed);
				this.generateOres(fmlRandom, loc.x, loc.z, event.world, false);
				chunks.remove(0);
			}
		if(counter > 0&&IEConfig.Ores.retrogen_log_remaining)
			IELogger.info("Retrogen was performed on "+counter+" Chunks, "+Math.max(0, chunks.size())+" chunks remaining");
	}
}