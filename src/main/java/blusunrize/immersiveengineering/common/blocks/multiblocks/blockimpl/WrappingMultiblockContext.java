/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import java.util.function.BooleanSupplier;

public record WrappingMultiblockContext<State>(
		IMultiblockContext<?> inner, State ownState
) implements IMultiblockContext<State>
{
	@Override
	public <T> CapabilityReference<T> getCapabilityAt(Capability<T> capability, BlockPos posRelativeToMB, RelativeBlockFace face)
	{
		return inner.getCapabilityAt(capability, posRelativeToMB, face);
	}

	@Override
	public void markMasterDirty()
	{
		inner.markMasterDirty();
	}

	@Override
	public State getState()
	{
		return ownState;
	}

	@Override
	public IMultiblockLevel getLevel()
	{
		return inner.getLevel();
	}

	@Override
	public <T> LazyOptional<T> registerCapability(T value)
	{
		return inner.registerCapability(value);
	}

	@Override
	public BooleanSupplier isValid()
	{
		return inner.isValid();
	}

	@Override
	public void requestMasterBESync()
	{
		inner.requestMasterBESync();
	}

	@Override
	public void setComparatorOutputFor(BlockPos posInMultiblock, int newValue)
	{
		inner.setComparatorOutputFor(posInMultiblock, newValue);
	}

	@Override
	public int getRedstoneInputValue(BlockPos posInMultiblock, RelativeBlockFace side, int fallback)
	{
		return inner.getRedstoneInputValue(posInMultiblock, side, fallback);
	}

	@Override
	public int getRedstoneInputValue(BlockPos posInMultiblock, int fallback)
	{
		return inner.getRedstoneInputValue(posInMultiblock, fallback);
	}
}
