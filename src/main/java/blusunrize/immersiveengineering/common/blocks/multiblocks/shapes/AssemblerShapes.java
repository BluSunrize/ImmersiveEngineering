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

public class AssemblerShapes implements Function<BlockPos, VoxelShape>
{
	public static final Function<BlockPos, VoxelShape> SHAPE_GETTER = new AssemblerShapes();

	private static final Set<BlockPos> FULL_BLOCKS = Set.of(
			new BlockPos(1, 1, 2),
			new BlockPos(1, 1, 1),
			new BlockPos(1, 1, 0),
			new BlockPos(1, 2, 1)
	);

	private AssemblerShapes()
	{
	}

	@Override
	public VoxelShape apply(BlockPos posInMultiblock)
	{
		if(posInMultiblock.getY()==0||FULL_BLOCKS.contains(posInMultiblock))
			return Shapes.block();
		float xMin = 0;
		float yMin = 0;
		float zMin = 0;
		float xMax = 1;
		float yMax = 1;
		float zMax = 1;
		if(posInMultiblock.getZ()==0)
			zMin = .25f;
		else if(posInMultiblock.getZ()==2)
			zMax = .75f;
		if(posInMultiblock.getX()==0)
			xMin = .1875f;
		else if(posInMultiblock.getX()==2)
			xMax = .8125f;
		return Shapes.box(xMin, yMin, zMin, xMax, yMax, zMax);
	}
}
