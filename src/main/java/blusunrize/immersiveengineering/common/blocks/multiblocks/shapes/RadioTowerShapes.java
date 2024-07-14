/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.shapes;

import blusunrize.immersiveengineering.api.utils.shapes.ShapeUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Set;
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
//		if(new BlockPos(0, 0, 4).equals(posInMultiblock))
//			return Shapes.or(
//					Shapes.box(0, 0, 0, 1, .5f, 1),
//					Shapes.box(0.125, .5f, 0.625, 0.25, 1, 0.875),
//					Shapes.box(0.75, .5f, 0.625, 0.875, 1, 0.875)
//			);
//		else if(posInMultiblock.getZ()==0&&posInMultiblock.getY()==1&&posInMultiblock.getX() >= 1&&posInMultiblock.getX() <= 3)
//			return Shapes.or(
//					Shapes.box(0, 0, 0.25, 1, 1, 1),
//					Shapes.box(0.25, .25f, 0, 0.75, .75, 0.25)
//			);
//		else if(posInMultiblock.getX()%4==0&&posInMultiblock.getZ() <= 2)
//		{
//			VoxelShape result;
//			if(posInMultiblock.getY()==0)
//				result = Shapes.box(0, 0, 0, 1, .5f, 1);
//			else
//				result = Shapes.empty();
//			final boolean flip = posInMultiblock.getX()==4;
//			double minX = !flip?.5f: 0;
//			double maxX = flip?.5f: 1;
//			if(posInMultiblock.getX()!=3)
//				result = Shapes.or(result, Shapes.box(minX, posInMultiblock.getY()==0?.5: 0, 0, maxX, 1, 1));
//			if(posInMultiblock.getY()==0)
//			{
//				int move = (4-posInMultiblock.getZ())-2;
//				minX = !flip?.125f: .625f;
//				maxX = !flip?.375f: .875f;
//				result = Shapes.or(result, Shapes.box(minX, .6875, -1.625f+move, maxX, .9375, 0.625+move));
//
//				minX = !flip?.375f: .5f;
//				maxX = !flip?.5f: .625f;
//				result = Shapes.or(result, Shapes.box(minX, .6875, 0.375+move, maxX, .9375, 0.625+move));
//				result = Shapes.or(result, Shapes.box(minX, .6875, -1.625f+move, maxX, .9375, -1.375f+move));
//			}
//			else if(posInMultiblock.getY()==1)
//			{
//				int move = (4-posInMultiblock.getZ())-2;
//				minX = !flip?.125f: .625f;
//				maxX = !flip?.375f: .875f;
//				result = Shapes.or(result, Shapes.box(minX, .125, -1.625f+move, maxX, .375, .625f+move));
//
//				minX = !flip?.375f: .5f;
//				maxX = !flip?.5f: .625f;
//				double offsetY = posInMultiblock.getX()==0?0.6875: 0;
//				result = Shapes.or(result, Shapes.box(minX, .125+offsetY, 0.375+move, maxX, .375+offsetY, 0.625+move));
//				if(posInMultiblock.getX()==0)
//				{
//					minX = !flip?.125f: .625f;
//					maxX = !flip?.375f: .875f;
//					result = Shapes.or(result, Shapes.box(minX, .375, 0.375+move, maxX, 1.0625, 0.625+move));
//				}
//				minX = !flip?.375f: .5f;
//				maxX = !flip?.5f: .625f;
//				result = Shapes.or(result, Shapes.box(minX, .125, -1.625f+move, maxX, .375, -1.375f+move));
//			}
//			else if(Set.of(
//					new BlockPos(4, 2, 2),
//					new BlockPos(0, 2, 2)
//			).contains(posInMultiblock))
//			{
//				minX = !flip?.375f: .5f;
//				maxX = !flip?.5f: .625f;
//				result = Shapes.or(result, Shapes.box(minX, .25, 0.25, maxX, .75, 0.75));
//			}
//			return result;
//		}
//		if(Set.of(
//				new BlockPos(3, 0, 4),
//				new BlockPos(1, 0, 4)
//		).contains(posInMultiblock))
//			return Shapes.box(0, 0, 0, 1, .5f, .5625f);
//		else if(posInMultiblock.getY()==0&&posInMultiblock.getZ() > 0&&!posInMultiblock.equals(new BlockPos(2, 0, 4)))
//			return Shapes.box(0, 0, 0, 1, .5f, 1);
//		else if(new BlockPos(0, 1, 4).equals(posInMultiblock))
//			return Shapes.box(0, 0, .5f, 1, 1, 1);
//		else if(new BoundingBox(1, 1, 1, 3, 1, 2).isInside(posInMultiblock))
//		{
//			AABB aabb;
//			if(posInMultiblock.getX()==2)
//				aabb = new AABB(0, 0.5, 0, 1, 1, 1);
//			else
//				aabb = Utils.flipBox(false, posInMultiblock.getX()==3,
//						new AABB(0.125, 0.5, 0.125, 1, 1, 0.875));
//			if(posInMultiblock.getZ()==2)
//				aabb = aabb.move(0, 0, 0.875);
//			return Shapes.create(aabb);
//		}
//		else if(Set.of(
//				new BlockPos(4, 1, 1),
//				new BlockPos(0, 1, 1)
//		).contains(posInMultiblock))
//			return ShapeUtils.join(Utils.flipBoxes(
//					false, posInMultiblock.getX()==4, new AABB(.125f, .125f, 0, .375f, .375f, 1)
//			));
//		else if(posInMultiblock.getZ()==0&&posInMultiblock.getY()==1&&posInMultiblock.getX() >= 1&&posInMultiblock.getX() <= 3)
//			return Shapes.box(0, 0, .25f, 1, 1, 1);
//		else if(new BlockPos(2, 3, 0).equals(posInMultiblock))
//			return Shapes.box(0, 0, .375f, 1, 1, .625f);
//		else if(new BlockPos(2, 4, 0).equals(posInMultiblock))
//			return Shapes.box(0, 0, .3125f, 1, .9375f, 1);
//		else if(new BlockPos(2, 4, 1).equals(posInMultiblock))
//			return Shapes.box(0, .625f, 0, 1, .9375f, 1);
//		else if(new BlockPos(2, 4, 2).equals(posInMultiblock))
//			return Shapes.box(0, 0, 0, 1, .9375f, .875f);
//		else if(Set.of(
//				new BlockPos(3, 2, 4),
//				new BlockPos(1, 2, 4),
//				new BlockPos(3, 3, 0),
//				new BlockPos(1, 3, 0),
//				new BlockPos(3, 4, 0),
//				new BlockPos(1, 4, 0)
//		).contains(posInMultiblock))
//			return ShapeUtils.join(Utils.flipBoxes(
//					false, posInMultiblock.getX()==3, new AABB(.5f, 0, 0, 1, 1, 1)
//			));
//		else
		if(posInMultiblock.getY() < 2)
		{
			if(posInMultiblock.getZ() <= 3) // concrete foundation
				return Shapes.block();
			else if(posInMultiblock.getX()==2) // control box center
				return Shapes.block();
			else if(posInMultiblock.getY()==0)
			{
				if(posInMultiblock.getX()==1)
					return Shapes.or(
							Shapes.box(0, 0, 0, 1, .5, 1),
							Shapes.box(.25, .5, 0, 1, 1, 1)
					);
				else if(posInMultiblock.getX()==3)
					return Shapes.or(
							Shapes.box(0, 0, 0, 1, .5, 1),
							Shapes.box(0, .5, 0, .75, 1, 1)
					);
			}
			else if(posInMultiblock.getX()==1)
				return Shapes.box(.25, 0, 0, 1, 1, 1);
			else if(posInMultiblock.getX()==3)
				return Shapes.box(0, 0, 0, .75, 1, 1);
		}
		else if(posInMultiblock.getY() < 10)
		{
			// calculate sloping, .5 inwards over 10 blocks means .05 per step?
			final double indentBot = (posInMultiblock.getY()-2)*.05;
			final double indentTop = (posInMultiblock.getY()-1)*.05;
			double xMin = 0;
			double xMax = 1;
			double zMin = 0;
			double zMax = 1;

			if(posInMultiblock.getX()==1)
				xMin += indentBot;
			if(posInMultiblock.getX()==3)
				xMax -= indentBot;
			if(posInMultiblock.getZ()==1)
				zMin += indentBot;
			if(posInMultiblock.getZ()==3)
				zMax -= indentBot;

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
