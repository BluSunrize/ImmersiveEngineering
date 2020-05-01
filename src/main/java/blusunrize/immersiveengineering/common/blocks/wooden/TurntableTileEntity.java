/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHammerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.INeighbourChangeTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.util.RotationUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.Rotation;

public class TurntableTileEntity extends IEBaseTileEntity implements IStateBasedDirectional, INeighbourChangeTile, IHammerInteraction
{
	public static TileEntityType<TurntableTileEntity> TYPE;
	private boolean redstone = false;
	//rotationMapping is rotating clockwise around the face of the turntable, starting at North for top/bottom facing turntables and Top for sideways facing turntables
	private Rotation[] rotationMapping = new Rotation[]{Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_90};

	@Override
	public Direction getFacing()
	{
		return null;
	}

	public boolean invert = false;

	public TurntableTileEntity()
	{
		super(TYPE);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		redstone = nbt.getBoolean("redstone");
		invert = nbt.getBoolean("invert");
		byte rotationMapValue = nbt.getByte("rotationMapping");
		for(int i = 0; i < rotationMapping.length; i++)
			rotationMapping[i] = intToRotation((rotationMapValue >> 2*i) & 3);
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.putBoolean("redstone", redstone);
		nbt.putBoolean("invert", invert);
		byte rotationMapValue = 0;
		for(int i = 0; i <  rotationMapping.length; i++)
			rotationMapValue += (byte)(rotationToInt(rotationMapping[i]) << 2*i);
		nbt.putByte("rotationMapping", rotationMapValue);
	}

	@Override
	public void onNeighborBlockChange(BlockPos otherPos)
	{
		boolean r = this.world.isBlockPowered(pos);
		if(r!=this.redstone)
		{
			this.redstone = r;
			if(this.redstone)
			{
				System.out.println("test");
				BlockPos target = pos.offset(getFacing());
				RotationUtil.rotateBlock(this.world, target, invert);
			}
		}
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.PISTON_LIKE;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return placer.isSneaking();
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3d hit, LivingEntity entity)
	{
		return !entity.isSneaking();
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return true;
	}

	@Override
	public boolean hammerUseSide(Direction side, PlayerEntity player, Vec3d hitVec)
	{
		if(player.isSneaking())
		{
			if(!world.isRemote)
			{
				invert = !invert;
				markDirty();
				world.addBlockEvent(getPos(), this.getBlockState().getBlock(), 254, 0);
			}
			//Rotation.CLOCKWISE_90;
			return true;
		}
		return false;
	}

	@Override
	public EnumProperty<Direction> getFacingProperty()
	{
		return IEProperties.FACING_ALL;
	}

	private Rotation intToRotation(int rotationValue) {
		switch(rotationValue) {
			case 2: return Rotation.CLOCKWISE_180;
			case 3: return Rotation.COUNTERCLOCKWISE_90;
			case 0: //illegal value, replace by default one
			case 1:
			default: return Rotation.CLOCKWISE_90;
		}
	}
	private int rotationToInt(Rotation rotation) {
		switch(rotation) {
			case CLOCKWISE_180: return 2;
			case COUNTERCLOCKWISE_90: return 3;
			case NONE: //illegal value, replace by default one
			case CLOCKWISE_90:
			default: return 1;
		}
	}
}