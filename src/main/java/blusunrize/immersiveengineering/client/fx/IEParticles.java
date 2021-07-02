/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.fx;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.fx.FluidSplashParticle.DataDeserializer;
import blusunrize.immersiveengineering.client.fx.FractalParticle.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber(modid = ImmersiveEngineering.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public class IEParticles
{
	public static final DeferredRegister<ParticleType<?>> REGISTER = DeferredRegister.create(
			ForgeRegistries.PARTICLE_TYPES, Lib.MODID
	);

	public static final RegistryObject<ParticleType<FluidSplashParticle.Data>> FLUID_SPLASH = REGISTER.register(
			"fluid_splash", () -> new IEParticleType<>(false, new DataDeserializer(), FluidSplashParticle.CODEC)
	);
	public static final RegistryObject<ParticleType<Data>> FRACTAL = REGISTER.register(
			"fractal", () -> new IEParticleType<>(false, new FractalParticle.DataDeserializer(), FractalParticle.CODEC)
	);
	public static final RegistryObject<BasicParticleType> IE_BUBBLE = REGISTER.register(
			"ie_bubble", () -> new BasicParticleType(false)
	);
	public static final RegistryObject<BasicParticleType> SPARKS = REGISTER.register(
			"sparks", () -> new BasicParticleType(false)
	);

	@SubscribeEvent
	public static void registerParticleFactories(ParticleFactoryRegisterEvent event)
	{
		ParticleManager manager = Minecraft.getInstance().particles;
		manager.registerFactory(IEParticles.FLUID_SPLASH.get(), new FluidSplashParticle.Factory());
		manager.registerFactory(IEParticles.FRACTAL.get(), new FractalParticle.Factory());
		manager.registerFactory(IEParticles.SPARKS.get(), SparksParticle.Factory::new);
		manager.registerFactory(IEParticles.IE_BUBBLE.get(), IEBubbleParticle.Factory::new);
	}
}
