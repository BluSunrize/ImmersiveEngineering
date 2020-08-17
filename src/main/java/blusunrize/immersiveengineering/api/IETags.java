/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;


import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags.Blocks;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import static blusunrize.immersiveengineering.common.data.IEDataGenerator.rl;

public class IETags
{

	private static final Map<Tag<Block>, Tag<Item>> toItemTag = new HashMap<>();
	private static final Map<EnumMetals, MetalTags> metals = new HashMap<>();

	//Vanilla
	public static final Tag<Item> clay = new ItemTags.Wrapper(forgeLoc("clay"));
	public static final Tag<Block> clayBlock = createBlockTag(getStorageBlock("clay"));
	public static final Tag<Item> charCoal = new ItemTags.Wrapper(forgeLoc("charcoal"));
	public static final Tag<Block> glowstoneBlock = createBlockTag(getStorageBlock("glowstone"));
	//Other mods
	public static final Tag<Block> charCoalBlocks = createBlockTag(getStorageBlock("charcoal"));
	//IE Blocks
	public static final Tag<Block> treatedWood = createBlockTag(forgeLoc("treated_wood"));
	public static final Tag<Block> treatedWoodSlab = createBlockTag(forgeLoc("treated_wood_slab"));
	public static final Tag<Block> coalCokeBlock = createBlockTag(getStorageBlock("coal_coke"));
	public static final Tag<Block> scaffoldingSteel = createBlockTag(rl("scaffoldings/steel"));
	public static final Tag<Block> scaffoldingAlu = createBlockTag(rl("scaffoldings/aluminum"));
	public static final Tag<Block> sheetmetals = createBlockTag(forgeLoc("sheetmetals"));
	//IE Items
	public static final Tag<Item> treatedStick = new ItemTags.Wrapper(getRod("treated_wood"));
	public static final Tag<Item> ironRod = new ItemTags.Wrapper(getRod("iron"));
	public static final Tag<Item> steelRod = new ItemTags.Wrapper(getRod("steel"));
	public static final Tag<Item> metalRods = new ItemTags.Wrapper(getRod("all_metal"));
	public static final Tag<Item> aluminumRod = new ItemTags.Wrapper(getRod("aluminum"));
	public static final Tag<Item> fiberHemp = new ItemTags.Wrapper(forgeLoc("fiber_hemp"));
	public static final Tag<Item> fabricHemp = new ItemTags.Wrapper(forgeLoc("fabric_hemp"));
	public static final Tag<Item> coalCoke = new ItemTags.Wrapper(forgeLoc("coal_coke"));
	public static final Tag<Item> slag = new ItemTags.Wrapper(forgeLoc("slag"));
	public static final Tag<Item> coalCokeDust = new ItemTags.Wrapper(getDust("coal_coke"));
	public static final Tag<Item> hopGraphiteDust = new ItemTags.Wrapper(getDust("hop_graphite"));
	public static final Tag<Item> hopGraphiteIngot = new ItemTags.Wrapper(getIngot("hop_graphite"));
	public static final Tag<Item> copperWire = new ItemTags.Wrapper(getWire("copper"));
	public static final Tag<Item> electrumWire = new ItemTags.Wrapper(getWire("electrum"));
	public static final Tag<Item> aluminumWire = new ItemTags.Wrapper(getWire("aluminum"));
	public static final Tag<Item> steelWire = new ItemTags.Wrapper(getWire("steel"));
	public static final Tag<Item> saltpeterDust = new ItemTags.Wrapper(getDust("saltpeter"));
	public static final Tag<Item> sulfurDust = new ItemTags.Wrapper(getDust("sulfur"));
	public static final Tag<Item> plates = new ItemTags.Wrapper(forgeLoc("plates"));

	public static final Tag<Fluid> fluidCreosote = new FluidTags.Wrapper(forgeLoc("creosote"));
	public static final Tag<Fluid> fluidPlantoil = new FluidTags.Wrapper(forgeLoc("plantoil"));
	public static final Tag<Fluid> fluidEthanol = new FluidTags.Wrapper(forgeLoc("ethanol"));
	public static final Tag<Fluid> fluidBiodiesel = new FluidTags.Wrapper(forgeLoc("biodiesel"));
	public static final Tag<Fluid> fluidConcrete = new FluidTags.Wrapper(forgeLoc("concrete"));
	public static final Tag<Fluid> fluidHerbicide = new FluidTags.Wrapper(forgeLoc("herbicide"));
	public static final Tag<Fluid> fluidPotion = new FluidTags.Wrapper(forgeLoc("potion"));

	static
	{
		for(EnumMetals m : EnumMetals.values())
			metals.put(m, new MetalTags(m));
	}

	public static Tag<Item> getItemTag(Tag<Block> blockTag)
	{
		return toItemTag.get(blockTag);
	}

	public static MetalTags getTagsFor(EnumMetals metal)
	{
		return metals.get(metal);
	}

	private static Tag<Block> createBlockTag(ResourceLocation name)
	{
		Tag<Block> blockTag = new BlockTags.Wrapper(name);
		toItemTag.put(blockTag, new ItemTags.Wrapper(name));
		return blockTag;
	}

	public static void forAllBlocktags(BiConsumer<Tag<Block>, Tag<Item>> out)
	{
		for(Entry<Tag<Block>, Tag<Item>> entry : toItemTag.entrySet())
		{
			out.accept(entry.getKey(), entry.getValue());
		}
	}

	public static class MetalTags
	{
		public final Tag<Item> ingot;
		public final Tag<Item> nugget;
		public final Tag<Item> plate;
		public final Tag<Item> dust;
		public final Tag<Block> storage;
		public final Tag<Block> sheetmetal;
		@Nullable
		public final Tag<Block> ore;

		private MetalTags(EnumMetals m)
		{
			String name = m.tagName();
			Tag<Block> ore = null;
			if(m.shouldAddOre())
				ore = createBlockTag(getOre(name));
			if(!m.isVanillaMetal())
				storage = createBlockTag(getStorageBlock(name));
			else if(m==EnumMetals.IRON)
			{
				storage = Blocks.STORAGE_BLOCKS_IRON;
				ore = Blocks.ORES_IRON;
			}
			else if(m==EnumMetals.GOLD)
			{
				storage = Blocks.STORAGE_BLOCKS_GOLD;
				ore = Blocks.ORES_GOLD;
			}
			else
				throw new RuntimeException("Unkown vanilla metal: "+m.name());
			sheetmetal = createBlockTag(getSheetmetalBlock(name));
			nugget = new ItemTags.Wrapper(getNugget(name));
			ingot = new ItemTags.Wrapper(getIngot(name));
			plate = new ItemTags.Wrapper(getPlate(name));
			dust = new ItemTags.Wrapper(getDust(name));
			this.ore = ore;
		}
	}

	private static ResourceLocation forgeLoc(String path)
	{
		return new ResourceLocation("forge", path);
	}

	public static ResourceLocation getOre(String type)
	{
		return forgeLoc("ores/"+type);
	}

	public static ResourceLocation getNugget(String type)
	{
		return forgeLoc("nuggets/"+type);
	}

	public static ResourceLocation getIngot(String type)
	{
		return forgeLoc("ingots/"+type);
	}

	public static ResourceLocation getGem(String type)
	{
		return forgeLoc("gems/"+type);
	}

	public static ResourceLocation getStorageBlock(String type)
	{
		return forgeLoc("storage_blocks/"+type);
	}

	public static ResourceLocation getDust(String type)
	{
		return forgeLoc("dusts/"+type);
	}

	public static ResourceLocation getPlate(String type)
	{
		return forgeLoc("plates/"+type);
	}

	public static ResourceLocation getRod(String type)
	{
		return forgeLoc("rods/"+type);
	}

	public static ResourceLocation getGear(String type)
	{
		return forgeLoc("gears/"+type);
	}

	public static ResourceLocation getWire(String type)
	{
		return forgeLoc("wires/"+type);
	}

	public static ResourceLocation getSheetmetalBlock(String type)
	{
		return forgeLoc("sheetmetals/"+type);
	}
}
