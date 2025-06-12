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
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public interface IMultiblockComponent<State>
{
	default void registerCapabilities(CapabilityRegistrar<State> register)
	{
	}

	default void onEntityCollision(IMultiblockContext<State> ctx, BlockPos posInMultiblock, Entity collided)
	{
	}

	default ItemInteractionResult click(
			IMultiblockContext<State> ctx, BlockPos posInMultiblock, Player player,
			InteractionHand hand,
			BlockHitResult absoluteHit,
			boolean isClient
	)
	{
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	/**
	 * Drops any items that need to be dropped when the multiblock is broken. Note that this may be called multiple
	 * times for the same multiblock (e.g. when the master block is broken using a drill), so the dropped items should
	 * be removed from the multiblock's own inventory.
	 */
	default void dropExtraItems(State state, Consumer<ItemStack> drop)
	{
	}

	default void onRemoved(IMultiblockContext<State> context)
	{
	}


	interface StateWrapper<OuterState, OwnState>
	{
		OwnState wrapState(OuterState outer);
	}

	interface CapabilityRegistrar<State>
	{
		<T> void register(BlockCapability<T, @Nullable Direction> capability, CapabilityGetter<T, State> getter);

		default <T> void registerAtBlockPos(
				BlockCapability<T, @Nullable Direction> capability,
				BlockPos atPosition,
				Function<State, T> getter
		)
		{
			register(capability, (state, position) -> Objects.equals(position.posInMultiblock(), atPosition)?getter.apply(state): null);
		}

		default <T> void registerAt(
				BlockCapability<T, @Nullable Direction> capability,
				CapabilityPosition atPosition,
				Function<State, T> getter
		)
		{
			register(capability, (state, position) -> Objects.equals(position, atPosition)?getter.apply(state): null);
		}

		default <T> void registerAtOrNull(
				BlockCapability<T, @Nullable Direction> capability,
				CapabilityPosition atPosition,
				Function<State, T> getter
		)
		{
			register(capability, (state, position) -> atPosition.equalsOrNullFace(position)?getter.apply(state): null);
		}

		default <T> void registerEverywhere(
				BlockCapability<T, @Nullable Direction> capability, Function<State, T> getter
		)
		{
			register(capability, (state, position) -> getter.apply(state));
		}
	}

	interface CapabilityGetter<T, State>
	{
		@Nullable
		T getCapability(State state, CapabilityPosition position);
	}
}
