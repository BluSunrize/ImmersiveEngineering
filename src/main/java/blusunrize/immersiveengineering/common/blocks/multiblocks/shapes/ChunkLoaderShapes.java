/*
 * BluSunrize
 * Copyright (c) 2025
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.shapes;


import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Function;

public class ChunkLoaderShapes implements Function<BlockPos, VoxelShape>
{
	public static final Function<BlockPos, VoxelShape> SHAPE_GETTER = new ChunkLoaderShapes();

	@Override
	public VoxelShape apply(BlockPos pos)
	{
		// Base
		if(pos.getY()==0)
			return Shapes.block();
		// Inset area for everywhere else
		VoxelShape base = Shapes.box(
				pos.getX()==0?.5f: 0,
				0,
				pos.getZ()==0?.5f: 0,
				pos.getX()==2?.5f: 1,
				pos.getY()==4?.5f: 1,
				pos.getZ()==2?.5f: 1
		);

		boolean isCorner = pos.getX()!=1&&pos.getZ()!=1;
		// Interfacing elements
		if(pos.getY()==1&&!isCorner)
		{
			// Item hatch or redstone panel
			if(pos.getX()==0||pos.getZ()==2)
				return Shapes.block();
			// Printer
			if(pos.getZ()==0)
				return Shapes.or(base, Shapes.box(0, .6875, .25, 1, 1, .5));
			// Power plug
			if(pos.getX()==2)
				return Shapes.or(base, Shapes.box(.5, .25, .25, 1, .75, .75));
			// Everything else
			return base;
		}

		// Additional elements, empty by default, defined on certain positions
		VoxelShape post = Shapes.empty();
		VoxelShape frameX = Shapes.empty();
		VoxelShape frameZ = Shapes.empty();
		float postWidth = pos.getY()==1?.25f: .375f;

		if(isCorner)
			post = Shapes.box(
					pos.getX()==2?1-postWidth: 0,
					0,
					pos.getZ()==2?1-postWidth: 0,
					pos.getX()==0?postWidth: 1,
					1,
					pos.getZ()==0?postWidth: 1
			);
		if((pos.getY()==2||pos.getY()==4)&&pos.getX()!=1)
			frameX = Shapes.box(
					pos.getX()==2?1-postWidth: 0,
					pos.getY()==4?1-postWidth: 0,
					0,
					pos.getX()==0?postWidth: 1,
					pos.getY()==2?postWidth: 1,
					1
			);
		if((pos.getY()==2||pos.getY()==4)&&pos.getZ()!=1)
			frameZ = Shapes.box(
					0,
					pos.getY()==4?1-postWidth: 0,
					pos.getZ()==2?1-postWidth: 0,
					1,
					pos.getY()==2?postWidth: 1,
					pos.getZ()==0?postWidth: 1
			);
		return Shapes.or(base, post, frameX, frameZ);
	}
}
