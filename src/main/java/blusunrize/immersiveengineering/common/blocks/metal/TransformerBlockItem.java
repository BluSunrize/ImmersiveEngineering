/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.register.IEBlocks.Connectors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class TransformerBlockItem extends BlockItemIE
{
	public TransformerBlockItem(Block b)
	{
		super(b);
	}

	@Nullable
	@Override
	protected BlockState getPlacementState(BlockPlaceContext context)
	{
		Level w = context.getLevel();
		BlockPos possiblePost = context.getClickedPos();
		if(!context.replacingClickedOnBlock())
			possiblePost = possiblePost.relative(context.getClickedFace(), -1);
		if(PostTransformerBlock.isAttacheablePost(possiblePost, w))
			return Connectors.POST_TRANSFORMER.defaultBlockState();
		else
			return super.getPlacementState(context);
	}
}
