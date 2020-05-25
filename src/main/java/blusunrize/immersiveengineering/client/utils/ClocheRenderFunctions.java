package blusunrize.immersiveengineering.client.utils;

import blusunrize.immersiveengineering.api.crafting.ClocheRecipe;
import blusunrize.immersiveengineering.api.crafting.ClocheRenderFunction;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Misc;
import blusunrize.immersiveengineering.common.blocks.plant.EnumHempGrowth;
import blusunrize.immersiveengineering.common.blocks.plant.HempBlock;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.*;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.state.IProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;

public class ClocheRenderFunctions
{
	private static final TransformationMatrix DEFAULT_TRANSFORMATION = new TransformationMatrix(null);

	public static void init()
	{
		// register farmland texture
		ClocheRecipe.registerSoilTexture(Ingredient.fromStacks(new ItemStack(Items.DIRT), new ItemStack(Items.COARSE_DIRT),
				new ItemStack(Items.GRASS_BLOCK), new ItemStack(Items.GRASS_PATH)), new ResourceLocation("block/farmland_moist"));

		// register defaults
		ClocheRenderFunction.RENDER_FUNCTION_FACTORIES.put("crop", RenderFunctionCrop::new);
		ClocheRenderFunction.RENDER_FUNCTION_FACTORIES.put("stacking", RenderFunctionStacking::new);
		ClocheRenderFunction.RENDER_FUNCTION_FACTORIES.put("stem", RenderFunctionStem::new);
		ClocheRenderFunction.RENDER_FUNCTION_FACTORIES.put("generic", RenderFunctionGeneric::new);

		ClocheRenderFunction.RENDER_FUNCTION_FACTORIES.put("hemp", block -> new RenderFunctionHemp());
		ClocheRenderFunction.RENDER_FUNCTION_FACTORIES.put("chorus", block -> new RenderFunctionChorus());
	}

	public static class RenderFunctionCrop implements ClocheRenderFunction
	{
		final Block cropBlock;
		int maxAge;
		IntegerProperty ageProperty;

		public RenderFunctionCrop(Block cropBlock)
		{
			this.cropBlock = cropBlock;
			if(cropBlock instanceof CropsBlock)
			{
				this.maxAge = ((CropsBlock)cropBlock).getMaxAge();
				this.ageProperty = ((CropsBlock)cropBlock).getAgeProperty();
			}
			else
			{
				for(IProperty<?> prop : cropBlock.getDefaultState().getProperties())
					if("age".equals(prop.getName())&&prop instanceof IntegerProperty)
					{
						int tmp = -1;
						for(Integer allowed : ((IntegerProperty)prop).getAllowedValues())
							if(allowed!=null&&allowed > tmp)
								tmp = allowed;
						if(tmp > 0)
						{
							this.maxAge = tmp;
							this.ageProperty = (IntegerProperty)prop;
							break;
						}
					}
			}
			if(this.ageProperty==null||this.maxAge <= 0)
				throw new IllegalArgumentException("Block "+cropBlock.getTranslationKey()+" is not a valid crop block");
		}

		@Override
		public float getScale(ItemStack seed, float growth)
		{
			return 0.875f;
		}

		@Override
		public Collection<Pair<BlockState, TransformationMatrix>> getBlocks(ItemStack stack, float growth)
		{
			int age = Math.min(this.maxAge, Math.round(this.maxAge*growth));
			return ImmutableList.of(Pair.of(this.cropBlock.getDefaultState().with(this.ageProperty, age), DEFAULT_TRANSFORMATION));
		}
	}

	public static class RenderFunctionStacking implements ClocheRenderFunction
	{
		final Block cropBlock;

		public RenderFunctionStacking(Block cropBlock)
		{
			this.cropBlock = cropBlock;
		}

		@Override
		public float getScale(ItemStack seed, float growth)
		{
			return 0.6875f;
		}

		@Override
		public Collection<Pair<BlockState, TransformationMatrix>> getBlocks(ItemStack stack, float growth)
		{
			TransformationMatrix bottom = new TransformationMatrix(new Vector3f(0, growth-1, 0), null, null, null);
			TransformationMatrix top = new TransformationMatrix(new Vector3f(0, growth, 0), null, null, null);
			return ImmutableList.of(
					Pair.of(this.cropBlock.getDefaultState(), bottom),
					Pair.of(this.cropBlock.getDefaultState(), top));
		}
	}

	public static class RenderFunctionStem implements ClocheRenderFunction
	{
		final StemGrownBlock cropBlock;
		final StemBlock stemBlock;
		final AttachedStemBlock attachedStemBlock;

		public RenderFunctionStem(Block cropBlock)
		{
			Preconditions.checkArgument(cropBlock instanceof StemGrownBlock);
			this.cropBlock = (StemGrownBlock)cropBlock;
			this.stemBlock = this.cropBlock.getStem();
			this.attachedStemBlock = this.cropBlock.getAttachedStem();
		}

		@Override
		public float getScale(ItemStack seed, float growth)
		{
			return 1;
		}

		@Override
		public Collection<Pair<BlockState, TransformationMatrix>> getBlocks(ItemStack stack, float growth)
		{
			MatrixStack transform = new MatrixStack();
			transform.translate(0, .0625f, 0.25f);

			if(growth < .375)
			{
				int age = Math.round(7*growth/.375f);
				return ImmutableList.of(Pair.of(this.stemBlock.getDefaultState().with(StemBlock.AGE, age),
						new TransformationMatrix(transform.getLast().getMatrix())));
			}
			else
			{
				float scale = ((growth-.375f)/.625f)*.3125f;
				TransformationMatrix cropMatrix = new TransformationMatrix(
						new Vector3f(0.5f-scale/2, .5625f-scale, 0.25f-scale/2),
						null,
						new Vector3f(scale, scale, scale),
						null
				);
				return ImmutableList.of(
						Pair.of(this.attachedStemBlock.getDefaultState(), new TransformationMatrix(transform.getLast().getMatrix())),
						Pair.of(this.cropBlock.getDefaultState(), cropMatrix)
				);
			}
		}
	}

	public static class RenderFunctionGeneric implements ClocheRenderFunction
	{
		final Block cropBlock;

		public RenderFunctionGeneric(Block cropBlock)
		{
			this.cropBlock = cropBlock;
		}

		@Override
		public float getScale(ItemStack seed, float growth)
		{
			return 0.75f;
		}

		@Override
		public Collection<Pair<BlockState, TransformationMatrix>> getBlocks(ItemStack stack, float growth)
		{
			Vector3f transl = new Vector3f(0.5f-growth/2, 0, 0.5f-growth/2);
			Vector3f scale = new Vector3f(growth, growth, growth);
			return ImmutableList.of(Pair.of(this.cropBlock.getDefaultState(), new TransformationMatrix(transl, null, scale, null)));
		}
	}

	public static class RenderFunctionChorus implements ClocheRenderFunction
	{
		@Override
		public float getScale(ItemStack seed, float growth)
		{
			return 0.5f;
		}

		@Override
		public Collection<Pair<BlockState, TransformationMatrix>> getBlocks(ItemStack stack, float growth)
		{
			growth *= 2;
			TransformationMatrix bottom = new TransformationMatrix(new Vector3f(0, growth-2, 0), null, null, null);
			TransformationMatrix middle = new TransformationMatrix(new Vector3f(0, growth-1, 0), null, null, null);
			TransformationMatrix top = new TransformationMatrix(new Vector3f(0, growth, 0), null, null, null);
			BlockState stem = Blocks.CHORUS_PLANT.getDefaultState().with(ChorusPlantBlock.DOWN, true).with(ChorusPlantBlock.UP, true);
			return ImmutableList.of(
					Pair.of(stem, bottom),
					Pair.of(stem, middle),
					Pair.of(Blocks.CHORUS_FLOWER.getDefaultState(), top));
		}
	}

	public static class RenderFunctionHemp implements ClocheRenderFunction
	{
		@Override
		public float getScale(ItemStack seed, float growth)
		{
			return 0.6875f;
		}

		@Override
		public Collection<Pair<BlockState, TransformationMatrix>> getBlocks(ItemStack stack, float growth)
		{
			int age = Math.min(4, Math.round(growth*4));
			if(age==4)
			{
				TransformationMatrix top = new TransformationMatrix(new Vector3f(0, 1, 0), null, null, null);
				return ImmutableList.of(
						Pair.of(Misc.hempPlant.getDefaultState().with(HempBlock.GROWTH, EnumHempGrowth.BOTTOM4), new TransformationMatrix(null)),
						Pair.of(Misc.hempPlant.getDefaultState().with(HempBlock.GROWTH, EnumHempGrowth.TOP0), top)
				);
			}
			return ImmutableList.of(Pair.of(Misc.hempPlant.getDefaultState().with(HempBlock.GROWTH, EnumHempGrowth.values()[age]), new TransformationMatrix(null)));
		}
	}
}
