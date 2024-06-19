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
import blusunrize.immersiveengineering.data.blockstates.BlockStates;
import blusunrize.immersiveengineering.data.blockstates.ConnectorBlockStates;
import blusunrize.immersiveengineering.data.blockstates.MultiblockStates;
import blusunrize.immersiveengineering.data.loot.AllLoot;
import blusunrize.immersiveengineering.data.loot.LootModifierProvider;
import blusunrize.immersiveengineering.data.manual.ManualDataGenerator;
import blusunrize.immersiveengineering.data.recipes.RootRecipeProvider;
import blusunrize.immersiveengineering.data.tags.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = ImmersiveEngineering.MODID, bus = Bus.MOD)
public class IEDataGenerator
{
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event)
	{
		ExistingFileHelper exHelper = event.getExistingFileHelper();
		DataGenerator gen = event.getGenerator();
		final var output = gen.getPackOutput();
		final var lookup = event.getLookupProvider();
		if(event.includeServer())
		{
			BlockTagsProvider blockTags = new IEBlockTags(output, lookup, exHelper);
			gen.addProvider(true, blockTags);
			gen.addProvider(true, new IEItemTags(output, lookup, blockTags.contentsGetter(), exHelper));
			gen.addProvider(true, new FluidTags(output, lookup, exHelper));
			gen.addProvider(true, new BlockEntityTags(output, lookup, exHelper));
			gen.addProvider(true, new BannerTags(output, lookup, exHelper));
			gen.addProvider(true, new PoiTags(output, lookup, exHelper));
			gen.addProvider(true, new EntityTypeTags(output, lookup, exHelper));
			gen.addProvider(true, new BiomeTags(output, lookup, exHelper));
			gen.addProvider(true, new RootRecipeProvider(output));
			gen.addProvider(true, new AllLoot(output));
			gen.addProvider(true, new BlockStates(output, exHelper));
			MultiblockStates multiblocks = new MultiblockStates(output, exHelper);
			gen.addProvider(true, multiblocks);
			gen.addProvider(true, new ConnectorBlockStates(output, exHelper));
			gen.addProvider(true, new ItemModels(output, exHelper, multiblocks));
			gen.addProvider(true, new Advancements(output, lookup, exHelper));
			gen.addProvider(true, new StructureUpdater("structures/multiblocks", Lib.MODID, exHelper, output));
			gen.addProvider(true, new StructureUpdater("structures/village", Lib.MODID, exHelper, output));
			gen.addProvider(true, new DynamicModels(multiblocks, output, exHelper));
			gen.addProvider(true, new LootModifierProvider(output));
			gen.addProvider(true, new Language(output, exHelper, "en_us"));
			for(final DataProvider provider : WorldGenerationProvider.makeProviders(output, lookup, exHelper))
				gen.addProvider(true, provider);
			ManualDataGenerator.addProviders(gen, exHelper);
		}
	}
}
