/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.shapes;

import blusunrize.immersiveengineering.api.utils.shapes.ShapeUtils;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.DieselGeneratorLogic;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.function.Function;

public class DieselGeneratorShapes implements Function<BlockPos, VoxelShape>
{
	public static final Function<BlockPos, VoxelShape> SHAPE_GETTER = new DieselGeneratorShapes();
	public static final Function<BlockPos, VoxelShape> GETTER_WITH_REDSTONE_SUPPORT = DieselGeneratorShapes::redstoneSupported;

	private DieselGeneratorShapes()
	{
	}

	@Override
	public VoxelShape apply(BlockPos posInMultiblock)
	{
		if(new BlockPos(1, 1, 4).equals(posInMultiblock))
			return Shapes.or(
					Shapes.box(0, .5f, 0, 1, 1, 1),
					Shapes.box(0, -.5f, -.625f, 1, .5f, 1)
			);
		else if(posInMultiblock.getY()==1&&posInMultiblock.getZ()==4)
			return ShapeUtils.join(Utils.flipBoxes(false, posInMultiblock.getX()==2,
					new AABB(0, .5f, 0, 1, 1, 1),
					new AABB(.125f, 0, .125f, .375f, .5f, .375f),
					new AABB(.125f, 0, .625f, .375f, .5f, .875f)
			));
		if(new BlockPos(2, 1, 2).equals(posInMultiblock))
			return Shapes.or(
					getBlockBounds(posInMultiblock),
					Shapes.box(.5f, .25f, .3125f, 1, .75f, .6875f),
					Shapes.box(.6875f, -.5f, .4375f, .8125f, .25f, .5625f)
			);

		if(posInMultiblock.getX()%2==0&&posInMultiblock.getY()==0&&posInMultiblock.getZ() < 4)
		{
			List<AABB> list = Lists.newArrayList(getBlockBounds(posInMultiblock).bounds());
			if(posInMultiblock.getZ() > 2)
			{
				list.add(new AABB(0.125, .5625f, 0.25, 1, .8125f, 0.5));
				list.add(new AABB(0.125, .5625f, 0.5, 0.375, .8125f, 1));
			}
			else if(posInMultiblock.getZ() > 0)
			{
				final double offset = posInMultiblock.getZ() > 1?0: 1;
				list.add(new AABB(0.4375, .5f, -0.5625+offset, 1, 1, 0.75+offset));
			}
			if(posInMultiblock.getZ() < 2)
			{
				final double offset = posInMultiblock.getZ()==1?0: 1;
				list.add(new AABB(0.375, .5625f, 0.5625+offset, 0.4375, .8125f, 0.8125+offset));
				list.add(new AABB(0.375, .5625f, -0.875+offset, 0.5, .8125f, -0.625+offset));
				list.add(new AABB(0.125, .5625f, -0.875+offset, 0.375, .8125f, 0.8125+offset));
			}
			return ShapeUtils.join(Utils.flipBoxes(false, posInMultiblock.getX()==2, list));
		}
		return getBlockBounds(posInMultiblock);
	}

	private static VoxelShape getBlockBounds(BlockPos posInMultiblock)
	{
		if(new BlockPos(1, 0, 4).equals(posInMultiblock))
			return Shapes.box(0, .5f, -.625f, 1, 1.5f, 1);
		if(ImmutableSet.of(
				new BlockPos(0, 0, 4),
				new BlockPos(2, 1, 0),
				new BlockPos(2, 2, 0)
		).contains(posInMultiblock))
			return Shapes.box(0, 0, 0, .5f, posInMultiblock.getY()==2?.8125f: 1, posInMultiblock.getZ()==0?1.125f: 1);
		if(ImmutableSet.of(
				new BlockPos(2, 0, 4),
				new BlockPos(0, 1, 0),
				new BlockPos(0, 2, 0)
		).contains(posInMultiblock))
			return Shapes.box(.5f, 0, 0, 1, posInMultiblock.getY()==2?.8125f: 1, posInMultiblock.getZ()==0?1.125f: 1);
		if(new BlockPos(1, 2, 0).equals(posInMultiblock))
			return Shapes.box(0, 0, 0, 1, posInMultiblock.getY()==2?.8125f: 1, posInMultiblock.getZ()==0?.625f: 1);

		if(posInMultiblock.getY()==1&&posInMultiblock.getZ()==4)
			return Shapes.box(0, .5f, 0, 1, 1, 1);

		if(posInMultiblock.getX()==1&&posInMultiblock.getY() > 0&&posInMultiblock.getZ()==3)
			return Shapes.box(.0625f, 0, 0, .9375f, posInMultiblock.getY()==2?.3125f: 1, .625f);
		if(new BoundingBox(1, 2, 1, 1, 2, 2).isInside(posInMultiblock))
			return Shapes.box(.0625f, 0, 0, .9375f, .3125f, 1);

		if(posInMultiblock.getX()%2==0&&posInMultiblock.getY()==0)
			return Shapes.box(0, 0, 0, 1, .5f, 1);

		//TODO more sensible name
		boolean lessThan21 = posInMultiblock.getY()==0||(posInMultiblock.getY()==1&&posInMultiblock.getZ() > 2);
		if(posInMultiblock.getX()==0&&posInMultiblock.getY() < 2)
			return Shapes.box(.9375f, -.5f, 0, 1, .625f, lessThan21?.625f: 1);
		if(posInMultiblock.getX()==2&&posInMultiblock.getY() < 2)
			return Shapes.box(0, -.5f, 0, .0625f, .625f, lessThan21?.625f: 1);

		if(posInMultiblock.getX()%2==0&&posInMultiblock.getY()==2&&posInMultiblock.getZ()==2)
			return Shapes.create(Utils.flipBox(false, posInMultiblock.getX()==2,
					new AABB(
							0.5625, 0, -0.0625,
							1.0625, posInMultiblock.getX()==2?1.125f: .75f, 0.4375
					)));

		return Shapes.block();
	}

	private static VoxelShape redstoneSupported(BlockPos pos)
	{
		final VoxelShape baseShape = SHAPE_GETTER.apply(pos);
		if(pos.equals(DieselGeneratorLogic.REDSTONE_POS))
			return Shapes.or(baseShape, Shapes.box(0.9375, 0, 0, 1, 1, 1));
		else
			return baseShape;
	}
}
