/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasObjProperty;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ILightValue;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;

import java.util.List;
import java.util.Map;

//TODO replace with blockstates?
public class LanternTileEntity extends IEBaseTileEntity implements IStateBasedDirectional, IHasObjProperty, IBlockBounds, ILightValue
{
	public static TileEntityType<LanternTileEntity> TYPE;

	public LanternTileEntity()
	{
		super(TYPE);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
	}

	@Override
	public EnumProperty<Direction> getFacingProperty()
	{
		return IEProperties.FACING_ALL;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.SIDE_CLICKED;
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
		return new float[]{getFacing()==Direction.EAST?0: .25f, getFacing()==Direction.UP?0: getFacing()==Direction.DOWN?.125f: .0625f, getFacing()==Direction.SOUTH?0: .25f, getFacing()==Direction.WEST?1: .75f, getFacing()==Direction.DOWN?1: .875f, getFacing()==Direction.NORTH?1: .75f};
	}

	static Map<Direction, List<String>> displayList = ImmutableMap.<Direction, List<String>>builder()
			.put(Direction.DOWN, Lists.newArrayList("base", "attach_t"))
			.put(Direction.UP, Lists.newArrayList("base", "attach_b"))
			.put(Direction.NORTH, Lists.newArrayList("base", "attach_n"))
			.put(Direction.SOUTH, Lists.newArrayList("base", "attach_s"))
			.put(Direction.WEST, Lists.newArrayList("base", "attach_w"))
			.put(Direction.EAST, Lists.newArrayList("base", "attach_e"))
			.build();

	@Override
	public List<String> compileDisplayList()
	{
		return displayList.get(getFacing());
	}

	@Override
	public int getLightValue()
	{
		return 14;
	}
}