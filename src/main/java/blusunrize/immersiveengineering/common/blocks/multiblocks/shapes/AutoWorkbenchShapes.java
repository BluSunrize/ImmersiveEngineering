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

public class AutoWorkbenchShapes implements Function<BlockPos, VoxelShape>
{
	public static final Function<BlockPos, VoxelShape> SHAPE_GETTER = new AutoWorkbenchShapes();
	private static final Set<BlockPos> HIGH_FULL_BLOCKS = Set.of(
			new BlockPos(0, 1, 2),
			new BlockPos(0, 1, 1)
	);
	private static final Set<BlockPos> CONVEYORS = Set.of(
			new BlockPos(1, 1, 1),
			new BlockPos(2, 1, 1),
			new BlockPos(0, 1, 0),
			new BlockPos(1, 1, 0)
	);

	private AutoWorkbenchShapes()
	{
	}

	@Override
	public VoxelShape apply(BlockPos posInMultiblock)
	{
		if(posInMultiblock.getY()==0||HIGH_FULL_BLOCKS.contains(posInMultiblock))
			return Shapes.box(0, 0, 0, 1, 1, 1);
		if(CONVEYORS.contains(posInMultiblock))
			return Shapes.box(0, 0, 0, 1, .125f, 1);
		float xMin = 0;
		float yMin = 0;
		float zMin = 0;
		float xMax = 1;
		float yMax = 1;
		float zMax = 1;
		if(Set.of(
				new BlockPos(1, 1, 2),
				new BlockPos(2, 1, 2)
		).contains(posInMultiblock))
		{
			//TODO more sensible name
			boolean is11 = new BlockPos(2, 1, 2).equals(posInMultiblock);
			yMax = .8125f;
			zMin = .1875f;
			if(is11)
				xMax = .875f;
		}
		if(new BlockPos(2, 1, 0).equals(posInMultiblock))
		{
			yMax = .3125f;
			zMin = .25f;
			xMax = .875f;
		}
		return Shapes.box(xMin, yMin, zMin, xMax, yMax, zMax);
	}
}
