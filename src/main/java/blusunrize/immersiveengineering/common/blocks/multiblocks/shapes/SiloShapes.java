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

import java.util.function.Function;

public class SiloShapes implements Function<BlockPos, VoxelShape>
{
	public static final Function<BlockPos, VoxelShape> SHAPE_GETTER = new SiloShapes();

	private SiloShapes()
	{
	}

	@Override
	public VoxelShape apply(BlockPos posInMultiblock)
	{
		if(posInMultiblock.getX()%2!=0||posInMultiblock.getY()!=0||posInMultiblock.getZ()%2!=0)
			return Shapes.block();
		float xMin = posInMultiblock.getX()==2?.75f: 0;
		float xMax = posInMultiblock.getX()==0?.25f: 1;
		float zMin = posInMultiblock.getZ()==2?.75f: 0;
		float zMax = posInMultiblock.getZ()==0?.25f: 1;
		return Shapes.box(xMin, 0, zMin, xMax, 1, zMax);
	}
}
