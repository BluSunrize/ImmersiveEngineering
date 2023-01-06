/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.config.IEServerConfig.Ores.VeinType;
import blusunrize.immersiveengineering.common.register.IEBlocks.Metals;
import blusunrize.immersiveengineering.common.world.IECountPlacement;
import blusunrize.immersiveengineering.common.world.IEHeightProvider;
import blusunrize.immersiveengineering.common.world.IEOreFeature.IEOreFeatureConfig;
import blusunrize.immersiveengineering.common.world.IEWorldGen;
import com.google.common.collect.ImmutableList;
import net.minecraft.Util;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstapContext;
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
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers.AddFeaturesBiomeModifier;
import net.minecraftforge.registries.ForgeRegistries.Keys;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

public class WorldGenerationProvider
{
	public static DataProvider makeProvider(
			PackOutput output, CompletableFuture<HolderLookup.Provider> vanillaRegistries
	)
	{
		final Map<VeinType, FeatureRegistration> oreFeatures = new EnumMap<>(VeinType.class);
		for(VeinType type : VeinType.VALUES)
		{
			final FeatureRegistration typeReg = new FeatureRegistration(ImmersiveEngineering.rl(type.getVeinName()));
			oreFeatures.put(type, typeReg);
		}
		final Registrations registrations = new Registrations(
				oreFeatures, new FeatureRegistration(ImmersiveEngineering.rl("mineral_veins"))
		);
		final RegistrySetBuilder registryBuilder = new RegistrySetBuilder();
		registryBuilder.add(Registries.CONFIGURED_FEATURE, ctx -> bootstrapConfiguredFeatures(ctx, registrations));
		registryBuilder.add(Registries.PLACED_FEATURE, ctx -> bootstrapPlacedFeatures(ctx, registrations));
		registryBuilder.add(Keys.BIOME_MODIFIERS, ctx -> bootstrapBiomeModifiers(ctx, registrations));
		return new DatapackBuiltinEntriesProvider(output, vanillaRegistries, registryBuilder, Set.of(Lib.MODID));
	}

	private static void bootstrapConfiguredFeatures(
			BootstapContext<ConfiguredFeature<?, ?>> ctx, Registrations registrations
	)
	{
		final TagMatchTest replaceDeepslate = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
		final TagMatchTest replaceStone = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
		for(final Entry<VeinType, FeatureRegistration> entry : registrations.oreFeatures.entrySet())
		{
			final VeinType type = entry.getKey();
			final EnumMetals metal = entry.getKey().metal;
			List<TargetBlockState> targetList = ImmutableList.of(
					OreConfiguration.target(replaceStone, Metals.ORES.get(metal).defaultBlockState()),
					OreConfiguration.target(replaceDeepslate, Metals.DEEPSLATE_ORES.get(metal).defaultBlockState())
			);
			entry.getValue().registerConfigured(
					ctx, new ConfiguredFeature<>(IEWorldGen.IE_CONFIG_ORE.get(), new IEOreFeatureConfig(targetList, type))
			);
		}
		registrations.mineralVeins.registerConfigured(
				ctx, new ConfiguredFeature<>(IEWorldGen.MINERAL_VEIN_FEATURE.get(), new NoneFeatureConfiguration())
		);
	}

	private static void bootstrapPlacedFeatures(BootstapContext<PlacedFeature> ctx, Registrations registrations)
	{
		for(final Entry<VeinType, FeatureRegistration> entry : registrations.oreFeatures.entrySet())
		{
			final VeinType type = entry.getKey();
			final List<PlacementModifier> placements = List.of(
					HeightRangePlacement.of(new IEHeightProvider(type)),
					InSquarePlacement.spread(),
					new IECountPlacement(type)
			);
			entry.getValue().registerPlaced(ctx, placements);
		}
		registrations.mineralVeins.registerPlaced(ctx, List.of());
	}

	private static void bootstrapBiomeModifiers(BootstapContext<BiomeModifier> ctx, Registrations registrations)
	{
		final HolderGetter<Biome> biomeReg = ctx.lookup(Registries.BIOME);
		for(final FeatureRegistration entry : registrations.allFeatures)
			ctx.register(
					ResourceKey.create(Keys.BIOME_MODIFIERS, entry.name),
					new AddFeaturesBiomeModifier(
							biomeReg.getOrThrow(BiomeTags.IS_OVERWORLD),
							HolderSet.direct(entry.placed),
							Decoration.UNDERGROUND_ORES
					)
			);
	}

	private static class FeatureRegistration
	{
		public Reference<ConfiguredFeature<?, ?>> configured;
		public Reference<PlacedFeature> placed;
		public final ResourceLocation name;

		private FeatureRegistration(ResourceLocation name)
		{
			this.name = name;
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

	private record Registrations(
			List<FeatureRegistration> allFeatures,
			Map<VeinType, FeatureRegistration> oreFeatures,
			FeatureRegistration mineralVeins
	)
	{
		public Registrations(
				Map<VeinType, FeatureRegistration> oreFeatures, FeatureRegistration mineralVeins
		)
		{
			this(
					Util.make(new ArrayList<>(oreFeatures.values()), l -> l.add(mineralVeins)),
					oreFeatures,
					mineralVeins
			);
		}
	}
}
