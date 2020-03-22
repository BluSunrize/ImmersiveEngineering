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
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.*;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.IProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
				return ItemStack.areItemsEqual(stack, fertilizer);
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

		@OnlyIn(Dist.CLIENT)
		BlockState[] getRenderedPlant(ItemStack seed, ItemStack soil, float growth, TileEntity tile);

		@OnlyIn(Dist.CLIENT)
		default boolean overrideRender(ItemStack seed, ItemStack soil, float growth, TileEntity tile, BlockRendererDispatcher blockRenderer)
		{
			return false;
		}

		@OnlyIn(Dist.CLIENT)
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

	private static Map<ComparableItemStack, IngredientStack> seedSoilMap = new HashMap<>();
	private static Map<ComparableItemStack, ItemStack[]> seedOutputMap = new HashMap<>();
	private static Map<ComparableItemStack, BlockState[]> seedRenderMap = new HashMap<>();
	private static Map<StemBlock, AttachedStemBlock> stemToAttachedStem = new HashMap<>();

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
		@OnlyIn(Dist.CLIENT)
		public BlockState[] getRenderedPlant(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
		{
			BlockState[] states = seedRenderMap.get(new ComparableItemStack(seed, false, false));
			if(states!=null)
				return states;
			return null;
		}

		public void register(ItemStack seed, ItemStack[] output, Object soil, BlockState... render)
		{
			register(seed, output, ApiUtils.createIngredientStack(soil), render);
		}

		public void register(ItemStack seed, ItemStack[] output, IngredientStack soil, BlockState... render)
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
		@OnlyIn(Dist.CLIENT)
		public BlockState[] getRenderedPlant(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
		{
			BlockState[] states = seedRenderMap.get(new ComparableItemStack(seed, false, false));
			if(states!=null)
			{
				BlockState[] ret = new BlockState[states.length];
				for(int i = 0; i < states.length; i++)
					if(states[i]!=null)
						if(states[i].getBlock() instanceof CropsBlock)
						{
							int max = ((CropsBlock)states[i].getBlock()).getMaxAge();
							ret[i] = ((CropsBlock)states[i].getBlock()).withAge(Math.min(max, Math.round(max*growth)));
						}
						else
						{
							for(IProperty<?> prop : states[i].getProperties())
								if("age".equals(prop.getName())&&prop instanceof IntegerProperty)
								{
									int max = 0;
									for(Integer allowed : ((IntegerProperty)prop).getAllowedValues())
										if(allowed!=null&&allowed > max)
											max = allowed;
									ret[i] = states[i].with((IntegerProperty)prop, Math.min(max, Math.round(max*growth)));
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
		@OnlyIn(Dist.CLIENT)
		public BlockState[] getRenderedPlant(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
		{
			return new BlockState[0];
		}

		@Override
		@OnlyIn(Dist.CLIENT)
		public float getRenderSize(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
		{
			return 1f;
		}

		@Override
		@OnlyIn(Dist.CLIENT)
		public boolean overrideRender(ItemStack seed, ItemStack soil, float growth, TileEntity tile, BlockRendererDispatcher blockRenderer)
		{
			ComparableItemStack comp = new ComparableItemStack(seed, false, false);
			BlockState[] renderStates = seedRenderMap.get(comp);
			if(renderStates.length > 0&&renderStates[0]!=null&&renderStates[0].getBlock() instanceof StemBlock)
			{
				GlStateManager.rotatef(-90, 0, 1, 0);
				boolean renderFruit = growth >= 0.5;
				StemBlock stem = (StemBlock)renderStates[0].getBlock();
				BlockState state;
				if(renderFruit)
				{
					Preconditions.checkArgument(stemToAttachedStem.containsKey(stem));
					AttachedStemBlock attached = stemToAttachedStem.get(stem);
					state = attached.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH);
				}
				else
					state = stem.getDefaultState().with(StemBlock.AGE, (int)(renderFruit?7: 2*growth*7));
				IBakedModel model = blockRenderer.getModelForState(state);
				GlStateManager.translatef(.25f, .0625f, 0);
				GlStateManager.pushMatrix();
				blockRenderer.getBlockModelRenderer().renderModelBrightness(model, state, 1, true);
				GlStateManager.popMatrix();
				if(renderFruit)
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
							GlStateManager.translated(-scale/2, .5-scale, -.5+scale/2);
							GlStateManager.scalef(scale, scale, scale);
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
		@OnlyIn(Dist.CLIENT)
		public BlockState[] getRenderedPlant(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
		{
			BlockState[] states = seedRenderMap.get(new ComparableItemStack(seed, false, false));
			if(states!=null)
				return states;
			return null;
		}

		@Override
		@OnlyIn(Dist.CLIENT)
		public float getRenderSize(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
		{
			BlockState[] states = seedRenderMap.get(new ComparableItemStack(seed, false, false));
			if(states!=null&&states.length > 2)
				return .6875f-(states.length)*.0625f;
			return .6875f;
		}

		@Override
		@OnlyIn(Dist.CLIENT)
		public boolean overrideRender(ItemStack seed, ItemStack soil, float growth, TileEntity tile, BlockRendererDispatcher blockRenderer)
		{
			BlockState[] states = seedRenderMap.get(new ComparableItemStack(seed, false, false));
			if(states!=null)
				GlStateManager.translatef(0, (-1+growth)*(states.length-1), 0);
			return false;
		}
	};

	public static void init()
	{
		soilTextureMap.put(new ComparableItemStack(new ItemStack(Blocks.DIRT), false, false), new ResourceLocation("block/farmland_moist"));
		registerHandler(cropHandler);
		registerHandler(stemHandler);
		registerHandler(stackingHandler);

		cropHandler.register(new ItemStack(Items.WHEAT_SEEDS), new ItemStack[]{new ItemStack(Items.WHEAT, 2), new ItemStack(Items.WHEAT_SEEDS, 1)}, new ItemStack(Blocks.DIRT), Blocks.WHEAT.getDefaultState());
		cropHandler.register(new ItemStack(Items.POTATO), new ItemStack[]{new ItemStack(Items.POTATO, 2)}, new ItemStack(Blocks.DIRT), Blocks.POTATOES.getDefaultState());
		cropHandler.register(new ItemStack(Items.CARROT), new ItemStack[]{new ItemStack(Items.CARROT, 2)}, new ItemStack(Blocks.DIRT), Blocks.CARROTS.getDefaultState());
		cropHandler.register(new ItemStack(Items.BEETROOT_SEEDS), new ItemStack[]{new ItemStack(Items.BEETROOT, 2), new ItemStack(Items.BEETROOT_SEEDS, 1)}, new ItemStack(Blocks.DIRT), Blocks.BEETROOTS.getDefaultState());
		cropHandler.register(new ItemStack(Items.NETHER_WART), new ItemStack[]{new ItemStack(Items.NETHER_WART, 2)}, new ItemStack(Blocks.SOUL_SAND), Blocks.NETHER_WART.getDefaultState());

		stemHandler.register(new ItemStack(Items.PUMPKIN_SEEDS), new ItemStack[]{new ItemStack(Blocks.PUMPKIN)}, new ItemStack(Blocks.DIRT), Blocks.PUMPKIN_STEM.getDefaultState());
		stemHandler.register(new ItemStack(Items.MELON_SEEDS), new ItemStack[]{new ItemStack(Blocks.MELON)}, new ItemStack(Blocks.DIRT), Blocks.MELON_STEM.getDefaultState());
		stemToAttachedStem.put((StemBlock)Blocks.PUMPKIN_STEM, (AttachedStemBlock)Blocks.ATTACHED_PUMPKIN_STEM);
		stemToAttachedStem.put((StemBlock)Blocks.MELON_STEM, (AttachedStemBlock)Blocks.ATTACHED_MELON_STEM);

		stackingHandler.register(new ItemStack(Items.SUGAR_CANE), new ItemStack[]{new ItemStack(Items.SUGAR_CANE, 2)}, BlockTags.SAND, Blocks.SUGAR_CANE.getDefaultState(), Blocks.SUGAR_CANE.getDefaultState());
		stackingHandler.register(new ItemStack(Blocks.CACTUS), new ItemStack[]{new ItemStack(Blocks.CACTUS, 2)}, BlockTags.SAND, Blocks.CACTUS.getDefaultState(), Blocks.CACTUS.getDefaultState());
		stackingHandler.register(new ItemStack(Blocks.CHORUS_FLOWER), new ItemStack[]{new ItemStack(Items.CHORUS_FRUIT, 1)}, new ItemStack(Blocks.END_STONE), Blocks.CHORUS_PLANT.getDefaultState().with(ChorusPlantBlock.DOWN, true).with(ChorusPlantBlock.UP, true), Blocks.CHORUS_PLANT.getDefaultState().with(ChorusPlantBlock.DOWN, true).with(ChorusPlantBlock.UP, true), Blocks.CHORUS_FLOWER.getDefaultState());

		IngredientStack shroomSoil = new IngredientStack(ImmutableList.of(new ItemStack(Blocks.MYCELIUM), new ItemStack(Blocks.PODZOL)));
		cropHandler.register(new ItemStack(Blocks.RED_MUSHROOM), new ItemStack[]{new ItemStack(Blocks.RED_MUSHROOM, 2)}, shroomSoil, Blocks.RED_MUSHROOM.getDefaultState());
		cropHandler.register(new ItemStack(Blocks.BROWN_MUSHROOM), new ItemStack[]{new ItemStack(Blocks.BROWN_MUSHROOM, 2)}, shroomSoil, Blocks.BROWN_MUSHROOM.getDefaultState());

		registerFluidFertilizer(new FluidFertilizerHandler()
		{
			@Override
			public boolean isValid(@Nullable FluidStack fertilizer)
			{
				return fertilizer!=null&&fertilizer.getFluid()==Fluids.WATER;
			}

			@Override
			public float getGrowthMultiplier(FluidStack fertilizer, ItemStack seed, ItemStack soil, TileEntity tile)
			{
				return fluidFertilizerModifier;
			}
		});
		registerItemFertilizer(new ItemFertilizerHandler()
		{
			final ItemStack bonemeal = new ItemStack(Items.BONE_MEAL, 1);

			@Override
			public boolean isValid(ItemStack fertilizer)
			{
				return !fertilizer.isEmpty()&&ItemStack.areItemsEqual(bonemeal, fertilizer);
			}

			@Override
			public float getGrowthMultiplier(ItemStack fertilizer, ItemStack seed, ItemStack soil, TileEntity tile)
			{
				return solidFertilizerModifier*1.25f;
			}
		});
	}
}