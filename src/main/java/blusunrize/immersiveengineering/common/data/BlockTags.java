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
import blusunrize.immersiveengineering.common.blocks.wooden.TreatedWoodStyles;
import net.minecraft.block.Block;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;

class BlockTags extends BlockTagsProvider
{

	public BlockTags(DataGenerator gen)
	{
		super(gen);
	}

	@Override
	protected void registerTags()
	{
		getBuilder(net.minecraft.tags.BlockTags.FENCES)
				.add(MetalDecoration.aluFence)
				.add(MetalDecoration.steelFence)
				.add(WoodenDecoration.treatedFence);
		getBuilder(net.minecraft.tags.BlockTags.WOODEN_FENCES)
				.add(WoodenDecoration.treatedFence);
		for(EnumMetals metal : EnumMetals.values())
		{
			MetalTags tags = IETags.getTagsFor(metal);
			if(!metal.isVanillaMetal())
			{
				Block storage = IEBlocks.Metals.storage.get(metal);
				getBuilder(tags.storage).add(storage);
				if(metal.shouldAddOre())
				{
					Block ore = IEBlocks.Metals.ores.get(metal);
					assert (tags.ore!=null&&ore!=null);
					getBuilder(tags.ore).add(ore);
				}
			}
			Block sheetmetal = IEBlocks.Metals.sheetmetal.get(metal);
			getBuilder(tags.sheetmetal).add(sheetmetal);
		}
		getBuilder(IETags.treatedWood).add(WoodenDecoration.treatedWood.get(TreatedWoodStyles.HORIZONTAL), WoodenDecoration.treatedWood.get(TreatedWoodStyles.VERTICAL), WoodenDecoration.treatedWood.get(TreatedWoodStyles.PACKAGED));
		getBuilder(IETags.coalCokeBlock).add(StoneDecoration.coke);
	}
}
