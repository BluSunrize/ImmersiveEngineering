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
import blusunrize.immersiveengineering.common.blocks.generic.ConnectorBlock;
import blusunrize.immersiveengineering.common.blocks.metal.ConveyorBlock;
import blusunrize.immersiveengineering.common.blocks.metal.MetalLadderBlock;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.blocks.wooden.TreatedWoodStyles;
import blusunrize.immersiveengineering.common.fluids.IEFluidBlock;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEBlocks.*;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.Tag.Builder;
import net.minecraft.tags.Tag.BuilderEntry;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fmllegacy.RegistryObject;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
				tag(tags.storage).add(IEBlocks.Metals.storage.get(metal).get());
				tag(Tags.Blocks.STORAGE_BLOCKS).addTag(tags.storage);
				if(metal.shouldAddOre())
				{
					assert tags.ore!=null;
					tag(tags.ore).add(IEBlocks.Metals.ores.get(metal).get());
					tag(Tags.Blocks.ORES).addTag(tags.ore);
				}
			}
			//TODO Forge#7891
			if(metal==EnumMetals.COPPER)
			{
				tag(tags.storage).add(IEBlocks.Metals.storage.get(metal).get());
				tag(Tags.Blocks.STORAGE_BLOCKS).addTag(tags.storage);
				assert tags.ore!=null;
				tag(tags.ore).add(IEBlocks.Metals.ores.get(metal).get());
				tag(Tags.Blocks.ORES).addTag(tags.ore);
			}
			tag(tags.sheetmetal).add(IEBlocks.Metals.sheetmetal.get(metal).get());
			tag(IETags.sheetmetals).addTag(tags.sheetmetal);
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

		registerHammerMineable();
		registerPickaxeMineable();
		registerAxeMineable();
		tag(BlockTags.MINEABLE_WITH_SHOVEL)
				.add(WoodenDecoration.sawdust.get());
		tag(IETags.wirecutterHarvestable)
				.add(MetalDevices.razorWire.get());
		tag(IETags.drillHarvestable)
				.addTag(BlockTags.MINEABLE_WITH_SHOVEL)
				.addTag(BlockTags.MINEABLE_WITH_PICKAXE);
		checkAllRegisteredForBreaking();

		/* MOD COMPAT STARTS HERE */

		// TConstruct
		tag(TagUtils.createBlockWrapper(new ResourceLocation("tconstruct:harvestable/stackable")))
				.add(Misc.hempPlant.get());
	}

	private void registerHammerMineable()
	{
		TagAppender<Block> tag = tag(IETags.hammerHarvestable);
		MetalDecoration.metalLadder.values().forEach(b -> tag.add(b.get()));
		tag.add(StoneDecoration.concreteSprayed.get())
				.add(Cloth.curtain.get());
		//TODO not really the nicest approach, but maintains 1.16 behavior
		for(RegistryObject<Block> regObject : IEBlocks.REGISTER.getEntries())
		{
			Block block = regObject.get();
			if(block instanceof ConnectorBlock<?>||block instanceof ConveyorBlock)
				tag.add(block);
		}
	}

	private void registerAxeMineable()
	{
		TagAppender<Block> tag = tag(BlockTags.MINEABLE_WITH_AXE);
		IEBlocks.REGISTER.getEntries().stream()
				.map(RegistryObject::get)
				.filter(b -> b.defaultBlockState().getMaterial()==Material.WOOD)
				.forEach(tag::add);
		tag.add(Cloth.shaderBannerWall.get());
		tag.add(Cloth.shaderBanner.get());
	}

	private void registerPickaxeMineable()
	{
		TagAppender<Block> tag = tag(BlockTags.MINEABLE_WITH_PICKAXE);
		IEBlocks.REGISTER.getEntries().stream()
				.map(RegistryObject::get)
				.filter(b -> {
					Material material = b.defaultBlockState().getMaterial();
					return material==Material.STONE||material==Material.METAL;
				})
				.forEach(tag::add);
		setOreMiningLevel(EnumMetals.COPPER, Tiers.STONE);
		setOreMiningLevel(EnumMetals.ALUMINUM, Tiers.STONE);
		setOreMiningLevel(EnumMetals.LEAD, Tiers.IRON);
		setOreMiningLevel(EnumMetals.SILVER, Tiers.IRON);
		setOreMiningLevel(EnumMetals.NICKEL, Tiers.IRON);
		setOreMiningLevel(EnumMetals.URANIUM, Tiers.IRON);
		setStorageMiningLevel(EnumMetals.COPPER, Tiers.STONE);
		setStorageMiningLevel(EnumMetals.ALUMINUM, Tiers.STONE);
		setStorageMiningLevel(EnumMetals.LEAD, Tiers.IRON);
		setStorageMiningLevel(EnumMetals.SILVER, Tiers.IRON);
		setStorageMiningLevel(EnumMetals.NICKEL, Tiers.IRON);
		setStorageMiningLevel(EnumMetals.URANIUM, Tiers.IRON);
		setStorageMiningLevel(EnumMetals.CONSTANTAN, Tiers.IRON);
		setStorageMiningLevel(EnumMetals.ELECTRUM, Tiers.IRON);
		setStorageMiningLevel(EnumMetals.STEEL, Tiers.IRON);
	}

	private void setOreMiningLevel(EnumMetals metal, Tiers level)
	{
		setMiningLevel(Metals.ores.get(metal), level);
	}

	private void setStorageMiningLevel(EnumMetals metal, Tiers level)
	{
		setMiningLevel(Metals.storage.get(metal), level);
	}

	private void setMiningLevel(Supplier<Block> block, Tiers level)
	{
		Named<Block> tag = switch(level)
				{
					case STONE -> BlockTags.NEEDS_STONE_TOOL;
					case IRON -> BlockTags.NEEDS_IRON_TOOL;
					case DIAMOND -> BlockTags.NEEDS_DIAMOND_TOOL;
					default -> throw new IllegalArgumentException("No tag available for "+level.name());
				};
		tag(tag).add(block.get());
	}

	private void checkAllRegisteredForBreaking()
	{
		List<Named<Block>> knownHarvestTags = ImmutableList.of(
				BlockTags.MINEABLE_WITH_AXE,
				BlockTags.MINEABLE_WITH_PICKAXE,
				BlockTags.MINEABLE_WITH_SHOVEL,
				IETags.wirecutterHarvestable,
				IETags.hammerHarvestable
		);
		Set<ResourceLocation> harvestable = knownHarvestTags.stream()
				.map(this::tag)
				.map(TagAppender::getInternalBuilder)
				.flatMap(Builder::getEntries)
				.map(BuilderEntry::getEntry)
				.filter(e -> e instanceof Tag.ElementEntry)
				.map(Object::toString)
				.map(ResourceLocation::new)
				.collect(Collectors.toSet());
		Set<ResourceLocation> knownNonHarvestable = Stream.of(
						Cloth.balloon, Cloth.cushion, Misc.fakeLight, Misc.pottedHemp, Misc.hempPlant
				)
				.map(BlockEntry::getId)
				.collect(Collectors.toSet());
		Set<ResourceLocation> registered = IEBlocks.REGISTER.getEntries().stream()
				.map(RegistryObject::get)
				.filter(b -> !(b instanceof IEFluidBlock))
				.map(Block::getRegistryName)
				.filter(name -> !knownNonHarvestable.contains(name))
				.collect(Collectors.toSet());
		Set<ResourceLocation> notHarvestable = Sets.difference(registered, harvestable);
		if(!notHarvestable.isEmpty())
		{
			notHarvestable.forEach(rl -> IELogger.logger.error("Not harvestable: {}", rl));
			throw new RuntimeException();
		}
	}
}
