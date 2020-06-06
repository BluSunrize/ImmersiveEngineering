/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.world;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig.FillerBlockType;
import net.minecraft.world.gen.placement.CountRange;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Stream;

@EventBusSubscriber(modid = ImmersiveEngineering.MODID, bus = Bus.MOD)
public class IEWorldGen
{
	public static Map<String, ConfiguredFeature<?>> features = new HashMap<>();
	public static Map<String, ConfiguredFeature<?>> retroFeatures = new HashMap<>();
	public static List<ResourceLocation> oreDimBlacklist = new ArrayList<>();
	public static Set<String> retrogenOres = new HashSet<>();

	public static void addOreGen(String name, BlockState state, int maxVeinSize, int minY, int maxY, int chunkOccurence)
	{
		OreFeatureConfig cfg = new OreFeatureConfig(FillerBlockType.NATURAL_STONE, state, maxVeinSize);
		ConfiguredFeature<?> feature = Biome.createDecoratedFeature(Feature.ORE, cfg, COUNT_RANGE_IE,
				new CountRangeConfig(chunkOccurence, minY, minY, maxY));
		for(Biome biome : ForgeRegistries.BIOMES.getValues())
			biome.addFeature(Decoration.UNDERGROUND_ORES, feature);
		features.put(name, feature);

		ConfiguredFeature<?> retroFeature = Biome.createDecoratedFeature(IEContent.ORE_RETROGEN, cfg, COUNT_RANGE_IE,
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
					gen.getValue().place(world, world.getChunkProvider().getChunkGenerator(), random, new BlockPos(16*chunkX, 0, 16*chunkZ));
			}
			else
			{
				for(Entry<String, ConfiguredFeature<?>> gen : retroFeatures.entrySet())
				{
					if(retrogenOres.contains("retrogen_"+gen.getKey()))
						gen.getValue().place(world, world.getChunkProvider().getChunkGenerator(), random, new BlockPos(16*chunkX, 0, 16*chunkZ));
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
	public static void chunkLoad(ChunkDataEvent.Load event)
	{
		if(event.getChunk().getStatus()==ChunkStatus.FULL)
		{
			DimensionType dimension = event.getChunk().getWorldForge().getDimension().getType();
			if(!event.getData().getCompound("ImmersiveEngineering").contains(IEConfig.ORES.retrogen_key.get())&&
					!retrogenOres.isEmpty())
			{
				if(IEConfig.ORES.retrogen_log_flagChunk.get())
					IELogger.info("Chunk "+event.getChunk().getPos()+" has been flagged for Ore RetroGeneration by IE.");
				synchronized(retrogenChunks)
				{
					retrogenChunks.computeIfAbsent(dimension, d -> new ArrayList<>()).add(event.getChunk().getPos());
				}
			}
		}
	}

	public static final Map<DimensionType, List<ChunkPos>> retrogenChunks = new HashMap<>();

	int indexToRemove = 0;

	@SubscribeEvent
	public void serverWorldTick(TickEvent.WorldTickEvent event)
	{
		if(event.side==LogicalSide.CLIENT||event.phase==TickEvent.Phase.START)
			return;
		DimensionType dimension = event.world.getDimension().getType();
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
						long worldSeed = event.world.getSeed();
						Random fmlRandom = new Random(worldSeed);
						long xSeed = (fmlRandom.nextLong() >> 3);
						long zSeed = (fmlRandom.nextLong() >> 3);
						fmlRandom.setSeed(xSeed*loc.x+zSeed*loc.z^worldSeed);
						this.generateOres(fmlRandom, loc.x, loc.z, event.world, false);
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

	@SubscribeEvent
	public static void registerPlacements(RegistryEvent.Register<Placement<?>> ev)
	{
		COUNT_RANGE_IE = new CountRangeInIEDimensions(CountRangeConfig::deserialize);
		COUNT_RANGE_IE.setRegistryName(ImmersiveEngineering.MODID, "count_range_in_ie_dimensions");
		ev.getRegistry().register(COUNT_RANGE_IE);
	}

	private static class CountRangeInIEDimensions extends Placement<CountRangeConfig>
	{
		private final CountRange countRange;

		public CountRangeInIEDimensions(Function<Dynamic<?>, ? extends CountRangeConfig> configFactoryIn)
		{
			super(configFactoryIn);
			this.countRange = new CountRange(configFactoryIn);
		}

		@Nonnull
		@Override
		public Stream<BlockPos> getPositions(@Nonnull IWorld worldIn,
											 @Nonnull ChunkGenerator<? extends GenerationSettings> generatorIn,
											 @Nonnull Random random, @Nonnull CountRangeConfig configIn, @Nonnull BlockPos pos)
		{
			DimensionType d = worldIn.getDimension().getType();
			if(!oreDimBlacklist.contains(d.getRegistryName()))
			{
				return countRange.getPositions(worldIn, generatorIn, random, configIn, pos);
			}
			else
			{
				return Stream.empty();
			}
		}
	}
}
