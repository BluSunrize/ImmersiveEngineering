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
		func_240522_a_(BlockTags.FENCES)
				.func_240532_a_(MetalDecoration.aluFence)
				.func_240532_a_(MetalDecoration.steelFence)
				.func_240532_a_(WoodenDecoration.treatedFence);
		func_240522_a_(BlockTags.WOODEN_FENCES)
				.func_240532_a_(WoodenDecoration.treatedFence);
		func_240522_a_(IETags.clayBlock)
				.func_240532_a_(Blocks.CLAY);
		func_240522_a_(IETags.glowstoneBlock)
				.func_240532_a_(Blocks.GLOWSTONE);
		for(EnumMetals metal : EnumMetals.values())
		{
			MetalTags tags = IETags.getTagsFor(metal);
			if(!metal.isVanillaMetal())
			{
				Block storage = IEBlocks.Metals.storage.get(metal);
				func_240522_a_(tags.storage).func_240532_a_(storage);
				func_240522_a_(Tags.Blocks.STORAGE_BLOCKS).func_240532_a_(storage);
				if(metal.shouldAddOre())
				{
					Block ore = IEBlocks.Metals.ores.get(metal);
					assert (tags.ore!=null&&ore!=null);
					func_240522_a_(tags.ore).func_240532_a_(ore);
					func_240522_a_(Tags.Blocks.ORES).func_240532_a_(ore);
				}
			}
			Block sheetmetal = IEBlocks.Metals.sheetmetal.get(metal);
			func_240522_a_(tags.sheetmetal).func_240532_a_(sheetmetal);
			func_240522_a_(IETags.sheetmetals).func_240532_a_(sheetmetal);
		}
		for(TreatedWoodStyles style : TreatedWoodStyles.values())
		{
			func_240522_a_(IETags.treatedWood).func_240532_a_(WoodenDecoration.treatedWood.get(style));
			func_240522_a_(IETags.treatedWoodSlab).func_240532_a_(IEBlocks.toSlab.get(IEBlocks.WoodenDecoration.treatedWood.get(style)));
		}
		for(MetalScaffoldingType t : MetalScaffoldingType.values())
		{
			func_240522_a_(IETags.scaffoldingSteel).func_240532_a_(MetalDecoration.steelScaffolding.get(t));
			func_240522_a_(IETags.scaffoldingAlu).func_240532_a_(MetalDecoration.aluScaffolding.get(t));
		}
		func_240522_a_(IETags.coalCokeBlock)
				.func_240532_a_(StoneDecoration.coke);
	}
}
