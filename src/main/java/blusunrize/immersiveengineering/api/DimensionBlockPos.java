/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class DimensionBlockPos extends BlockPos
{
	public int dimension;

	public DimensionBlockPos(int x, int y, int z, int dim)
	{
		super(x, y, z);
		dimension = dim;
	}

	public DimensionBlockPos(int x, int y, int z, World w)
	{
		this(x, y, z, w.provider.getDimension());
	}

	public DimensionBlockPos(BlockPos pos, World w)
	{
		this(pos.getX(), pos.getY(), pos.getZ(), w.provider.getDimension());
	}

	public DimensionBlockPos(BlockPos pos, int dim)
	{
		this(pos.getX(), pos.getY(), pos.getZ(), dim);
	}

	public DimensionBlockPos(TileEntity te)
	{
		this(te.getPos(), te.getWorld());
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime*result+dimension;
		result = prime*result+getX();
		result = prime*result+getY();
		result = prime*result+getZ();
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this==obj)
			return true;
		if(obj==null)
			return false;
		if(getClass()!=obj.getClass())
			return false;
		DimensionBlockPos other = (DimensionBlockPos)obj;
		if(dimension!=other.dimension)
			return false;
		if(getX()!=other.getX())
			return false;
		if(getY()!=other.getY())
			return false;
		return getZ()==other.getZ();
	}

	@Nonnull
	@Override
	public String toString()
	{
		return "Dimension: "+dimension+" Pos: "+super.toString();
	}
}
