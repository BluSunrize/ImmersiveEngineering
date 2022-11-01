package blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl;

import blusunrize.immersiveengineering.api.multiblocks.blocks.*;
import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLogic.IMultiblockState;
import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.function.Function;

public class MultiblockBEHelperDummy<State extends IMultiblockState> implements IMultiblockBEHelperDummy<State>
{
	private final BlockEntity be;
	private final MultiblockRegistration<State> multiblock;
	private final MultiblockOrientation orientation;
	private final MultiblockLevel level;
	private BlockPos positionInMB;

	public MultiblockBEHelperDummy(BlockEntity be, MultiblockRegistration<State> multiblock)
	{
		this.be = be;
		this.multiblock = multiblock;
		this.orientation = new MultiblockOrientation(be.getBlockState(), multiblock.mirrorable());
		this.level = new MultiblockLevel(be::getLevel, orientation, () -> {
			final BlockPos absoluteOffset = orientation.getAbsoluteOffset(positionInMB);
			return be.getBlockPos().subtract(absoluteOffset);
		});
		this.positionInMB = BlockPos.ZERO;
	}

	@Override
	@Nullable
	public State getState()
	{
		return getOnMaster(IMultiblockBEHelperMaster::getState);
	}

	@Override
	@Nullable
	public IMultiblockContext<State> getContext()
	{
		return getOnMaster(IMultiblockBEHelperMaster::getContext);
	}

	@Override
	public void load(CompoundTag tag)
	{
		this.positionInMB = NbtUtils.readBlockPos(tag.getCompound("posInMB"));
	}

	@Override
	public void saveAdditional(CompoundTag tag)
	{
		tag.put("posInMB", NbtUtils.writeBlockPos(this.positionInMB));
	}

	@Override
	public CompoundTag getUpdateTag()
	{
		CompoundTag result = new CompoundTag();
		saveAdditional(result);
		return result;
	}

	@Override
	public void handleUpdateTag(CompoundTag tag)
	{
		load(tag);
	}

	@Override
	public void onDataPacket(CompoundTag tag)
	{
		load(tag);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side)
	{
		// TODO right now this is 2 master lookups, but we should really just cache the master BE in a weak reference
		//  and check if it's still valid
		final var state = getState();
		final var ctx = getContext();
		if(state==null||ctx==null)
			return LazyOptional.empty();
		final var relativeSide = RelativeBlockFace.from(orientation, side);
		return multiblock.logic().getCapability(ctx, positionInMB, relativeSide, cap);
	}

	@Override
	public MultiblockRegistration<State> getMultiblock()
	{
		return multiblock;
	}

	@Nullable
	private <T> T getOnMaster(Function<IMultiblockBEHelperMaster<State>, T> get)
	{
		final var master = getMasterHelper();
		if(master!=null)
			return get.apply(master);
		else
			return null;
	}

	@Nullable
	private IMultiblockBEHelperMaster<State> getMasterHelper()
	{
		final var beAtMasterPos = level.getBlockEntity(multiblock.masterPosInMB());
		if(!(beAtMasterPos instanceof MultiblockBlockEntityMaster<?> masterBE))
			return null;
		final var masterHelper = masterBE.getHelper();
		if(masterHelper.getMultiblock()==multiblock)
			//noinspection unchecked
			return (IMultiblockBEHelperMaster<State>)masterHelper;
		else
			return null;
	}

	@Override
	public void setPositionInMB(BlockPos pos)
	{
		Preconditions.checkArgument(!multiblock.masterPosInMB().equals(pos));
		this.positionInMB = pos;
		this.be.setChanged();
	}
}
