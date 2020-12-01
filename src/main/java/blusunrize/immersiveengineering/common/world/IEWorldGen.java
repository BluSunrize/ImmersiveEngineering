/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.world;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig.Ores.OreConfig;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.world.IECountPlacement.IEFeatureSpreadConfig;
import blusunrize.immersiveengineering.common.world.IEOreFeature.IEOreFeatureConfig;
import blusunrize.immersiveengineering.common.world.IERangePlacement.IETopSolidRangeConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.OreFeatureConfig.FillerBlockType;
import net.minecraft.world.gen.placement.ConfiguredPlacement;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.Map.Entry;

public class IEWorldGen
{
	public static Map<String, ConfiguredFeature<?, ?>> features = new HashMap<>();
	public static Map<String, Pair<OreConfig, BlockState>> retroFeatures = new HashMap<>();
	public static boolean anyRetrogenEnabled = false;

	public static void addOreGen(Block block, String name, OreConfig config)
	{
		IEOreFeatureConfig cfg = new IEOreFeatureConfig(FillerBlockType.field_241882_a, block.getDefaultState(), config);
		ConfiguredFeature<?, ?> feature = register(new ResourceLocation(Lib.MODID, name),
				IE_CONFIG_ORE.get().withConfiguration(cfg)
						.withPlacement(IE_RANGE_PLACEMENT.get().configure(new IETopSolidRangeConfig(config)))
						.func_242728_a/* spreadHorizontally */()
						.withPlacement(IE_COUNT_PLACEMENT.get().configure(new IEFeatureSpreadConfig(config)))
		);
		features.put(name, feature);
		retroFeatures.put(name, Pair.of(config, block.getDefaultState()));
	}

	public static void registerMineralVeinGen()
	{
		ConfiguredFeature<?, ?> veinFeature = register(new ResourceLocation(Lib.MODID, "mineral_veins"),
				MINERAL_VEIN_FEATURE.get().withConfiguration(new NoFeatureConfig())
						.withPlacement(
								new ConfiguredPlacement<>(Placement.NOPE, IPlacementConfig.NO_PLACEMENT_CONFIG)
						));
		features.put("veins", veinFeature);
	}

	public static void onConfigUpdated()
	{
		anyRetrogenEnabled = false;
		for(Pair<OreConfig, BlockState> config : retroFeatures.values())
			anyRetrogenEnabled |= config.getLeft().retrogenEnabled.get();
	}

	@SubscribeEvent
	public void onBiomeLoad(BiomeLoadingEvent ev)
	{
		BiomeGenerationSettingsBuilder generation = ev.getGeneration();
		for(Entry<String, ConfiguredFeature<?, ?>> e : features.entrySet())
			generation.withFeature(Decoration.UNDERGROUND_ORES, e.getValue());
	}

	private void generateOres(Random random, int chunkX, int chunkZ, ServerWorld world)
	{
		for(Entry<String, Pair<OreConfig, BlockState>> gen : retroFeatures.entrySet())
		{
			OreConfig config = gen.getValue().getKey();
			BlockState state = gen.getValue().getRight();
			if(config.retrogenEnabled.get())
			{
				ConfiguredFeature<?, ?> retroFeature = IEContent.ORE_RETROGEN
						.withConfiguration(new OreFeatureConfig(FillerBlockType.field_241882_a, state, config.veinSize.get()))
						.withPlacement(new IERangePlacement().configure(new IETopSolidRangeConfig(config)))
						.func_242728_a/* spreadHorizontally */()
						.withPlacement(new IECountPlacement().configure(new IEFeatureSpreadConfig(config)));
				retroFeature.func_242765_a(
						world,
						world.getChunkProvider().getChunkGenerator(),
						random,
						new BlockPos(16*chunkX, 0, 16*chunkZ)
				);
			}
		}
	}

	@SubscribeEvent
	public void chunkDataSave(ChunkDataEvent.Save event)
	{
		CompoundNBT levelTag = event.getData().getCompound("Level");
		CompoundNBT nbt = new CompoundNBT();
		levelTag.put("ImmersiveEngineering", nbt);
		nbt.putBoolean(IEServerConfig.ORES.retrogen_key.get(), true);
	}

	@SubscribeEvent
	public void chunkDataLoad(ChunkDataEvent.Load event)
	{
		IWorld world = event.getWorld();
		if(event.getChunk().getStatus()==ChunkStatus.FULL && world instanceof World)
		{
			if(!event.getData().getCompound("ImmersiveEngineering").contains(IEServerConfig.ORES.retrogen_key.get())&&
					anyRetrogenEnabled)
			{
				if(IEServerConfig.ORES.retrogen_log_flagChunk.get())
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
						this.generateOres(fmlRandom, loc.x, loc.z, (ServerWorld)event.world);
						counter++;
						chunks.remove(indexToRemove);
					}
					else
						++indexToRemove;
				}
			}
			remaining = chunks==null?0: chunks.size();
		}
		if(counter > 0&&IEServerConfig.ORES.retrogen_log_remaining.get())
			IELogger.info("Retrogen was performed on "+counter+" Chunks, "+remaining+" chunks remaining");
	}

	private static DeferredRegister<Feature<?>> FEATURE_REGISTER = DeferredRegister.create(ForgeRegistries.FEATURES, ImmersiveEngineering.MODID);
	private static DeferredRegister<Placement<?>> PLACEMENT_REGISTER = DeferredRegister.create(ForgeRegistries.DECORATORS, ImmersiveEngineering.MODID);
	private static RegistryObject<FeatureMineralVein> MINERAL_VEIN_FEATURE = FEATURE_REGISTER.register(
			"mineral_vein", FeatureMineralVein::new
	);
	private static RegistryObject<IEOreFeature> IE_CONFIG_ORE = FEATURE_REGISTER.register(
			"ie_ore", IEOreFeature::new
	);
	private static RegistryObject<IERangePlacement> IE_RANGE_PLACEMENT = PLACEMENT_REGISTER.register(
			"ie_range", IERangePlacement::new
	);
	private static RegistryObject<IECountPlacement> IE_COUNT_PLACEMENT = PLACEMENT_REGISTER.register(
			"ie_ount", IECountPlacement::new
	);

	public static void init()
	{
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		FEATURE_REGISTER.register(bus);
		PLACEMENT_REGISTER.register(bus);
	}

	private static <FC extends IFeatureConfig>
	ConfiguredFeature<FC, ?> register(ResourceLocation key, ConfiguredFeature<FC, ?> configuredFeature)
	{
		return Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, key, configuredFeature);
	}
}
