package blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl;

import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLogic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.RelativeBlockFace;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public record MultiblockContext<State extends IMultiblockState>(
		MultiblockBEHelperMaster<State> masterHelper,
		MultiblockRegistration<State> multiblock,
		MultiblockLevel level
) implements IMultiblockContext<State>
{
	@Override
	public State getState()
	{
		return masterHelper.getState();
	}

	@Override
	public void markMasterDirty()
	{
		masterHelper.getMasterBE().setChanged();
	}

	@Override
	public IMultiblockLevel getLevel()
	{
		return level;
	}

	@Override
	public <T> LazyOptional<T> registerCapability(T value)
	{
		LazyOptional<T> result = CapabilityUtils.constantOptional(value);
		masterHelper.addCapability(result);
		return result;
	}

	@Override
	public <T> CapabilityReference<T> getCapabilityAt(
			Capability<T> capability, BlockPos posRelativeToMB, RelativeBlockFace face
	)
	{
		return CapabilitySource.getCapabilityAt(
				masterHelper.getMasterBE(), masterHelper.getOrientation(), multiblock.masterPosInMB(),
				capability, posRelativeToMB, face
		);
	}
}
