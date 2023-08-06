/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks.blocks.registry;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperDummy;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class MultiblockBlockEntityDummy<State extends IMultiblockState>
		extends BlockEntity
		implements IModelOffsetProvider, IMultiblockBE<State>
{
	private final IMultiblockBEHelperDummy<State> helper;

	public MultiblockBlockEntityDummy(
			BlockEntityType<?> type,
			BlockPos worldPosition,
			BlockState blockState,
			MultiblockRegistration<State> multiblock
	)
	{
		super(type, worldPosition, blockState);
		this.helper = IMultiblockBEHelperDummy.MAKE_HELPER.getValue().makeFor(this, multiblock);
	}

	@Override
	public void load(@Nonnull CompoundTag tag)
	{
		super.load(tag);
		helper.load(tag);
	}

	@Override
	protected void saveAdditional(@Nonnull CompoundTag tag)
	{
		super.saveAdditional(tag);
		helper.saveAdditional(tag);
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

	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket()
	{
		return helper.getUpdatePacket();
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
	{
		final LazyOptional<T> ownCap = helper.getCapability(cap, side);
		if(ownCap.isPresent())
			return ownCap;
		else
			return super.getCapability(cap, side);
	}

	@Override
	public IMultiblockBEHelperDummy<State> getHelper()
	{
		return helper;
	}

	@Override
	public BlockPos getModelOffset(BlockState state, Vec3i size)
	{
		BlockPos mirroredPosInMB = helper.getPositionInMB();
		if(helper.getMultiblock().mirrorable()&&state.getValue(IEProperties.MIRRORED))
			mirroredPosInMB = new BlockPos(
					size.getX()-mirroredPosInMB.getX()-1,
					mirroredPosInMB.getY(),
					mirroredPosInMB.getZ()
			);
		return mirroredPosInMB.subtract(helper.getMultiblock().masterPosInMB());
	}
}
