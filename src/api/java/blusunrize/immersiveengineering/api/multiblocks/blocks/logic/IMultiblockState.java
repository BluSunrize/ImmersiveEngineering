/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks.blocks.logic;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;

public interface IMultiblockState
{
	void writeSaveNBT(CompoundTag nbt, Provider provider);

	default void writeSyncNBT(CompoundTag nbt, Provider provider)
	{
	}

	void readSaveNBT(CompoundTag nbt, Provider provider);

	default void readSyncNBT(CompoundTag nbt, Provider provider)
	{
	}
}
