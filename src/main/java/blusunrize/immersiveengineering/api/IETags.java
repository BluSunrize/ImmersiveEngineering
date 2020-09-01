/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;


import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import com.google.common.base.Preconditions;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags.Blocks;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import static blusunrize.immersiveengineering.common.data.DataGenUtils.*;
import static blusunrize.immersiveengineering.common.data.IEDataGenerator.rl;

public class IETags
{

	private static final Map<INamedTag<Block>, INamedTag<Item>> toItemTag = new HashMap<>();
	private static final Map<EnumMetals, MetalTags> metals = new HashMap<>();

	//Vanilla
	public static final INamedTag<Item> clay = createItemWrapper(forgeLoc("clay"));
	public static final INamedTag<Block> clayBlock = createBlockTag(getStorageBlock("clay"));
	public static final INamedTag<Item> charCoal = createItemWrapper(forgeLoc("charcoal"));
	public static final INamedTag<Block> glowstoneBlock = createBlockTag(getStorageBlock("glowstone"));
	//IE Blocks
	public static final INamedTag<Block> treatedWood = createBlockTag(forgeLoc("treated_wood"));
	public static final INamedTag<Block> treatedWoodSlab = createBlockTag(forgeLoc("treated_wood_slab"));
	public static final INamedTag<Block> coalCokeBlock = createBlockTag(getStorageBlock("coal_coke"));
	public static final INamedTag<Block> scaffoldingSteel = createBlockTag(rl("scaffoldings/steel"));
	public static final INamedTag<Block> scaffoldingAlu = createBlockTag(rl("scaffoldings/aluminum"));
	public static final INamedTag<Block> sheetmetals = createBlockTag(forgeLoc("sheetmetals"));
	//IE Items
	public static final INamedTag<Item> treatedStick = createItemWrapper(getRod("treated_wood"));
	public static final INamedTag<Item> ironRod = createItemWrapper(getRod("iron"));
	public static final INamedTag<Item> steelRod = createItemWrapper(getRod("steel"));
	public static final INamedTag<Item> metalRods = createItemWrapper(getRod("all_metal"));
	public static final INamedTag<Item> aluminumRod = createItemWrapper(getRod("aluminum"));
	public static final INamedTag<Item> fiberHemp = createItemWrapper(forgeLoc("fiber_hemp"));
	public static final INamedTag<Item> fabricHemp = createItemWrapper(forgeLoc("fabric_hemp"));
	public static final INamedTag<Item> coalCoke = createItemWrapper(forgeLoc("coal_coke"));
	public static final INamedTag<Item> slag = createItemWrapper(forgeLoc("slag"));
	public static final INamedTag<Item> coalCokeDust = createItemWrapper(getDust("coal_coke"));
	public static final INamedTag<Item> hopGraphiteDust = createItemWrapper(getDust("hop_graphite"));
	public static final INamedTag<Item> hopGraphiteIngot = createItemWrapper(getIngot("hop_graphite"));
	public static final INamedTag<Item> copperWire = createItemWrapper(getWire("copper"));
	public static final INamedTag<Item> electrumWire = createItemWrapper(getWire("electrum"));
	public static final INamedTag<Item> aluminumWire = createItemWrapper(getWire("aluminum"));
	public static final INamedTag<Item> steelWire = createItemWrapper(getWire("steel"));
	public static final INamedTag<Item> saltpeterDust = createItemWrapper(getDust("saltpeter"));
	public static final INamedTag<Item> sulfurDust = createItemWrapper(getDust("sulfur"));
	public static final INamedTag<Item> plates = createItemWrapper(forgeLoc("plates"));

	public static final INamedTag<Fluid> fluidCreosote = createFluidWrapper(forgeLoc("creosote"));
	public static final INamedTag<Fluid> fluidPlantoil = createFluidWrapper(forgeLoc("plantoil"));
	public static final INamedTag<Fluid> fluidEthanol = createFluidWrapper(forgeLoc("ethanol"));
	public static final INamedTag<Fluid> fluidBiodiesel = createFluidWrapper(forgeLoc("biodiesel"));
	public static final INamedTag<Fluid> fluidConcrete = createFluidWrapper(forgeLoc("concrete"));
	public static final INamedTag<Fluid> fluidHerbicide = createFluidWrapper(forgeLoc("herbicide"));
	public static final INamedTag<Fluid> fluidPotion = createFluidWrapper(forgeLoc("potion"));

	static
	{
		for(EnumMetals m : EnumMetals.values())
			metals.put(m, new MetalTags(m));
	}

	public static INamedTag<Item> getItemTag(INamedTag<Block> blockTag)
	{
		Preconditions.checkArgument(toItemTag.containsKey(blockTag));
		return toItemTag.get(blockTag);
	}

	public static MetalTags getTagsFor(EnumMetals metal)
	{
		return metals.get(metal);
	}

	private static INamedTag<Block> createBlockTag(ResourceLocation name)
	{
		INamedTag<Block> blockTag = createBlockWrapper(name);
		toItemTag.put(blockTag, createItemWrapper(name));
		return blockTag;
	}

	public static void forAllBlocktags(BiConsumer<INamedTag<Block>, INamedTag<Item>> out)
	{
		for(Entry<INamedTag<Block>, INamedTag<Item>> entry : toItemTag.entrySet())
			out.accept(entry.getKey(), entry.getValue());
	}

	public static class MetalTags
	{
		public final INamedTag<Item> ingot;
		public final INamedTag<Item> nugget;
		public final INamedTag<Item> plate;
		public final INamedTag<Item> dust;
		public final INamedTag<Block> storage;
		public final INamedTag<Block> sheetmetal;
		@Nullable
		public final INamedTag<Block> ore;

		private MetalTags(EnumMetals m)
		{
			String name = m.tagName();
			INamedTag<Block> ore = null;
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
			nugget = createItemWrapper(getNugget(name));
			ingot = createItemWrapper(getIngot(name));
			plate = createItemWrapper(getPlate(name));
			dust = createItemWrapper(getDust(name));
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
