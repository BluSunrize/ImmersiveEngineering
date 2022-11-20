package blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MultiblockBEHelperMaster<State extends IMultiblockState>
		extends MultiblockBEHelperCommon<State>
		implements IMultiblockBEHelperMaster<State>
{
	private final State state;
	private final MultiblockContext<State> context;
	private final List<LazyOptional<?>> capabilities = new ArrayList<>();
	private final Object2IntMap<BlockPos> currentComparatorOutputs = new Object2IntOpenHashMap<>();

	public MultiblockBEHelperMaster(MultiblockBlockEntityMaster<State> be, MultiblockRegistration<State> multiblock)
	{
		super(be, multiblock, be.getBlockState());
		this.state = multiblock.logic().createInitialState(new InitialMultiblockContext<>(
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

	@Nullable
	@Override
	protected IMultiblockBEHelperMaster<State> getMasterHelperWithChunkloads()
	{
		return this;
	}

	@Nullable
	@Override
	protected MultiblockBEHelperMaster<State> getMasterHelper()
	{
		return this;
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
	public Packet<ClientGamePacketListener> getUpdatePacket()
	{
		return ClientboundBlockEntityDataPacket.create(getMasterBE());
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
	public MultiblockRegistration<State> getMultiblock()
	{
		return multiblock;
	}

	@Override
	public BlockPos getPositionInMB()
	{
		return multiblock.masterPosInMB();
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

	public Object2IntMap<BlockPos> getCurrentComparatorOutputs()
	{
		return currentComparatorOutputs;
	}
}
