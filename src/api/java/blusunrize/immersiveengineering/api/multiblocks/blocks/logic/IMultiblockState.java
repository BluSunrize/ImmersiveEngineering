/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

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
