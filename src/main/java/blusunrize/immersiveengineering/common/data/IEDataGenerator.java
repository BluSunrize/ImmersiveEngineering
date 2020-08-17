/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IERecipes;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@EventBusSubscriber(modid = ImmersiveEngineering.MODID, bus = Bus.MOD)
public class IEDataGenerator
{

	@SubscribeEvent
	public static void gatherData(GatherDataEvent event)
	{
		DataGenerator gen = event.getGenerator();
		if(event.includeServer())
		{
			gen.addProvider(new Recipes(gen));
			gen.addProvider(new IEBlockTags(gen));
			gen.addProvider(new ItemTags(gen));
			gen.addProvider(new FluidTags(gen));
			gen.addProvider(new BlockLoot(gen));
			gen.addProvider(new GeneralLoot(gen));
			LoadedModels loadedModels = new LoadedModels(gen, event.getExistingFileHelper());
			BlockStates blockStates = new BlockStates(gen, event.getExistingFileHelper(), loadedModels);
			gen.addProvider(blockStates);
			gen.addProvider(loadedModels);
			gen.addProvider(new ItemModels(gen, event.getExistingFileHelper(), blockStates));
			gen.addProvider(new Advancements(gen));
		}
	}

	public static ResourceLocation rl(String path)
	{
		return new ResourceLocation(ImmersiveEngineering.MODID, path);
	}
}