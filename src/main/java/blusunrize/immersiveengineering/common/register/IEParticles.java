/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.fx.*;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class IEParticles
{
	public static final DeferredRegister<ParticleType<?>> REGISTER = DeferredRegister.create(
			BuiltInRegistries.PARTICLE_TYPE, Lib.MODID
	);

	public static final DeferredHolder<ParticleType<?>, ParticleType<FluidSplashOptions>> FLUID_SPLASH = REGISTER.register(
			"fluid_splash", () -> new IEParticleType<>(false, FluidSplashOptions.CODEC, FluidSplashOptions.STREAM_CODEC)
	);
	public static final DeferredHolder<ParticleType<?>, ParticleType<FractalOptions>> FRACTAL = REGISTER.register(
			"fractal", () -> new IEParticleType<>(false, FractalOptions.CODEC, FractalOptions.STREAM_CODEC)
	);
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> IE_BUBBLE = REGISTER.register(
			"ie_bubble", () -> new SimpleParticleType(false)
	);
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SPARKS = REGISTER.register(
			"sparks", () -> new SimpleParticleType(false)
	);

	@EventBusSubscriber(modid = ImmersiveEngineering.MODID, bus = Bus.MOD, value = Dist.CLIENT)
	private static class Client
	{
		@SubscribeEvent
		public static void registerParticleFactories(RegisterParticleProvidersEvent event)
		{
			event.registerSprite(IEParticles.FLUID_SPLASH.get(), new FluidSplashParticle.Factory());
			event.registerSpecial(IEParticles.FRACTAL.get(), new FractalParticle.Factory());
			event.registerSpriteSet(IEParticles.SPARKS.get(), SparksParticle.Factory::new);
			event.registerSpriteSet(IEParticles.IE_BUBBLE.get(), IEBubbleParticle.Factory::new);
		}
	}
}
