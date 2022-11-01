package blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl;

import blusunrize.immersiveengineering.api.multiblocks.blocks.*;
import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLogic.IMultiblockState;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MultiblockBEHelperMaster<State extends IMultiblockState> implements IMultiblockBEHelperMaster<State>
{
	private final MultiblockBlockEntityMaster<State> be;
	private final MultiblockRegistration<State> multiblock;
	private final MultiblockOrientation orientation;
	private final State state;
	private final MultiblockContext<State> context;
	private final List<LazyOptional<?>> capabilities = new ArrayList<>();

	public MultiblockBEHelperMaster(MultiblockBlockEntityMaster<State> be, MultiblockRegistration<State> multiblock)
	{
		this.be = be;
		this.multiblock = multiblock;
		this.orientation = new MultiblockOrientation(be.getBlockState(), multiblock.mirrorable());
		this.state = multiblock.logic().createInitialState(new CapabilitySource(
				be, orientation, multiblock.masterPosInMB()
		));
		final var multiblockOrigin = be.getBlockPos().subtract(
				orientation.getAbsoluteOffset(multiblock.masterPosInMB())
		);
		final var level = new MultiblockLevel(be::getLevel, this.orientation, multiblockOrigin);
		this.context = new MultiblockContext<>(this, multiblock, level);
	}

	@Nonnull
	@Override
	public State getState()
	{
		return state;
	}

	@Nonnull
	@Override
	public MultiblockContext<State> getContext()
	{
		return context;
	}

	@Override
	public void load(CompoundTag tag)
	{
		state.readSaveNBT(tag);
	}

	@Override
	public void saveAdditional(CompoundTag tag)
	{
		state.writeSaveNBT(tag);
	}

	@Override
	public CompoundTag getUpdateTag()
	{
		CompoundTag result = new CompoundTag();
		state.writeSyncNBT(result);
		return result;
	}

	@Override
	public void handleUpdateTag(CompoundTag tag)
	{
		state.readSyncNBT(tag);
	}

	@Override
	public void onDataPacket(CompoundTag tag)
	{
		state.readSyncNBT(tag);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side)
	{
		return multiblock.logic().getCapability(
				context, multiblock.masterPosInMB(), RelativeBlockFace.from(orientation, side), cap
		);
	}

	@Override
	public MultiblockRegistration<State> getMultiblock()
	{
		return multiblock;
	}

	@Override
	public VoxelShape getShape()
	{
		// TODO cache!
		return orientation.transformRelativeShape(multiblock.logic().getShape(multiblock.masterPosInMB()));
	}

	@Override
	public void invalidateCaps()
	{
		this.capabilities.forEach(LazyOptional::invalidate);
	}

	public BlockEntity getMasterBE()
	{
		return be;
	}

	public MultiblockOrientation getOrientation()
	{
		return orientation;
	}

	public void addCapability(LazyOptional<?> cap)
	{
		this.capabilities.add(cap);
	}
}
