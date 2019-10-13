/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDecoration;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Metals;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.WoodenDecoration;
import blusunrize.immersiveengineering.common.blocks.generic.IEFenceBlock;
import blusunrize.immersiveengineering.common.data.Models.MetalModels;
import blusunrize.immersiveengineering.common.data.blockstate.BlockstateGenerator;
import blusunrize.immersiveengineering.common.data.blockstate.VariantBlockstate.Builder;
import blusunrize.immersiveengineering.common.data.model.ModelFile;
import blusunrize.immersiveengineering.common.data.model.ModelFile.ExistingModelFile;
import blusunrize.immersiveengineering.common.data.model.ModelHelper.BasicStairsShape;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.SlabType;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import static blusunrize.immersiveengineering.common.data.IEDataGenerator.rl;

public class BlockStates extends BlockstateGenerator
{
	private final Models models;

	public BlockStates(DataGenerator gen, Models models)
	{
		super(gen);
		this.models = models;
	}

	@Override
	protected void registerStates(BiConsumer<Block, IVariantModelGenerator> variantBased, BiConsumer<Block, List<MultiPart>> multipartBased)
	{
		for(EnumMetals m : EnumMetals.values())
		{
			MetalModels metalModels = models.metalModels.get(m);
			if(!m.isVanillaMetal())
			{
				if(m.shouldAddOre())
					createBasicBlock(Metals.ores.get(m), metalModels.ore, variantBased);
				createBasicBlock(Metals.storage.get(m), metalModels.storage, variantBased);
			}
			createBasicBlock(Metals.sheetmetal.get(m), metalModels.sheetmetal, variantBased);
		}
		createFenceBlock(WoodenDecoration.treatedFence, models.treatedFencePost, models.treatedFenceSide, multipartBased);
		createFenceBlock(MetalDecoration.steelFence, models.steelFencePost, models.steelFenceSide, multipartBased);
		createFenceBlock(MetalDecoration.aluFence, models.aluFencePost, models.aluFenceSide, multipartBased);
		for(Entry<Block, ModelFile> entry : models.simpleBlocks.entrySet())
			createBasicBlock(entry.getKey(), entry.getValue(), variantBased);
		for(Entry<Block, Map<SlabType, ModelFile>> entry : models.slabs.entrySet())
			createSlabBlock(entry.getKey(), entry.getValue(), SlabBlock.TYPE, variantBased);
//		for(MetalScaffoldingType type : MetalScaffoldingType.values())
//		{
//			createStairsBlock(MetalDecoration.aluScaffoldingStair.get(type), models.aluScaffoldingStairs.get(type),
//					StairsBlock.FACING, StairsBlock.HALF, StairsBlock.SHAPE, variantBased);
//			createStairsBlock(MetalDecoration.steelScaffoldingStair.get(type), models.steelScaffoldingStairs.get(type),
//					StairsBlock.FACING, StairsBlock.HALF, StairsBlock.SHAPE, variantBased);
//		}
		for(Entry<Block, Map<BasicStairsShape, ModelFile>> entry : models.stairs.entrySet())
			createStairsBlock(entry.getKey(), entry.getValue(), StairsBlock.FACING, StairsBlock.HALF, StairsBlock.SHAPE, variantBased);

		createMultiblock(Multiblocks.excavator, new ExistingModelFile(rl("block/metal_multiblock/excavator.obj")),
				new ExistingModelFile(rl("block/metal_multiblock/excavator_mirrored.obj")),
				variantBased);
		createMultiblock(Multiblocks.crusher, new ExistingModelFile(rl("block/metal_multiblock/crusher_mirrored.obj")),
				new ExistingModelFile(rl("block/metal_multiblock/crusher.obj")),
				variantBased);
		createMultiblock(Multiblocks.metalPress, new ExistingModelFile(rl("block/metal_multiblock/metal_press.obj")), variantBased);
		createMultiblock(Multiblocks.blastFurnaceAdv, new ExistingModelFile(rl("block/blastfurnace_advanced.obj")), variantBased);

		createMultiblock(Multiblocks.cokeOven, models.cokeOvenOff, models.cokeOvenOn, IEProperties.MULTIBLOCKSLAVE,
				IEProperties.FACING_HORIZONTAL, IEProperties.ACTIVE, 180, variantBased);
		createMultiblock(Multiblocks.alloySmelter, models.alloySmelterOff, models.alloySmelterOn, IEProperties.MULTIBLOCKSLAVE,
				IEProperties.FACING_HORIZONTAL, IEProperties.ACTIVE, 180, variantBased);
		createMultiblock(Multiblocks.blastFurnace, models.blastFurnaceOff, models.blastFurnaceOn, IEProperties.MULTIBLOCKSLAVE,
				IEProperties.FACING_HORIZONTAL, IEProperties.ACTIVE, 180, variantBased);
	}

	private void createBasicBlock(Block block, ModelFile model, BiConsumer<Block, IVariantModelGenerator> out)
	{
		ConfiguredModel configuredModel = new ConfiguredModel(model);
		createBasicBlock(block, configuredModel, out);
	}

	private void createBasicBlock(Block block, ConfiguredModel model, BiConsumer<Block, IVariantModelGenerator> out)
	{
		IVariantModelGenerator gen = new Builder(block)
				.setModel(block.getDefaultState(), model)
				.build();
		out.accept(block, gen);
	}

	private void createSlabBlock(Block block, Map<SlabType, ModelFile> baseModels, EnumProperty<SlabType> typeProp, BiConsumer<Block, IVariantModelGenerator> out)
	{
		Builder b = new Builder(block);
		for(SlabType type : SlabType.values())
		{
			Map<IProperty<?>, ?> partialState = ImmutableMap.<IProperty<?>, Object>builder()
					.put(typeProp, type)
					.build();
			b.setForAllWithState(partialState, new ConfiguredModel(baseModels.get(type)));
		}
		out.accept(block, b.build());
	}

	private void createStairsBlock(Block block, Map<BasicStairsShape, ModelFile> baseModels, EnumProperty<Direction> facingProp,
								   EnumProperty<Half> halfProp, EnumProperty<StairsShape> shapeProp, BiConsumer<Block, IVariantModelGenerator> out)
	{
		Builder b = new Builder(block);
		for(Direction dir : Direction.BY_HORIZONTAL_INDEX)
		{
			for(Half half : Half.values())
			{
				for(StairsShape shape : StairsShape.values())
				{
					Map<IProperty<?>, ?> partialState = ImmutableMap.<IProperty<?>, Object>builder()
							.put(facingProp, dir)
							.put(halfProp, half)
							.put(shapeProp, shape)
							.build();
					ModelFile base = baseModels.get(BasicStairsShape.toBasicShape(shape));
					int xRot = 0;
					if(half==Half.TOP)
						xRot = 180;
					int yRot = getAngle(dir, 90);
					if(shape==StairsShape.INNER_LEFT||shape==StairsShape.OUTER_LEFT)
						yRot = (yRot+270)%360;
					b.setForAllWithState(partialState, new ConfiguredModel(base, xRot, yRot, true, ImmutableMap.of()));
				}
			}
		}
		out.accept(block, b.build());
	}

	private void createFenceBlock(IEFenceBlock block, ModelFile post, ModelFile side, BiConsumer<Block, List<MultiPart>> out)
	{
		List<MultiPart> parts = new ArrayList<>();
		ConfiguredModel postModel = new ConfiguredModel(post, 0, 0, false, ImmutableMap.of());
		parts.add(new MultiPart(postModel, false));
		for(Direction dir : Direction.BY_HORIZONTAL_INDEX)
		{
			int angle = getAngle(dir, 180);
			ConfiguredModel sideModel = new ConfiguredModel(side, 0, angle, true, ImmutableMap.of());
			BooleanProperty sideActive = block.getFacingStateMap().get(dir);
			parts.add(new MultiPart(sideModel, false, new PropertyWithValues<>(sideActive, true)));
		}
		out.accept(block, parts);
	}

	private void createMultiblock(Block b, ModelFile masterModel, ModelFile mirroredModel, int rotationOffset,
								  BiConsumer<Block, IVariantModelGenerator> out)
	{
		createMultiblock(b, masterModel, mirroredModel, IEProperties.MULTIBLOCKSLAVE, IEProperties.FACING_HORIZONTAL, IEProperties.MIRRORED, rotationOffset, out);
	}

	private void createMultiblock(Block b, ModelFile masterModel, ModelFile mirroredModel,
								  BiConsumer<Block, IVariantModelGenerator> out)
	{
		createMultiblock(b, masterModel, mirroredModel, 180, out);
	}

	private void createMultiblock(Block b, ModelFile masterModel, BiConsumer<Block, IVariantModelGenerator> out)
	{
		createMultiblock(b, masterModel, null, IEProperties.MULTIBLOCKSLAVE, IEProperties.FACING_HORIZONTAL, null, 180, out);
	}

	private void createMultiblock(Block b, ModelFile masterModel, @Nullable ModelFile mirroredModel, IProperty<Boolean> isSlave,
								  EnumProperty<Direction> facing, @Nullable IProperty<Boolean> mirroredState, int rotationOffset,
								  BiConsumer<Block, IVariantModelGenerator> out)
	{
		Preconditions.checkArgument((mirroredModel==null)==(mirroredState==null));
		Builder builder = new Builder(b);
		ModelFile empty = new ExistingModelFile(rl("block/ie_empty"));
		builder.setForAllWithState(ImmutableMap.of(isSlave, true), new ConfiguredModel(empty));
		boolean[] possibleMirrorStates;
		if(mirroredState!=null)
			possibleMirrorStates = new boolean[]{false, true};
		else
			possibleMirrorStates = new boolean[1];
		for(boolean mirrored : possibleMirrorStates)
			for(Direction dir : Direction.BY_HORIZONTAL_INDEX)
			{
				int angle = getAngle(dir, rotationOffset);
				ModelFile model = mirrored?mirroredModel: masterModel;
				ImmutableMap.Builder<IProperty<?>, Comparable<?>> partialState = ImmutableMap.builder();
				partialState.put(isSlave, Boolean.FALSE)
						.put(facing, dir);
				if(mirroredState!=null)
					partialState.put(mirroredState, mirrored);
				builder.setForAllWithState(partialState.build(),
						new ConfiguredModel(model, 0, angle, true, ImmutableMap.of("flip-v", true)));
			}
		out.accept(b, builder.build());
	}

	private int getAngle(Direction dir, int offset)
	{
		return (int)((dir.getHorizontalAngle()+offset)%360);
	}
}
