package blusunrize.immersiveengineering.api.multiblocks.blocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLogic.IMultiblockState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.function.Function;

public interface IMultiblockLogic<State extends IMultiblockState>
{
	State createInitialState(MultiblockCapabilitySource capabilitySource);

	<T> LazyOptional<T> getCapability(
			IMultiblockContext<State> ctx, BlockPos posInMultiblock, @Nullable RelativeBlockFace side, Capability<T> cap
	);

	// TODO split into collision and selection?
	Function<BlockPos, VoxelShape> shapeGetter();

	default void onEntityCollision(IMultiblockContext<State> ctx, BlockPos posInMultiblock, Entity collided)
	{
	}

	default InteractionResult click(
			IMultiblockContext<State> ctx, BlockPos posInMultiblock, Player player,
			InteractionHand hand,
			// TODO make relative instead? Bit of a pain to compute, and possibly not that useful
			BlockHitResult absoluteHit,
			boolean isClient
	)
	{
		return InteractionResult.PASS;
	}

	interface IMultiblockState
	{
		void writeSaveNBT(CompoundTag nbt);

		default void writeSyncNBT(CompoundTag nbt)
		{
		}

		void readSaveNBT(CompoundTag nbt);

		default void readSyncNBT(CompoundTag nbt)
		{
		}
	}
}
