/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data;

import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDecoration;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Metals;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.WoodenDecoration;
import blusunrize.immersiveengineering.common.blocks.generic.IEFenceBlock;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.data.Models.MetalModels;
import blusunrize.immersiveengineering.common.data.blockstate.BlockstateGenerator;
import blusunrize.immersiveengineering.common.data.blockstate.VariantBlockstate.Builder;
import blusunrize.immersiveengineering.common.data.model.ModelFile;
import blusunrize.immersiveengineering.common.data.model.ModelHelper.BasicStairsShape;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.StairsBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.util.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

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
		for(MetalScaffoldingType type : MetalScaffoldingType.values())
		{
			createStairsBlock(MetalDecoration.aluScaffoldingStair.get(type), models.aluScaffoldingStairs.get(type),
					StairsBlock.FACING, StairsBlock.HALF, StairsBlock.SHAPE, variantBased);
			createStairsBlock(MetalDecoration.steelScaffoldingStair.get(type), models.steelScaffoldingStairs.get(type),
					StairsBlock.FACING, StairsBlock.HALF, StairsBlock.SHAPE, variantBased);
		}
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
					b.setForAllWithState(partialState, new ConfiguredModel(base, xRot, yRot, true));
				}
			}
		}
		out.accept(block, b.build());
	}

	private void createFenceBlock(IEFenceBlock block, ModelFile post, ModelFile side, BiConsumer<Block, List<MultiPart>> out)
	{
		List<MultiPart> parts = new ArrayList<>();
		ConfiguredModel postModel = new ConfiguredModel(post, 0, 0, false);
		parts.add(new MultiPart(postModel, false));
		for(Direction dir : Direction.BY_HORIZONTAL_INDEX)
		{
			int angle = getAngle(dir, 180);
			ConfiguredModel sideModel = new ConfiguredModel(side, 0, angle, true);
			BooleanProperty sideActive = block.getFacingStateMap().get(dir);
			parts.add(new MultiPart(sideModel, false, new PropertyWithValues<>(sideActive, true)));
		}
		out.accept(block, parts);
	}

	private int getAngle(Direction dir, int offset)
	{
		return (int)((dir.getHorizontalAngle()+offset)%360);
	}
}
