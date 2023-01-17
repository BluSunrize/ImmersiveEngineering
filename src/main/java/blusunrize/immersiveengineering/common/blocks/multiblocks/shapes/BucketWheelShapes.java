/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Set;
import java.util.function.Function;

public class BucketWheelShapes implements Function<BlockPos, VoxelShape>
{
	public static final Function<BlockPos, VoxelShape> SHAPE_GETTER = new BucketWheelShapes();

	private BucketWheelShapes()
	{
	}

	@Override
	public VoxelShape apply(BlockPos posInMultiblock)
	{
		if(Set.of(
				new BlockPos(3, 0, 0),
				new BlockPos(2, 1, 0),
				new BlockPos(4, 1, 0)
		).contains(posInMultiblock))
			return Shapes.box(0, .25f, 0, 1, 1, 1);
		else if(Set.of(
				new BlockPos(3, 6, 0),
				new BlockPos(2, 5, 0),
				new BlockPos(4, 5, 0)
		).contains(posInMultiblock))
			return Shapes.box(0, 0, 0, 1, .75f, 1);
		else if(new BlockPos(0, 3, 0).equals(posInMultiblock))
			return Shapes.box(.25f, 0, 0, 1, 1, 1);
		else if(new BlockPos(6, 3, 0).equals(posInMultiblock))
			return Shapes.box(0, 0, 0, .75f, 1, 1);
		else if(Set.of(
				new BlockPos(1, 2, 0),
				new BlockPos(1, 4, 0)
		).contains(posInMultiblock))
			return Shapes.box(.25f, 0, 0, 1, 1, 1);
		else if(Set.of(
				new BlockPos(5, 2, 0),
				new BlockPos(5, 4, 0)
		).contains(posInMultiblock))
			return Shapes.box(0, 0, 0, .75f, 1, 1);
		else
			return Shapes.block();
	}
}
