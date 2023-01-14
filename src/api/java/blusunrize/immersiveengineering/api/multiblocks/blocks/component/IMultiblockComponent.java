/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks.blocks.component;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import java.util.function.Consumer;

public interface IMultiblockComponent<State>
{
	default <T>
	LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap)
	{
		return LazyOptional.empty();
	}

	default void onEntityCollision(IMultiblockContext<State> ctx, BlockPos posInMultiblock, Entity collided)
	{
	}

	default InteractionResult click(
			IMultiblockContext<State> ctx, BlockPos posInMultiblock, Player player,
			InteractionHand hand,
			BlockHitResult absoluteHit,
			boolean isClient
	)
	{
		return InteractionResult.PASS;
	}

	default void dropExtraItems(State state, Consumer<ItemStack> drop)
	{
	}

	interface StateWrapper<OuterState, OwnState>
	{
		OwnState wrapState(OuterState outer);
	}
}
