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
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;

import java.util.ArrayList;

//TODO replace with blockstates?
public class TileEntityLantern extends TileEntityIEBase implements IDirectionalTile, IHasObjProperty, IBlockBounds, ILightValue
{
	public static TileEntityType<TileEntityLantern> TYPE;

	public Direction facing = Direction.NORTH;

	public TileEntityLantern()
	{
		super(TYPE);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		facing = Direction.byIndex(nbt.getInt("facing"));
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.putInt("facing", facing.ordinal());
	}

	@Override
	public Direction getFacing()
	{
		return facing;
	}

	@Override
	public void setFacing(Direction facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 0;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(Direction side, float hitX, float hitY, float hitZ, LivingEntity entity)
	{
		return true;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return true;
	}

	@Override
	public float[] getBlockBounds()
	{
		return new float[]{facing==Direction.EAST?0: .25f, facing==Direction.UP?0: facing==Direction.DOWN?.125f: .0625f, facing==Direction.SOUTH?0: .25f, facing==Direction.WEST?1: .75f, facing==Direction.DOWN?1: .875f, facing==Direction.NORTH?1: .75f};
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
		if(facing==Direction.UP)
			return displayList[1];
		else if(facing==Direction.DOWN)
			return displayList[0];

		return displayList[3];
	}

	@Override
	public int getLightValue()
	{
		return 14;
	}
}