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

public class BottlingMachineShapes implements Function<BlockPos, VoxelShape>
{
	public static final Function<BlockPos, VoxelShape> SHAPE_GETTER = new BottlingMachineShapes();

	private BottlingMachineShapes()
	{
	}

	@Override
	public VoxelShape apply(BlockPos posInMultiblock)
	{
		if(new BlockPos(1, 0, 0).equals(posInMultiblock))
			return Shapes.box(0, 0, 0, 1, .5f, 1);
		else if(posInMultiblock.getY()==0||new BlockPos(2, 1, 0).equals(posInMultiblock))
			return Shapes.box(0, 0, 0, 1, 1, 1);
		else if(posInMultiblock.getZ()==1&&posInMultiblock.getY()==1)
			return Shapes.box(0, 0, 0, 1, .125f, 1);
		else if(new BlockPos(1, 1, 0).equals(posInMultiblock))
			return Shapes.box(.0625f, 0, .0625f, .9375f, 1, .9375f);
		else if(new BlockPos(1, 1, 0).equals(posInMultiblock))
			return Shapes.box(-.0625f, .0625f, .125f, .75f, .6875f, .75);
		else if(new BlockPos(1, 2, 1).equals(posInMultiblock))
			return Shapes.box(.21875f, -.4375f, 0, .78125f, .5625f, .78125f);
		else if(new BlockPos(1, 2, 0).equals(posInMultiblock))
			return Shapes.box(.125f, -1, .8125f, .875f, .25f, 1);
		else
			return Shapes.block();
	}
}
