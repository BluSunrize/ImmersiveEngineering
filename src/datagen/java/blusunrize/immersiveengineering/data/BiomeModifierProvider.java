package blusunrize.immersiveengineering.data;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.world.AddGlobalFeatureBiomeModifier;
import blusunrize.immersiveengineering.common.world.IEWorldGen;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.JsonCodecProvider;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers.AddFeaturesBiomeModifier;
import net.minecraftforge.registries.ForgeRegistries.Keys;

import java.util.function.Consumer;

public class BiomeModifierProvider
{
	public static void addTo(
			DataGenerator dataGenerator, ExistingFileHelper existingFileHelper, Consumer<DataProvider> add
	)
	{
		IEWorldGen.initLate();
		final RegistryAccess registryAccess = RegistryAccess.builtinCopy();
		final RegistryOps<JsonElement> jsonOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
		ImmutableMap.Builder<ResourceLocation, BiomeModifier> modifiers = ImmutableMap.builder();
		final Registry<Biome> biomeReg = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
		final Registry<PlacedFeature> featureReg = registryAccess.registryOrThrow(Registry.PLACED_FEATURE_REGISTRY);
		for(final String name : IEWorldGen.features.keySet())
		{
			final ResourceLocation nameRL = ImmersiveEngineering.rl(name);
			final ResourceKey<PlacedFeature> key = ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, nameRL);
			TagKey<Biome> biomeTag = IEWorldGen.features.get(name).getSecond();
			final Holder<PlacedFeature> featureHolder = featureReg.getHolderOrThrow(key);
			BiomeModifier modifier;
			if(biomeTag!=null)
				modifier = new AddFeaturesBiomeModifier(
						new Named<>(biomeReg, biomeTag), HolderSet.direct(featureHolder), Decoration.UNDERGROUND_ORES
				);
			else
				modifier = new AddGlobalFeatureBiomeModifier(featureHolder, Decoration.UNDERGROUND_ORES);
			modifiers.put(nameRL, modifier);
		}
		add.accept(JsonCodecProvider.forDatapackRegistry(
				dataGenerator, existingFileHelper, Lib.MODID, jsonOps, Keys.BIOME_MODIFIERS, modifiers.build()
		));
	}
}
