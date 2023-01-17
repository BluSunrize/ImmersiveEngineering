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

import java.util.Set;
import java.util.function.Function;

public class MixerShapes implements Function<BlockPos, VoxelShape>
{
	public static final Function<BlockPos, VoxelShape> SHAPE_GETTER = new MixerShapes();

	private MixerShapes()
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
		else if(posInMultiblock.getX() > 0&&posInMultiblock.getY()==0&&posInMultiblock.getZ() < 2)
		{
			VoxelShape result = ShapeUtils.join(
					Utils.flipBoxes(posInMultiblock.getZ()==0, posInMultiblock.getX()==2,
							new AABB(0, 0, 0, 1, .5f, 1),
							new AABB(0.0625, .5f, 0.6875, 0.3125, 1, 0.9375)
					)
			);

			if(new BlockPos(1, 0, 1).equals(posInMultiblock))
				return Shapes.or(
						result,
						Shapes.box(0, .5f, 0.375, 1.125, .75f, 0.625),
						Shapes.box(0.875, .5f, -0.125, 1.125, .75f, 0.375),
						Shapes.box(0.875, .75f, -0.125, 1.125, 1, 0.125)
				);
			else
				return result;
		}
		else if(posInMultiblock.getX() > 0&&posInMultiblock.getY()==1&&posInMultiblock.getZ() < 2)
			return ShapeUtils.join(Utils.flipBoxes(posInMultiblock.getZ()==0, posInMultiblock.getX()==2,
					new AABB(0.1875, -.25, 0, 1, 0, 0.8125),
					new AABB(0.0625, 0, 0, 0.1875, 1, 0.9375),
					new AABB(0.1875, 0, 0.8125, 1, 1, 0.9375)
			));
		else if(new BlockPos(0, 2, 1).equals(posInMultiblock))
			return Shapes.box(0.1875, 0, 0.1875, 1, .625f, 0.6875);
		else if(new BlockPos(1, 2, 1).equals(posInMultiblock))
			return Shapes.or(
					Shapes.box(0.5625, .1875, -0.4375, 1.4375, 1, 0.4375),
					Shapes.box(0, 0, 0, 0.5625, .875, 0.5)
			);
		else if(posInMultiblock.getY()==0&&!Set.of(
				new BlockPos(0, 0, 2),
				new BlockPos(0, 0, 1),
				new BlockPos(1, 0, 2)
		).contains(posInMultiblock))
			return Shapes.box(0, 0, 0, 1, .5f, 1);
		else if(new BlockPos(2, 1, 2).equals(posInMultiblock))
			return Shapes.box(0, 0, 0.5, 1, 1, 1);
		else
			return Shapes.box(0, 0, 0, 1, 1, 1);
	}
}
