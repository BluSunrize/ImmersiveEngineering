package blusunrize.immersiveengineering.api.multiblocks.blocks.env;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

import javax.annotation.Nullable;

@NonExtendable
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

	VoxelShape getShape(CollisionContext ctx);

	void disassemble();

	void markDisassembling();

	BlockPos getPositionInMB();

	InteractionResult click(Player player, InteractionHand hand, BlockHitResult hit);

	void onEntityCollided(Entity collided);

	int getComparatorValue();

	void onNeighborChanged(BlockPos fromPos);

	int getRedstoneInput(RelativeBlockFace side);

	BlockState getOriginalBlock(Level level);
}
