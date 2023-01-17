/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks.blocks.logic;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Function;

public interface IMultiblockLogic<State extends IMultiblockState> extends IMultiblockComponent<State>
{
	State createInitialState(IInitialMultiblockContext<State> capabilitySource);

	// TODO this API does not work for variable-size MBs
	Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType);

	default VoxelShape postProcessAbsoluteShape(
			IMultiblockContext<State> ctx,
			VoxelShape defaultShape,
			CollisionContext shapeCtx,
			BlockPos posInMultiblock,
			ShapeType type
	)
	{
		return defaultShape;
	}
}
