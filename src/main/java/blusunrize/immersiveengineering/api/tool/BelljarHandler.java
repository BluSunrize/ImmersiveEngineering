package blusunrize.immersiveengineering.api.tool;

import blusunrize.immersiveengineering.api.ComparableItemStack;
import net.minecraft.block.Block;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author BluSunrize - 09.03.2017
 *         <p>
 *         A handler for plantgrowth within the belljars/cloches
 */
public class BelljarHandler
{
	private static HashSet<IPlantHandler> handlers = new HashSet<>();
	private static HashMap<ComparableItemStack, ResourceLocation> soilTextureMap = new HashMap<>();

	public static void registerHandler(IPlantHandler handler)
	{
		handlers.add(handler);
	}
	public static IPlantHandler getHandler(ItemStack seed)
	{
		if(seed==null)
			return null;
		for(IPlantHandler handler : handlers)
			if(handler.isValid(seed))
				return handler;
		return null;
	}

	public static ResourceLocation getSoilTexture(ItemStack soil)
	{
		return soilTextureMap.get(new ComparableItemStack(soil));
	}

	public interface IPlantHandler extends IPlantRenderer
	{
		boolean isCorrectSoil(ItemStack seed, ItemStack soil);
		float getGrowthStep(ItemStack seed, ItemStack soil, float growth, TileEntity tile);
		ItemStack[] getOutput(ItemStack seed, ItemStack soil, TileEntity tile);
		default float resetGrowth(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
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


	private static HashMap<ComparableItemStack, ItemStack> seedSoilMap = new HashMap<>();
	private static HashMap<ComparableItemStack, ItemStack[]> seedOutputMap = new HashMap<>();
	private static HashMap<ComparableItemStack, IBlockState> seedRenderMap = new HashMap<>();

	public abstract static class DefaultPlantHandler implements IPlantHandler
	{
		protected abstract HashSet<ComparableItemStack> getSeedSet();

		@Override
		public boolean isValid(ItemStack seed)
		{
			return seed!=null&&getSeedSet().contains(new ComparableItemStack(seed));
		}
		@Override
		public boolean isCorrectSoil(ItemStack seed, ItemStack soil)
		{
			ItemStack reqSoil = seedSoilMap.get(new ComparableItemStack(seed));
			return OreDictionary.itemMatches(reqSoil, soil, false);
		}
		@Override
		public float getGrowthStep(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
		{
			return .00625f;
		}
		@Override
		public ItemStack[] getOutput(ItemStack seed, ItemStack soil, TileEntity tile)
		{
			return seedOutputMap.get(new ComparableItemStack(seed));
		}

		@Override
		@SideOnly(Side.CLIENT)
		public IBlockState[] getRenderedPlant(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
		{
			IBlockState state = seedRenderMap.get(new ComparableItemStack(seed));
			if(state!=null)
				return new IBlockState[]{state};
			return null;
		}

		public void register(ItemStack seed, ItemStack[] output, ItemStack soil, IBlockState render)
		{
			ComparableItemStack comp = new ComparableItemStack(seed);
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
			IBlockState state = seedRenderMap.get(new ComparableItemStack(seed));
			if(state!=null)
			{
				if(state.getBlock() instanceof BlockCrops)
				{
					int max = ((BlockCrops)state.getBlock()).getMaxAge();
					return new IBlockState[]{((BlockCrops)state.getBlock()).withAge(Math.min(max, Math.round(max*growth)))};
				}
				else
					for(IProperty prop : state.getPropertyNames())
						if("age".equals(prop.getName()) && prop instanceof PropertyInteger)
						{
							int max = 0;
							for(Integer i : ((PropertyInteger)prop).getAllowedValues())
								if(i!=null && i>max)
									max = i;
							return new IBlockState[]{state.withProperty(prop, Math.min(max, Math.round(max*growth)))};
						}
				return new IBlockState[]{state};
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
		public float getGrowthStep(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
		{
			return growth<.5?.00625f:.003125f;
		}
		@Override
		public float resetGrowth(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
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
			ComparableItemStack comp = new ComparableItemStack(seed);
			IBlockState renderState = seedRenderMap.get(comp);
			if(renderState.getBlock() instanceof BlockStem)
			{
				BlockStem stem = (BlockStem)renderState.getBlock();
				IBlockState state = stem.getDefaultState().withProperty(BlockStem.AGE, (int)(growth >= .5?7: 2*growth*7));
				if(growth>=.5)
					state = state.withProperty(BlockStem.FACING, EnumFacing.NORTH);
				IBakedModel model = blockRenderer.getModelForState(state);
				GlStateManager.translate(.25f,.0625f,0);
				GlStateManager.pushMatrix();
				blockRenderer.getBlockModelRenderer().renderModelBrightness(model, state, 1, true);
				GlStateManager.popMatrix();
				if(growth>=.5)
				{
					ItemStack[] fruit = seedOutputMap.get(new ComparableItemStack(seed));
					if(fruit!=null&&fruit.length>0&&fruit[0]!=null&&fruit[0].getItem()!=null)
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
			IBlockState state = seedRenderMap.get(new ComparableItemStack(seed));
			if(state!=null)
				return new IBlockState[]{state,state};
			return null;
		}
		@Override
		@SideOnly(Side.CLIENT)
		public float getRenderSize(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
		{
			return .6875f;
		}
	};

	static
	{
		soilTextureMap.put(new ComparableItemStack(new ItemStack(Blocks.DIRT)), new ResourceLocation("minecraft:blocks/farmland_wet"));
		registerHandler(cropHandler);
		registerHandler(stemHandler);
		registerHandler(stackingHandler);

		cropHandler.register(new ItemStack(Items.WHEAT_SEEDS), new ItemStack[]{new ItemStack(Items.WHEAT),new ItemStack(Items.WHEAT_SEEDS,2)}, new ItemStack(Blocks.DIRT), Blocks.WHEAT.getDefaultState());
		cropHandler.register(new ItemStack(Items.POTATO), new ItemStack[]{new ItemStack(Items.POTATO,2)}, new ItemStack(Blocks.DIRT), Blocks.POTATOES.getDefaultState());
		cropHandler.register(new ItemStack(Items.CARROT), new ItemStack[]{new ItemStack(Items.CARROT,2)}, new ItemStack(Blocks.DIRT), Blocks.CARROTS.getDefaultState());
		cropHandler.register(new ItemStack(Items.BEETROOT_SEEDS), new ItemStack[]{new ItemStack(Items.BEETROOT),new ItemStack(Items.BEETROOT_SEEDS,2)}, new ItemStack(Blocks.DIRT), Blocks.BEETROOTS.getDefaultState());
		cropHandler.register(new ItemStack(Items.NETHER_WART), new ItemStack[]{new ItemStack(Items.NETHER_WART,2)}, new ItemStack(Blocks.SOUL_SAND), Blocks.NETHER_WART.getDefaultState());

		stemHandler.register(new ItemStack(Items.PUMPKIN_SEEDS), new ItemStack[]{new ItemStack(Blocks.PUMPKIN)}, new ItemStack(Blocks.DIRT), Blocks.PUMPKIN_STEM.getDefaultState());
		stemHandler.register(new ItemStack(Items.MELON_SEEDS), new ItemStack[]{new ItemStack(Blocks.MELON_BLOCK)}, new ItemStack(Blocks.DIRT), Blocks.MELON_STEM.getDefaultState());

		stackingHandler.register(new ItemStack(Items.REEDS), new ItemStack[]{new ItemStack(Items.REEDS,2)}, new ItemStack(Blocks.SAND), Blocks.REEDS.getDefaultState());
		stackingHandler.register(new ItemStack(Blocks.CACTUS), new ItemStack[]{new ItemStack(Blocks.CACTUS,2)}, new ItemStack(Blocks.SAND), Blocks.CACTUS.getDefaultState());

		cropHandler.register(new ItemStack(Blocks.RED_MUSHROOM), new ItemStack[]{new ItemStack(Blocks.RED_MUSHROOM,2)}, new ItemStack(Blocks.MYCELIUM), Blocks.RED_MUSHROOM.getDefaultState());
		cropHandler.register(new ItemStack(Blocks.BROWN_MUSHROOM), new ItemStack[]{new ItemStack(Blocks.BROWN_MUSHROOM,2)}, new ItemStack(Blocks.MYCELIUM), Blocks.BROWN_MUSHROOM.getDefaultState());

	}
}