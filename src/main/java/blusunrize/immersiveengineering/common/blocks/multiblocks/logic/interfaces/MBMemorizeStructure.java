/*
 * BluSunrize
 * Copyright (c) 2025
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic.interfaces;

import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;

public interface MBMemorizeStructure<State extends IMultiblockState> extends IMultiblockLogic<State>
{
	void setMemorizedBlockState(State state, BlockPos pos, BlockState blockState);

	BlockState getMemorizedBlockState(State state, BlockPos pos);

	class StructureMemo extends HashMap<BlockPos, BlockState> implements IMultiblockState
	{
		@Override
		public void writeSaveNBT(CompoundTag nbt, Provider provider)
		{
			ListTag list = new ListTag();
			this.forEach((pos, state) -> {
				CompoundTag compound = new CompoundTag();
				compound.put("pos", NbtUtils.writeBlockPos(pos));
				compound.put("state", NbtUtils.writeBlockState(state));
				list.add(compound);
			});
			nbt.put("memorizedStates", list);
		}

		@Override
		public void readSaveNBT(CompoundTag nbt, Provider provider)
		{
			HolderGetter<Block> lookup = provider.lookupOrThrow(Registries.BLOCK);
			ListTag list = nbt.getList("memorizedStates", 10);
			list.forEach(tag -> {
				if(tag instanceof CompoundTag compound)
					this.put(
							NbtUtils.readBlockPos(compound, "pos").orElseThrow(),
							NbtUtils.readBlockState(lookup, compound.getCompound("state"))
					);
			});
		}
	}
}
