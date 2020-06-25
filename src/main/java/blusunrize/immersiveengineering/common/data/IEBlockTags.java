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
		getBuilder(BlockTags.FENCES)
				.add(MetalDecoration.aluFence)
				.add(MetalDecoration.steelFence)
				.add(WoodenDecoration.treatedFence);
		getBuilder(BlockTags.WOODEN_FENCES)
				.add(WoodenDecoration.treatedFence);
		getBuilder(IETags.clayBlock)
				.add(Blocks.CLAY);
		getBuilder(IETags.glowstoneBlock)
				.add(Blocks.GLOWSTONE);
		for(EnumMetals metal : EnumMetals.values())
		{
			MetalTags tags = IETags.getTagsFor(metal);
			if(!metal.isVanillaMetal())
			{
				Block storage = IEBlocks.Metals.storage.get(metal);
				getBuilder(tags.storage).add(storage);
				getBuilder(Tags.Blocks.STORAGE_BLOCKS).add(storage);
				if(metal.shouldAddOre())
				{
					Block ore = IEBlocks.Metals.ores.get(metal);
					assert (tags.ore!=null&&ore!=null);
					getBuilder(tags.ore).add(ore);
					getBuilder(Tags.Blocks.ORES).add(ore);
				}
			}
			Block sheetmetal = IEBlocks.Metals.sheetmetal.get(metal);
			getBuilder(tags.sheetmetal).add(sheetmetal);
			getBuilder(IETags.sheetmetals).add(sheetmetal);
		}
		for(TreatedWoodStyles style : TreatedWoodStyles.values())
		{
			getBuilder(IETags.treatedWood).add(WoodenDecoration.treatedWood.get(style));
			getBuilder(IETags.treatedWoodSlab).add(IEBlocks.toSlab.get(IEBlocks.WoodenDecoration.treatedWood.get(style)));
		}
		for(MetalScaffoldingType t : MetalScaffoldingType.values())
		{
			getBuilder(IETags.scaffoldingSteel).add(MetalDecoration.steelScaffolding.get(t));
			getBuilder(IETags.scaffoldingAlu).add(MetalDecoration.aluScaffolding.get(t));
		}
		getBuilder(IETags.coalCokeBlock)
				.add(StoneDecoration.coke);
	}
}
