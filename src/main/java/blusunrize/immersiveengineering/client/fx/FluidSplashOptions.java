/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.fx;


import blusunrize.immersiveengineering.common.register.IEParticles;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nonnull;

public record FluidSplashOptions(Fluid fluid) implements ParticleOptions
{
	public static final Codec<FluidSplashOptions> CODEC = BuiltInRegistries.FLUID.byNameCodec()
			.xmap(FluidSplashOptions::new, FluidSplashOptions::fluid);
	public static final StreamCodec<RegistryFriendlyByteBuf, FluidSplashOptions> STREAM_CODEC = ByteBufCodecs.registry(Registries.FLUID)
			.map(FluidSplashOptions::new, FluidSplashOptions::fluid);

	@Nonnull
	@Override
	public ParticleType<?> getType()
	{
		return IEParticles.FLUID_SPLASH.get();
	}
}
