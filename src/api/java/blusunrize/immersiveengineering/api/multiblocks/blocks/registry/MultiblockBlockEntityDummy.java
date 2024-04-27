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
		this.helper = IMultiblockBEHelperDummy.MAKE_HELPER.get().makeFor(this, multiblock);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, Provider provider)
	{
		super.loadAdditional(tag, provider);
		helper.load(tag);
	}

	@Override
	protected void saveAdditional(@Nonnull CompoundTag tag, Provider provider)
	{
		super.saveAdditional(tag, provider);
		helper.saveAdditional(tag);
	}

	@Nonnull
	@Override
	public CompoundTag getUpdateTag(Provider provider)
	{
		return helper.getUpdateTag();
	}

	@Override
	public void handleUpdateTag(CompoundTag tag, Provider provider)
	{
		helper.handleUpdateTag(tag);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, Provider provider)
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
