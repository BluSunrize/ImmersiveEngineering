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
import blusunrize.immersiveengineering.common.data.Models.MetalModels;
import blusunrize.immersiveengineering.common.data.blockstate.BlockstateGenerator;
import blusunrize.immersiveengineering.common.data.blockstate.VariantBlockstate.Builder;
import blusunrize.immersiveengineering.common.data.model.ModelFile;
import com.google.common.base.Preconditions;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
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
			String name = m.tagName();
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

	private void createFenceBlock(IEFenceBlock block, ModelFile post, ModelFile side, BiConsumer<Block, List<MultiPart>> out)
	{
		ResourceLocation name = Preconditions.checkNotNull(block.getRegistryName());
		List<MultiPart> parts = new ArrayList<>();
		ConfiguredModel postModel = new ConfiguredModel(post, 0, 0, false);
		parts.add(new MultiPart(postModel, false));
		for(Direction dir : Direction.BY_HORIZONTAL_INDEX)
		{
			int angle = (int)(dir.getHorizontalAngle()+180)%360;
			ConfiguredModel sideModel = new ConfiguredModel(side, 0, angle, true);
			BooleanProperty sideActive = block.getFacingStateMap().get(dir);
			parts.add(new MultiPart(sideModel, false, new PropertyWithValues<>(sideActive, true)));
		}
		out.accept(block, parts);
	}
}
