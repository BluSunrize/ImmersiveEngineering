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

public record SiloTankShapes(int height, boolean offsetLegs) implements Function<BlockPos, VoxelShape>
{
	@Override
	public VoxelShape apply(BlockPos posInMultiblock)
	{
		boolean isCenter = posInMultiblock.getX()==1&&posInMultiblock.getZ()==1;
		if(!isCenter&&posInMultiblock.getY()==0)
		{
			// Wooden supports
			float xMin = .375f;
			float xMax = .625f;
			float zMin = .375f;
			float zMax = .625f;
			if(offsetLegs)
			{
				xMin = posInMultiblock.getX()==2?.75f: 0;
				xMax = posInMultiblock.getX()==0?.25f: 1;
				zMin = posInMultiblock.getZ()==2?.75f: 0;
				zMax = posInMultiblock.getZ()==0?.25f: 1;
			}
			return Shapes.box(xMin, 0, zMin, xMax, 1, zMax);
		}
		else if(!isCenter&&posInMultiblock.getY()==height)
		{
			// Top level of the tank, stair-like structure
			float xMin = posInMultiblock.getX()==0?0.5f: 0;
			float xMax = posInMultiblock.getX()==2?0.5f: 1;
			float zMin = posInMultiblock.getZ()==0?0.5f: 0;
			float zMax = posInMultiblock.getZ()==2?0.5f: 1;
			return Shapes.or(
					Shapes.box(0, 0, 0, 1, 0.5, 1),
					Shapes.box(xMin, 0, zMin, xMax, 1, zMax)
			);
		}
		else
			return Shapes.block();
	}
}
