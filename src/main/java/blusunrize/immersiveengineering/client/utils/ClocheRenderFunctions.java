/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.utils;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.crafting.ClocheRecipe;
import blusunrize.immersiveengineering.api.crafting.ClocheRenderFunction;
import blusunrize.immersiveengineering.common.blocks.plant.HempBlock;
import blusunrize.immersiveengineering.common.register.IEBlocks.Misc;
import blusunrize.immersiveengineering.mixin.accessors.CropBlockAccess;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.function.Function;

public class ClocheRenderFunctions
{
	public static void init()
	{
		// register farmland texture
		ClocheRecipe.registerSoilTexture(Ingredient.of(new ItemStack(Items.DIRT), new ItemStack(Items.COARSE_DIRT),
				new ItemStack(Items.GRASS_BLOCK), new ItemStack(Items.DIRT_PATH)), ResourceLocation.withDefaultNamespace("block/farmland_moist"));

		// register defaults
		register("crop", RenderFunctionCrop.CODEC);
		register("stacking", RenderFunctionStacking.CODEC);
		register("stem", RenderFunctionStem.CODEC);
		register("generic", RenderFunctionGeneric.CODEC);

		register("hemp", RenderFunctionHemp.CODEC);
		register("chorus", RenderFunctionChorus.CODEC);
	}

	private static void register(String path, MapCodec<? extends ClocheRenderFunction> codec)
	{
		ClocheRenderFunction.RENDER_FUNCTION_FACTORIES.put(IEApi.ieLoc(path), codec);
	}

	private static <F extends ClocheRenderFunction>
	MapCodec<F> byBlockCodec(Function<F, Block> getBlock, Function<Block, F> make)
	{
		return BuiltInRegistries.BLOCK.byNameCodec().xmap(make, getBlock).fieldOf("block");
	}

	public static class RenderFunctionCrop implements ClocheRenderFunction
	{
		public static final MapCodec<RenderFunctionCrop> CODEC = byBlockCodec(f -> f.cropBlock, RenderFunctionCrop::new);

		final Block cropBlock;
		int maxAge;
		IntegerProperty ageProperty;

		public RenderFunctionCrop(Block cropBlock)
		{
			this.cropBlock = cropBlock;
			if(cropBlock instanceof CropBlock)
			{
				this.maxAge = ((CropBlock)cropBlock).getMaxAge();
				this.ageProperty = ((CropBlockAccess)cropBlock).invokeGetAgeProperty();
			}
			else
			{
				for(Property<?> prop : cropBlock.defaultBlockState().getProperties())
					if("age".equals(prop.getName())&&prop instanceof IntegerProperty)
					{
						int tmp = -1;
						for(Integer allowed : ((IntegerProperty)prop).getPossibleValues())
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
				throw new IllegalArgumentException("Block "+cropBlock.getDescriptionId()+" is not a valid crop block");
		}

		@Override
		public float getScale(ItemStack seed, float growth)
		{
			return 0.875f;
		}

		@Override
		public Collection<Pair<BlockState, Transformation>> getBlocks(ItemStack stack, float growth)
		{
			int age = Math.min(this.maxAge, Math.round(this.maxAge*growth));
			BlockState state;
			if(this.cropBlock instanceof CropBlock crop)
				state = crop.getStateForAge(age);
			else
				state = this.cropBlock.defaultBlockState().setValue(this.ageProperty, age);
			return ImmutableList.of(Pair.of(state, new Transformation(null)));
		}

		@Override
		public MapCodec<? extends ClocheRenderFunction> codec()
		{
			return CODEC;
		}
	}

	public static class RenderFunctionStacking implements ClocheRenderFunction
	{
		public static final MapCodec<RenderFunctionStacking> CODEC = byBlockCodec(f -> f.cropBlock, RenderFunctionStacking::new);

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
		public Collection<Pair<BlockState, Transformation>> getBlocks(ItemStack stack, float growth)
		{
			Transformation bottom = new Transformation(new Vector3f(0, growth-1, 0), null, null, null);
			Transformation top = new Transformation(new Vector3f(0, growth, 0), null, null, null);
			return ImmutableList.of(
					Pair.of(this.cropBlock.defaultBlockState(), bottom),
					Pair.of(this.cropBlock.defaultBlockState(), top));
		}

		@Override
		public MapCodec<? extends ClocheRenderFunction> codec()
		{
			return CODEC;
		}
	}

	public record RenderFunctionStem(
			Block cropBlock, Block stemBlock, Block attachedStemBlock
	) implements ClocheRenderFunction
	{
		public static final MapCodec<RenderFunctionStem> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
				BuiltInRegistries.BLOCK.byNameCodec().fieldOf("crop").forGetter(f -> f.cropBlock),
				BuiltInRegistries.BLOCK.byNameCodec().fieldOf("stem").forGetter(f -> f.stemBlock),
				BuiltInRegistries.BLOCK.byNameCodec().fieldOf("attachedStem").forGetter(f -> f.attachedStemBlock)
		).apply(inst, RenderFunctionStem::new));

		@Override
		public float getScale(ItemStack seed, float growth)
		{
			return 1;
		}

		@Override
		public Collection<Pair<BlockState, Transformation>> getBlocks(ItemStack stack, float growth)
		{
			PoseStack transform = new PoseStack();
			transform.translate(0, .0625f, 0.25f);

			if(growth < .375)
			{
				int age = Math.round(7*growth/.375f);
				return ImmutableList.of(Pair.of(this.stemBlock.defaultBlockState().setValue(StemBlock.AGE, age),
						new Transformation(transform.last().pose())));
			}
			else
			{
				float scale = ((growth-.375f)/.625f)*.3125f;
				Transformation cropMatrix = new Transformation(
						new Vector3f(0.5f-scale/2, .5625f-scale, 0.25f-scale/2),
						null,
						new Vector3f(scale, scale, scale),
						null
				);
				return ImmutableList.of(
						Pair.of(this.attachedStemBlock.defaultBlockState(), new Transformation(transform.last().pose())),
						Pair.of(this.cropBlock.defaultBlockState(), cropMatrix)
				);
			}
		}

		@Override
		public MapCodec<? extends ClocheRenderFunction> codec()
		{
			return CODEC;
		}
	}

	public static class RenderFunctionGeneric implements ClocheRenderFunction
	{
		public static final MapCodec<RenderFunctionGeneric> CODEC = byBlockCodec(f -> f.cropBlock, RenderFunctionGeneric::new);

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
		public Collection<Pair<BlockState, Transformation>> getBlocks(ItemStack stack, float growth)
		{
			Vector3f transl = new Vector3f(0.5f-growth/2, 0, 0.5f-growth/2);
			Vector3f scale = new Vector3f(growth, growth, growth);
			return ImmutableList.of(Pair.of(this.cropBlock.defaultBlockState(), new Transformation(transl, null, scale, null)));
		}

		@Override
		public MapCodec<? extends ClocheRenderFunction> codec()
		{
			return CODEC;
		}
	}

	public static class RenderFunctionChorus implements ClocheRenderFunction
	{
		public static final MapCodec<RenderFunctionChorus> CODEC = MapCodec.unit(new RenderFunctionChorus());

		@Override
		public float getScale(ItemStack seed, float growth)
		{
			return 0.5f;
		}

		@Override
		public Collection<Pair<BlockState, Transformation>> getBlocks(ItemStack stack, float growth)
		{
			growth *= 2;
			Transformation bottom = new Transformation(new Vector3f(0, growth-2, 0), null, null, null);
			Transformation middle = new Transformation(new Vector3f(0, growth-1, 0), null, null, null);
			Transformation top = new Transformation(new Vector3f(0, growth, 0), null, null, null);
			BlockState stem = Blocks.CHORUS_PLANT.defaultBlockState().setValue(ChorusPlantBlock.DOWN, true).setValue(ChorusPlantBlock.UP, true);
			return ImmutableList.of(
					Pair.of(stem, bottom),
					Pair.of(stem, middle),
					Pair.of(Blocks.CHORUS_FLOWER.defaultBlockState(), top));
		}

		@Override
		public MapCodec<? extends ClocheRenderFunction> codec()
		{
			return CODEC;
		}
	}

	public static class RenderFunctionHemp implements ClocheRenderFunction
	{
		public static final MapCodec<RenderFunctionHemp> CODEC = MapCodec.unit(new RenderFunctionHemp());

		@Override
		public float getScale(ItemStack seed, float growth)
		{
			return 0.6875f;
		}

		@Override
		public Collection<Pair<BlockState, Transformation>> getBlocks(ItemStack stack, float growth)
		{
			int age = Math.min(4, Math.round(growth*4));
			if(age==4)
			{
				Transformation top = new Transformation(new Vector3f(0, 1, 0), null, null, null);
				return ImmutableList.of(
						Pair.of(Misc.HEMP_PLANT.defaultBlockState().setValue(HempBlock.AGE, 4), new Transformation(null)),
						Pair.of(Misc.HEMP_PLANT.defaultBlockState().setValue(HempBlock.TOP, true), top)
				);
			}
			return ImmutableList.of(Pair.of(Misc.HEMP_PLANT.defaultBlockState().setValue(HempBlock.AGE, age), new Transformation(null)));
		}

		@Override
		public MapCodec<? extends ClocheRenderFunction> codec()
		{
			return CODEC;
		}
	}
}
