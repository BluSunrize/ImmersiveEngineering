/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasObjProperty;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ILightValue;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import com.google.common.collect.Lists;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;

public class TileEntityLantern extends TileEntityIEBase implements IDirectionalTile, IHasObjProperty, IBlockBounds, ILightValue
{
	public EnumFacing facing = EnumFacing.NORTH;

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		facing = EnumFacing.byIndex(nbt.getInteger("facing"));
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("facing", facing.ordinal());
	}

	@Override
	public EnumFacing getFacing()
	{
		return facing;
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 0;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return true;
	}

	@Override
	public boolean canRotate(EnumFacing axis)
	{
		return true;
	}

	@Override
	public float[] getBlockBounds()
	{
		return new float[]{facing==EnumFacing.EAST?0: .25f, facing==EnumFacing.UP?0: facing==EnumFacing.DOWN?.125f: .0625f, facing==EnumFacing.SOUTH?0: .25f, facing==EnumFacing.WEST?1: .75f, facing==EnumFacing.DOWN?1: .875f, facing==EnumFacing.NORTH?1: .75f};
	}

	static ArrayList[] displayList = {
			Lists.newArrayList("base", "attach_t"),
			Lists.newArrayList("base", "attach_b"),
			Lists.newArrayList("base", "attach_n"),
			Lists.newArrayList("base", "attach_s"),
			Lists.newArrayList("base", "attach_w"),
			Lists.newArrayList("base", "attach_e")};

	@Override
	public ArrayList<String> compileDisplayList()
	{
		if(facing==EnumFacing.UP)
			return displayList[1];
		else if(facing==EnumFacing.DOWN)
			return displayList[0];

		return displayList[3];
	}

	@Override
	public int getLightValue()
	{
		return 14;
	}
}