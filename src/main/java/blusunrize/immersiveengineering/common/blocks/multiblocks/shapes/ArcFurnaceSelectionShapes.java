/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Set;
import java.util.function.Function;

import static net.minecraft.world.phys.shapes.Shapes.box;
import static net.minecraft.world.phys.shapes.Shapes.or;

public class ArcFurnaceSelectionShapes implements Function<BlockPos, VoxelShape>
{
	public static final Function<BlockPos, VoxelShape> SHAPE_GETTER = new ArcFurnaceSelectionShapes();

	private ArcFurnaceSelectionShapes()
	{
	}

	@Override
	public VoxelShape apply(BlockPos posInMultiblock)
	{
		if(new BlockPos(0, 0, 4).equals(posInMultiblock))
			return or(
					box(0, 0, 0, 1, .5f, 1),
					box(0.125, .5f, 0.625, 0.25, 1, 0.875),
					box(0.75, .5f, 0.625, 0.875, 1, 0.875)
			);
		else if(posInMultiblock.getZ()==0&&posInMultiblock.getY()==1&&posInMultiblock.getX() >= 1&&posInMultiblock.getX() <= 3)
			return or(
					box(0, 0, 0.25, 1, 1, 1),
					box(0.25, .25f, 0, 0.75, .75, 0.25));
		else if(posInMultiblock.getX()%4==0&&posInMultiblock.getZ() <= 2)
		{
			VoxelShape fullShape;
			if(posInMultiblock.getY()==0)
				fullShape = box(0, 0, 0, 1, .5f, 1);
			else
				fullShape = Shapes.empty();
			final boolean flip = posInMultiblock.getX()==4;
			double minX = !flip?.5f: 0;
			double maxX = flip?.5f: 1;
			if(posInMultiblock.getX()!=3)
				fullShape = or(fullShape, box(minX, posInMultiblock.getY()==0?.5: 0, 0, maxX, 1, 1));
			if(posInMultiblock.getY()==0)
			{
				int move = (4-posInMultiblock.getZ())-2;
				minX = !flip?.125f: .625f;
				maxX = !flip?.375f: .875f;
				fullShape = or(fullShape, box(minX, .6875, -1.625f+move, maxX, .9375, 0.625+move));

				minX = !flip?.375f: .5f;
				maxX = !flip?.5f: .625f;
				fullShape = or(fullShape, box(minX, .6875, 0.375+move, maxX, .9375, 0.625+move));
				fullShape = or(fullShape, box(minX, .6875, -1.625f+move, maxX, .9375, -1.375f+move));
			}
			else if(posInMultiblock.getY()==1)
			{
				int move = (4-posInMultiblock.getZ())-2;
				minX = !flip?.125f: .625f;
				maxX = !flip?.375f: .875f;
				fullShape = or(fullShape, box(minX, .125, -1.625f+move, maxX, .375, .625f+move));

				minX = !flip?.375f: .5f;
				maxX = !flip?.5f: .625f;
				AABB aabb = new AABB(minX, .125, 0.375, maxX, .375, 0.625);
				aabb = aabb.move(0, 0, move);
				if(posInMultiblock.getX()==0)
					aabb = aabb.move(0, .6875, 0);
				fullShape = or(fullShape, Shapes.create(aabb));
				if(posInMultiblock.getX()==0)
				{
					minX = !flip?.125f: .625f;
					maxX = !flip?.375f: .875f;
					fullShape = or(fullShape, box(minX, .375, 0.375+move, maxX, 1.0625, 0.625+move));
				}
				minX = !flip?.375f: .5f;
				maxX = !flip?.5f: .625f;
				fullShape = or(fullShape, box(minX, .125, -1.625f+move, maxX, .375, -1.375f+move));
			}
			else if(Set.of(
					new BlockPos(4, 2, 2),
					new BlockPos(0, 2, 2)
			).contains(posInMultiblock))
			{
				minX = !flip?.375f: .5f;
				maxX = !flip?.5f: .625f;
				fullShape = or(fullShape, box(minX, .25, 0.25, maxX, .75, 0.75));
			}
			return fullShape;
		}
		else
			return ArcFurnaceShapes.SHAPE_GETTER.apply(posInMultiblock);
	}
}
