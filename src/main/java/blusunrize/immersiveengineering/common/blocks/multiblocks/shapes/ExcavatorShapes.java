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

public class ExcavatorShapes implements Function<BlockPos, VoxelShape>
{
	public static final Function<BlockPos, VoxelShape> SHAPE_GETTER = new ExcavatorShapes();

	private ExcavatorShapes()
	{
	}

	@Override
	public VoxelShape apply(BlockPos posInMultiblock)
	{
		if(posInMultiblock.getX()==2&&posInMultiblock.getZ()==4)
			return Shapes.or(
					Shapes.box(0, 0, 0, .5f, 1, 1),
					Shapes.box(.5f, .25f, .25f, 1, .75f, .75f)
			);
		else if(posInMultiblock.getZ() < 3&&posInMultiblock.getY()==0&&posInMultiblock.getX()==0)
		{
			VoxelShape shape = Shapes.box(.5f, 0, 0, 1, 1, 1);
			if(posInMultiblock.getZ()==2)
				return Shapes.or(shape, Shapes.box(0, .5f, 0, .5f, 1, .5f));
			else if(posInMultiblock.getZ()==1)
				return Shapes.or(shape, Shapes.box(0, .5f, 0, .5f, 1, 1));
			else
				return Shapes.or(shape, Shapes.box(0, .5f, .5f, .5f, 1, 1));
		}
		else if(new BlockPos(2, 2, 2).equals(posInMultiblock))
			return Shapes.or(
					Shapes.box(0, 0, .375f, 1, 1, .5f),
					Shapes.box(.875f, 0, 0, 1, 1, .375f)
			);
		else if(new BlockPos(2, 2, 0).equals(posInMultiblock))
			return Shapes.or(
					Shapes.box(0, 0, .5f, 1, 1, .625f),
					Shapes.box(.875f, 0, .625f, 1, 1, 1)
			);
		if(new BlockPos(0, 2, 2).equals(posInMultiblock))
			return Shapes.box(0, 0, 0, 1, .5f, .5f);
		else if(new BlockPos(0, 2, 1).equals(posInMultiblock))
			return Shapes.box(0, 0, 0, 1, .5f, 1);
		else if(new BlockPos(0, 2, 0).equals(posInMultiblock))
			return Shapes.box(0, 0, .5f, 1, .5f, 1);
		else if(new BlockPos(2, 2, 2).equals(posInMultiblock))
			return Shapes.box(0, 0, .375f, 1, 1, .5f);
		else if(new BlockPos(2, 2, 1).equals(posInMultiblock))
			return Shapes.box(.875f, 0, 0, 1, 1, 1);
		else if(new BlockPos(2, 2, 0).equals(posInMultiblock))
			return Shapes.box(0, 0, .5f, 1, 1, .625f);
		else if(posInMultiblock.getX()==2&&posInMultiblock.getZ()==4)
			return Shapes.box(0, 0, 0, .5f, 1, 1);
		else if(posInMultiblock.getZ() < 3&&posInMultiblock.getY()==0&&posInMultiblock.getX()==0)
			return Shapes.box(.5f, 0, 0, 1, 1, 1);
		else if(posInMultiblock.getZ() < 3&&posInMultiblock.getY()==0&&posInMultiblock.getX()==2)
			return Shapes.box(0, 0, 0, .5f, 1, 1);
		else
			return Shapes.block();
	}
}
