/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.fx;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.fx.FluidSplashParticle.DataDeserializer;
import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = ImmersiveEngineering.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public class IEParticles
{
	public static final ParticleType<FluidSplashParticle.Data> FLUID_SPLASH = new ParticleType<FluidSplashParticle.Data>(
			false, new DataDeserializer())
	{
		@Override
		public Codec<FluidSplashParticle.Data> func_230522_e_()
		{
			return FluidSplashParticle.CODEC;
		}
	};
	public static final ParticleType<FractalParticle.Data> FRACTAL = new ParticleType<FractalParticle.Data>(
			false, new FractalParticle.DataDeserializer())
	{
		@Override
		public Codec<FractalParticle.Data> func_230522_e_()
		{
			return FractalParticle.CODEC;
		}
	};
	public static final BasicParticleType IE_BUBBLE = new BasicParticleType(false);
	public static final BasicParticleType SPARKS = new BasicParticleType(false);

	@SubscribeEvent
	public static void registerParticleTypes(RegistryEvent.Register<ParticleType<?>> evt)
	{
		FLUID_SPLASH.setRegistryName(ImmersiveEngineering.MODID, "fluid_splash");
		FRACTAL.setRegistryName(ImmersiveEngineering.MODID, "fractal");
		IE_BUBBLE.setRegistryName(ImmersiveEngineering.MODID, "ie_bubble");
		SPARKS.setRegistryName(ImmersiveEngineering.MODID, "sparks");
		evt.getRegistry().registerAll(FLUID_SPLASH, FRACTAL, IE_BUBBLE, SPARKS);
	}

	@SubscribeEvent
	public static void registerParticleFactories(ParticleFactoryRegisterEvent event)
	{
		ParticleManager manager = Minecraft.getInstance().particles;
		manager.registerFactory(IEParticles.FLUID_SPLASH, new FluidSplashParticle.Factory());
		manager.registerFactory(IEParticles.FRACTAL, new FractalParticle.Factory());
		manager.registerFactory(IEParticles.SPARKS, SparksParticle.Factory::new);
		manager.registerFactory(IEParticles.IE_BUBBLE, IEBubbleParticle.Factory::new);
	}
}
