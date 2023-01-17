/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.shapes;

import blusunrize.immersiveengineering.api.utils.shapes.ShapeUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class SqueezerShapes implements Function<BlockPos, VoxelShape>
{
	public static final Function<BlockPos, VoxelShape> SHAPE_GETTER = new SqueezerShapes();

	private SqueezerShapes()
	{
	}

	@Override
	public VoxelShape apply(BlockPos posInMultiblock)
	{
		if(new BlockPos(2, 0, 2).equals(posInMultiblock))
			return Shapes.or(
					Shapes.box(0, 0, 0, 1, .5f, 1),
					Shapes.box(0.125, .5f, 0.625, 0.25, 1, 0.875),
					Shapes.box(0.75, .5f, 0.625, 0.875, 1, 0.875)
			);
		if(new BoundingBox(0, 0, 0, 1, 0, 1).isInside(posInMultiblock))
		{
			List<AABB> list = new ArrayList<>();
			list.add(new AABB(0, 0, 0, 1, .5f, 1));
			list.add(new AABB(0.0625, .5f, 0.6875, 0.3125, 1, 0.9375));

			if(new BlockPos(1, 0, 1).equals(posInMultiblock))
			{
				list.add(new AABB(0, .5f, 0.375, 1.125, .75f, 0.625));
				list.add(new AABB(0.875, .5f, -0.125, 1.125, .75f, 0.375));
				list.add(new AABB(0.875, .75f, -0.125, 1.125, 1, 0.125));
			}

			return ShapeUtils.join(Utils.flipBoxes(
					posInMultiblock.getZ()==0, posInMultiblock.getX()==1, list
			));
		}
		if(new BoundingBox(0, 1, 0, 1, 2, 1).isInside(posInMultiblock))
		{
			List<AABB> list = new ArrayList<>(2);
			if(posInMultiblock.getY()==1)
				list.add(new AABB(0, 0, 0, 1, .125f, 1));
			float minY = posInMultiblock.getY()==1?.125f: -.875f;
			float maxY = posInMultiblock.getY()==1?1.125f: .125f;

			list.add(new AABB(0, minY, 0.84375, 0.15625, maxY, 1));
			list.add(new AABB(0.0625, minY, 0, 0.1875, maxY, 0.84375));
			list.add(new AABB(0.15625, minY, 0.8125, 1, maxY, 0.9375));

			if(posInMultiblock.getY()==2)
				list.add(new AABB(0.75, .375f, -0.25, 1.25, .9375f, 0.25));
			return ShapeUtils.join(Utils.flipBoxes(
					posInMultiblock.getZ()==0, posInMultiblock.getX()==1, list
			));
		}
		else if(posInMultiblock.getY()==0&&!Set.of(
				new BlockPos(0, 0, 0),
				new BlockPos(2, 0, 1)
		).contains(posInMultiblock))
			return Shapes.box(0, 0, 0, 1, .5f, 1);
		else if(new BlockPos(2, 1, 2).equals(posInMultiblock))
			return Shapes.box(0, 0, 0.5, 1, 1, 1);
		else
			return Shapes.block();
	}
}
