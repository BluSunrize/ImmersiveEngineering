/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.ComparableItemStack;
import blusunrize.immersiveengineering.api.tool.BelljarHandler;
import blusunrize.immersiveengineering.api.tool.BelljarHandler.DefaultPlantHandler;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashSet;

public class ExtraUtilsHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void registerRecipes()
	{
	}

	@Override
	public void init()
	{
		Item lillySeeds = Item.REGISTRY.getObject(new ResourceLocation("extrautils2:enderlilly"));
		Block lillyBlock = Block.REGISTRY.getObject(new ResourceLocation("extrautils2:enderlilly"));
		Item orchidSeeds = Item.REGISTRY.getObject(new ResourceLocation("extrautils2:redorchid"));
		Block orchidBlock = Block.REGISTRY.getObject(new ResourceLocation("extrautils2:redorchid"));
		if(lillySeeds!=null&&lillyBlock!=null)
			registerXUPlant(new ItemStack(lillySeeds), lillyBlock, new ItemStack(Blocks.END_STONE), new ItemStack[]{new ItemStack(Items.ENDER_PEARL)}, 7, .0000125f, false);
		if(orchidSeeds!=null&&orchidBlock!=null)
			registerXUPlant(new ItemStack(orchidSeeds), orchidBlock, new ItemStack(Blocks.REDSTONE_ORE), new ItemStack[]{new ItemStack(Items.REDSTONE)}, 6, .0125f, true);
	}

	@Override
	public void postInit()
	{
	}

	static void registerXUPlant(ItemStack seed, Block block, ItemStack soil, ItemStack[] output, final int maxAge, final float growthStep, final boolean useFertilizer)
	{
		IProperty propGrowth = null;
		final IBlockState state = block.getDefaultState();
		for(IProperty prop : state.getPropertyKeys())
			if("growth".equals(prop.getName()))
				propGrowth = prop;
		if(propGrowth!=null)
		{
			IProperty finalPropGrowth = propGrowth;
			DefaultPlantHandler handler = new DefaultPlantHandler()
			{
				private HashSet<ComparableItemStack> validSeeds = new HashSet<>();

				@Override
				protected HashSet<ComparableItemStack> getSeedSet()
				{
					return validSeeds;
				}

				@Override
				public float getGrowthStep(ItemStack seed, ItemStack soil, float growth, TileEntity tile, float fertilizer, boolean render)
				{
					return !useFertilizer?growthStep: (growthStep*fertilizer);
				}

				@Override
				@SideOnly(Side.CLIENT)
				public IBlockState[] getRenderedPlant(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
				{
					return new IBlockState[]{state.withProperty(finalPropGrowth, Math.min(maxAge, Math.round(maxAge*growth)))};
				}
			};
			handler.register(seed, output, soil, state);
			BelljarHandler.registerHandler(handler);
		}
	}
}