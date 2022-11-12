package blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl;

import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLogic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerChunkCache;
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
	public boolean isValid()
	{
		return !masterHelper.getMasterBE().isRemoved();
	}

	@Override
	public void requestMasterBESync()
	{
		final var level = this.level.getRawLevel();
		if(level!=null&&level.getChunkSource() instanceof ServerChunkCache chunkCache)
			chunkCache.blockChanged(this.masterHelper.getMasterBE().getBlockPos());
	}

	@Override
	public void setComparatorOutputFor(BlockPos posInMultiblock, int newValue)
	{
		Preconditions.checkState(masterHelper.multiblock.hasComparatorOutput());
		final var oldValue = masterHelper.getCurrentComparatorOutputs().put(posInMultiblock, newValue);
		if(oldValue!=newValue)
			level.updateNeighbourForOutputSignal(posInMultiblock);
	}

	@Override
	public <T> CapabilityReference<T> getCapabilityAt(
			Capability<T> capability, BlockPos posRelativeToMB, RelativeBlockFace face
	)
	{
		return InitialMultiblockContext.getCapabilityAt(
				masterHelper.getMasterBE(), masterHelper.getOrientation(), multiblock.masterPosInMB(),
				capability, posRelativeToMB, face
		);
	}

	@Override
	public int getRedstoneInputValue(BlockPos posInMultiblock, RelativeBlockFace side, int fallback)
	{
		Preconditions.checkState(masterHelper.multiblock.redstoneInputAware());
		if(level.getBlockEntity(posInMultiblock) instanceof IMultiblockBE<?> beAtPos)
			return beAtPos.getHelper().getRedstoneInput(side);
		else
			return fallback;
	}

	@Override
	public int getRedstoneInputValue(BlockPos posInMultiblock, int fallback)
	{
		Preconditions.checkState(masterHelper.multiblock.redstoneInputAware());
		if(!(level.getBlockEntity(posInMultiblock) instanceof IMultiblockBE<?> beAtPos))
			return fallback;
		int result = 0;
		for(final var face : RelativeBlockFace.values())
			result = Math.max(result, beAtPos.getHelper().getRedstoneInput(face));
		return result;
	}
}
