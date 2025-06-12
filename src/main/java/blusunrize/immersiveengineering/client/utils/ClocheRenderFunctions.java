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
import blusunrize.immersiveengineering.mixin.accessors.CropBlockAccess;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualCompositeMapCodecs;
import malte0811.dualcodecs.DualMapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
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

		register("doubleflower", RenderFunctionDoubleFlower.CODEC);
		register("doublecrop", RenderFunctionDoubleCrop.CODEC);
		register("chorus", RenderFunctionChorus.CODEC);
	}

	private static void register(String path, DualMapCodec<? super RegistryFriendlyByteBuf, ? extends ClocheRenderFunction> codec)
	{
		ClocheRenderFunction.RENDER_FUNCTION_FACTORIES.put(IEApi.ieLoc(path), codec);
	}

	private static <F extends ClocheRenderFunction>
	DualMapCodec<? super RegistryFriendlyByteBuf, F> byBlockCodec(Function<F, Block> getBlock, Function<Block, F> make)
	{
		return DualCodecs.registryEntry(BuiltInRegistries.BLOCK)
				.map(make, getBlock)
				.fieldOf("block");
	}

	private static Pair<IntegerProperty, Integer> getCropAge(Block block) throws IllegalArgumentException
	{
		if(block instanceof CropBlock crop)
			return Pair.of(((CropBlockAccess)crop).invokeGetAgeProperty(), crop.getMaxAge());
		else
		{
			for(Property<?> prop : block.defaultBlockState().getProperties())
				if("age".equals(prop.getName())&&prop instanceof IntegerProperty intProp)
				{
					int max = intProp.getPossibleValues().stream().max(Integer::compare).orElse(-1);
					if(max > 0)
						return Pair.of(intProp, max);
				}
		}
		throw new IllegalArgumentException("Block "+block.getDescriptionId()+" is not a valid crop block");
	}

	public static class RenderFunctionCrop implements ClocheRenderFunction
	{
		public static final DualMapCodec<? super RegistryFriendlyByteBuf, RenderFunctionCrop> CODEC = byBlockCodec(f -> f.cropBlock, RenderFunctionCrop::new);

		final Block cropBlock;
		int maxAge;
		IntegerProperty ageProperty;

		public RenderFunctionCrop(Block cropBlock)
		{
			this.cropBlock = cropBlock;
			var cropAge = getCropAge(cropBlock);
			this.ageProperty = cropAge.getFirst();
			this.maxAge = cropAge.getSecond();
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
		public DualMapCodec<? super RegistryFriendlyByteBuf, ? extends ClocheRenderFunction> codec()
		{
			return CODEC;
		}
	}

	public static class RenderFunctionStacking implements ClocheRenderFunction
	{
		public static final DualMapCodec<? super RegistryFriendlyByteBuf, RenderFunctionStacking> CODEC = byBlockCodec(f -> f.cropBlock, RenderFunctionStacking::new);

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
		public DualMapCodec<? super RegistryFriendlyByteBuf, ? extends ClocheRenderFunction> codec()
		{
			return CODEC;
		}
	}

	public record RenderFunctionStem(
			Block cropBlock, Block stemBlock, Block attachedStemBlock
	) implements ClocheRenderFunction
	{
		public static final DualMapCodec<? super RegistryFriendlyByteBuf, RenderFunctionStem> CODEC = DualCompositeMapCodecs.composite(
				DualCodecs.registryEntry(BuiltInRegistries.BLOCK).fieldOf("crop"), f -> f.cropBlock,
				DualCodecs.registryEntry(BuiltInRegistries.BLOCK).fieldOf("stem"), f -> f.stemBlock,
				DualCodecs.registryEntry(BuiltInRegistries.BLOCK).fieldOf("attachedStem"), f -> f.attachedStemBlock,
				RenderFunctionStem::new
		);

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
		public DualMapCodec<? super RegistryFriendlyByteBuf, ? extends ClocheRenderFunction> codec()
		{
			return CODEC;
		}
	}

	public static class RenderFunctionGeneric implements ClocheRenderFunction
	{
		public static final DualMapCodec<? super RegistryFriendlyByteBuf, RenderFunctionGeneric> CODEC = byBlockCodec(f -> f.cropBlock, RenderFunctionGeneric::new);

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
		public DualMapCodec<? super RegistryFriendlyByteBuf, ? extends ClocheRenderFunction> codec()
		{
			return CODEC;
		}
	}

	public static class RenderFunctionDoubleFlower implements ClocheRenderFunction
	{
		public static final DualMapCodec<? super RegistryFriendlyByteBuf, RenderFunctionDoubleFlower> CODEC = byBlockCodec(f -> f.cropBlock, RenderFunctionDoubleFlower::new);

		final Block cropBlock;

		public RenderFunctionDoubleFlower(Block cropBlock)
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
			Vector3f transl1 = new Vector3f(0.5f-growth/2, 0+growth, 0.5f-growth/2);
			Vector3f scale = new Vector3f(growth, growth, growth);
			return ImmutableList.of(
					Pair.of(this.cropBlock.defaultBlockState(), new Transformation(transl, null, scale, null)),
					Pair.of(this.cropBlock.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), new Transformation(transl1, null, scale, null)));
		}

		@Override
		public DualMapCodec<? super RegistryFriendlyByteBuf, ? extends ClocheRenderFunction> codec()
		{
			return CODEC;
		}
	}

	public static class RenderFunctionChorus implements ClocheRenderFunction
	{
		public static final DualMapCodec<? super RegistryFriendlyByteBuf, RenderFunctionChorus> CODEC = DualMapCodec.unit(new RenderFunctionChorus());

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
		public DualMapCodec<? super RegistryFriendlyByteBuf, ? extends ClocheRenderFunction> codec()
		{
			return CODEC;
		}
	}

	public static class RenderFunctionDoubleCrop implements ClocheRenderFunction
	{
		public static final DualMapCodec<? super RegistryFriendlyByteBuf, RenderFunctionDoubleCrop> CODEC = DualCompositeMapCodecs.composite(
				DualCodecs.registryEntry(BuiltInRegistries.BLOCK).fieldOf("block"), r -> r.cropBlock,
				DualCodecs.INT.fieldOf("doublingAge"), r -> r.doublingAge,
				RenderFunctionDoubleCrop::new
		);

		final Block cropBlock;
		IntegerProperty ageProperty;
		int maxAge;
		final int doublingAge;

		public RenderFunctionDoubleCrop(Block cropBlock, int doublingAge)
		{
			this.cropBlock = cropBlock;
			var cropAge = getCropAge(cropBlock);
			this.ageProperty = cropAge.getFirst();
			this.maxAge = cropAge.getSecond();
			this.doublingAge = doublingAge;
		}

		@Override
		public float getScale(ItemStack seed, float growth)
		{
			return 0.6875f;
		}

		@Override
		public Collection<Pair<BlockState, Transformation>> getBlocks(ItemStack stack, float growth)
		{
			int age = Math.min(this.maxAge, Math.round(this.maxAge*growth));
			if(age >= doublingAge)
			{
				Transformation top = new Transformation(new Vector3f(0, 1, 0), null, null, null);
				return ImmutableList.of(
						Pair.of(cropBlock.defaultBlockState().setValue(ageProperty, age), new Transformation(null)),
						Pair.of(cropBlock.defaultBlockState().setValue(ageProperty, age).setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER), top)
				);
			}
			return ImmutableList.of(Pair.of(cropBlock.defaultBlockState().setValue(ageProperty, age), new Transformation(null)));
		}

		@Override
		public DualMapCodec<? super RegistryFriendlyByteBuf, ? extends ClocheRenderFunction> codec()
		{
			return CODEC;
		}
	}

}
