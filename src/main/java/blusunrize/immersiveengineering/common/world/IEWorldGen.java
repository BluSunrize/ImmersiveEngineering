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
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.world.IEOreFeature.IEOreFeatureConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.event.level.ChunkDataEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = Lib.MODID, bus = Bus.GAME)
public class IEWorldGen
{
	private static final DeferredRegister<Feature<?>> FEATURE_REGISTER = DeferredRegister.create(BuiltInRegistries.FEATURE, ImmersiveEngineering.MODID);
	public static final DeferredHolder<Feature<?>, FeatureMineralVein> MINERAL_VEIN_FEATURE = FEATURE_REGISTER.register(
			"mineral_vein", FeatureMineralVein::new
	);
	public static final DeferredHolder<Feature<?>, IEOreFeature> IE_CONFIG_ORE = FEATURE_REGISTER.register(
			"ie_ore", IEOreFeature::new
	);

	private static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_REGISTER = DeferredRegister.create(
			Registries.PLACEMENT_MODIFIER_TYPE, ImmersiveEngineering.MODID
	);
	public static DeferredHolder<PlacementModifierType<?>, PlacementModifierType<IECountPlacement>> IE_COUNT_PLACEMENT = PLACEMENT_REGISTER.register(
			"ie_count", () -> () -> IECountPlacement.CODEC
	);

	private static final DeferredRegister<HeightProviderType<?>> HEIGHT_REGISTER = DeferredRegister.create(
			Registries.HEIGHT_PROVIDER_TYPE, ImmersiveEngineering.MODID
	);
	public static DeferredHolder<HeightProviderType<?>, HeightProviderType<IEHeightProvider>> IE_HEIGHT_PROVIDER = HEIGHT_REGISTER.register(
			"ie_range", () -> () -> IEHeightProvider.CODEC
	);

	public static void init(IEventBus modBus)
	{
		FEATURE_REGISTER.register(modBus);
		PLACEMENT_REGISTER.register(modBus);
		HEIGHT_REGISTER.register(modBus);
	}

	private static volatile List<PlacedFeature> RETROGEN_FEATURES = List.of();

	public static void onConfigUpdated()
	{
		List<PlacedFeature> enabledFeatures = new ArrayList<>();
		var server = ServerLifecycleHooks.getCurrentServer();
		if(server!=null)
		{
			Registry<PlacedFeature> registry = server.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
			for(final PlacedFeature feature : registry)
				if(isRetrogenFeature(feature))
					enabledFeatures.add(feature);
		}
		RETROGEN_FEATURES = List.copyOf(enabledFeatures);
	}

	private static boolean isRetrogenFeature(PlacedFeature placed)
	{
		ConfiguredFeature<?, ?> feature = placed.feature().value();
		if(feature.config() instanceof IEOreFeatureConfig featureConfig)
			return IEServerConfig.ORES.ores.get(featureConfig.type()).retrogenEnabled.get();
		else if(feature.feature() instanceof FeatureMineralVein)
			return IEServerConfig.ORES.retrogenExcavatorVeins.get();
		else
			return false;
	}

	private static void generateOres(RandomSource random, int chunkX, int chunkZ, ServerLevel world)
	{
		for(PlacedFeature placed : RETROGEN_FEATURES)
			placed.place(world, world.getChunkSource().getGenerator(), random, new BlockPos(16*chunkX, 0, 16*chunkZ));
	}

	@SubscribeEvent
	public static void chunkDataSave(ChunkDataEvent.Save event)
	{
		CompoundTag levelTag = event.getData().getCompound("Level");
		CompoundTag nbt = new CompoundTag();
		levelTag.put("ImmersiveEngineering", nbt);
		nbt.putBoolean(IEServerConfig.ORES.retrogen_key.get(), true);
	}

	@SubscribeEvent
	public static void chunkDataLoad(ChunkDataEvent.Load event)
	{
		if(RETROGEN_FEATURES.isEmpty())
			return;
		LevelAccessor world = event.getLevel();
		if(event.getChunk().getPersistedStatus()!=ChunkStatus.FULL||!(world instanceof Level))
			return;
		if(event.getData().getCompound("ImmersiveEngineering").contains(IEServerConfig.ORES.retrogen_key.get()))
			return;
		if(IEServerConfig.ORES.retrogen_log_flagChunk.get())
			IELogger.info("Chunk "+event.getChunk().getPos()+" has been flagged for Ore RetroGeneration by IE.");
		ResourceKey<Level> dimension = ((Level)world).dimension();
		synchronized(retrogenChunks)
		{
			retrogenChunks.computeIfAbsent(dimension, d -> new ArrayList<>()).add(event.getChunk().getPos());
		}
	}

	public static final Map<ResourceKey<Level>, List<ChunkPos>> retrogenChunks = new HashMap<>();

	private static int indexToRemove = 0;

	@SubscribeEvent
	public static void serverWorldTick(LevelTickEvent.Post event)
	{
		if(!(event.getLevel() instanceof ServerLevel serverLevel))
			return;
		ResourceKey<Level> dimension = serverLevel.dimension();
		int counter = 0;
		int remaining;
		synchronized(retrogenChunks)
		{
			final List<ChunkPos> chunks = retrogenChunks.get(dimension);

			if(chunks!=null&&!chunks.isEmpty())
			{
				if(indexToRemove >= chunks.size())
					indexToRemove = 0;
				for(int i = 0; i < 2&&indexToRemove < chunks.size(); ++i)
				{
					if(chunks.size() <= 0)
						break;
					ChunkPos loc = chunks.get(indexToRemove);
					if(serverLevel.hasChunk(loc.x, loc.z))
					{
						long worldSeed = serverLevel.getSeed();
						RandomSource fmlRandom = RandomSource.create(worldSeed);
						long xSeed = (fmlRandom.nextLong()>>3);
						long zSeed = (fmlRandom.nextLong()>>3);
						fmlRandom.setSeed(xSeed*loc.x+zSeed*loc.z^worldSeed);
						generateOres(fmlRandom, loc.x, loc.z, serverLevel);
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
}
