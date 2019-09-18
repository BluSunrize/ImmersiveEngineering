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
import blusunrize.immersiveengineering.common.data.blockstate.BlockstateGenerator;
import blusunrize.immersiveengineering.common.data.blockstate.ModelHelper;
import blusunrize.immersiveengineering.common.data.blockstate.VariantBlockstate.Builder;
import com.google.common.base.Preconditions;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static blusunrize.immersiveengineering.common.data.IEDataGenerator.rl;

public class BlockStates extends BlockstateGenerator
{
	public BlockStates(DataGenerator gen)
	{
		super(gen);
	}

	@Override
	protected void registerStates(BiConsumer<Block, IVariantModelGenerator> variantBased, BiConsumer<Block, List<MultiPart>> multipartBased)
	{
		for(EnumMetals m : EnumMetals.values())
		{
			String name = m.tagName();
			if(!m.isVanillaMetal())
			{
				if(m.shouldAddOre())
					createBasicBlock(Metals.ores.get(m), rl("block/ore_"+name), variantBased);
				if(m!=EnumMetals.URANIUM)
					createBasicBlock(Metals.storage.get(m), rl("block/storage_"+name), variantBased);
				else
				{
					ResourceLocation side = rl("block/storage_"+name+"_side");
					ResourceLocation top = rl("block/storage_"+name+"_top");
					createBasicBlock(Metals.storage.get(m), side, top, top, variantBased);
				}
			}
			createBasicBlock(Metals.sheetmetal.get(m), rl("block/sheetmetal_"+name), variantBased);
		}
		createFenceBlock(WoodenDecoration.treatedFence, rl("block/treated_wood_horizontal"), multipartBased);
		createFenceBlock(MetalDecoration.steelFence, rl("block/storage_steel"), multipartBased);
		createFenceBlock(MetalDecoration.aluFence, rl("block/storage_aluminum"), multipartBased);
	}

	private void createBasicBlock(Block block, ResourceLocation texture, BiConsumer<Block, IVariantModelGenerator> out)
	{
		ResourceLocation blockName = Preconditions.checkNotNull(block.getRegistryName());
		Model model = new Model(new ResourceLocation(blockName.getNamespace(), "block/"+blockName.getPath()),
				ModelHelper.createBasicCube(texture));
		createBasicBlock(block, model, out);
	}

	private void createBasicBlock(Block block, ResourceLocation sides, ResourceLocation top, ResourceLocation bottom,
								  BiConsumer<Block, IVariantModelGenerator> out)
	{
		ResourceLocation blockName = Preconditions.checkNotNull(block.getRegistryName());
		Model model = new Model(new ResourceLocation(blockName.getNamespace(), "block/"+blockName.getPath()),
				ModelHelper.createBasicCube(sides, top, bottom));
		createBasicBlock(block, model, out);
	}

	private void createBasicBlock(Block block, Model model, BiConsumer<Block, IVariantModelGenerator> out)
	{
		IVariantModelGenerator gen = new Builder(block)
				.setModel(block.getDefaultState(), model)
				.build();
		out.accept(block, gen);
	}

	private void createFenceBlock(IEFenceBlock block, ResourceLocation texture, BiConsumer<Block, List<MultiPart>> out)
	{
		ResourceLocation name = Preconditions.checkNotNull(block.getRegistryName());
		List<MultiPart> parts = new ArrayList<>();
		Model postModel = new Model(rl("block/"+name.getPath()+"_post"), 0, 0,
				false, ModelHelper.createFencePost(texture));
		parts.add(new MultiPart(postModel, false));
		for(Direction dir : Direction.BY_HORIZONTAL_INDEX)
		{
			int angle = (int)(dir.getHorizontalAngle()+180)%360;
			Model sideModel = new Model(rl("block/"+name.getPath()+"_side"), 0, angle,
					true, ModelHelper.createFenceSide(texture));
			BooleanProperty sideActive = block.getFacingStateMap().get(dir);
			parts.add(new MultiPart(sideModel, false, new PropertyWithValues<>(sideActive, true)));
		}
		out.accept(block, parts);
	}
}
