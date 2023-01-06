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

import java.util.Set;
import java.util.function.Function;

public class SawmillShapes implements Function<BlockPos, VoxelShape>
{
	public static final Function<BlockPos, VoxelShape> SHAPE_GETTER = new SawmillShapes();

	private SawmillShapes()
	{
	}

	@Override
	public VoxelShape apply(BlockPos posInMultiblock)
	{
		// Slabs
		Set<BlockPos> slabs = Set.of(
				new BlockPos(0, 0, 0),
				new BlockPos(4, 0, 0),
				new BlockPos(4, 0, 2)
		);
		if(slabs.contains(posInMultiblock))
			return Shapes.box(0, 0, 0, 1, .5f, 1);
		// Redstone panel feet
		if(new BlockPos(0, 0, 2).equals(posInMultiblock))
			return Shapes.or(
					Shapes.box(0, 0, 0, 1, .5f, 1),
					Shapes.box(.125, .5f, .625, .25, 1, .875),
					Shapes.box(.75, .5f, .625, .875, 1, .875)
			);
		// Restone panel
		if(new BlockPos(0, 1, 2).equals(posInMultiblock))
			return Shapes.box(0, 0, .5f, 1, 1, 1);
		// Stripper
		if(new BlockPos(1, 1, 1).equals(posInMultiblock))
			return Shapes.box(.25, 0, 0, .875, 1, 1);
		// Vacuum
		if(new BlockPos(1, 1, 2).equals(posInMultiblock))
			return Shapes.or(
					Shapes.box(.25, 0, 0, .875, 1, .125),
					Shapes.box(.25, 0, .125, .875, .875, .75),
					Shapes.box(.1875, 0, 0, .9375, .125, .8125)
			);
		if(new BlockPos(1, 0, 2).equals(posInMultiblock))
			return Shapes.or(
					Shapes.box(0, 0, 0, 1, .5, 1),
					Shapes.box(.1875, .5, 0, .9375, 1, .8125),
					Shapes.box(.9375, .5, .25, 1, .875, .625)
			);
		if(new BlockPos(2, 0, 2).equals(posInMultiblock))
			return Shapes.or(
					Shapes.box(0, 0, 0, 1, .5, 1),
					Shapes.box(0, .5, .25, 1, .875, .625)
			);
		// Conveyors
		if(posInMultiblock.getY()==1&&posInMultiblock.getZ()==1)
			return Shapes.box(0, 0, 0, 1, .125, 1);
		// Rest
		return Shapes.block();
	}
}
