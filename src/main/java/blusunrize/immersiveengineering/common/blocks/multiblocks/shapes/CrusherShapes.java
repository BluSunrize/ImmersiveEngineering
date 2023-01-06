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
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Set;
import java.util.function.Function;

import static net.minecraft.world.phys.shapes.Shapes.box;
import static net.minecraft.world.phys.shapes.Shapes.or;

public class CrusherShapes implements Function<BlockPos, VoxelShape>
{
	public static final Function<BlockPos, VoxelShape> SHAPE_GETTER = new CrusherShapes();

	private static final Set<BlockPos> SLABS = ImmutableSet.of(
			new BlockPos(3, 0, 2),
			new BlockPos(1, 0, 2),
			new BlockPos(0, 0, 2),
			new BlockPos(3, 0, 1),
			new BlockPos(1, 0, 1),
			new BlockPos(3, 0, 0),
			new BlockPos(2, 0, 0),
			new BlockPos(1, 0, 0),
			new BlockPos(0, 0, 0),
			new BlockPos(0, 1, 1)
	);

	private CrusherShapes()
	{
	}

	@Override
	public VoxelShape apply(BlockPos posInMultiblock)
	{
		if(posInMultiblock.getZ()==1&&posInMultiblock.getX()==2)
			return getBasicShape(posInMultiblock);
		if(new BlockPos(0, 0, 2).equals(posInMultiblock))
			return or(
					box(.125, .5f, .625, .25, 1, .875),
					box(0, 0, 0, 1, .5f, 1),
					box(.75, .5f, .625, .875, 1, .875)
			);
		if(new BoundingBox(1, 1, 1, 3, 2, 1).isInside(posInMultiblock))
		{
			VoxelShape result = Shapes.empty();
			float minY = .5f;
			float minX = posInMultiblock.getX()==1?.4375f: 0;
			float maxX = posInMultiblock.getX()==3?.5625f: 1;
			if(posInMultiblock.getY()==1)
				result = or(result, box(minX, .5f, 0, maxX, .75f, 1));
			else
				minY = 0;

			if(posInMultiblock.getX()==1)
				minX = .1875f;
			else
				minX = posInMultiblock.getX()==3?.5625f: 0;
			maxX = posInMultiblock.getX()==3?.8125f: posInMultiblock.getX()==1?.4375f: 1;
			return or(result, box(minX, minY, 0, maxX, 1, 1));
		}
		if((posInMultiblock.getZ()==0||posInMultiblock.getZ()==2)&&posInMultiblock.getY() > 0&&posInMultiblock.getX() > 0&&posInMultiblock.getX() < 4)
		{
			boolean front = posInMultiblock.getZ()==0;
			boolean right = posInMultiblock.getX()==1;
			boolean left = posInMultiblock.getX()==3;
			VoxelShape result = Shapes.empty();
			float minY = .5f;
			float minX = right?.4375f: 0;
			float maxX = left?.5625f: 1;
			float minZ = front?.4375f: 0;
			float maxZ = !front?.5625f: 1;
			if(posInMultiblock.getY()==1)
				result = or(result, box(minX, .5f, minZ, maxX, .75f, maxZ));
			else
				minY = 0;

			minX = right?.1875f: (float)0;
			maxX = left?.8125f: (float)1;
			minZ = front?.1875f: .5625f;
			maxZ = !front?.8125f: .4375f;
			result = or(result, box(minX, minY, minZ, maxX, 1, maxZ));
			if(!ImmutableSet.of(
					new BlockPos(2, 1, 2),
					new BlockPos(2, 2, 2),
					new BlockPos(2, 1, 0),
					new BlockPos(2, 2, 0)
			).contains(posInMultiblock))
			{
				minX = right?.1875f: .5625f;
				maxX = left?.8125f: .4375f;
				minZ = front?.4375f: 0;
				maxZ = !front?.5625f: 1;
				result = or(result, box(minX, minY, minZ, maxX, 1, maxZ));

				if(ImmutableSet.of(
						new BlockPos(3, 1, 2),
						new BlockPos(2, 1, 2),
						new BlockPos(1, 1, 2),
						new BlockPos(3, 1, 0),
						new BlockPos(2, 1, 0),
						new BlockPos(1, 1, 0)
				).contains(posInMultiblock))
				{
					minZ = front?.25f: .5f;
					maxZ = front?.5f: .75f;
					result = or(result, box(0.25, 0, minZ, 0.75, .5f, maxZ));
				}
			}
			return result;
		}
		if(ImmutableSet.of(
				new BlockPos(3, 0, 2),
				new BlockPos(1, 0, 2),
				new BlockPos(3, 0, 0),
				new BlockPos(1, 0, 0)
		).contains(posInMultiblock))
		{
			return ShapeUtils.join(Utils.flipBoxes(
					posInMultiblock.getZ()==0,
					posInMultiblock.getX()==3,
					new AABB(0.25, 0.5, 0.5, 0.5, 1, 0.75),
					new AABB(0, 0, 0, 1, .5f, 1)
			));
		}

		return getBasicShape(posInMultiblock);
	}

	public static VoxelShape getBasicShape(BlockPos posInMultiblock)
	{
		if(SLABS.contains(posInMultiblock))
			return box(0, 0, 0, 1, .5f, 1);
		if(new BlockPos(2, 1, 1).equals(posInMultiblock))
			return box(0, 0, 0, 1, .75f, 1);
		if(new BlockPos(2, 2, 1).equals(posInMultiblock))
			return box(0, 0, 0, 0, 0, 0);

		if(posInMultiblock.getY() > 0&&posInMultiblock.getX() > 0&&posInMultiblock.getX() < 4)
		{
			float minX = 0;
			float maxX = 1;
			float minZ = 0;
			float maxZ = 1;
			if(posInMultiblock.getX()==3)
				minX = .1875f;
			else if(posInMultiblock.getX()==1)
				maxX = .8125f;
			if(posInMultiblock.getZ()==2)
				maxZ = .8125f;

			return box(minX, 0, minZ, maxX, 1, maxZ);
		}
		if(new BlockPos(0, 1, 2).equals(posInMultiblock))
			return box(0, 0, .5f, 1, 1, 1);

		return Shapes.block();
	}
}
