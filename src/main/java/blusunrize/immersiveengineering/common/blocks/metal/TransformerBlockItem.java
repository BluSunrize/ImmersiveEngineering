/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IPostBlock;
import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Connectors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class TransformerBlockItem extends BlockItemIE
{
	public TransformerBlockItem(Block b, Properties props)
	{
		super(b, props);
	}

	@Nullable
	@Override
	protected BlockState getStateForPlacement(BlockItemUseContext context)
	{
		World w = context.getWorld();
		BlockPos possiblePost = context.getPos();
		if(!context.replacingClickedOnBlock())
			possiblePost = possiblePost.offset(context.getFace(), -1);
		if(PostTransformerBlock.isAttacheablePost(possiblePost, w))
			return Connectors.postTransformer.getDefaultState();
		else
			return super.getStateForPlacement(context);
	}
}
