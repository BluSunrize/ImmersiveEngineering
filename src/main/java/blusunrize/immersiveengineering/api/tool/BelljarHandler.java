/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.ComparableItemStack;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChorusPlant;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockStem;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author BluSunrize - 09.03.2017
 * <p>
 * A handler for plantgrowth within the belljars/cloches
 */
public class BelljarHandler
{
	private static HashSet<IPlantHandler> plantHandlers = new HashSet<>();
	private static HashMap<ComparableItemStack, ResourceLocation> soilTextureMap = new HashMap<>();
	private static HashSet<FluidFertilizerHandler> fluidFertilizers = new HashSet<>();
	private static HashSet<ItemFertilizerHandler> itemFertilizers = new HashSet<>();

	public static float solidFertilizerModifier = 1;
	public static float fluidFertilizerModifier = 1;

	public static void registerHandler(IPlantHandler handler)
	{
		plantHandlers.add(handler);
	}

	public static IPlantHandler getHandler(ItemStack seed)
	{
		if(seed.isEmpty())
			return null;
		for(IPlantHandler handler : plantHandlers)
			if(handler.isValid(seed))
				return handler;
		return null;
	}

	public static void registerFluidFertilizer(FluidFertilizerHandler handler)
	{
		fluidFertilizers.add(handler);
	}

	public static FluidFertilizerHandler getFluidFertilizerHandler(FluidStack fluid)
	{
		if(fluid==null)
			return null;
		for(FluidFertilizerHandler handler : fluidFertilizers)
			if(handler.isValid(fluid))
				return handler;
		return null;
	}


	public static void registerItemFertilizer(ItemFertilizerHandler handler)
	{
		itemFertilizers.add(handler);
	}

	public static ItemFertilizerHandler getItemFertilizerHandler(ItemStack itemStack)
	{
		if(itemStack.isEmpty())
			return null;
		for(ItemFertilizerHandler handler : itemFertilizers)
			if(handler.isValid(itemStack))
				return handler;
		return null;
	}

	public static void registerBasicItemFertilizer(final ItemStack stack, final float growthMultiplier)
	{
		registerItemFertilizer(new ItemFertilizerHandler()
		{
			@Override
			public boolean isValid(@Nullable ItemStack fertilizer)
			{
				return OreDictionary.itemMatches(stack, fertilizer, false);
			}

			@Override
			public float getGrowthMultiplier(ItemStack fertilizer, ItemStack seed, ItemStack soil, TileEntity tile)
			{
				return solidFertilizerModifier*growthMultiplier;
			}
		});
	}

	public static ResourceLocation getSoilTexture(ItemStack soil)
	{
		return soilTextureMap.get(new ComparableItemStack(soil, false, false));
	}

	public interface IPlantHandler extends IPlantRenderer
	{
		boolean isCorrectSoil(ItemStack seed, ItemStack soil);

		float getGrowthStep(ItemStack seed, ItemStack soil, float growth, TileEntity tile, float fertilizer, boolean render);

		ItemStack[] getOutput(ItemStack seed, ItemStack soil, TileEntity tile);

		default float resetGrowth(ItemStack seed, ItemStack soil, float growth, TileEntity tile, boolean render)
		{
			return 0;
		}

		default ResourceLocation getSoilTexture(ItemStack seed, ItemStack soil, TileEntity tile)
		{
			return null;
		}
	}

	public interface IPlantRenderer
	{
		boolean isValid(ItemStack seed);

		@SideOnly(Side.CLIENT)
		IBlockState[] getRenderedPlant(ItemStack seed, ItemStack soil, float growth, TileEntity tile);

		@SideOnly(Side.CLIENT)
		default boolean overrideRender(ItemStack seed, ItemStack soil, float growth, TileEntity tile, BlockRendererDispatcher blockRenderer)
		{
			return false;
		}

		@SideOnly(Side.CLIENT)
		default float getRenderSize(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
		{
			return .875f;
		}
	}

	public interface FluidFertilizerHandler
	{
		boolean isValid(@Nullable FluidStack fertilizer);

		float getGrowthMultiplier(FluidStack fertilizer, ItemStack seed, ItemStack soil, TileEntity tile);
	}

	public interface ItemFertilizerHandler
	{
		boolean isValid(ItemStack fertilizer);

		float getGrowthMultiplier(ItemStack fertilizer, ItemStack seed, ItemStack soil, TileEntity tile);
	}

	private static HashMap<ComparableItemStack, IngredientStack> seedSoilMap = new HashMap<>();
	private static HashMap<ComparableItemStack, ItemStack[]> seedOutputMap = new HashMap<>();
	private static HashMap<ComparableItemStack, IBlockState[]> seedRenderMap = new HashMap<>();

	public abstract static class DefaultPlantHandler implements IPlantHandler
	{
		protected abstract HashSet<ComparableItemStack> getSeedSet();

		@Override
		public boolean isValid(ItemStack seed)
		{
			return seed!=null&&getSeedSet().contains(new ComparableItemStack(seed, false, false));
		}

		@Override
		public boolean isCorrectSoil(ItemStack seed, ItemStack soil)
		{
			IngredientStack reqSoil = seedSoilMap.get(new ComparableItemStack(seed, false, false));
			return reqSoil.matchesItemStack(soil);
		}

		@Override
		public float getGrowthStep(ItemStack seed, ItemStack soil, float growth, TileEntity tile, float fertilizer, boolean render)
		{
			return .003125f*fertilizer;
		}

		@Override
		public ItemStack[] getOutput(ItemStack seed, ItemStack soil, TileEntity tile)
		{
			return seedOutputMap.get(new ComparableItemStack(seed, false, false));
		}

		@Override
		@SideOnly(Side.CLIENT)
		public IBlockState[] getRenderedPlant(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
		{
			IBlockState[] states = seedRenderMap.get(new ComparableItemStack(seed, false, false));
			if(states!=null)
				return states;
			return null;
		}

		public void register(ItemStack seed, ItemStack[] output, Object soil, IBlockState... render)
		{
			register(seed, output, ApiUtils.createIngredientStack(soil), render);
		}

		public void register(ItemStack seed, ItemStack[] output, IngredientStack soil, IBlockState... render)
		{
			ComparableItemStack comp = new ComparableItemStack(seed, false, false);
			getSeedSet().add(comp);
			seedSoilMap.put(comp, soil);
			seedOutputMap.put(comp, output);
			seedRenderMap.put(comp, render);
		}
	}

	public static DefaultPlantHandler cropHandler = new DefaultPlantHandler()
	{
		private HashSet<ComparableItemStack> validSeeds = new HashSet<>();

		@Override
		protected HashSet<ComparableItemStack> getSeedSet()
		{
			return validSeeds;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public IBlockState[] getRenderedPlant(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
		{
			IBlockState[] states = seedRenderMap.get(new ComparableItemStack(seed, false, false));
			if(states!=null)
			{
				IBlockState[] ret = new IBlockState[states.length];
				for(int i = 0; i < states.length; i++)
					if(states[i]!=null)
						if(states[i].getBlock() instanceof BlockCrops)
						{
							int max = ((BlockCrops)states[i].getBlock()).getMaxAge();
							ret[i] = ((BlockCrops)states[i].getBlock()).withAge(Math.min(max, Math.round(max*growth)));
						}
						else
						{
							for(IProperty prop : states[i].getPropertyKeys())
								if("age".equals(prop.getName())&&prop instanceof PropertyInteger)
								{
									int max = 0;
									for(Integer allowed : ((PropertyInteger)prop).getAllowedValues())
										if(allowed!=null&&allowed > max)
											max = allowed;
									ret[i] = states[i].withProperty(prop, Math.min(max, Math.round(max*growth)));
								}
							if(ret[i]==null)
								ret[i] = states[i];
						}
				return ret;
			}
			return null;
		}
	};
	public static DefaultPlantHandler stemHandler = new DefaultPlantHandler()
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
			return (growth < .5?.00625f: .0015625f)*fertilizer;
		}

		@Override
		public float resetGrowth(ItemStack seed, ItemStack soil, float growth, TileEntity tile, boolean render)
		{
			return .5f;
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
			return 1f;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public boolean overrideRender(ItemStack seed, ItemStack soil, float growth, TileEntity tile, BlockRendererDispatcher blockRenderer)
		{
			ComparableItemStack comp = new ComparableItemStack(seed, false, false);
			IBlockState[] renderStates = seedRenderMap.get(comp);
			if(renderStates.length > 0&&renderStates[0]!=null&&renderStates[0].getBlock() instanceof BlockStem)
			{
				GlStateManager.rotate(-90, 0, 1, 0);
				BlockStem stem = (BlockStem)renderStates[0].getBlock();
				IBlockState state = stem.getDefaultState().withProperty(BlockStem.AGE, (int)(growth >= .5?7: 2*growth*7));
				if(growth >= .5)
					state = state.withProperty(BlockStem.FACING, EnumFacing.NORTH);
				IBakedModel model = blockRenderer.getModelForState(state);
				GlStateManager.translate(.25f, .0625f, 0);
				GlStateManager.pushMatrix();
				blockRenderer.getBlockModelRenderer().renderModelBrightness(model, state, 1, true);
				GlStateManager.popMatrix();
				if(growth >= .5)
				{
					ItemStack[] fruit = seedOutputMap.get(new ComparableItemStack(seed, false, false));
					if(fruit!=null&&fruit.length > 0&&!fruit[0].isEmpty())
					{
						Block fruitBlock = Block.getBlockFromItem(fruit[0].getItem());
						if(fruitBlock!=null)
						{
							state = fruitBlock.getDefaultState();
							model = blockRenderer.getModelForState(state);
							GlStateManager.pushMatrix();
							float scale = (growth-.5f)*.5f;
							GlStateManager.translate(-scale/2, .5-scale, -.5+scale/2);
							GlStateManager.scale(scale, scale, scale);
							blockRenderer.getBlockModelRenderer().renderModelBrightness(model, state, 1, true);
							GlStateManager.popMatrix();
						}
					}
				}
			}
			return true;
		}
	};
	public static DefaultPlantHandler stackingHandler = new DefaultPlantHandler()
	{
		private HashSet<ComparableItemStack> validSeeds = new HashSet<>();

		@Override
		protected HashSet<ComparableItemStack> getSeedSet()
		{
			return validSeeds;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public IBlockState[] getRenderedPlant(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
		{
			IBlockState[] states = seedRenderMap.get(new ComparableItemStack(seed, false, false));
			if(states!=null)
				return states;
			return null;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public float getRenderSize(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
		{
			IBlockState[] states = seedRenderMap.get(new ComparableItemStack(seed, false, false));
			if(states!=null&&states.length > 2)
				return .6875f-(states.length)*.0625f;
			return .6875f;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public boolean overrideRender(ItemStack seed, ItemStack soil, float growth, TileEntity tile, BlockRendererDispatcher blockRenderer)
		{
			IBlockState[] states = seedRenderMap.get(new ComparableItemStack(seed, false, false));
			if(states!=null)
				GlStateManager.translate(0, (-1+growth)*(states.length-1), 0);
			return false;
		}
	};

	public static void init()
	{
		soilTextureMap.put(new ComparableItemStack(new ItemStack(Blocks.DIRT), false, false), new ResourceLocation("minecraft:blocks/farmland_wet"));
		registerHandler(cropHandler);
		registerHandler(stemHandler);
		registerHandler(stackingHandler);

		cropHandler.register(new ItemStack(Items.WHEAT_SEEDS), new ItemStack[]{new ItemStack(Items.WHEAT, 2), new ItemStack(Items.WHEAT_SEEDS, 1)}, new ItemStack(Blocks.DIRT), Blocks.WHEAT.getDefaultState());
		cropHandler.register(new ItemStack(Items.POTATO), new ItemStack[]{new ItemStack(Items.POTATO, 2)}, new ItemStack(Blocks.DIRT), Blocks.POTATOES.getDefaultState());
		cropHandler.register(new ItemStack(Items.CARROT), new ItemStack[]{new ItemStack(Items.CARROT, 2)}, new ItemStack(Blocks.DIRT), Blocks.CARROTS.getDefaultState());
		cropHandler.register(new ItemStack(Items.BEETROOT_SEEDS), new ItemStack[]{new ItemStack(Items.BEETROOT, 2), new ItemStack(Items.BEETROOT_SEEDS, 1)}, new ItemStack(Blocks.DIRT), Blocks.BEETROOTS.getDefaultState());
		cropHandler.register(new ItemStack(Items.NETHER_WART), new ItemStack[]{new ItemStack(Items.NETHER_WART, 2)}, new ItemStack(Blocks.SOUL_SAND), Blocks.NETHER_WART.getDefaultState());

		stemHandler.register(new ItemStack(Items.PUMPKIN_SEEDS), new ItemStack[]{new ItemStack(Blocks.PUMPKIN)}, new ItemStack(Blocks.DIRT), Blocks.PUMPKIN_STEM.getDefaultState());
		stemHandler.register(new ItemStack(Items.MELON_SEEDS), new ItemStack[]{new ItemStack(Blocks.MELON_BLOCK)}, new ItemStack(Blocks.DIRT), Blocks.MELON_STEM.getDefaultState());

		stackingHandler.register(new ItemStack(Items.REEDS), new ItemStack[]{new ItemStack(Items.REEDS, 2)}, "sand", Blocks.REEDS.getDefaultState(), Blocks.REEDS.getDefaultState());
		stackingHandler.register(new ItemStack(Blocks.CACTUS), new ItemStack[]{new ItemStack(Blocks.CACTUS, 2)}, "sand", Blocks.CACTUS.getDefaultState(), Blocks.CACTUS.getDefaultState());
		stackingHandler.register(new ItemStack(Blocks.CHORUS_FLOWER), new ItemStack[]{new ItemStack(Items.CHORUS_FRUIT, 1)}, new ItemStack(Blocks.END_STONE), Blocks.CHORUS_PLANT.getDefaultState().withProperty(BlockChorusPlant.DOWN, true).withProperty(BlockChorusPlant.UP, true), Blocks.CHORUS_PLANT.getDefaultState().withProperty(BlockChorusPlant.DOWN, true).withProperty(BlockChorusPlant.UP, true), Blocks.CHORUS_FLOWER.getDefaultState());

		IngredientStack shroomSoil = new IngredientStack(ImmutableList.of(new ItemStack(Blocks.MYCELIUM), new ItemStack(Blocks.DIRT, 1, 2)));
		cropHandler.register(new ItemStack(Blocks.RED_MUSHROOM), new ItemStack[]{new ItemStack(Blocks.RED_MUSHROOM, 2)}, shroomSoil, Blocks.RED_MUSHROOM.getDefaultState());
		cropHandler.register(new ItemStack(Blocks.BROWN_MUSHROOM), new ItemStack[]{new ItemStack(Blocks.BROWN_MUSHROOM, 2)}, shroomSoil, Blocks.BROWN_MUSHROOM.getDefaultState());

		registerFluidFertilizer(new FluidFertilizerHandler()
		{
			@Override
			public boolean isValid(@Nullable FluidStack fertilizer)
			{
				return fertilizer!=null&&fertilizer.getFluid()==FluidRegistry.WATER;
			}

			@Override
			public float getGrowthMultiplier(FluidStack fertilizer, ItemStack seed, ItemStack soil, TileEntity tile)
			{
				return fluidFertilizerModifier;
			}
		});
		registerItemFertilizer(new ItemFertilizerHandler()
		{
			final ItemStack bonemeal = new ItemStack(Items.DYE, 1, 15);

			@Override
			public boolean isValid(ItemStack fertilizer)
			{
				return !fertilizer.isEmpty()&&OreDictionary.itemMatches(bonemeal, fertilizer, true);
			}

			@Override
			public float getGrowthMultiplier(ItemStack fertilizer, ItemStack seed, ItemStack soil, TileEntity tile)
			{
				return solidFertilizerModifier*1.25f;
			}
		});
	}
}