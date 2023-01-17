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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class RefineryShapes implements Function<BlockPos, VoxelShape>
{
	public static final Function<BlockPos, VoxelShape> SHAPE_GETTER = new RefineryShapes();

	private RefineryShapes()
	{
	}

	@Override
	public VoxelShape apply(BlockPos posInMultiblock)
	{
		if(posInMultiblock.getZ()%2==0&&posInMultiblock.getY()==0&&posInMultiblock.getX()%4==0)
		{
			List<AABB> list = Utils.flipBoxes(posInMultiblock.getZ()==0, posInMultiblock.getX()==0,
					new AABB(0, 0, 0, 1, .5f, 1),
					new AABB(0.25, .5f, 0, 0.5, 1.375f, 0.25)
			);
			if(new BlockPos(4, 0, 2).equals(posInMultiblock))
			{
				list.add(new AABB(0.125, .5f, 0.625, 0.25, 1, 0.875));
				list.add(new AABB(0.75, .5f, 0.625, 0.875, 1, 0.875));
			}
			return ShapeUtils.join(list);
		}
		if(posInMultiblock.getZ()%2==0&&posInMultiblock.getY()==0&&posInMultiblock.getX()%2==1)
			return ShapeUtils.join(Utils.flipBoxes(posInMultiblock.getZ()==0, posInMultiblock.getX()==1,
					new AABB(0, 0, 0, 1, .5f, 1),
					new AABB(0, .5f, 0, 0.25, 1.375f, 0.25)
			));

		if(posInMultiblock.getZ() < 2&&posInMultiblock.getY() > 0&&posInMultiblock.getX()%4==0)
		{
			float minZ = -.25f;
			float maxZ = 1.25f;
			float minY = posInMultiblock.getY()==1?.5f: -.5f;
			float maxY = posInMultiblock.getY()==1?2f: 1f;
			if(posInMultiblock.getZ()==0)
			{
				minZ += 1;
				maxZ += 1;
			}
			return Shapes.create(Utils.flipBox(false, posInMultiblock.getX()==4,
					new AABB(0.5, minY, minZ, 2, maxY, maxZ)
			));
		}
		if(posInMultiblock.getZ() < 2&&posInMultiblock.getY() > 0&&posInMultiblock.getX()%2==1)
		{
			float minZ = -.25f;
			float maxZ = 1.25f;
			float minY = posInMultiblock.getY()==1?.5f: -.5f;
			float maxY = posInMultiblock.getY()==1?2f: 1f;
			if(posInMultiblock.getZ()==0)
			{
				minZ += 1;
				maxZ += 1;
			}
			return Shapes.create(Utils.flipBox(false, posInMultiblock.getX()==3,
					new AABB(-0.5, minY, minZ, 1, maxY, maxZ)
			));
		}
		else if(Set.of(
				new BlockPos(0, 0, 2),
				new BlockPos(1, 0, 2),
				new BlockPos(3, 0, 2)
		).contains(posInMultiblock))
			return Shapes.box(0, 0, 0, 1, .5f, 1);
		else if(new BlockPos(4, 1, 2).equals(posInMultiblock))
			return Shapes.box(0, 0, 0.5, 1, 1, 1);
		else if(new BlockPos(2, 1, 2).equals(posInMultiblock))
			return Shapes.box(.0625f, 0, .0625f, .9375f, 1, .9375f);
		else
			return Shapes.block();
	}
}
