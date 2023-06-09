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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class IEParticles
{
	public static final DeferredRegister<ParticleType<?>> REGISTER = DeferredRegister.create(
			ForgeRegistries.PARTICLE_TYPES, Lib.MODID
	);

	public static final RegistryObject<ParticleType<FluidSplashOptions>> FLUID_SPLASH = REGISTER.register(
			"fluid_splash", () -> new IEParticleType<>(false, new FluidSplashOptions.DataDeserializer(), FluidSplashOptions.CODEC)
	);
	public static final RegistryObject<ParticleType<FractalOptions>> FRACTAL = REGISTER.register(
			"fractal", () -> new IEParticleType<>(false, new FractalOptions.DataDeserializer(), FractalOptions.CODEC)
	);
	public static final RegistryObject<SimpleParticleType> IE_BUBBLE = REGISTER.register(
			"ie_bubble", () -> new SimpleParticleType(false)
	);
	public static final RegistryObject<SimpleParticleType> SPARKS = REGISTER.register(
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
