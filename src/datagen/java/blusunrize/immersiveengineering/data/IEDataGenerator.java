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
import blusunrize.immersiveengineering.api.wires.redstone.CapabilityRedstoneNetwork;
import blusunrize.immersiveengineering.common.blocks.multiblocks.StaticTemplateManager;
import blusunrize.immersiveengineering.data.blockstates.BlockStates;
import blusunrize.immersiveengineering.data.blockstates.ConnectorBlockStates;
import blusunrize.immersiveengineering.data.blockstates.MultiblockStates;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.items.CapabilityItemHandler;

@EventBusSubscriber(modid = ImmersiveEngineering.MODID, bus = Bus.MOD)
public class IEDataGenerator
{
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event)
	{
		// Capabilities are not registered yet, but cap references throw on null caps
		CapabilityItemHandler.register();
		CapabilityFluidHandler.register();
		CapabilityEnergy.register();
		CapabilityRedstoneNetwork.register();

		ExistingFileHelper exHelper = event.getExistingFileHelper();
		StaticTemplateManager.EXISTING_HELPER = exHelper;
		DataGenerator gen = event.getGenerator();
		if(event.includeServer())
		{
			BlockTagsProvider blockTags = new IEBlockTags(gen, exHelper);
			gen.addProvider(blockTags);
			gen.addProvider(new ItemTags(gen, blockTags, exHelper));
			gen.addProvider(new FluidTags(gen, exHelper));
			gen.addProvider(new TileTags(gen, exHelper));
			gen.addProvider(new Recipes(gen));
			gen.addProvider(new BlockLoot(gen));
			gen.addProvider(new GeneralLoot(gen));
			gen.addProvider(new BlockStates(gen, exHelper));
			MultiblockStates blockStates = new MultiblockStates(gen, exHelper);
			gen.addProvider(blockStates);
			gen.addProvider(new ConnectorBlockStates(gen, exHelper));
			gen.addProvider(new ItemModels(gen, exHelper, blockStates));
			gen.addProvider(new Advancements(gen));
			gen.addProvider(new StructureUpdater("structures/multiblocks", Lib.MODID, exHelper, gen));
			gen.addProvider(new StructureUpdater("structures/village", Lib.MODID, exHelper, gen));
			// Always keep this as the last provider!
			gen.addProvider(new RunCompleteHelper());
		}
	}
}