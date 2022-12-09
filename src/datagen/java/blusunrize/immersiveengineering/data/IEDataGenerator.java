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
import blusunrize.immersiveengineering.data.tags.*;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = ImmersiveEngineering.MODID, bus = Bus.MOD)
public class IEDataGenerator
{
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event)
	{
		ExistingFileHelper exHelper = event.getExistingFileHelper();
		StaticTemplateManager.EXISTING_HELPER = exHelper;
		DataGenerator gen = event.getGenerator();
		final var output = gen.getPackOutput();
		final var lookup = event.getLookupProvider();
		if(event.includeServer())
		{
			BlockTagsProvider blockTags = new IEBlockTags(output, lookup, exHelper);
			gen.addProvider(true, blockTags);
			gen.addProvider(true, new IEItemTags(output, lookup, blockTags, exHelper));
			gen.addProvider(true, new FluidTags(output, lookup, exHelper));
			gen.addProvider(true, new BlockEntityTags(output, lookup, exHelper));
			gen.addProvider(true, new BannerTags(output, lookup, exHelper));
			gen.addProvider(true, new PoiTags(output, lookup, exHelper));
			gen.addProvider(true, new EntityTypeTags(output, lookup, exHelper));
			gen.addProvider(true, new Recipes(output));
			gen.addProvider(true, new AllLoot(output));
			gen.addProvider(true, new BlockStates(gen, exHelper));
			MultiblockStates multiblocks = new MultiblockStates(gen, exHelper);
			gen.addProvider(true, multiblocks);
			gen.addProvider(true, new ConnectorBlockStates(gen, exHelper));
			gen.addProvider(true, new ItemModels(gen, exHelper, multiblocks));
			gen.addProvider(true, new Advancements(output, lookup, exHelper));
			gen.addProvider(true, new StructureUpdater("structures/multiblocks", Lib.MODID, exHelper, gen));
			gen.addProvider(true, new StructureUpdater("structures/village", Lib.MODID, exHelper, gen));
			gen.addProvider(true, new DynamicModels(multiblocks, gen, exHelper));
			BiomeModifierProvider.addTo(gen, exHelper, d -> gen.addProvider(true, d));
			ManualDataGenerator.addProviders(gen, exHelper);
			// TODO find a new way to detect crashes in datagen
		}
	}
}
