/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import com.google.common.base.Preconditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.Tags.Blocks;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import static blusunrize.immersiveengineering.api.utils.TagUtils.*;

public class IETags
{

	private static final Map<Named<Block>, Named<Item>> toItemTag = new HashMap<>();
	private static final Map<EnumMetals, MetalTags> metals = new HashMap<>();

	//Vanilla
	public static final Named<Item> clay = createItemWrapper(forgeLoc("clay"));
	public static final Named<Block> clayBlock = createBlockTag(getStorageBlock("clay"));
	public static final Named<Item> charCoal = createItemWrapper(forgeLoc("charcoal"));
	public static final Named<Block> glowstoneBlock = createBlockTag(getStorageBlock("glowstone"));
	public static final Named<Block> colorlessSandstoneBlocks = createBlockTag(forgeLoc("sandstone/colorless"));
	public static final Named<Block> redSandstoneBlocks = createBlockTag(forgeLoc("sandstone/red"));
	//IE Blocks
	public static final Named<Block> treatedWood = createBlockTag(forgeLoc("treated_wood"));
	public static final Named<Block> treatedWoodSlab = createBlockTag(forgeLoc("treated_wood_slab"));
	public static final Named<Block> coalCokeBlock = createBlockTag(getStorageBlock("coal_coke"));
	public static final Named<Block> scaffoldingSteel = createBlockTag(rl("scaffoldings/steel"));
	public static final Named<Block> scaffoldingAlu = createBlockTag(rl("scaffoldings/aluminum"));
	public static final Named<Block> sheetmetals = createBlockTag(forgeLoc("sheetmetals"));
	public static final Named<Block> fencesSteel = createBlockTag(forgeLoc("fences/steel"));
	public static final Named<Block> fencesAlu = createBlockTag(forgeLoc("fences/aluminum"));
	//IE Items
	public static final Named<Item> treatedStick = createItemWrapper(getRod("treated_wood"));
	public static final Named<Item> ironRod = createItemWrapper(getRod("iron"));
	public static final Named<Item> steelRod = createItemWrapper(getRod("steel"));
	public static final Named<Item> metalRods = createItemWrapper(getRod("all_metal"));
	public static final Named<Item> aluminumRod = createItemWrapper(getRod("aluminum"));
	public static final Named<Item> fiberHemp = createItemWrapper(forgeLoc("fiber_hemp"));
	public static final Named<Item> fabricHemp = createItemWrapper(forgeLoc("fabric_hemp"));
	public static final Named<Item> coalCoke = createItemWrapper(forgeLoc("coal_coke"));
	public static final Named<Item> slag = createItemWrapper(forgeLoc("slag"));
	public static final Named<Item> coalCokeDust = createItemWrapper(getDust("coal_coke"));
	public static final Named<Item> hopGraphiteDust = createItemWrapper(getDust("hop_graphite"));
	public static final Named<Item> hopGraphiteIngot = createItemWrapper(getIngot("hop_graphite"));
	public static final Named<Item> copperWire = createItemWrapper(getWire("copper"));
	public static final Named<Item> electrumWire = createItemWrapper(getWire("electrum"));
	public static final Named<Item> aluminumWire = createItemWrapper(getWire("aluminum"));
	public static final Named<Item> steelWire = createItemWrapper(getWire("steel"));
	public static final Named<Item> leadWire = createItemWrapper(getWire("lead"));
	public static final Named<Item> allWires = createItemWrapper(forgeLoc("wires"));
	public static final Named<Item> saltpeterDust = createItemWrapper(getDust("saltpeter"));
	public static final Named<Item> sulfurDust = createItemWrapper(getDust("sulfur"));
	public static final Named<Item> sawdust = createItemWrapper(getDust("wood"));
	public static final Named<Item> plates = createItemWrapper(forgeLoc("plates"));
	public static final Named<Item> sawblades = createItemWrapper(forgeLoc("sawblades"));
	//Utility tags
	public static final Named<Item> forbiddenInCrates = createItemWrapper(rl("forbidden_in_crates"));
	public static final Named<Item> circuitPCB = createItemWrapper(rl("circuits/pcb"));
	public static final Named<Item> circuitLogic = createItemWrapper(rl("circuits/logic"));
	public static final Named<Item> circuitSolder = createItemWrapper(rl("circuits/solder"));

	public static final Named<Fluid> fluidCreosote = createFluidWrapper(forgeLoc("creosote"));
	public static final Named<Fluid> fluidPlantoil = createFluidWrapper(forgeLoc("plantoil"));
	public static final Named<Fluid> fluidEthanol = createFluidWrapper(forgeLoc("ethanol"));
	public static final Named<Fluid> fluidBiodiesel = createFluidWrapper(forgeLoc("biodiesel"));
	public static final Named<Fluid> fluidConcrete = createFluidWrapper(forgeLoc("concrete"));
	public static final Named<Fluid> fluidHerbicide = createFluidWrapper(forgeLoc("herbicide"));
	public static final Named<Fluid> fluidPotion = createFluidWrapper(forgeLoc("potion"));
	public static final Named<Fluid> drillFuel = createFluidWrapper(forgeLoc("drill_fuel"));

	static
	{
		for(EnumMetals m : EnumMetals.values())
			metals.put(m, new MetalTags(m));
	}

	public static Named<Item> getItemTag(Named<Block> blockTag)
	{
		Preconditions.checkArgument(toItemTag.containsKey(blockTag));
		return toItemTag.get(blockTag);
	}

	public static MetalTags getTagsFor(EnumMetals metal)
	{
		return metals.get(metal);
	}

	private static Named<Block> createBlockTag(ResourceLocation name)
	{
		Named<Block> blockTag = createBlockWrapper(name);
		toItemTag.put(blockTag, createItemWrapper(name));
		return blockTag;
	}

	public static void forAllBlocktags(BiConsumer<Named<Block>, Named<Item>> out)
	{
		for(Entry<Named<Block>, Named<Item>> entry : toItemTag.entrySet())
			out.accept(entry.getKey(), entry.getValue());
	}

	public static class MetalTags
	{
		public final Named<Item> ingot;
		public final Named<Item> nugget;
		public final Named<Item> plate;
		public final Named<Item> dust;
		public final Named<Block> storage;
		public final Named<Block> sheetmetal;
		@Nullable
		public final Named<Block> ore;

		private MetalTags(EnumMetals m)
		{
			String name = m.tagName();
			Named<Block> ore = null;
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

	private static ResourceLocation rl(String path)
	{
		return new ResourceLocation(Lib.MODID, path);
	}
}
