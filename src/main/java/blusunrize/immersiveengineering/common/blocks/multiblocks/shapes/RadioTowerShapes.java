/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.shapes;

import blusunrize.immersiveengineering.api.utils.shapes.ShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Function;

public class RadioTowerShapes implements Function<BlockPos, VoxelShape>
{
	public static final Function<BlockPos, VoxelShape> SHAPE_GETTER = new RadioTowerShapes();

	private RadioTowerShapes()
	{
	}

	@Override
	public VoxelShape apply(BlockPos posInMultiblock)
	{
		if(posInMultiblock.getY() < 2)
		{
			if(posInMultiblock.getZ() <= 3) // concrete foundation
				return Shapes.block();
			else if(posInMultiblock.getZ()==5) // control box front
				return Shapes.block();
			else if(posInMultiblock.getZ()==4&&posInMultiblock.getY()==0) // control center bottom
				return Shapes.block();
			else if(posInMultiblock.getX()==2) // control center top
				return Shapes.box(0, 0, 0, 1, .625, 1);
			else
			{
				// control center sides
				if(posInMultiblock.getX()==1)
					return Shapes.or(
							Shapes.box(0, 0, 0, 1, .5, 1),
							Shapes.box(.375, .5, .3125, 1, .625, 1)
					);
				else if(posInMultiblock.getX()==3)
					return Shapes.or(
							Shapes.box(0, 0, 0, 1, .5, 1),
							Shapes.box(0, .5, .3125, .625, .625, 1)
					);
			}
		}
		else if(posInMultiblock.getY() < 10)
		{
			// calculate sloping, .5 inwards over 10 blocks means .05 per step?
			final double indent = (posInMultiblock.getY()-2)*.05;
			double xMin = 0;
			double xMax = 1;
			double zMin = 0;
			double zMax = 1;

			if(posInMultiblock.getX()==1)
				xMin += indent;
			if(posInMultiblock.getX()==3)
				xMax -= indent;
			if(posInMultiblock.getZ()==1)
				zMin += indent;
			if(posInMultiblock.getZ()==3)
				zMax -= indent;

			return Shapes.box(xMin, 0, zMin, xMax, 1, zMax);
		}
		else if(posInMultiblock.getY() < 15)
		{
			Direction side = posInMultiblock.getZ()==0?Direction.NORTH: posInMultiblock.getZ()==4?Direction.SOUTH: posInMultiblock.getX()==0?Direction.WEST: Direction.EAST;

			// cental column
			if(posInMultiblock.getX() >= 1&&posInMultiblock.getX() <= 3&&posInMultiblock.getZ() >= 1&&posInMultiblock.getZ() <= 3)
				return Shapes.block();
			// sloping at the top
			if(posInMultiblock.getY()==14)
				return Shapes.or(
						Shapes.create(ShapeUtils.transformAABB(new AABB(0, 0, 0, 1, .3125, .5), side)),
						Shapes.create(ShapeUtils.transformAABB(new AABB(0, 0, .5, 1, .6875, 1), side))
				);
			// sloping at the bottom
			if(posInMultiblock.getY()==10)
				return Shapes.or(
						Shapes.create(ShapeUtils.transformAABB(new AABB(0, .6875, 0, 1, 1, .5), side)),
						Shapes.create(ShapeUtils.transformAABB(new AABB(0, .3125, .5, 1, 1, 1), side))
				);
			// solid faces
			if((posInMultiblock.getX()==0||posInMultiblock.getX()==4)&&posInMultiblock.getZ() >= 1&&posInMultiblock.getZ() <= 3)
				return Shapes.block();
			if(posInMultiblock.getX() >= 1&&posInMultiblock.getX() <= 3&&(posInMultiblock.getZ()==0||posInMultiblock.getZ()==4))
				return Shapes.block();
			// vertical corners
			VoxelShape zBox;
			VoxelShape xBox;

			if(posInMultiblock.getZ()==0)
				zBox = Shapes.box(0, 0, .5, 1, 1, 1);
			else
				zBox = Shapes.box(0, 0, 0, 1, 1, .5);
			if(posInMultiblock.getX()==0)
				xBox = Shapes.box(.5, 0, 0, 1, 1, 1);
			else
				xBox = Shapes.box(0, 0, 0, .5, 1, 1);
			return Shapes.or(zBox, xBox);
		}
		else
		{
			if(posInMultiblock.getX()!=2||posInMultiblock.getZ()!=2)
				return Shapes.empty();

			// .2 inwards over 4 blocks, .05 per step
			final double indent = (posInMultiblock.getY()-15)*.05;
			return Shapes.box(indent, 0, indent, 1-indent, 1, 1-indent);
		}
		return Shapes.empty();
	}
}
