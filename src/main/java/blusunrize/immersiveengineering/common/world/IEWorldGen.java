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
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class IEWorldGen implements IWorldGenerator
{
	public static class OreGen
	{
		String name;
		WorldGenMinable mineableGen;
		int minY;
		int maxY;
		int chunkOccurence;
		int weight;

		public OreGen(String name, IBlockState state, int maxVeinSize, Block replaceTarget, int minY, int maxY, int chunkOccurence, int weight)
		{
			this.name = name;
			this.mineableGen = new WorldGenMinable(state, maxVeinSize, BlockMatcher.forBlock(replaceTarget));
			this.minY = minY;
			this.maxY = maxY;
			this.chunkOccurence = chunkOccurence;
			this.weight = weight;
		}

		public void generate(World world, Random rand, int x, int z)
		{
			BlockPos pos;
			for(int i = 0; i < chunkOccurence; i++)
				if(rand.nextInt(100) < weight)
				{
					pos = new BlockPos(x+rand.nextInt(16), minY+rand.nextInt(maxY-minY), z+rand.nextInt(16));
					mineableGen.generate(world, rand, pos);
				}
		}
	}

	public static ArrayList<OreGen> orespawnList = new ArrayList();
	public static ArrayList<Integer> oreDimBlacklist = new ArrayList();
	public static HashMap<String, Boolean> retrogenMap = new HashMap();

	public static OreGen addOreGen(String name, IBlockState state, int maxVeinSize, int minY, int maxY, int chunkOccurence, int weight)
	{
		OreGen gen = new OreGen(name, state, maxVeinSize, Blocks.STONE, minY, maxY, chunkOccurence, weight);
		orespawnList.add(gen);
		return gen;
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider)
	{
		this.generateOres(random, chunkX, chunkZ, world, true);
	}

	public void generateOres(Random random, int chunkX, int chunkZ, World world, boolean newGeneration)
	{
		if(!oreDimBlacklist.contains(world.provider.getDimension()))
			for(OreGen gen : orespawnList)
				if(newGeneration||retrogenMap.get("retrogen_"+gen.name))
					gen.generate(world, random, chunkX*16, chunkZ*16);
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
		int dimension = event.getWorld().provider.getDimension();
		if((!event.getData().getCompoundTag("ImmersiveEngineering").hasKey(IEConfig.Ores.retrogen_key))&&(IEConfig.Ores.retrogen_copper||IEConfig.Ores.retrogen_bauxite||IEConfig.Ores.retrogen_lead||IEConfig.Ores.retrogen_silver||IEConfig.Ores.retrogen_nickel||IEConfig.Ores.retrogen_uranium))
		{
			if(IEConfig.Ores.retrogen_log_flagChunk)
				IELogger.info("Chunk "+event.getChunk().getPos()+" has been flagged for Ore RetroGeneration by IE.");
			retrogenChunks.put(dimension, event.getChunk().getPos());
		}
	}

	public static ArrayListMultimap<Integer, ChunkPos> retrogenChunks = ArrayListMultimap.create();

	@SubscribeEvent
	public void serverWorldTick(TickEvent.WorldTickEvent event)
	{
		if(event.side==Side.CLIENT||event.phase==TickEvent.Phase.START)
			return;
		int dimension = event.world.provider.getDimension();
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