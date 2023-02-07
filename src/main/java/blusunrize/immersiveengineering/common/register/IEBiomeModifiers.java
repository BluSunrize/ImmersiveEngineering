/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.world.AddGlobalFeatureBiomeModifier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries.Keys;
import net.minecraftforge.registries.RegistryObject;

public class IEBiomeModifiers
{
	public static final DeferredRegister<Codec<? extends BiomeModifier>> REGISTER = DeferredRegister.create(
			Keys.BIOME_MODIFIER_SERIALIZERS, Lib.MODID
	);

	public static final RegistryObject<Codec<AddGlobalFeatureBiomeModifier>> ADD_GLOBAL = REGISTER.register(
			"add_global_modifier",
			() -> RecordCodecBuilder.create(inst -> inst.group(
					PlacedFeature.CODEC.fieldOf("feature").forGetter(AddGlobalFeatureBiomeModifier::feature),
					Decoration.CODEC.fieldOf("step").forGetter(AddGlobalFeatureBiomeModifier::step)
			).apply(inst, AddGlobalFeatureBiomeModifier::new))
	);
}
