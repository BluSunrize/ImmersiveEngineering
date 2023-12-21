/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.world;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class IEWorldGen
{
	// TODO make retrogen happen again
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
}
