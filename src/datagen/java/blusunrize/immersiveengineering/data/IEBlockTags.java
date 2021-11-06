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
import blusunrize.immersiveengineering.common.blocks.IEBlocks.*;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.blocks.wooden.TreatedWoodStyles;
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
				.add(MetalDecoration.aluFence)
				.add(MetalDecoration.steelFence)
				.add(WoodenDecoration.treatedFence);
		tag(BlockTags.WOODEN_FENCES)
				.add(WoodenDecoration.treatedFence);
		tag(IETags.fencesSteel)
				.add(MetalDecoration.steelFence);
		tag(IETags.fencesAlu)
				.add(MetalDecoration.aluFence);
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
		for(Block b : MetalDecoration.metalLadder.values())
			tag(BlockTags.CLIMBABLE).add(b);
		for(EnumMetals metal : EnumMetals.values())
		{
			MetalTags tags = IETags.getTagsFor(metal);
			if(!metal.isVanillaMetal())
			{
				Block storage = IEBlocks.Metals.storage.get(metal);
				tag(tags.storage).add(storage);
				tag(Tags.Blocks.STORAGE_BLOCKS).add(storage);
				if(metal.shouldAddOre())
				{
					Block ore = IEBlocks.Metals.ores.get(metal);
					assert (tags.ore!=null&&ore!=null);
					tag(tags.ore).add(ore);
					tag(Tags.Blocks.ORES).add(ore);
				}
			}
			Block sheetmetal = IEBlocks.Metals.sheetmetal.get(metal);
			tag(tags.sheetmetal).add(sheetmetal);
			tag(IETags.sheetmetals).add(sheetmetal);
		}
		for(DyeColor dye : DyeColor.values())
			tag(IETags.sheetmetals).add(MetalDecoration.coloredSheetmetal.get(dye));
		for(TreatedWoodStyles style : TreatedWoodStyles.values())
		{
			tag(IETags.treatedWood).add(WoodenDecoration.treatedWood.get(style));
			tag(IETags.treatedWoodSlab).add(IEBlocks.toSlab.get(IEBlocks.WoodenDecoration.treatedWood.get(style)));
		}
		for(MetalScaffoldingType t : MetalScaffoldingType.values())
		{
			tag(IETags.scaffoldingSteel).add(MetalDecoration.steelScaffolding.get(t));
			tag(IETags.scaffoldingAlu).add(MetalDecoration.aluScaffolding.get(t));
		}
		tag(IETags.coalCokeBlock)
				.add(StoneDecoration.coke);
		tag(BlockTags.FLOWER_POTS)
				.add(Misc.pottedHemp);

		/* MOD COMPAT STARTS HERE */

		// TConstruct
		tag(TagUtils.createBlockWrapper(new ResourceLocation("tconstruct:harvestable/stackable")))
				.add(Misc.hempPlant);
		tag(TagUtils.createBlockWrapper(new ResourceLocation("chiselsandbits:chiselable/forced")))
				.add(StoneDecoration.insulatingGlass)
				.add(WoodenDevices.woodenBarrel)
				.add(WoodenDevices.turntable)
				.add(WoodenDevices.crate)
				.add(WoodenDevices.reinforcedCrate)
				.add(WoodenDevices.itemBatcher)
				.add(WoodenDevices.fluidSorter)
				.add(WoodenDevices.sorter)
				.add(MetalDevices.capacitorLV)
				.add(MetalDevices.capacitorMV)
				.add(MetalDevices.capacitorHV)
				.add(MetalDevices.capacitorCreative)
				.add(MetalDevices.barrel)
				.add(MetalDevices.furnaceHeater)
				.add(MetalDevices.dynamo)
				.add(MetalDevices.thermoelectricGen);
	}
}
