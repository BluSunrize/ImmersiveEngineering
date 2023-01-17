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
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

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
		this.helper = IMultiblockBEHelperMaster.MAKE_HELPER.getValue().makeFor(this, multiblock);
	}

	@Override
	public void load(CompoundTag tag)
	{
		super.load(tag);
		helper.load(tag);
	}

	@Override
	protected void saveAdditional(CompoundTag tag)
	{
		super.saveAdditional(tag);
		helper.saveAdditional(tag);
	}

	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket()
	{
		return helper.getUpdatePacket();
	}

	@Nonnull
	@Override
	public CompoundTag getUpdateTag()
	{
		return helper.getUpdateTag();
	}

	@Override
	public void handleUpdateTag(CompoundTag tag)
	{
		helper.handleUpdateTag(tag);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
	{
		helper.onDataPacket(pkt.getTag());
	}

	@Override
	public void invalidateCaps()
	{
		super.invalidateCaps();
		helper.invalidateCaps();
	}

	@Override
	public IMultiblockBEHelperMaster<State> getHelper()
	{
		return helper;
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
	{
		return helper.getCapability(cap, side);
	}

	@Override
	public BlockPos getModelOffset(BlockState state, @javax.annotation.Nullable Vec3i size)
	{
		return BlockPos.ZERO;
	}

	@Override
	public AABB getRenderBoundingBox()
	{
		return helper.getRenderBoundingBox();
	}
}
