/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks.blocks.registry;

import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

// TODO invalidate caps on dummy blocks when loaded or unloaded
public class MultiblockBlockEntityMaster<State extends IMultiblockState>
		extends BlockEntity
		implements IModelOffsetProvider, IMultiblockBE<State>
{
	private final IMultiblockBEHelperMaster<State> helper;

	public MultiblockBlockEntityMaster(
			BlockEntityType<?> type,
			BlockPos worldPosition,
			BlockState blockState,
			MultiblockRegistration<State> multiblock
	)
	{
		super(type, worldPosition, blockState);
		this.helper = IMultiblockBEHelperMaster.MAKE_HELPER.get().makeFor(this, multiblock);
	}

	@Override
	public void loadAdditional(CompoundTag tag, Provider provider)
	{
		super.loadAdditional(tag, provider);
		helper.load(tag, provider);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, Provider provider)
	{
		super.saveAdditional(tag, provider);
		helper.saveAdditional(tag, provider);
	}

	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket()
	{
		return helper.getUpdatePacket();
	}

	@Nonnull
	@Override
	public CompoundTag getUpdateTag(Provider provider)
	{
		return helper.getUpdateTag(provider);
	}

	@Override
	public void handleUpdateTag(CompoundTag tag, Provider provider)
	{
		helper.handleUpdateTag(tag, provider);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, Provider provider)
	{
		helper.onDataPacket(pkt.getTag(), provider);
	}

	@Override
	public IMultiblockBEHelperMaster<State> getHelper()
	{
		return helper;
	}

	@Override
	public BlockPos getModelOffset(BlockState state, @javax.annotation.Nullable Vec3i size)
	{
		return BlockPos.ZERO;
	}

	@Override
	public void setRemoved()
	{
		super.setRemoved();
		helper.onRemoved();
		helper.invalidateAllCaps();
	}

	@Override
	public void onLoad()
	{
		super.onLoad();
		helper.invalidateAllCaps();
	}
}
