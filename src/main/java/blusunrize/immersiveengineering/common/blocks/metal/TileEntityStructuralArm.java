package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class TileEntityStructuralArm extends TileEntityIEBase implements IDirectionalTile
{
	public EnumFacing facing = EnumFacing.NORTH;
	public boolean inverted = false;
	
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		facing = EnumFacing.getFront(nbt.getInteger("facing"));
		inverted = nbt.getBoolean("inverted");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("facing", facing.ordinal());
		nbt.setBoolean("inverted", inverted);
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
		return true;
	}
	@Override
	public boolean canRotate(EnumFacing axis)
	{
		return true;
	}
}
