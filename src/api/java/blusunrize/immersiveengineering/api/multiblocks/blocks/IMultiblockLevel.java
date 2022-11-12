package blusunrize.immersiveengineering.api.multiblocks.blocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public interface IMultiblockLevel
{
	BlockState getBlock(BlockPos relativePosition);

	void setBlock(BlockPos relativePosition, BlockState state);

	@Nullable
	BlockEntity getBlockEntity(BlockPos relativePosition);

	/**
	 * This method will load chunks if the given position is not loaded yet!
	 */
	@Nullable
	BlockEntity forciblyGetBlockEntity(BlockPos relativePosition);

	@Nullable
	<T> T getCapabilityValue(Capability<T> capability, BlockPos relativePosition, @Nullable RelativeBlockFace face);

	boolean shouldTickModulo(int interval);

	BlockPos getAbsoluteOrigin();

	MultiblockOrientation getOrientation();

	BlockPos toAbsolute(BlockPos relative);

	@Nullable
	Direction toAbsolute(@Nullable RelativeBlockFace relative);

	AABB toAbsolute(AABB relative);

	Vec3 toAbsolute(Vec3 relative);

	BlockPos toRelative(BlockPos absolute);

	RelativeBlockFace toRelative(Direction absolute);

	boolean isThundering();

	boolean isRaining();

	int getMaxBuildHeight();

	Level getRawLevel();

	void updateNeighbourForOutputSignal(BlockPos posInMultiblock);
}
