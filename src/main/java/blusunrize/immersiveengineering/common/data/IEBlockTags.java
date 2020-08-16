/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.IETags.MetalTags;
import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDecoration;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.StoneDecoration;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.WoodenDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.blocks.wooden.TreatedWoodStyles;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.Tags;

class IEBlockTags extends BlockTagsProvider
{

	public IEBlockTags(DataGenerator gen)
	{
		super(gen);
	}

	@Override
	protected void registerTags()
	{
		getOrCreateBuilder(BlockTags.FENCES)
				.addItemEntry(MetalDecoration.aluFence)
				.addItemEntry(MetalDecoration.steelFence)
				.addItemEntry(WoodenDecoration.treatedFence);
		getOrCreateBuilder(BlockTags.WOODEN_FENCES)
				.addItemEntry(WoodenDecoration.treatedFence);
		getOrCreateBuilder(IETags.clayBlock)
				.addItemEntry(Blocks.CLAY);
		getOrCreateBuilder(IETags.glowstoneBlock)
				.addItemEntry(Blocks.GLOWSTONE);
		for(EnumMetals metal : EnumMetals.values())
		{
			MetalTags tags = IETags.getTagsFor(metal);
			if(!metal.isVanillaMetal())
			{
				Block storage = IEBlocks.Metals.storage.get(metal);
				getOrCreateBuilder(tags.storage).addItemEntry(storage);
				getOrCreateBuilder(Tags.Blocks.STORAGE_BLOCKS).addItemEntry(storage);
				if(metal.shouldAddOre())
				{
					Block ore = IEBlocks.Metals.ores.get(metal);
					assert (tags.ore!=null&&ore!=null);
					getOrCreateBuilder(tags.ore).addItemEntry(ore);
					getOrCreateBuilder(Tags.Blocks.ORES).addItemEntry(ore);
				}
			}
			Block sheetmetal = IEBlocks.Metals.sheetmetal.get(metal);
			getOrCreateBuilder(tags.sheetmetal).addItemEntry(sheetmetal);
			getOrCreateBuilder(IETags.sheetmetals).addItemEntry(sheetmetal);
		}
		for(TreatedWoodStyles style : TreatedWoodStyles.values())
		{
			getOrCreateBuilder(IETags.treatedWood).addItemEntry(WoodenDecoration.treatedWood.get(style));
			getOrCreateBuilder(IETags.treatedWoodSlab).addItemEntry(IEBlocks.toSlab.get(IEBlocks.WoodenDecoration.treatedWood.get(style)));
		}
		for(MetalScaffoldingType t : MetalScaffoldingType.values())
		{
			getOrCreateBuilder(IETags.scaffoldingSteel).addItemEntry(MetalDecoration.steelScaffolding.get(t));
			getOrCreateBuilder(IETags.scaffoldingAlu).addItemEntry(MetalDecoration.aluScaffolding.get(t));
		}
		getOrCreateBuilder(IETags.coalCokeBlock)
				.addItemEntry(StoneDecoration.coke);
	}
}
