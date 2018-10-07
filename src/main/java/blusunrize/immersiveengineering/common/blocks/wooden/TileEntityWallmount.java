/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAttachedIntegerProperies;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHammerInteraction;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class TileEntityWallmount extends TileEntityIEBase implements IBlockBounds, IAdvancedDirectionalTile, IHammerInteraction, IAttachedIntegerProperies
{
	public EnumFacing facing = EnumFacing.NORTH;
	public int orientation = 0;

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		facing = EnumFacing.byIndex(nbt.getInteger("facing"));
		orientation = nbt.getInteger("orientation");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("facing", facing.ordinal());
		nbt.setInteger("orientation", orientation);
	}

	@Override
	public String[] getIntPropertyNames()
	{
		return new String[]{"orientation"};
	}

	@Override
	public PropertyInteger getIntProperty(String name)
	{
		return IEProperties.INT_4;
	}

	@Override
	public int getIntPropertyValue(String name)
	{
		return orientation;
	}

	@Override
	public void setValue(String name, int value)
	{
		orientation = value;
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
		return 2;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return !entity.isSneaking();
	}

	@Override
	public boolean canRotate(EnumFacing axis)
	{
		return true;
	}

	@Override
	public boolean hammerUseSide(EnumFacing side, EntityPlayer player, float hitX, float hitY, float hitZ)
	{
		if(player.isSneaking())
		{
			if(orientation==0)
				orientation = 1;
			else if(orientation==1)
				orientation = 0;
			else if(orientation==2)
				orientation = 3;
			else if(orientation==3)
				orientation = 2;
			return true;
		}
		return false;
	}

	@Override
	public void onDirectionalPlacement(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase placer)
	{
		if(side==EnumFacing.UP)
			orientation = 3;
		else if(side==EnumFacing.DOWN)
			orientation = 2;
		else if(hitY < .5)
			orientation = 1;
	}

	@Override
	public float[] getBlockBounds()
	{
		EnumFacing towards = orientation > 1?facing.getOpposite(): facing;
		float minX = towards==EnumFacing.WEST?0: .3125f;
		float minY = orientation==0?.375f: orientation==2?.3125f: 0;
		float minZ = towards==EnumFacing.NORTH?0: .3125f;
		float maxX = towards==EnumFacing.EAST?1: .6875f;
		float maxY = orientation==1?.625f: orientation==3?.6875f: 1;
		float maxZ = towards==EnumFacing.SOUTH?1: .6875f;
		return new float[]{minX, minY, minZ, maxX, maxY, maxZ};
	}
}