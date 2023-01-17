/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks.blocks.registry;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;

public class MultiblockItem extends BlockItem
{
	public MultiblockItem(Block block)
	{
		super(block, new Properties());
	}

	@Nonnull
	@Override
	public InteractionResult place(@Nonnull BlockPlaceContext context)
	{
		// Do not allow multiblocks to be placed directly
		return InteractionResult.FAIL;
	}
}
