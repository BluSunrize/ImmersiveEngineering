/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.world;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig.Ores.OreConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig.Ores.VeinType;
import blusunrize.immersiveengineering.common.register.IEBlocks.Metals;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.world.IEOreFeature.IEOreFeatureConfig;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;

public class IEWorldGen
{
	public static Map<String, Holder<PlacedFeature>> features = new HashMap<>();
	public static Map<String, Pair<VeinType, List<TargetBlockState>>> retroFeatures = new HashMap<>();
	public static boolean anyRetrogenEnabled = false;

	public static void addOreGen(VeinType type)
	{
		EnumMetals metal = type.metal;
		List<TargetBlockState> targetList = ImmutableList.of(
				OreConfiguration.target(OreFeatures.STONE_ORE_REPLACEABLES, Metals.ORES.get(metal).defaultBlockState()),
				OreConfiguration.target(OreFeatures.DEEPSLATE_ORE_REPLACEABLES, Metals.DEEPSLATE_ORES.get(metal).defaultBlockState())
		);
		IEOreFeatureConfig cfg = new IEOreFeatureConfig(targetList, type);
		String name = type.getVeinName();
		Holder<PlacedFeature> feature = register(rl(name), IE_CONFIG_ORE, cfg, getOreModifiers(type));
		features.put(name, feature);
		retroFeatures.put(name, Pair.of(type, targetList));
	}

	public static void registerMineralVeinGen()
	{
		final var path = "mineral_veins";
		Holder<PlacedFeature> veinFeature = register(
				//TODO does this do what I want?
				rl(path), MINERAL_VEIN_FEATURE, new NoneFeatureConfiguration(), List.of()
		);
		features.put(path, veinFeature);
	}

	public static void onConfigUpdated()
	{
		anyRetrogenEnabled = false;
		for(Pair<VeinType, List<TargetBlockState>> config : retroFeatures.values())
			anyRetrogenEnabled |= IEServerConfig.ORES.ores.get(config.getFirst()).retrogenEnabled.get();
	}

	private void generateOres(RandomSource random, int chunkX, int chunkZ, ServerLevel world)
	{
		for(Entry<String, Pair<VeinType, List<TargetBlockState>>> gen : retroFeatures.entrySet())
		{
			VeinType type = gen.getValue().getFirst();
			List<TargetBlockState> targetList = gen.getValue().getSecond();
			OreConfig config = IEServerConfig.ORES.ores.get(type);
			if(config.retrogenEnabled.get())
			{
				ConfiguredFeature<OreConfiguration, Feature<OreConfiguration>> configured = new ConfiguredFeature<>(
						ORE_RETROGEN.get(), new OreConfiguration(targetList, config.veinSize.get())
				);
				PlacedFeature placed = new PlacedFeature(Holder.direct(configured), getOreModifiers(type));
				placed.place(
						world, world.getChunkSource().getGenerator(), random, new BlockPos(16*chunkX, 0, 16*chunkZ)
				);
			}
		}
	}

	private static List<PlacementModifier> getOreModifiers(VeinType type)
	{
		return ImmutableList.of(
				HeightRangePlacement.of(new IEHeightProvider(type)),
				InSquarePlacement.spread(),
				new IECountPlacement(type)
		);
	}

	@SubscribeEvent
	public void chunkDataSave(ChunkDataEvent.Save event)
	{
		CompoundTag levelTag = event.getData().getCompound("Level");
		CompoundTag nbt = new CompoundTag();
		levelTag.put("ImmersiveEngineering", nbt);
		nbt.putBoolean(IEServerConfig.ORES.retrogen_key.get(), true);
	}

	@SubscribeEvent
	public void chunkDataLoad(ChunkDataEvent.Load event)
	{
		LevelAccessor world = event.getLevel();
		if(event.getChunk().getStatus()==ChunkStatus.FULL&&world instanceof Level)
		{
			if(!event.getData().getCompound("ImmersiveEngineering").contains(IEServerConfig.ORES.retrogen_key.get())&&
					anyRetrogenEnabled)
			{
				if(IEServerConfig.ORES.retrogen_log_flagChunk.get())
					IELogger.info("Chunk "+event.getChunk().getPos()+" has been flagged for Ore RetroGeneration by IE.");
				ResourceKey<Level> dimension = ((Level)world).dimension();
				synchronized(retrogenChunks)
				{
					retrogenChunks.computeIfAbsent(dimension, d -> new ArrayList<>()).add(event.getChunk().getPos());
				}
			}
		}
	}

	public static final Map<ResourceKey<Level>, List<ChunkPos>> retrogenChunks = new HashMap<>();

	int indexToRemove = 0;

	@SubscribeEvent
	public void serverWorldTick(TickEvent.LevelTickEvent event)
	{
		if(event.side==LogicalSide.CLIENT||event.phase==TickEvent.Phase.START||!(event.level instanceof ServerLevel))
			return;
		ResourceKey<Level> dimension = event.level.dimension();
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
					if(event.level.hasChunk(loc.x, loc.z))
					{
						long worldSeed = ((WorldGenLevel)event.level).getSeed();
						RandomSource fmlRandom = RandomSource.create(worldSeed);
						long xSeed = (fmlRandom.nextLong()>>3);
						long zSeed = (fmlRandom.nextLong()>>3);
						fmlRandom.setSeed(xSeed*loc.x+zSeed*loc.z^worldSeed);
						this.generateOres(fmlRandom, loc.x, loc.z, (ServerLevel)event.level);
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

	private static final DeferredRegister<Feature<?>> FEATURE_REGISTER = DeferredRegister.create(ForgeRegistries.FEATURES, ImmersiveEngineering.MODID);
	private static final RegistryObject<FeatureMineralVein> MINERAL_VEIN_FEATURE = FEATURE_REGISTER.register(
			"mineral_vein", FeatureMineralVein::new
	);
	private static final RegistryObject<IEOreFeature> IE_CONFIG_ORE = FEATURE_REGISTER.register(
			"ie_ore", IEOreFeature::new
	);
	public static final RegistryObject<OreRetrogenFeature> ORE_RETROGEN = FEATURE_REGISTER.register(
			"ore_retro", OreRetrogenFeature::new
	);

	private static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_REGISTER = DeferredRegister.create(
			Registries.PLACEMENT_MODIFIER, ImmersiveEngineering.MODID
	);
	public static RegistryObject<PlacementModifierType<IECountPlacement>> IE_COUNT_PLACEMENT = PLACEMENT_REGISTER.register(
			"ie_count", () -> () -> IECountPlacement.CODEC
	);

	private static final DeferredRegister<HeightProviderType<?>> HEIGHT_REGISTER = DeferredRegister.create(
			Registries.HEIGHT_PROVIDER_TYPE, ImmersiveEngineering.MODID
	);
	public static RegistryObject<HeightProviderType<IEHeightProvider>> IE_HEIGHT_PROVIDER = HEIGHT_REGISTER.register(
			"ie_range", () -> () -> IEHeightProvider.CODEC
	);

	public static void init()
	{
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		FEATURE_REGISTER.register(bus);
		PLACEMENT_REGISTER.register(bus);
		HEIGHT_REGISTER.register(bus);
	}

	public static void initLate()
	{
		for(VeinType type : VeinType.VALUES)
			IEWorldGen.addOreGen(type);
		IEWorldGen.registerMineralVeinGen();
	}

	// TODO what's the right point/way of registering to builtin/dynamic registries?
	private static <Cfg extends FeatureConfiguration, F extends Feature<Cfg>>
	Holder<PlacedFeature> register(ResourceLocation rl, RegistryObject<F> feature, Cfg cfg, List<PlacementModifier> oreModifiers)
	{
		// TODO
		throw new UnsupportedOperationException();
		/*
		Holder<ConfiguredFeature<?, ?>> configured = BuiltinRegistries.register(
				BuiltinRegistries.CONFIGURED_FEATURE, rl, new ConfiguredFeature<>(feature.get(), cfg)
		);
		return BuiltinRegistries.register(
				BuiltinRegistries.PLACED_FEATURE, rl, new PlacedFeature(configured, oreModifiers)
		);
		 */
	}
}
