/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@EventBusSubscriber(modid = ImmersiveEngineering.MODID, bus = Bus.MOD)
public class IEDataGenerator
{
	public static ExistingFileHelper EXISTING_HELPER = null;

	@SubscribeEvent
	public static void gatherData(GatherDataEvent event)
	{
		EXISTING_HELPER = event.getExistingFileHelper();
		DataGenerator gen = event.getGenerator();
		if(event.includeServer())
		{
			gen.addProvider(new Recipes(gen));
			BlockTagsProvider blockTags = new IEBlockTags(gen, EXISTING_HELPER);
			gen.addProvider(blockTags);
			gen.addProvider(new ItemTags(gen, blockTags, EXISTING_HELPER));
			gen.addProvider(new FluidTags(gen, EXISTING_HELPER));
			gen.addProvider(new BlockLoot(gen));
			gen.addProvider(new GeneralLoot(gen));
			BlockStates blockStates = new BlockStates(gen, EXISTING_HELPER);
			gen.addProvider(blockStates);
			gen.addProvider(new ItemModels(gen, EXISTING_HELPER, blockStates));
			gen.addProvider(new Advancements(gen));
			gen.addProvider(new TileTags(gen, EXISTING_HELPER));
			// Always keep this as the last provider!
			gen.addProvider(new RunCompleteHelper());
		}
	}

	public static ResourceLocation rl(String path)
	{
		return new ResourceLocation(ImmersiveEngineering.MODID, path);
	}
}