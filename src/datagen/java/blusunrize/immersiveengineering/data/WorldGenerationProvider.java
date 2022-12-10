package blusunrize.immersiveengineering.data;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.config.IEServerConfig.Ores.VeinType;
import blusunrize.immersiveengineering.common.register.IEBlocks.Metals;
import blusunrize.immersiveengineering.common.world.IECountPlacement;
import blusunrize.immersiveengineering.common.world.IEHeightProvider;
import blusunrize.immersiveengineering.common.world.IEOreFeature.IEOreFeatureConfig;
import blusunrize.immersiveengineering.common.world.IEWorldGen;
import blusunrize.immersiveengineering.mixin.coremods.temp.DatagenRegistryAccess;
import blusunrize.immersiveengineering.mixin.coremods.temp.RegSetBuilderAccess;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.*;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.JsonCodecProvider;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers.AddFeaturesBiomeModifier;
import net.minecraftforge.registries.ForgeRegistries.Keys;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class WorldGenerationProvider implements DataProvider
{
	private final DataGenerator dataGenerator;
	private final HolderLookup.Provider ownLookupProvider;
	private final ExistingFileHelper existingFileHelper;
	private final Map<VeinType, FeatureRegistration> oreFeatures = new EnumMap<>(VeinType.class);
	private final FeatureRegistration mineralVeins;
	private final List<FeatureRegistration> allFeatures = new ArrayList<>();

	public WorldGenerationProvider(DataGenerator dataGenerator, ExistingFileHelper existingFileHelper)
	{
		this.dataGenerator = dataGenerator;
		this.existingFileHelper = existingFileHelper;
		for(VeinType type : VeinType.VALUES)
		{
			final var typeReg = new FeatureRegistration(ImmersiveEngineering.rl(type.getVeinName()));
			oreFeatures.put(type, typeReg);
		}
		this.mineralVeins = new FeatureRegistration(ImmersiveEngineering.rl("mineral_veins"));

		final var registryBuilder = new RegistrySetBuilder();
		for(final var stub : ((RegSetBuilderAccess)DatagenRegistryAccess.getBUILDER()).getEntries())
		{
			if(stub.key().equals(Registries.CONFIGURED_FEATURE))
				stub.addTo(registryBuilder, this::bootstrapConfiguredFeatures);
			else if(stub.key().equals(Registries.PLACED_FEATURE))
				stub.addTo(registryBuilder, this::bootstrapPlacedFeatures);
			else
				stub.addTo(registryBuilder);
		}
		RegistryAccess.Frozen regAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
		this.ownLookupProvider = registryBuilder.build(regAccess);
	}

	private void bootstrapConfiguredFeatures(BootstapContext<ConfiguredFeature<?, ?>> ctx)
	{
		final var replaceDeepslate = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
		final var replaceStone = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
		for(final var entry : oreFeatures.entrySet())
		{
			final var type = entry.getKey();
			final var metal = entry.getKey().metal;
			List<TargetBlockState> targetList = ImmutableList.of(
					OreConfiguration.target(replaceStone, Metals.ORES.get(metal).defaultBlockState()),
					OreConfiguration.target(replaceDeepslate, Metals.DEEPSLATE_ORES.get(metal).defaultBlockState())
			);
			entry.getValue().registerConfigured(
					ctx, new ConfiguredFeature<>(IEWorldGen.IE_CONFIG_ORE.get(), new IEOreFeatureConfig(targetList, type))
			);
		}
		this.mineralVeins.registerConfigured(
				ctx, new ConfiguredFeature<>(IEWorldGen.MINERAL_VEIN_FEATURE.get(), new NoneFeatureConfiguration())
		);
	}

	private void bootstrapPlacedFeatures(BootstapContext<PlacedFeature> ctx)
	{
		for(final var entry : oreFeatures.entrySet())
		{
			final var type = entry.getKey();
			final var placements = List.of(
					HeightRangePlacement.of(new IEHeightProvider(type)),
					InSquarePlacement.spread(),
					new IECountPlacement(type)
			);
			entry.getValue().registerPlaced(ctx, placements);
		}
		this.mineralVeins.registerPlaced(ctx, List.of());
	}

	@Override
	public CompletableFuture<?> run(CachedOutput output)
	{
		final var providers = generate(this.ownLookupProvider);
		final var futures = providers.stream().map(jcp -> jcp.run(output)).toArray(CompletableFuture[]::new);
		return CompletableFuture.allOf(futures);
	}

	private List<JsonCodecProvider<?>> generate(Provider provider)
	{
		final RegistryOps<JsonElement> jsonOps = RegistryOps.create(JsonOps.INSTANCE, provider);
		ImmutableMap.Builder<ResourceLocation, BiomeModifier> modifiers = ImmutableMap.builder();
		ImmutableMap.Builder<ResourceLocation, PlacedFeature> placedFeatures = ImmutableMap.builder();
		ImmutableMap.Builder<ResourceLocation, ConfiguredFeature<?, ?>> configuredFeatures = ImmutableMap.builder();

		final RegistryLookup<Biome> biomeReg = provider.lookupOrThrow(Registries.BIOME);
		for(final var entry : allFeatures)
		{
			configuredFeatures.put(entry.name, entry.configured.value());
			placedFeatures.put(entry.name, entry.placed.value());
			modifiers.put(entry.name, new AddFeaturesBiomeModifier(
					HolderSet.emptyNamed(biomeReg, BiomeTags.IS_OVERWORLD),
					HolderSet.direct(entry.placed),
					Decoration.UNDERGROUND_ORES
			));
		}
		return List.of(
				makeProvider(jsonOps, Keys.BIOME_MODIFIERS, modifiers.build()),
				makeProvider(jsonOps, Registries.PLACED_FEATURE, placedFeatures.build()),
				makeProvider(jsonOps, Registries.CONFIGURED_FEATURE, configuredFeatures.build())
		);
	}

	private <T>
	JsonCodecProvider<T> makeProvider(
			RegistryOps<JsonElement> jsonOps, ResourceKey<Registry<T>> key, Map<ResourceLocation, T> entries
	)
	{
		return JsonCodecProvider.forDatapackRegistry(
				dataGenerator, existingFileHelper, Lib.MODID, jsonOps, key, entries
		);
	}

	@Override
	public String getName()
	{
		return "IE world generation";
	}

	private class FeatureRegistration
	{
		public Reference<ConfiguredFeature<?, ?>> configured;
		public Reference<PlacedFeature> placed;
		public final ResourceLocation name;

		private FeatureRegistration(ResourceLocation name)
		{
			this.name = name;
			allFeatures.add(this);
		}

		private void registerConfigured(
				BootstapContext<ConfiguredFeature<?, ?>> ctx, ConfiguredFeature<?, ?> configured
		)
		{
			this.configured = ctx.register(ResourceKey.create(Registries.CONFIGURED_FEATURE, this.name), configured);
		}

		private void registerPlaced(BootstapContext<PlacedFeature> ctx, List<PlacementModifier> placement)
		{
			this.placed = ctx.register(
					ResourceKey.create(Registries.PLACED_FEATURE, this.name), new PlacedFeature(configured, placement)
			);
		}
	}
}
