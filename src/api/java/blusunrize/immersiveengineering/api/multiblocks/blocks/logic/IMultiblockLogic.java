package blusunrize.immersiveengineering.api.multiblocks.blocks.logic;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Function;

public interface IMultiblockLogic<State extends IMultiblockState> extends IMultiblockComponent<State>
{
	State createInitialState(IInitialMultiblockContext<State> capabilitySource);

	// TODO split into collision and selection?
	// TODO this API does not work for variable-size MBs
	Function<BlockPos, VoxelShape> shapeGetter();

	default VoxelShape postProcessAbsoluteShape(
			IMultiblockContext<State> ctx, VoxelShape defaultShape, CollisionContext shapeCtx, BlockPos posInMultiblock
	)
	{
		return defaultShape;
	}
}
