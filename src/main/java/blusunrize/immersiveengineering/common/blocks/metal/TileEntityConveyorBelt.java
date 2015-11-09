package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;

public class TileEntityConveyorBelt extends TileEntityIEBase implements ISidedInventory
{
	public boolean transportUp=false;
	public boolean transportDown=false;
	public int facing=2;

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		transportUp = nbt.getBoolean("transportUp");
		transportDown = nbt.getBoolean("transportDown");
		facing = nbt.getInteger("facing");
		if(descPacket)
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setBoolean("transportUp", transportUp);
		nbt.setBoolean("transportDown", transportDown);
		nbt.setInteger("facing", facing);
	}

	@Override
	public int getSizeInventory()
	{
		return 1;
	}
	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return null;
	}
	@Override
	public ItemStack decrStackSize(int slot, int amount)
	{
		return null;
	}
	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		return null;
	}
	@Override
	public void setInventorySlotContents(int slot, ItemStack stack)
	{
		if(!worldObj.isRemote)
			worldObj.spawnEntityInWorld(new EntityItem(worldObj,xCoord+.5,yCoord+.25,zCoord+.5, stack));
	}
	@Override
	public String getInventoryName()
	{
		return "IEConveyor";
	}
	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}
	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}
	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		return false;
	}
	@Override
	public void openInventory() {}
	@Override
	public void closeInventory() {}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack)
	{
		return true;
	}
	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		if(side==ForgeDirection.OPPOSITES[facing])
			return new int[0];
		return new int[]{0};
	}
	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side)
	{
		return true;
	}
	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side)
	{
		return false;
	}

}
