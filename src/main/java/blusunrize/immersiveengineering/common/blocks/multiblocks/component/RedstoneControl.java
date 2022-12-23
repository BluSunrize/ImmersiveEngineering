package blusunrize.immersiveengineering.common.blocks.multiblocks.component;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockComponent.StateWrapper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.common.blocks.multiblocks.component.RedstoneControl.RSState;
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

	public RedstoneControl(StateWrapper<S, RSState> getState, BlockPos... positions)
	{
		this.positions = Arrays.asList(positions);
		this.getState = s -> {
			final var rsState = getState.wrapState(s);
			rsState.positions = this.positions;
			return rsState;
		};
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
			for(final BlockPos rsPos : positions)
			{
				final boolean hasRS = ctx.getRedstoneInputValue(rsPos, 0) > 0;
				if(rsEnablesMachine==hasRS)
					return true;
			}
			return false;
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
	}
}
