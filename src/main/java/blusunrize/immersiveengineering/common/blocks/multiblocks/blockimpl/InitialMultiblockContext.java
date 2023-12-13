/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.utils.DirectionalBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public record InitialMultiblockContext<State extends IMultiblockState>(
		BlockEntity masterBE,
		MultiblockOrientation orientation,
		BlockPos masterOffset
) implements IInitialMultiblockContext<State>
{
	@Override
	public <T, C>
	BlockCapabilityCache<T, ?> getCapabilityAt(BlockCapability<T, C> capability, BlockPos posRelativeToMB, C context)
	{
		return getCapabilityAt(masterBE, orientation, masterOffset, capability, posRelativeToMB, context);
	}

	@Override
	public <T> BlockCapabilityCache<T, ?> getCapabilityAt(
			BlockCapability<T, Direction> capability, BlockPos posRelativeToMB, RelativeBlockFace face
	)
	{
		return getCapabilityAt(capability, posRelativeToMB, face.forFront(orientation));
	}

	@Override
	public Supplier<@Nullable Level> levelSupplier()
	{
		return masterBE::getLevel;
	}

	@Override
	public Runnable getMarkDirtyRunnable()
	{
		return masterBE::setChanged;
	}

	@Override
	public Runnable getSyncRunnable()
	{
		return () -> MultiblockContext.requestBESync(masterBE);
	}

	public static <T, C> BlockCapabilityCache<T, ?> getCapabilityAt(
			BlockEntity masterBE, MultiblockOrientation orientation, BlockPos masterOffset,
			BlockCapability<T, C> capability, BlockPos posRelativeToMB, C context
	)
	{
		if(masterBE.getLevel() instanceof ServerLevel serverLevel)
		{
			final BlockPos offset = orientation.getAbsoluteOffset(posRelativeToMB.subtract(masterOffset));
			final BlockPos pos = masterBE.getBlockPos().offset(offset);
			return BlockCapabilityCache.create(capability, serverLevel, pos, context);
		}
		else
			// TODO uncached version for this?
			return null;
	}
}
