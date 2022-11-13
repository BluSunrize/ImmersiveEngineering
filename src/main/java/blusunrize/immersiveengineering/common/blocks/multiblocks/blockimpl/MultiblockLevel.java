package blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

public record MultiblockLevel(
		Supplier<Level> getLevel, MultiblockOrientation orientation, Supplier<BlockPos> origin
) implements IMultiblockLevel
{
	public MultiblockLevel(Supplier<Level> getLevel, MultiblockOrientation orientation, BlockPos origin)
	{
		this(getLevel, orientation, () -> origin);
	}

	@Override
	public BlockState getBlock(BlockPos relativePosition)
	{
		return SafeChunkUtils.getBlockState(level(), toAbsolute(relativePosition));
	}

	@Override
	public void setBlock(BlockPos relativePosition, BlockState state)
	{
		level().setBlock(toAbsolute(relativePosition), state, Block.UPDATE_ALL);
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos relativePosition)
	{
		return SafeChunkUtils.getSafeBE(level(), toAbsolute(relativePosition));
	}

	@Nullable
	@Override
	public BlockEntity forciblyGetBlockEntity(BlockPos relativePosition)
	{
		return level().getBlockEntity(toAbsolute(relativePosition));
	}

	@Override
	@Nullable
	public <T> T getCapabilityValue(
			Capability<T> capability, BlockPos relativePosition, @Nullable RelativeBlockFace face
	)
	{
		final var blockEntity = getBlockEntity(relativePosition);
		if(blockEntity==null)
			return null;
		final var absoluteFace = toAbsolute(face);
		return CapabilityUtils.getCapability(blockEntity, capability, absoluteFace);
	}

	@Override
	public boolean shouldTickModulo(int interval)
	{
		final var posRandom = 0x7f_ff_ff_ff&origin.hashCode();
		return posRandom%interval==level().getGameTime()%interval;
	}

	@Override
	public BlockPos getAbsoluteOrigin()
	{
		return origin.get();
	}

	@Override
	public MultiblockOrientation getOrientation()
	{
		return orientation;
	}

	@Override
	public BlockPos toAbsolute(BlockPos relative)
	{
		return getAbsoluteOrigin().offset(orientation.getAbsoluteOffset(relative));
	}

	@Override
	public @Nullable Direction toAbsolute(@Nullable RelativeBlockFace relative)
	{
		if(relative!=null)
			return relative.forFront(orientation);
		else
			return null;
	}

	@Override
	public AABB toAbsolute(AABB relative)
	{
		final var minPos = new Vec3(relative.minX, relative.minY, relative.minZ);
		final var maxPos = new Vec3(relative.maxX, relative.maxY, relative.maxZ);
		return new AABB(toAbsolute(minPos), toAbsolute(maxPos));
	}

	@Override
	public Vec3 toAbsolute(Vec3 relative)
	{
		return Vec3.atLowerCornerOf(getAbsoluteOrigin()).add(orientation.getAbsoluteOffset(relative));
	}

	@Override
	public BlockPos toRelative(BlockPos absolute)
	{
		final var absoluteOffset = absolute.subtract(getAbsoluteOrigin());
		return orientation.getPosInMB(absoluteOffset);
	}

	@Override
	public RelativeBlockFace toRelative(Direction absolute)
	{
		return RelativeBlockFace.from(orientation, absolute);
	}

	@Override
	public boolean isThundering()
	{
		return level().isThundering();
	}

	@Override
	public boolean isRaining()
	{
		return level().isRaining();
	}

	@Override
	public int getMaxBuildHeight()
	{
		return level().getMaxBuildHeight();
	}

	@Override
	public Level getRawLevel()
	{
		return level();
	}

	@Override
	public void updateNeighbourForOutputSignal(BlockPos posInMultiblock)
	{
		final var absolutePos = toAbsolute(posInMultiblock);
		if(!SafeChunkUtils.isChunkSafe(level(), absolutePos))
			return;
		final var stateAt = level().getBlockState(absolutePos);
		level().updateNeighbourForOutputSignal(absolutePos, stateAt.getBlock());
	}

	private Level level()
	{
		return Objects.requireNonNull(getLevel.get());
	}
}
