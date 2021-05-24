/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.IETags.MetalTags;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.utils.TagUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDecoration;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Misc;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.StoneDecoration;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.WoodenDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.blocks.wooden.TreatedWoodStyles;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.DyeColor;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

class IEBlockTags extends BlockTagsProvider
{

	public IEBlockTags(DataGenerator gen, ExistingFileHelper existing)
	{
		super(gen, Lib.MODID, existing);
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
		getOrCreateBuilder(IETags.colorlessSandstoneBlocks)
				.addItemEntry(Blocks.SANDSTONE)
				.addItemEntry(Blocks.CUT_SANDSTONE)
				.addItemEntry(Blocks.CHISELED_SANDSTONE)
				.addItemEntry(Blocks.SMOOTH_SANDSTONE);
		getOrCreateBuilder(IETags.redSandstoneBlocks)
				.addItemEntry(Blocks.RED_SANDSTONE)
				.addItemEntry(Blocks.CUT_RED_SANDSTONE)
				.addItemEntry(Blocks.CHISELED_RED_SANDSTONE)
				.addItemEntry(Blocks.SMOOTH_RED_SANDSTONE);
		for(Block b : MetalDecoration.metalLadder.values())
			getOrCreateBuilder(BlockTags.CLIMBABLE).addItemEntry(b);
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
		for(DyeColor dye : DyeColor.values())
			getOrCreateBuilder(IETags.sheetmetals).add(MetalDecoration.coloredSheetmetal.get(dye));
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
		getOrCreateBuilder(BlockTags.FLOWER_POTS)
				.addItemEntry(Misc.pottedHemp);

		/* MOD COMPAT STARTS HERE */

		// TConstruct
		getOrCreateBuilder(TagUtils.createBlockWrapper(new ResourceLocation("tconstruct:harvestable/stackable")))
				.addItemEntry(Misc.hempPlant);
	}
}
