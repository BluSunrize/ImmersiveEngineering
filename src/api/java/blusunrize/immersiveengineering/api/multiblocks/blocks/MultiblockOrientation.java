package blusunrize.immersiveengineering.api.multiblocks.blocks;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

public record MultiblockOrientation(Direction front, boolean mirrored)
{
	public MultiblockOrientation
	{
		Preconditions.checkArgument(front.getAxis().isHorizontal());
	}

	public MultiblockOrientation(BlockState blockState, boolean mirrorable)
	{
		this(
				blockState.getValue(IEProperties.FACING_HORIZONTAL),
				mirrorable&&blockState.getValue(IEProperties.MIRRORED)
		);
	}

	public BlockPos getAbsoluteOffset(BlockPos offsetInMB)
	{
		return TemplateMultiblock.getAbsoluteOffset(offsetInMB, mirrored, front);
	}

	public BlockPos getPosInMB(BlockPos absoluteOffset)
	{
		Rotation rot = DirectionUtils.getRotationBetweenFacings(front, Direction.NORTH);
		if(rot==null)
			return BlockPos.ZERO;
		final BlockPos withoutRotation = TemplateMultiblock.getAbsoluteOffset(absoluteOffset, Mirror.NONE, rot);
		return TemplateMultiblock.getAbsoluteOffset(
				withoutRotation, mirrored?Mirror.FRONT_BACK: Mirror.NONE, Rotation.NONE
		);
	}
}
