/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.ComparableItemStack;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.crafting.CrusherRecipe.SecondaryOutput;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
import blusunrize.immersiveengineering.client.utils.ClocheRenderHelper.RenderFunctionChorus;
import blusunrize.immersiveengineering.client.utils.ClocheRenderHelper.RenderFunctionHemp;
import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.StoneDecoration;
import blusunrize.immersiveengineering.common.crafting.MetalPressPackingRecipe;
import blusunrize.immersiveengineering.common.crafting.MetalPressUnpackingRecipe;
import blusunrize.immersiveengineering.common.crafting.OreCrushingRecipe;
import blusunrize.immersiveengineering.common.items.BulletItem;
import blusunrize.immersiveengineering.common.items.IEBaseItem;
import blusunrize.immersiveengineering.common.items.IEItems;
import blusunrize.immersiveengineering.common.items.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.items.IEItems.Molds;
import blusunrize.immersiveengineering.common.items.IEItems.Tools;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import static blusunrize.immersiveengineering.common.IEContent.*;
import static blusunrize.immersiveengineering.common.blocks.EnumMetals.*;
import static blusunrize.immersiveengineering.common.blocks.IEBlocks.Metals.ores;
import static blusunrize.immersiveengineering.common.blocks.IEBlocks.Metals.storage;
import static blusunrize.immersiveengineering.common.items.IEItems.Ingredients.*;
import static blusunrize.immersiveengineering.common.items.IEItems.Metals.ingots;
import static blusunrize.immersiveengineering.common.items.IEItems.Misc.hempSeeds;
import static blusunrize.immersiveengineering.common.items.IEItems.Molds.*;

public class IERecipes
{
	//TODO move these helpers somewhere else
	public static ResourceLocation getCrystal(String type)
	{
		//TODO dos anyone use this?
		return new ResourceLocation("forge", "crystal/"+type);
	}

	public static ResourceLocation getGem(String type)
	{
		return new ResourceLocation("forge", "gems/"+type);
	}

	public static ResourceLocation getDust(String type)
	{
		return new ResourceLocation("forge", "dusts/"+type);
	}

	public static ResourceLocation getIngot(String type)
	{
		return new ResourceLocation("forge", "ingots/"+type);
	}

	public static ResourceLocation getPlate(String type)
	{
		return new ResourceLocation("forge", "plates/"+type);
	}

	public static ResourceLocation getStick(String type)
	{
		return new ResourceLocation("forge", "sticks/"+type);
	}

	public static ResourceLocation getRod(String type)
	{
		return new ResourceLocation("forge", "rods/"+type);
	}

	public static ResourceLocation getOre(String type)
	{
		return new ResourceLocation("forge", "ores/"+type);
	}

	public static ResourceLocation getStorageBlock(String type)
	{
		return new ResourceLocation("forge", "storage_blocks/"+type);
	}

	public static ResourceLocation getSheetmetalBlock(String type)
	{
		return new ResourceLocation("forge", "sheetmetal/"+type);
	}

	public static ResourceLocation getNugget(String type)
	{
		return new ResourceLocation("forge", "nuggets/"+type);
	}
}
