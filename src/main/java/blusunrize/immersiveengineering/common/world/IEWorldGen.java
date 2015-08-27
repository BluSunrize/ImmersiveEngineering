package blusunrize.immersiveengineering.common.world;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import cpw.mods.fml.common.IWorldGenerator;

public class IEWorldGen implements IWorldGenerator
{
	public static class OreGen
	{
		WorldGenMinable mineableGen;
		
		int minY;
		int maxY;
		int chunkOccurence;
		int weight;
		public OreGen(Block block, int meta, int maxVeinSize, Block replaceTarget, int minY, int maxY,int chunkOccurence, int weight)
		{
			mineableGen = new WorldGenMinable(block, meta, maxVeinSize, replaceTarget);
			this.minY=minY;
			this.maxY=maxY;
			this.chunkOccurence=chunkOccurence;
			this.weight=weight;
		}

		public void generate(World world, Random rand, int x, int z)
		{
			for(int i=0; i<chunkOccurence; i++)
				if(rand.nextInt(100)<weight)
				{
					int xx = x + rand.nextInt(16);
					int yy = minY + rand.nextInt(maxY - minY);
					int zz = z + rand.nextInt(16);
					mineableGen.generate(world, rand, xx,yy,zz);
				}
		}
	}
	public static ArrayList<OreGen> orespawnList = new ArrayList();
	public static ArrayList<Integer> oreDimBlacklist = new ArrayList();
	public static OreGen addOreGen(Block block,int meta,int maxVeinSize,int minY, int maxY,int chunkOccurence,int weight)
	{
		OreGen gen = new OreGen(block, meta, maxVeinSize, Blocks.stone, minY, maxY, chunkOccurence, weight);
		orespawnList.add(gen);
		return gen;
	}

	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
	{
		if(!oreDimBlacklist.contains(world.provider.dimensionId))
			for(OreGen gen : orespawnList)
				gen.generate(world, random, chunkX*16, chunkZ*16);
	}
}