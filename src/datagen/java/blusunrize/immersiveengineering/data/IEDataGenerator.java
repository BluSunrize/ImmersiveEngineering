/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.multiblocks.StaticTemplateManager;
import blusunrize.immersiveengineering.data.blockstates.BlockStates;
import blusunrize.immersiveengineering.data.blockstates.ConnectorBlockStates;
import blusunrize.immersiveengineering.data.blockstates.MultiblockStates;
import blusunrize.immersiveengineering.data.loot.AllLoot;
import blusunrize.immersiveengineering.data.manual.ManualDataGenerator;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@EventBusSubscriber(modid = ImmersiveEngineering.MODID, bus = Bus.MOD)
public class IEDataGenerator
{
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event)
	{
		ExistingFileHelper exHelper = event.getExistingFileHelper();
		StaticTemplateManager.EXISTING_HELPER = exHelper;
		DataGenerator gen = event.getGenerator();
		if(event.includeServer())
		{
			BlockTagsProvider blockTags = new IEBlockTags(gen, exHelper);
			gen.addProvider(true, blockTags);
			gen.addProvider(true, new IEItemTags(gen, blockTags, exHelper));
			gen.addProvider(true, new FluidTags(gen, exHelper));
			gen.addProvider(true, new BlockEntityTags(gen, exHelper));
			gen.addProvider(true, new BannerTags(gen, exHelper));
			gen.addProvider(true, new Recipes(gen));
			gen.addProvider(true, new AllLoot(gen));
			gen.addProvider(true, new BlockStates(gen, exHelper));
			MultiblockStates multiblocks = new MultiblockStates(gen, exHelper);
			gen.addProvider(true, multiblocks);
			gen.addProvider(true, new ConnectorBlockStates(gen, exHelper));
			gen.addProvider(true, new ItemModels(gen, exHelper, multiblocks));
			gen.addProvider(true, new Advancements(gen));
			gen.addProvider(true, new StructureUpdater("structures/multiblocks", Lib.MODID, exHelper, gen));
			gen.addProvider(true, new StructureUpdater("structures/village", Lib.MODID, exHelper, gen));
			gen.addProvider(true, new DynamicModels(multiblocks, gen, exHelper));
			ManualDataGenerator.addProviders(gen, exHelper);
			// Always keep this as the last provider!
			gen.addProvider(true, new RunCompleteHelper());
		}
	}
}
