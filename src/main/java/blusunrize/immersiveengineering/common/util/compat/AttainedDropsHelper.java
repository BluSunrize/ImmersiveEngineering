/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.tool.BelljarHandler;
import blusunrize.immersiveengineering.api.tool.BelljarHandler.IPlantHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AttainedDropsHelper extends IECompatModule
{
	
	public static final String AD_MODID = "attaineddrops2";
	
	/**
	 * Map of soil stack meta to outputs, as defined in ADType below.  Soil meta is type.ordinal() + 1.
	 */
	private static Int2ObjectMap<ItemStack[]> outputMap = new Int2ObjectOpenHashMap<>();

	/**
	 * Map of soil stack meta to bulbs, as defined in ADType below.  Soil meta is type.ordinal() + 1.
	 */
	private static Int2ObjectMap<IBlockState> bulbMap = new Int2ObjectOpenHashMap<>();
	
	private Item seed;
	private Block plant;
	private Block bulb;
	private Block soil;
	private Item soilItem;

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
		seed = ForgeRegistries.ITEMS.getValue(new ResourceLocation(AD_MODID, "seed"));
		plant = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(AD_MODID, "plant"));
		bulb = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(AD_MODID, "bulb"));
		soil = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(AD_MODID, "soil"));
		soilItem = Item.getItemFromBlock(soil);

		addTypes();

		BelljarHandler.registerHandler(new IPlantHandler()
		{
			@Override
			public boolean isCorrectSoil(ItemStack seed, ItemStack soil)
			{
				return soil.getItem()==AttainedDropsHelper.this.soilItem && soil.getMetadata() > 0 && soil.getMetadata() <= 15;
			}

			@Override
			public float getGrowthStep(ItemStack seed, ItemStack soil, float growth, TileEntity tile, float fertilizer, boolean render)
			{
				return (growth < .5?.003125f: .0015625f)*fertilizer;
			}

			@Override
			public float resetGrowth(ItemStack seed, ItemStack soil, float growth, TileEntity tile, boolean render)
			{
				return .5f;
			}

			@Override
			public ItemStack[] getOutput(ItemStack seed, ItemStack soil, TileEntity tile)
			{
				ItemStack[] out = outputMap.get(soil.getMetadata());
				if(out==null)
					return new ItemStack[0];
				return out;
			}

			@Override
			public boolean isValid(ItemStack seed)
			{
				return seed.getItem()==AttainedDropsHelper.this.seed;
			}

			@Override
			@SideOnly(Side.CLIENT)
			public IBlockState[] getRenderedPlant(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
			{
				return new IBlockState[0];
			}

			@Override
			@SideOnly(Side.CLIENT)
			public float getRenderSize(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
			{
				return .875f;
			}

			@Override
			@SideOnly(Side.CLIENT)
			public boolean overrideRender(ItemStack seed, ItemStack soil, float growth, TileEntity tile, BlockRendererDispatcher blockRenderer)
			{
				IBlockState state = plant.getDefaultState().withProperty(BlockCrops.AGE, growth >= .5?7: Math.min(7, Math.round(7*growth*2)));
				IBakedModel model = blockRenderer.getModelForState(state);
				GlStateManager.pushMatrix();
				GlStateManager.translate(0, 0, 1);
				blockRenderer.getBlockModelRenderer().renderModelBrightness(model, state, 1, true);
				GlStateManager.popMatrix();
				if(growth >= .5)
				{
					state = bulbMap.get(soil.getMetadata());
					model = blockRenderer.getModelForState(state);
					if(model == null) return false;
					GlStateManager.pushMatrix();
					GlStateManager.translate(0, 0, 1);
					float scale = (growth-.5f)*2f;
					GlStateManager.translate(.5-scale/2, 1, -.5+scale/2);
					GlStateManager.scale(scale, scale, scale);
					blockRenderer.getBlockModelRenderer().renderModelBrightness(model, state, 1, true);
					GlStateManager.popMatrix();
				}
				return true;
			}
		});
	}
	
	@SuppressWarnings("deprecation")
	void addTypes()
	{
		for(ADType type : ADType.values())
		{
			outputMap.put(type.ordinal() + 1, new ItemStack[]{type.getDrop()});
			bulbMap.put(type.ordinal() + 1, this.bulb.getStateFromMeta(type.ordinal()));
		}
	}

	@Override
	public void postInit()
	{
	}

	private enum ADType 
	{
		BLAZE(new ItemStack(Items.BLAZE_ROD)),
		PEARL(new ItemStack(Items.ENDER_PEARL)),
		BONE(new ItemStack(Items.BONE)),
		SLIME(new ItemStack(Items.SLIME_BALL)),
		FLESH(new ItemStack(Items.ROTTEN_FLESH)),
		TEAR(new ItemStack(Items.GHAST_TEAR)),
		GUNPOWDER(new ItemStack(Items.GUNPOWDER)),
		STRING(new ItemStack(Items.STRING)),
		EYE(new ItemStack(Items.SPIDER_EYE)),
		PRISMARINE(new ItemStack(Items.PRISMARINE_SHARD)),
		WITHER(new ItemStack(Items.SKULL, 1, 1)),
		SHULKER(new ItemStack(Items.SHULKER_SHELL)),
		LEATHER(new ItemStack(Items.LEATHER)),
		FEATHER(new ItemStack(Items.FEATHER)),
		PRISMARINE_C(new ItemStack(Items.PRISMARINE_CRYSTALS));

		private final ItemStack drop;

		ADType(ItemStack drop) 
		{
			this.drop = drop;
		}

		public ItemStack getDrop() 
		{
			return drop;
		}
		
	}
}