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

public class LightningRodShapes implements Function<BlockPos, VoxelShape>
{
	public static final Function<BlockPos, VoxelShape> SHAPE_GETTER = new LightningRodShapes();

	private LightningRodShapes()
	{
	}

	@Override
	public VoxelShape apply(BlockPos posInMultiblock)
	{
		if(new BlockPos(1, 2, 1).equals(posInMultiblock))
			return Shapes.box(-.125f, 0, -.125f, 1.125f, 1, 1.125f);
		if((posInMultiblock.getX()==1&&posInMultiblock.getZ()==1)
				||(posInMultiblock.getY() < 2&&(posInMultiblock.getX()+posInMultiblock.getZ())%2==1))
			return Shapes.block();
		if(posInMultiblock.getY()==0)
			return Shapes.box(0, 0, 0, 1, .5f, 1);
		float xMin = 0;
		float xMax = 1;
		float yMin = 0;
		float yMax = 1;
		float zMin = 0;
		float zMax = 1;
		if(posInMultiblock.getX()%2==0&&posInMultiblock.getZ()%2==0)
		{
			if(posInMultiblock.getY() < 2)
			{
				yMin = -.5f;
				yMax = 1.25f;
				xMin = posInMultiblock.getX()!=0?.1875f: .5625f;
				xMax = posInMultiblock.getX()!=2?.8125f: .4375f;
				zMin = posInMultiblock.getZ() >= 2?.1875f: .5625f;
				zMax = posInMultiblock.getZ()!=2?.8125f: .4375f;
			}
			else
			{
				yMin = .25f;
				yMax = .75f;
				xMin = posInMultiblock.getX()!=0?0: .375f;
				xMax = posInMultiblock.getX()!=2?1: .625f;
				zMin = posInMultiblock.getZ() >= 2?0: .375f;
				zMax = posInMultiblock.getZ()!=2?1: .625f;
			}
		}
		else if(posInMultiblock.getY() >= 2)
		{
			yMin = .25f;
			yMax = .75f;
			xMin = posInMultiblock.getX()==0?.375f: 0;
			xMax = posInMultiblock.getX()==2?.625f: 1;
			zMin = posInMultiblock.getZ()==0?.375f: 0;
			zMax = posInMultiblock.getZ()==2?.625f: 1;
		}
		return Shapes.box(xMin, yMin, zMin, xMax, yMax, zMax);
	}
}
