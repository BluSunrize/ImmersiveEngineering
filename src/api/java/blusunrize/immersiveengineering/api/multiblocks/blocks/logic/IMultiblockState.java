package blusunrize.immersiveengineering.api.multiblocks.blocks.logic;

import net.minecraft.nbt.CompoundTag;

public interface IMultiblockState
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
