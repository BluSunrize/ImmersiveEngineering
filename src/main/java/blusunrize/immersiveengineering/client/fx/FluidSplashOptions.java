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
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

public record FluidSplashOptions(Fluid fluid) implements ParticleOptions
{
	public static final Codec<FluidSplashOptions> CODEC = ResourceLocation.CODEC.xmap(
			FluidSplashOptions::new, d -> BuiltInRegistries.FLUID.getKey(d.fluid)
	);

	public FluidSplashOptions(ResourceLocation name)
	{
		this(ForgeRegistries.FLUIDS.getValue(name));
	}

	@Nonnull
	@Override
	public ParticleType<?> getType()
	{
		return IEParticles.FLUID_SPLASH.get();
	}

	@Override
	public void writeToNetwork(FriendlyByteBuf buffer)
	{
		buffer.writeResourceLocation(BuiltInRegistries.FLUID.getKey(fluid));
	}

	@Nonnull
	@Override
	public String writeToString()
	{
		return BuiltInRegistries.FLUID.getKey(fluid).toString();
	}

	public static class DataDeserializer implements Deserializer<FluidSplashOptions>
	{
		@Nonnull
		@Override
		public FluidSplashOptions fromCommand(@Nonnull ParticleType<FluidSplashOptions> particleTypeIn, StringReader reader) throws CommandSyntaxException
		{
			String name = reader.getString();
			return new FluidSplashOptions(new ResourceLocation(name));
		}

		@Nonnull
		@Override
		public FluidSplashOptions fromNetwork(@Nonnull ParticleType<FluidSplashOptions> particleTypeIn, FriendlyByteBuf buffer)
		{
			return new FluidSplashOptions(buffer.readResourceLocation());
		}
	}
}
