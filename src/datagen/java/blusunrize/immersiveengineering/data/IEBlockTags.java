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
import blusunrize.immersiveengineering.common.blocks.IEStairsBlock;
import blusunrize.immersiveengineering.common.blocks.generic.ConnectorBlock;
import blusunrize.immersiveengineering.common.blocks.metal.ConveyorBlock;
import blusunrize.immersiveengineering.common.blocks.metal.MetalLadderBlock;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.blocks.wooden.TreatedWoodStyles;
import blusunrize.immersiveengineering.common.fluids.IEFluidBlock;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEBlocks.*;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.Tag.Builder;
import net.minecraft.tags.Tag.BuilderEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

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
				.add(MetalDecoration.ALU_FENCE.get())
				.add(MetalDecoration.STEEL_FENCE.get())
				.add(WoodenDecoration.TREATED_FENCE.get());
		tag(BlockTags.WOODEN_FENCES)
				.add(WoodenDecoration.TREATED_FENCE.get());
		tag(BlockTags.PLANKS).add(WoodenDecoration.FIBERBOARD.get());
		tag(IETags.fencesSteel)
				.add(MetalDecoration.STEEL_FENCE.get());
		tag(IETags.fencesAlu)
				.add(MetalDecoration.ALU_FENCE.get());
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
		for(BlockEntry<MetalLadderBlock> b : MetalDecoration.METAL_LADDER.values())
			tag(BlockTags.CLIMBABLE).add(b.get());
		for(EnumMetals metal : EnumMetals.values())
		{
			MetalTags tags = IETags.getTagsFor(metal);
			if(!metal.isVanillaMetal())
			{
				tag(tags.storage).add(IEBlocks.Metals.STORAGE.get(metal).get());
				tag(Tags.Blocks.STORAGE_BLOCKS).addTag(tags.storage);
				if(metal.shouldAddOre())
				{
					Preconditions.checkNotNull(tags.ore);
					tag(tags.ore)
							.add(IEBlocks.Metals.ORES.get(metal).get())
							.add(IEBlocks.Metals.DEEPSLATE_ORES.get(metal).get());
					tag(Tags.Blocks.ORES).addTag(tags.ore);
					Preconditions.checkNotNull(tags.rawBlock);
					tag(tags.rawBlock).add(IEBlocks.Metals.RAW_ORES.get(metal).get());
					tag(Tags.Blocks.STORAGE_BLOCKS).addTag(tags.rawBlock);
					tag(Tags.Blocks.ORES_IN_GROUND_STONE).add(Metals.ORES.get(metal).get());
					tag(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE).add(Metals.DEEPSLATE_ORES.get(metal).get());
					tag(Tags.Blocks.ORE_RATES_SINGULAR).add(Metals.ORES.get(metal).get())
							.add(Metals.DEEPSLATE_ORES.get(metal).get());
				}
			}
			tag(tags.sheetmetal).add(IEBlocks.Metals.SHEETMETAL.get(metal).get());
			tag(IETags.sheetmetals).addTag(tags.sheetmetal);
		}
		for(DyeColor dye : DyeColor.values())
			tag(IETags.sheetmetals).add(MetalDecoration.COLORED_SHEETMETAL.get(dye).get());
		for(TreatedWoodStyles style : TreatedWoodStyles.values())
		{
			tag(IETags.treatedWood).add(WoodenDecoration.TREATED_WOOD.get(style).get());
			tag(IETags.treatedWoodSlab).add(IEBlocks.TO_SLAB.get(IEBlocks.WoodenDecoration.TREATED_WOOD.get(style).getId()).get());
		}
		for(MetalScaffoldingType t : MetalScaffoldingType.values())
		{
			tag(IETags.scaffoldingSteel).add(MetalDecoration.STEEL_SCAFFOLDING.get(t).get());
			tag(IETags.scaffoldingAlu).add(MetalDecoration.ALU_SCAFFOLDING.get(t).get());
		}
		tag(IETags.coalCokeBlock)
				.add(StoneDecoration.COKE.get());
		tag(Tags.Blocks.GRAVEL)
				.add(StoneDecoration.SLAG_GRAVEL.get());
		tag(BlockTags.FLOWER_POTS)
				.add(Misc.POTTED_HEMP.get());

		registerHammerMineable();
		registerPickaxeMineable();
		registerAxeMineable();
		tag(BlockTags.MINEABLE_WITH_SHOVEL)
				.add(WoodenDecoration.SAWDUST.get())
				.add(StoneDecoration.SLAG_GRAVEL.get());
		tag(IETags.wirecutterHarvestable)
				.add(MetalDevices.RAZOR_WIRE.get());
		tag(IETags.drillHarvestable)
				.addTag(BlockTags.MINEABLE_WITH_SHOVEL)
				.addTag(BlockTags.MINEABLE_WITH_PICKAXE);
		tag(IETags.buzzsawTreeBlacklist)
				.addOptionalTag(new ResourceLocation("dynamictrees","branches"))
				.addOptionalTag(new ResourceLocation("dynamictrees","leaves"));
		checkAllRegisteredForBreaking();

		for(BlockEntry<SlabBlock> slab : IEBlocks.TO_SLAB.values())
			if(slab.get().defaultBlockState().getMaterial()==Material.WOOD)
				tag(BlockTags.WOODEN_SLABS).add(slab.get());
			else
				tag(BlockTags.SLABS).add(slab.get());
		for(BlockEntry<IEStairsBlock> stairs : IEBlocks.TO_STAIRS.values())
			if(stairs.get().defaultBlockState().getMaterial()==Material.WOOD)
				tag(BlockTags.WOODEN_STAIRS).add(stairs.get());
			else
				tag(BlockTags.STAIRS).add(stairs.get());

		/* MOD COMPAT STARTS HERE */

		// TConstruct
		tag(TagUtils.createBlockWrapper(new ResourceLocation("tconstruct:harvestable/stackable")))
				.add(Misc.HEMP_PLANT.get());
		tag(TagUtils.createBlockWrapper(new ResourceLocation("chiselsandbits:chiselable/forced")))
				.add(StoneDecoration.INSULATING_GLASS.get())
				.add(WoodenDevices.WOODEN_BARREL.get())
				.add(WoodenDevices.TURNTABLE.get())
				.add(WoodenDevices.CRATE.get())
				.add(WoodenDevices.REINFORCED_CRATE.get())
				.add(WoodenDevices.ITEM_BATCHER.get())
				.add(WoodenDevices.FLUID_SORTER.get())
				.add(WoodenDevices.SORTER.get())
				.add(MetalDevices.CAPACITOR_LV.get())
				.add(MetalDevices.CAPACITOR_MV.get())
				.add(MetalDevices.CAPACITOR_HV.get())
				.add(MetalDevices.CAPACITOR_CREATIVE.get())
				.add(MetalDevices.BARREL.get())
				.add(MetalDevices.FURNACE_HEATER.get())
				.add(MetalDevices.DYNAMO.get())
				.add(MetalDevices.THERMOELECTRIC_GEN.get());
	}

	private void registerHammerMineable()
	{
		TagAppender<Block> tag = tag(IETags.hammerHarvestable);
		MetalDecoration.METAL_LADDER.values().forEach(b -> tag.add(b.get()));
		tag.addTag(IETags.scaffoldingSteel);
		tag.addTag(IETags.scaffoldingAlu);
		tag.add(StoneDecoration.CONCRETE_SPRAYED.get())
				.add(Cloth.STRIP_CURTAIN.get());
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
		tag.add(Cloth.SHADER_BANNER_WALL.get());
		tag.add(Cloth.SHADER_BANNER.get());
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
		setMiningLevel(Metals.ORES.get(metal), level);
		setMiningLevel(Metals.DEEPSLATE_ORES.get(metal), level);
		setMiningLevel(Metals.RAW_ORES.get(metal), level);
	}

	private void setStorageMiningLevel(EnumMetals metal, Tiers level)
	{
		setMiningLevel(Metals.STORAGE.get(metal), level);
	}

	private void setMiningLevel(Supplier<Block> block, Tiers level)
	{
		TagKey<Block> tag = switch(level)
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
		List<TagKey<Block>> knownHarvestTags = ImmutableList.of(
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
				.map(BuilderEntry::entry)
				.filter(e -> e instanceof Tag.ElementEntry)
				.map(Object::toString)
				.map(ResourceLocation::new)
				.collect(Collectors.toSet());
		Set<ResourceLocation> knownNonHarvestable = Stream.of(
						Cloth.BALLOON, Cloth.CUSHION, Misc.FAKE_LIGHT, Misc.POTTED_HEMP, Misc.HEMP_PLANT
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
