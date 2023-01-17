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

public class AdvBlastFurnaceShapes implements Function<BlockPos, VoxelShape>
{
	public static final Function<BlockPos, VoxelShape> SHAPE_GETTER = new AdvBlastFurnaceShapes();

	private AdvBlastFurnaceShapes()
	{
	}

	@Override
	public VoxelShape apply(BlockPos posInMultiblock)
	{
		if((posInMultiblock.getX()==1&&posInMultiblock.getZ()==1)||Set.of(
				new BlockPos(1, 0, 0),
				new BlockPos(1, 1, 0),
				new BlockPos(1, 3, 1)
		).contains(posInMultiblock))
			return Shapes.block();
		if(new BlockPos(1, 0, 2).equals(posInMultiblock))
			return Shapes.box(.1875f, 0, 0, .8125f, .8125f, 1);

		float xMin = 0;
		float yMin = 0;
		float zMin = 0;
		float xMax = 1;
		float yMax = 1;
		float zMax = 1;
		float indent = 1;
		if(posInMultiblock.getY()==0)
			indent = posInMultiblock.getZ()==1?.5f: 0.6875f;
		else if(posInMultiblock.getY()==1)
			indent = .5f;
		else if(posInMultiblock.getY()==2)
			indent = 0.625f;

		if(posInMultiblock.getX()==2)
			xMax = 1-indent;
		if(posInMultiblock.getX()==0)
			xMin = indent;
		if(posInMultiblock.getZ()==2)
			zMax = 1-indent;
		if(posInMultiblock.getZ()==0)
			zMin = indent;

		return Shapes.box(xMin, yMin, zMin, xMax, yMax, zMax);
	}
}
