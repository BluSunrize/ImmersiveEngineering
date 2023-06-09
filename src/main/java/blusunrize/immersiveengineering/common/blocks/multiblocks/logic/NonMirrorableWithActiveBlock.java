/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockPartBlock;
import com.google.common.base.Preconditions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import javax.annotation.Nonnull;

public class NonMirrorableWithActiveBlock<S extends IMultiblockState> extends MultiblockPartBlock<S>
{
	public static final Property<Boolean> ACTIVE = IEProperties.ACTIVE;

	public NonMirrorableWithActiveBlock(Properties properties, MultiblockRegistration<S> multiblock)
	{
		super(properties, multiblock);
		Preconditions.checkState(!multiblock.mirrorable());
	}

	public static void setActive(
			IMultiblockLevel level, TemplateMultiblock multiblock, boolean active
	)
	{
		for(StructureBlockInfo info : multiblock.getStructure(level.getRawLevel()))
		{
			final BlockState state = level.getBlockState(info.pos());
			if(state.is(multiblock.getBlock()))
				level.setBlock(info.pos(), state.setValue(NonMirrorableWithActiveBlock.ACTIVE, active));
		}
	}

	@Override
	protected void createBlockStateDefinition(@Nonnull Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(ACTIVE);
	}
}
