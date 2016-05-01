package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

public abstract class TileEntityMultiblockPart<T extends TileEntityMultiblockPart<T>> extends TileEntityIEBase implements ITickable, IDirectionalTile, IBlockBounds
{
	public boolean formed = false;
	public int pos=-1;
	public int[] offset = {0,0,0};
	public boolean mirrored = false;
	public EnumFacing facing = EnumFacing.NORTH;

	@Override
	public EnumFacing getFacing()
	{
		return this.facing;
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
		return false;
	}
	
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		formed = nbt.getBoolean("formed");
		pos = nbt.getInteger("pos");
		offset = nbt.getIntArray("offset");
		mirrored = nbt.getBoolean("mirrored");
		facing = EnumFacing.getFront(nbt.getInteger("facing"));
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setBoolean("formed", formed);
		nbt.setInteger("pos", pos);
		nbt.setIntArray("offset", offset);
		nbt.setBoolean("mirrored", mirrored);
		nbt.setInteger("facing", facing.ordinal());
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
	}
	
	public static boolean _Immovable()
	{
		return true;
	}
	public T master()
	{
		if(offset[0]==0&&offset[1]==0&&offset[2]==0)
			return (T)this;
		TileEntity te = worldObj.getTileEntity(getPos().add(-offset[0],-offset[1],-offset[2]));
		return this.getClass().isInstance(te)?(T)te: null;
	}
	public boolean isDummy()
	{
		return offset[0]!=0 || offset[1]!=0 || offset[2]!=0;
	}
	public abstract ItemStack getOriginalBlock();
	public abstract void disassemble();
}