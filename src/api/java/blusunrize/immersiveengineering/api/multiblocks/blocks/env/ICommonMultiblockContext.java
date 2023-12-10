/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks.blocks.env;

import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface ICommonMultiblockContext
{
	default <T> BlockCapabilityCache<T, ?> getCapabilityAt(BlockCapability<T, Direction> capability, MultiblockFace face)
	{
		return getCapabilityAt(capability, face.posInMultiblock(), face.face());
	}

	<T> BlockCapabilityCache<T, ?> getCapabilityAt(
			BlockCapability<T, Direction> capability, BlockPos posRelativeToMB, RelativeBlockFace face
	);

	default <T> BlockCapabilityCache<T, ?> getVoidCapabilityAt(
			BlockCapability<T, Void> capability, BlockPos posRelativeToMB
	)
	{
		return getCapabilityAt(capability, posRelativeToMB, null);
	}

	<T, C> BlockCapabilityCache<T, ?> getCapabilityAt(
			BlockCapability<T, C> capability, BlockPos posRelativeToMB, C context
	);
}
