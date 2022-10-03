/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.register.IEFluids;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

@EventBusSubscriber(modid = ImmersiveEngineering.MODID, value = Dist.CLIENT, bus = Bus.MOD)
public class BlockRenderLayers
{
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent ev)
	{
		ItemBlockRenderTypes.setRenderLayer(IEFluids.POTION.get(), RenderType.translucent());
		for(RegistryObject<Fluid> f : IEFluids.REGISTER.getEntries())
			if(f.get()!=IEFluids.CONCRETE.getFlowing()&&f.get()!=IEFluids.CONCRETE.getStill())
				ItemBlockRenderTypes.setRenderLayer(f.get(), RenderType.translucent());
	}
}
