package blusunrize.immersiveengineering.data;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.world.IEWorldGen;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderSet;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
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
		final Registry<Biome> biomeReg = registryAccess.registryOrThrow(Registries.BIOME);
		final Registry<PlacedFeature> featureReg = registryAccess.registryOrThrow(Registries.PLACED_FEATURE);
		for(final String name : IEWorldGen.features.keySet())
		{
			final ResourceLocation nameRL = ImmersiveEngineering.rl(name);
			final ResourceKey<PlacedFeature> key = ResourceKey.create(Registries.PLACED_FEATURE, nameRL);
			modifiers.put(nameRL, new AddFeaturesBiomeModifier(
					new Named<>(biomeReg, BiomeTags.IS_OVERWORLD),
					HolderSet.direct(featureReg.getHolderOrThrow(key)),
					Decoration.UNDERGROUND_ORES
			));
		}
		add.accept(JsonCodecProvider.forDatapackRegistry(
				dataGenerator, existingFileHelper, Lib.MODID, jsonOps, Keys.BIOME_MODIFIERS, modifiers.build()
		));
	}
}
