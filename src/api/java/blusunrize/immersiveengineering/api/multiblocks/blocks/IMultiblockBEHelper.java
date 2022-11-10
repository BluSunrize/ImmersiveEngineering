package blusunrize.immersiveengineering.api.multiblocks.blocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLogic.IMultiblockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public interface IMultiblockBEHelper<State extends IMultiblockState>
{
	@Nullable
	State getState();

	@Nullable
	IMultiblockContext<State> getContext();

	void load(CompoundTag tag);

	void saveAdditional(CompoundTag tag);

	CompoundTag getUpdateTag();

	void handleUpdateTag(CompoundTag tag);

	void onDataPacket(CompoundTag tag);

	<T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side);

	MultiblockRegistration<State> getMultiblock();

	VoxelShape getShape();

	void disassemble();

	void markDisassembling();

	BlockPos getPositionInMB();

	InteractionResult click(Player player, InteractionHand hand, BlockHitResult hit);

	void onEntityCollided(Entity collided);

	int getComparatorValue();
}
