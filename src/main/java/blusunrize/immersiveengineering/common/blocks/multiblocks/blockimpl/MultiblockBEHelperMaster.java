/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration.ExtraComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class MultiblockBEHelperMaster<State extends IMultiblockState>
		extends MultiblockBEHelperCommon<State>
		implements IMultiblockBEHelperMaster<State>
{
	private final State state;
	private final MultiblockContext<State> context;
	private final List<ComponentInstance<?>> componentInstances;
	private final List<LazyOptional<?>> capabilities = new ArrayList<>();
	private final Object2IntMap<BlockPos> currentComparatorOutputs = new Object2IntOpenHashMap<>();
	private final CachedValue<BlockPos, MultiblockOrientation, AABB> renderBox;

	public MultiblockBEHelperMaster(MultiblockBlockEntityMaster<State> be, MultiblockRegistration<State> multiblock)
	{
		super(be, multiblock, be.getBlockState());
		this.state = multiblock.logic().createInitialState(new InitialMultiblockContext<>(
				be, orientation, multiblock.masterPosInMB()
		));
		final BlockPos multiblockOrigin = be.getBlockPos().subtract(
				orientation.getAbsoluteOffset(multiblock.masterPosInMB())
		);
		final MultiblockLevel level = new MultiblockLevel(be::getLevel, this.orientation, multiblockOrigin);
		this.context = new MultiblockContext<>(this, multiblock, level);
		this.componentInstances = new ArrayList<>();
		for(ExtraComponent<State, ?> c : multiblock.extraComponents())
			this.componentInstances.add(ComponentInstance.make(c, this.state, this.context));
		this.renderBox = new CachedValue<>((origin, orientation) -> {
			final BlockPos max = new BlockPos(multiblock.size(level.getRawLevel()));
			final BlockPos absoluteOffset = orientation.getAbsoluteOffset(max);
			return new AABB(origin, origin.offset(absoluteOffset));
		});
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
		load(tag, IMultiblockState::readSaveNBT);
	}

	@Override
	public void saveAdditional(CompoundTag tag)
	{
		save(tag, IMultiblockState::writeSaveNBT);
	}

	private void save(CompoundTag out, BiConsumer<IMultiblockState, CompoundTag> saveSingle)
	{
		saveSingle.accept(state, out);
		ListTag savedComponents = new ListTag();
		for(final ComponentInstance<?> component : componentInstances)
			if(component.state() instanceof IMultiblockState saveable)
			{
				CompoundTag componentNBT = new CompoundTag();
				saveSingle.accept(saveable, componentNBT);
				savedComponents.add(componentNBT);
			}
		if(!savedComponents.isEmpty())
			out.put("componentNBT", savedComponents);
	}

	private void load(CompoundTag in, BiConsumer<IMultiblockState, CompoundTag> loadSingle)
	{
		loadSingle.accept(state, in);
		ListTag savedComponents = in.getList("componentNBT", Tag.TAG_COMPOUND);
		int nextIndex = 0;
		for(final ComponentInstance<?> component : componentInstances)
			if(component.state() instanceof IMultiblockState saveable)
			{
				loadSingle.accept(saveable, savedComponents.getCompound(nextIndex));
				++nextIndex;
				if(nextIndex >= savedComponents.size())
					break;
			}
	}

	@Override
	public CompoundTag getUpdateTag()
	{
		CompoundTag result = new CompoundTag();
		save(result, IMultiblockState::writeSyncNBT);
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
		load(tag, IMultiblockState::readSyncNBT);
	}

	@Override
	public void onDataPacket(CompoundTag tag)
	{
		load(tag, IMultiblockState::readSyncNBT);
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

	@Override
	public AABB getRenderBoundingBox()
	{
		return renderBox.get(context.getLevel().getAbsoluteOrigin(), orientation);
	}

	@Override
	public void tickServer()
	{
		if(!SafeChunkUtils.isChunkSafe(be.getLevel(), be.getBlockPos()))
			return;
		final IMultiblockComponent<State> logic = multiblock.logic();
		if(logic instanceof IServerTickableComponent<State> serverTickable)
			serverTickable.tickServer(getContext());
		for(final ComponentInstance<?> component : componentInstances)
			component.tickServer();
	}

	@Override
	public void tickClient()
	{
		if(!SafeChunkUtils.isChunkSafe(be.getLevel(), be.getBlockPos()))
			return;
		final IMultiblockComponent<State> logic = multiblock.logic();
		if(logic instanceof IClientTickableComponent<State> clientTickable)
			clientTickable.tickClient(getContext());
		for(final ComponentInstance<?> component : componentInstances)
			component.tickClient();
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

	public List<ComponentInstance<?>> getComponentInstances()
	{
		return componentInstances;
	}
}
