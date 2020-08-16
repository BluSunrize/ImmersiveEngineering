/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.world;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.excavator.ExcavatorHandler;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.collect.HashMultimap;
import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.PerlinNoiseGenerator;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.OreFeatureConfig.FillerBlockType;
import net.minecraft.world.gen.placement.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class IEWorldGen
{
	public static Map<String, ConfiguredFeature<?, ?>> features = new HashMap<>();
	public static Map<String, ConfiguredFeature<?, ?>> retroFeatures = new HashMap<>();
	public static List<RegistryKey<DimensionType>> oreDimBlacklist = new ArrayList<>();
	public static Set<String> retrogenOres = new HashSet<>();

	public static void addOreGen(String name, BlockState state, int maxVeinSize, int minY, int maxY, int chunkOccurence)
	{
		/*
   func_243968_a("ore_iron",
		Feature.ORE.withConfiguration(
			new OreFeatureConfig(OreFeatureConfig.FillerBlockType.field_241882_a, Features.States.field_244046_aj, 9)
		)
		.func_242733_d(64)
		.func_242728_a()
		.func_242731_b(20)
    );
		 */
		OreFeatureConfig cfg = new OreFeatureConfig(FillerBlockType.field_241882_a, state, maxVeinSize);
		ConfiguredFeature<?, ?> feature = new ConfiguredFeature<>(Feature.ORE, cfg)
				.withPlacement(
						new ConfiguredPlacement<>(COUNT_RANGE_IE, new TopSolidRangeConfig(minY, minY, maxY))
				)
				// TODO actually understand what I'm doing here
				.func_242728_a()
				.func_242731_b(chunkOccurence);
		//TODO
		//for(Biome biome : WorldGenRegistries.field_243657_i)
		//	biome.addFeature(Decoration.UNDERGROUND_ORES, feature);
		features.put(name, feature);

		ConfiguredFeature<?, ?> retroFeature = new ConfiguredFeature<>(IEContent.ORE_RETROGEN, cfg)
				.withPlacement(
						new ConfiguredPlacement<>(COUNT_RANGE_IE, new TopSolidRangeConfig(minY, minY, maxY))
				)
				.func_242728_a()
				.func_242731_b(chunkOccurence);
		retroFeatures.put(name, retroFeature);
	}

	public static void registerMineralVeinGen()
	{
		ConfiguredFeature<?, ?> vein_feature = new ConfiguredFeature<>(MINERAL_VEIN_FEATURE, new NoFeatureConfig())
				.withPlacement(
						new ConfiguredPlacement<>(Placement.NOPE, IPlacementConfig.NO_PLACEMENT_CONFIG)
				);
		//TODO for(Biome biome : ForgeRegistries.BIOMES.getValues())
		//TODO 	biome.addFeature(Decoration.RAW_GENERATION, vein_feature);
	}

	public void generateOres(Random random, int chunkX, int chunkZ, ServerWorld world, boolean newGeneration)
	{
		if(!oreDimBlacklist.contains(world.getDimensionKey()))
		{
			if(newGeneration)
			{
				for(Entry<String, ConfiguredFeature<?, ?>> gen : features.entrySet())
					gen.getValue().func_242765_a(world, world.getChunkProvider().getChunkGenerator(), random, new BlockPos(16*chunkX, 0, 16*chunkZ));
			}
			else
			{
				for(Entry<String, ConfiguredFeature<?, ?>> gen : retroFeatures.entrySet())
				{
					if(retrogenOres.contains("retrogen_"+gen.getKey()))
						gen.getValue().func_242765_a(world, world.getChunkProvider().getChunkGenerator(), random, new BlockPos(16*chunkX, 0, 16*chunkZ));
				}
			}
		}

	}

	@SubscribeEvent
	public void chunkDataSave(ChunkDataEvent.Save event)
	{
		CompoundNBT levelTag = event.getData().getCompound("Level");
		CompoundNBT nbt = new CompoundNBT();
		levelTag.put("ImmersiveEngineering", nbt);
		nbt.putBoolean(IEConfig.ORES.retrogen_key.get(), true);
	}

	@SubscribeEvent
	public void chunkDataLoad(ChunkDataEvent.Load event)
	{
		IWorld world = event.getWorld();
		if(event.getChunk().getStatus()==ChunkStatus.FULL && world instanceof World)
		{
			if(!event.getData().getCompound("ImmersiveEngineering").contains(IEConfig.ORES.retrogen_key.get())&&
					!retrogenOres.isEmpty())
			{
				if(IEConfig.ORES.retrogen_log_flagChunk.get())
					IELogger.info("Chunk "+event.getChunk().getPos()+" has been flagged for Ore RetroGeneration by IE.");
				RegistryKey<World> dimension = ((World)world).getDimensionKey();
				synchronized(retrogenChunks)
				{
					retrogenChunks.computeIfAbsent(dimension, d -> new ArrayList<>()).add(event.getChunk().getPos());
				}
			}
		}
	}

	public static final Map<RegistryKey<World>, List<ChunkPos>> retrogenChunks = new HashMap<>();

	int indexToRemove = 0;

	@SubscribeEvent
	public void serverWorldTick(TickEvent.WorldTickEvent event)
	{
		if(event.side==LogicalSide.CLIENT||event.phase==TickEvent.Phase.START||!(event.world instanceof ServerWorld))
			return;
		RegistryKey<World> dimension = event.world.getDimensionKey();
		int counter = 0;
		int remaining;
		synchronized(retrogenChunks)
		{
			final List<ChunkPos> chunks = retrogenChunks.get(dimension);

			if(chunks!=null&&chunks.size() > 0)
			{
				if(indexToRemove >= chunks.size())
					indexToRemove = 0;
				for(int i = 0; i < 2&&indexToRemove < chunks.size(); ++i)
				{
					if(chunks.size() <= 0)
						break;
					ChunkPos loc = chunks.get(indexToRemove);
					if(event.world.chunkExists(loc.x, loc.z))
					{
						long worldSeed = ((ISeedReader)event.world).getSeed();
						Random fmlRandom = new Random(worldSeed);
						long xSeed = (fmlRandom.nextLong() >> 3);
						long zSeed = (fmlRandom.nextLong() >> 3);
						fmlRandom.setSeed(xSeed*loc.x+zSeed*loc.z^worldSeed);
						this.generateOres(fmlRandom, loc.x, loc.z, (ServerWorld)event.world, false);
						counter++;
						chunks.remove(indexToRemove);
					}
					else
						++indexToRemove;
				}
			}
			remaining = chunks==null?0: chunks.size();
		}
		if(counter > 0&&IEConfig.ORES.retrogen_log_remaining.get())
			IELogger.info("Retrogen was performed on "+counter+" Chunks, "+remaining+" chunks remaining");
	}

	private static CountRangeInIEDimensions COUNT_RANGE_IE;
	private static FeatureMineralVein MINERAL_VEIN_FEATURE;

	public void registerPlacements(RegistryEvent.Register<Placement<?>> ev)
	{
		COUNT_RANGE_IE = new CountRangeInIEDimensions(TopSolidRangeConfig.field_236985_a_);
		COUNT_RANGE_IE.setRegistryName(ImmersiveEngineering.MODID, "count_range_in_ie_dimensions");
		ev.getRegistry().register(COUNT_RANGE_IE);
	}

	public void registerFeatures(RegistryEvent.Register<Feature<?>> ev)
	{
		MINERAL_VEIN_FEATURE = new FeatureMineralVein();
		MINERAL_VEIN_FEATURE.setRegistryName(ImmersiveEngineering.MODID, "mineral_vein");
		ev.getRegistry().register(MINERAL_VEIN_FEATURE);
	}

	private static class CountRangeInIEDimensions extends Placement<TopSolidRangeConfig>
	{
		private final RangePlacement countRange;

		public CountRangeInIEDimensions(Codec<TopSolidRangeConfig> configFactoryIn)
		{
			super(configFactoryIn);
			this.countRange = new RangePlacement(configFactoryIn);
		}

		@Override
		public Stream<BlockPos> func_241857_a(WorldDecoratingHelper p_241857_1_, Random p_241857_2_, TopSolidRangeConfig p_241857_3_, BlockPos p_241857_4_)
		{
			//TODO
			//if(!oreDimBlacklist.contains(worldIn.getWorld().func_234922_V_()))
			//{
			//	return countRange.getPositions(worldIn, generatorIn, random, configIn, pos);
			//}
			//else
			//{
				return Stream.empty();
			//}
		}
	}

	private static class FeatureMineralVein extends Feature<NoFeatureConfig>
	{
		public static HashMultimap<RegistryKey<World>, ChunkPos> veinGeneratedChunks = HashMultimap.create();

		public FeatureMineralVein()
		{
			super(NoFeatureConfig.field_236558_a_);
		}

		@Override
		public boolean func_241855_a(ISeedReader worldIn, ChunkGenerator p_241855_2_, Random random, BlockPos pos, NoFeatureConfig p_241855_5_)
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
}
