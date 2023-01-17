/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks.blocks.env;

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
import org.jetbrains.annotations.ApiStatus.NonExtendable;

import javax.annotation.Nullable;

@NonExtendable
public interface IMultiblockLevel
{
	BlockState getBlockState(BlockPos relativePosition);

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
