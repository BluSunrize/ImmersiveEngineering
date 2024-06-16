/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.wooden.WoodenCrateBlockEntity;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class CrateBlock extends IEEntityBlock<WoodenCrateBlockEntity>
{
	public CrateBlock(Properties blockProps)
	{
		super(IEBlockEntities.WOODEN_CRATE, blockProps, false);
	}

	@Override
	public void attack(BlockState blockState, Level level, BlockPos pos, Player player)
	{
		if(level.getBlockEntity(pos) instanceof WoodenCrateBlockEntity crate&&!crate.isSealed())
			player.displayClientMessage(Component.translatable(Lib.CHAT_WARN+"crate_unsealed"), true);
	}
}

