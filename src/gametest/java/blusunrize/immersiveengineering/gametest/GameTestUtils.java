/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.gametest;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestAssertPosException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class GameTestUtils
{
	public static void formMultiblock(IMultiblock multiblock, GameTestHelper helper)
	{
		Player player = helper.makeMockPlayer();
		ItemStack hammer = new ItemStack(Tools.HAMMER);
		player.setItemInHand(InteractionHand.MAIN_HAND, hammer);
		BlockPos triggerRelative = multiblock.getTriggerOffset().above();
		BlockPos triggerAbsolute = helper.absolutePos(triggerRelative);
		BlockHitResult hitResult = new BlockHitResult(Vec3.ZERO, Direction.SOUTH, triggerAbsolute, false);
		InteractionResult result = hammer.onItemUseFirst(
				new UseOnContext(player, InteractionHand.MAIN_HAND, hitResult)
		);
		if(result!=InteractionResult.SUCCESS)
			helper.fail("Wrong interaction result: "+result.name());
	}

	// Mostly copied from vanilla, but without the restriction of requiring exactly one
	public static void assertContainerContainsSome(BlockPos pos, ItemLike itemLike, GameTestHelper helper)
	{
		BlockEntity blockentity = helper.getBlockEntity(pos);
		if(!(blockentity instanceof BaseContainerBlockEntity container))
			throw new GameTestAssertPosException(
					"Expected container BE", helper.absolutePos(pos), pos, helper.getTick()
			);
		Item item = itemLike.asItem();
		if(container.countItem(item) < 1)
			throw new GameTestAssertPosException(
					"Container should contain at least one "+item, helper.absolutePos(pos), pos, helper.getTick()
			);
	}
}
