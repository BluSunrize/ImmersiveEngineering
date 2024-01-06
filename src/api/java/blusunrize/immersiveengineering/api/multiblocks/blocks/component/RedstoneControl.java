/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks.blocks.component;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent.StateWrapper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl.RSState;
import blusunrize.immersiveengineering.api.utils.ComputerControlState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Arrays;
import java.util.List;

public class RedstoneControl<S> implements IMultiblockComponent<RSState>, StateWrapper<S, RSState>
{
	private final StateWrapper<S, RSState> getState;
	private final List<BlockPos> positions;
	private final boolean allowComputerControl;

	public RedstoneControl(StateWrapper<S, RSState> getState, BlockPos... positions)
	{
		this(getState, true, positions);
	}

	public RedstoneControl(StateWrapper<S, RSState> getState, boolean allowComputerControl, BlockPos... positions)
	{
		this.positions = Arrays.asList(positions);
		this.getState = s -> {
			final RSState rsState = getState.wrapState(s);
			rsState.positions = this.positions;
			return rsState;
		};
		this.allowComputerControl = allowComputerControl;
	}

	public boolean allowComputerControl()
	{
		return allowComputerControl;
	}

	@Override
	public RSState wrapState(S outer)
	{
		return getState.wrapState(outer);
	}

	@Override
	public InteractionResult click(
			IMultiblockContext<RSState> ctx,
			BlockPos posInMultiblock,
			Player player,
			InteractionHand hand,
			BlockHitResult absoluteHit,
			boolean isClient
	)
	{
		final ItemStack held = player.getItemInHand(hand);
		if(!held.is(IETags.screwdrivers)||!positions.contains(posInMultiblock))
			return InteractionResult.PASS;
		if(!isClient)
		{
			final boolean inverted = !ctx.getState().rsEnablesMachine;
			ctx.getState().rsEnablesMachine = inverted;
			player.displayClientMessage(
					Component.translatable(Lib.CHAT_INFO+"rsControl."+(inverted?"invertedOn": "invertedOff")), true
			);
			ctx.markMasterDirty();
		}
		return InteractionResult.SUCCESS;
	}

	public static class RSState implements IMultiblockState
	{
		private boolean rsEnablesMachine;
		private List<BlockPos> positions = List.of();
		private final ComputerControlState computerControlState = new ComputerControlState();

		public static RSState enabledByDefault()
		{
			return new RSState(false);
		}

		public static RSState disabledByDefault()
		{
			return new RSState(true);
		}

		private RSState(boolean rsEnablesMachine)
		{
			this.rsEnablesMachine = rsEnablesMachine;
		}

		public boolean isEnabled(IMultiblockContext<?> ctx)
		{
			if(computerControlState.isAttached()&&!computerControlState.isEnabled())
				return false;
			boolean hasRS = false;
			for(final BlockPos rsPos : positions)
				hasRS = hasRS || ctx.getRedstoneInputValue(rsPos, 0) > 0;
			return rsEnablesMachine==hasRS;
		}

		@Override
		public void writeSaveNBT(CompoundTag nbt)
		{
			nbt.putBoolean("rsEnablesMachine", rsEnablesMachine);
		}

		@Override
		public void readSaveNBT(CompoundTag nbt)
		{
			rsEnablesMachine = nbt.getBoolean("rsEnablesMachine");
		}

		public ComputerControlState getComputerControlState()
		{
			return computerControlState;
		}
	}
}
