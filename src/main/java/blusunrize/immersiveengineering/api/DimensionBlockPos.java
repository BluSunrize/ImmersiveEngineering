/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import javax.annotation.Nonnull;

public class DimensionBlockPos extends BlockPos
{
	public DimensionType dimension;

	public DimensionBlockPos(int x, int y, int z, DimensionType dim)
	{
		super(x, y, z);
		dimension = dim;
	}

	public DimensionBlockPos(int x, int y, int z, World w)
	{
		this(x, y, z, w.getDimension().getType());
	}

	public DimensionBlockPos(BlockPos pos, World w)
	{
		this(pos.getX(), pos.getY(), pos.getZ(), w.getDimension().getType());
	}

	public DimensionBlockPos(BlockPos pos, DimensionType dim)
	{
		this(pos.getX(), pos.getY(), pos.getZ(), dim);
	}

	public DimensionBlockPos(TileEntity te)
	{
		this(te.getPos(), te.getWorld());
	}

	public DimensionBlockPos(CompoundNBT nbt)
	{
		this(NBTUtil.readBlockPos(nbt), DimensionType.byName(new ResourceLocation(nbt.getString("dimension"))));
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime*result+dimension.getId();
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

	public CompoundNBT toNBT()
	{
		CompoundNBT ret = new CompoundNBT();
		NBTUtil.writeBlockPos(this);
		ret.putString("dimension", dimension.getRegistryName().toString());
		return ret;
	}
}
