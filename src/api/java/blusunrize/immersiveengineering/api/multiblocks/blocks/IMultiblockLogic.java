package blusunrize.immersiveengineering.api.multiblocks.blocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLogic.IMultiblockState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public interface IMultiblockLogic<State extends IMultiblockState>
{
	State createInitialState(MultiblockCapabilitySource capabilitySource);

	<T> LazyOptional<T> getCapability(
			IMultiblockContext<State> ctx, BlockPos posInMultiblock, @Nullable RelativeBlockFace side, Capability<T> cap
	);

	interface IMultiblockState
	{
		void writeSaveNBT(CompoundTag nbt);

		void writeSyncNBT(CompoundTag nbt);

		void readSaveNBT(CompoundTag nbt);

		void readSyncNBT(CompoundTag nbt);
	}
}
