package blusunrize.immersiveengineering.api.multiblocks.blocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLogic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import java.util.function.Function;

public interface IMultiblockLogic<State extends IMultiblockState>
{
	State createInitialState(IInitialMultiblockContext<State> capabilitySource);

	<T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap);

	// TODO split into collision and selection?
	// TODO this API does not work for variable-size MBs
	Function<BlockPos, VoxelShape> shapeGetter();

	default VoxelShape postProcessAbsoluteShape(
			IMultiblockContext<State> ctx, VoxelShape defaultShape, CollisionContext shapeCtx,
			BlockPos posInMultiblock)
	{
		return defaultShape;
	}

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
		return clickSimple(ctx, player, isClient);
	}

	default InteractionResult clickSimple(IMultiblockContext<State> ctx, Player player, boolean isClient)
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
