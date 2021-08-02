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
import blusunrize.immersiveengineering.common.blocks.metal.MetalLadderBlock;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.blocks.wooden.TreatedWoodStyles;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEBlocks.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

class IEBlockTags extends BlockTagsProvider
{

	public IEBlockTags(DataGenerator gen, ExistingFileHelper existing)
	{
		super(gen, Lib.MODID, existing);
	}

	@Override
	protected void addTags()
	{
		tag(BlockTags.FENCES)
				.add(MetalDecoration.aluFence.get())
				.add(MetalDecoration.steelFence.get())
				.add(WoodenDecoration.treatedFence.get());
		tag(BlockTags.WOODEN_FENCES)
				.add(WoodenDecoration.treatedFence.get());
		tag(IETags.fencesSteel)
				.add(MetalDecoration.steelFence.get());
		tag(IETags.fencesAlu)
				.add(MetalDecoration.aluFence.get());
		tag(IETags.clayBlock)
				.add(Blocks.CLAY);
		tag(IETags.glowstoneBlock)
				.add(Blocks.GLOWSTONE);
		tag(IETags.colorlessSandstoneBlocks)
				.add(Blocks.SANDSTONE)
				.add(Blocks.CUT_SANDSTONE)
				.add(Blocks.CHISELED_SANDSTONE)
				.add(Blocks.SMOOTH_SANDSTONE);
		tag(IETags.redSandstoneBlocks)
				.add(Blocks.RED_SANDSTONE)
				.add(Blocks.CUT_RED_SANDSTONE)
				.add(Blocks.CHISELED_RED_SANDSTONE)
				.add(Blocks.SMOOTH_RED_SANDSTONE);
		for(BlockEntry<MetalLadderBlock> b : MetalDecoration.metalLadder.values())
			tag(BlockTags.CLIMBABLE).add(b.get());
		for(EnumMetals metal : EnumMetals.values())
		{
			MetalTags tags = IETags.getTagsFor(metal);
			if(!metal.isVanillaMetal())
			{
				Block storage = IEBlocks.Metals.storage.get(metal).get();
				tag(tags.storage).add(storage);
				tag(Tags.Blocks.STORAGE_BLOCKS).add(storage);
				if(metal.shouldAddOre())
				{
					Block ore = IEBlocks.Metals.ores.get(metal).get();
					assert tags.ore!=null;
					tag(tags.ore).add(ore);
					tag(Tags.Blocks.ORES).add(ore);
				}
			}
			Block sheetmetal = IEBlocks.Metals.sheetmetal.get(metal).get();
			tag(tags.sheetmetal).add(sheetmetal);
			tag(IETags.sheetmetals).add(sheetmetal);
		}
		for(DyeColor dye : DyeColor.values())
			tag(IETags.sheetmetals).add(MetalDecoration.coloredSheetmetal.get(dye).get());
		for(TreatedWoodStyles style : TreatedWoodStyles.values())
		{
			tag(IETags.treatedWood).add(WoodenDecoration.treatedWood.get(style).get());
			tag(IETags.treatedWoodSlab).add(IEBlocks.toSlab.get(IEBlocks.WoodenDecoration.treatedWood.get(style).getId()).get());
		}
		for(MetalScaffoldingType t : MetalScaffoldingType.values())
		{
			tag(IETags.scaffoldingSteel).add(MetalDecoration.steelScaffolding.get(t).get());
			tag(IETags.scaffoldingAlu).add(MetalDecoration.aluScaffolding.get(t).get());
		}
		tag(IETags.coalCokeBlock)
				.add(StoneDecoration.coke.get());
		tag(BlockTags.FLOWER_POTS)
				.add(Misc.pottedHemp.get());

		/* MOD COMPAT STARTS HERE */

		// TConstruct
		tag(TagUtils.createBlockWrapper(new ResourceLocation("tconstruct:harvestable/stackable")))
				.add(Misc.hempPlant.get());
	}
}
