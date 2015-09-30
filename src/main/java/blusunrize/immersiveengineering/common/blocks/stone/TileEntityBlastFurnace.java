package blusunrize.immersiveengineering.common.blocks.stone;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockPart;

public class TileEntityBlastFurnace extends TileEntityMultiblockPart implements ISidedInventory
{
	ItemStack[] inventory = new ItemStack[3];
	public int facing = 2;
	public int process = 0;
	public int processMax = 0;
	public boolean active = false;
	public int burnTime = 0;
	public int lastBurnTime = 0;

	public TileEntityBlastFurnace master()
	{
		if(offset[0]==0&&offset[1]==0&&offset[2]==0)
			return null;
		TileEntity te = worldObj.getTileEntity(xCoord-offset[0], yCoord-offset[1], zCoord-offset[2]);
		return te instanceof TileEntityBlastFurnace?(TileEntityBlastFurnace)te : null;
	}

	public static boolean _Immovable()
	{
		return true;
	}

	@Override
	public float[] getBlockBounds()
	{
		return new float[]{0,0,0,1,1,1};
	}

	@Override
	public ItemStack getOriginalBlock()
	{
		return new ItemStack(IEContent.blockStoneDecoration,1,2);
	}

	@Override
	public void updateEntity()
	{
		if(!worldObj.isRemote&&formed&&master()==null)
		{
			boolean a = active;

			if(burnTime>0)
			{			
				if(process>0)
				{
					if(inventory[0]==null)
					{
						process=0;
						processMax=0;
					}
					else
					{
						process--;
						if(!active)
							active=true;
					}
					burnTime--;
					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				}

				if(process<=0)
				{
					if(active)
					{
						BlastFurnaceRecipe recipe = getRecipe();
						if(recipe!=null)
						{
							this.decrStackSize(0, 1);
							if(inventory[2]!=null)
								inventory[2].stackSize+=recipe.output.copy().stackSize;
							else
								inventory[2] = recipe.output.copy();
						}
						processMax=0;
						active=false;
					}
					else
					{
						BlastFurnaceRecipe recipe = getRecipe();
						if(recipe!=null)
						{
							this.process=recipe.time;
							this.processMax=process;
							this.active=true;
						}
					}
				}
			}
			else
			{
				if(active)
					active=false;
			}

			if(burnTime<=10 && getRecipe()!=null)
			{
				if(BlastFurnaceRecipe.isValidBlastFuel(inventory[1]))
				{
					burnTime += BlastFurnaceRecipe.getBlastFuelTime(inventory[1]);
					lastBurnTime = BlastFurnaceRecipe.getBlastFuelTime(inventory[1]);
					this.decrStackSize(1, 1);
					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				}
			}

			if(a!=active)
			{

				this.markDirty();
				int xMin= facing==5?-2: facing==4?0:-1;
				int xMax= facing==5? 0: facing==4?2: 1;
				int zMin= facing==3?-2: facing==2?0:-1;
				int zMax= facing==3? 0: facing==2?2: 1;
				TileEntity tileEntity;
				for(int yy=-1;yy<=1;yy++)
					for(int xx=xMin;xx<=xMax;xx++)
						for(int zz=zMin;zz<=zMax;zz++)
						{
							tileEntity = worldObj.getTileEntity(xCoord+xx, yCoord+yy, zCoord+zz);
							if(tileEntity!=null)
								tileEntity.markDirty();
							worldObj.markBlockForUpdate(xCoord+xx, yCoord+yy, zCoord+zz);
							worldObj.addBlockEvent(xCoord+xx, yCoord+yy, zCoord+zz, IEContent.blockStoneDevice, 1,active?1:0);
						}
			}
		}
	}
	public BlastFurnaceRecipe getRecipe()
	{
		BlastFurnaceRecipe recipe = BlastFurnaceRecipe.findRecipe(inventory[0]);
		if(recipe==null)
			return null;
		if(inventory[2]==null || (OreDictionary.itemMatches(inventory[2],recipe.output,true) && inventory[2].stackSize+recipe.output.stackSize<=getInventoryStackLimit()) )
			return recipe;
		return null;
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==0)
			this.formed = arg==1;
		else if(id==1)
			this.active = arg==1;
		markDirty();
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		return true;
	}

	@Override
	public int getSizeInventory()
	{
		if(!formed)
			return 0;
		return inventory.length;
	}
	@Override
	public ItemStack getStackInSlot(int slot)
	{
		if(!formed)
			return null;
		if(master()!=null)
			return master().getStackInSlot(slot);
		if(slot<inventory.length)
			return inventory[slot];
		return null;
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount)
	{
		if(!formed)
			return null;
		if(master()!=null)
			return master().decrStackSize(slot,amount);
		ItemStack stack = getStackInSlot(slot);
		if(stack != null)
			if(stack.stackSize <= amount)
				setInventorySlotContents(slot, null);
			else
			{
				stack = stack.splitStack(amount);
				if(stack.stackSize == 0)
					setInventorySlotContents(slot, null);
			}
		return stack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		if(!formed)
			return null;
		if(master()!=null)
			return master().getStackInSlotOnClosing(slot);
		ItemStack stack = getStackInSlot(slot);
		if (stack != null)
			setInventorySlotContents(slot, null);
		return stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack)
	{
		if(!formed)
			return;
		if(master()!=null)
		{
			master().setInventorySlotContents(slot,stack);
			return;
		}
		inventory[slot] = stack;
		if (stack != null && stack.stackSize > getInventoryStackLimit())
			stack.stackSize = getInventoryStackLimit();
	}

	@Override
	public String getInventoryName()
	{
		return "IEBlastFurnace";
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
	public boolean isUseableByPlayer(EntityPlayer p_70300_1_)
	{
		return true;
	}

	@Override
	public void openInventory()
	{
	}
	@Override
	public void closeInventory()
	{
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack)
	{
		if(!formed)
			return false;
		if(master()!=null)
			return master().isItemValidForSlot(slot,stack);
		if(BlastFurnaceRecipe.isValidBlastFuel(stack))
			return slot==1;
		if(slot==0)
			return BlastFurnaceRecipe.findRecipe(stack)!=null;

		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		if(!formed)
			return new int[0];
		if(master()!=null)
			return master().getAccessibleSlotsFromSide(side);
		return new int[]{0,1,2};
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side)
	{
		if(!formed)
			return false;
		if(master()!=null)
			return master().canInsertItem(slot,stack,side);
		return (slot==0||slot==1) && isItemValidForSlot(slot,stack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side)
	{
		if(!formed)
			return false;
		if(master()!=null)
			return master().canExtractItem(slot,stack,side);
		return slot==2;
	}



	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = nbt.getInteger("facing");
		process = nbt.getInteger("process");
		processMax = nbt.getInteger("processMax");
		active = nbt.getBoolean("active");
		burnTime = nbt.getInteger("burnTime");
		lastBurnTime = nbt.getInteger("lastBurnTime");
		if(!descPacket)
		{
			NBTTagList invList = nbt.getTagList("inventory", 10);
			for (int i=0; i<invList.tagCount(); i++)
			{
				NBTTagCompound itemTag = invList.getCompoundTagAt(i);
				int slot = itemTag.getByte("Slot") & 255;
				if(slot>=0 && slot<this.inventory.length)
					this.inventory[slot] = ItemStack.loadItemStackFromNBT(itemTag);
			}
		}
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", facing);
		nbt.setInteger("process", process);
		nbt.setInteger("processMax", processMax);
		nbt.setBoolean("active", active);
		nbt.setInteger("burnTime", burnTime);
		nbt.setInteger("lastBurnTime", lastBurnTime);
		if(!descPacket)
		{
			NBTTagList invList = new NBTTagList();
			for(int i=0; i<this.inventory.length; i++)
				if(this.inventory[i] != null)
				{
					NBTTagCompound itemTag = new NBTTagCompound();
					itemTag.setByte("Slot", (byte)i);
					this.inventory[i].writeToNBT(itemTag);
					invList.appendTag(itemTag);
				}
			nbt.setTag("inventory", invList);
		}
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		if(formed && !worldObj.isRemote)
		{
			int startX = xCoord - offset[0];
			int startY = yCoord - offset[1];
			int startZ = zCoord - offset[2];
			if(!(offset[0]==0&&offset[1]==0&&offset[2]==0) && !(worldObj.getTileEntity(startX, startY, startZ) instanceof TileEntityBlastFurnace))
				return;

			int xMin= facing==5?-2: facing==4?0:-1;
			int xMax= facing==5? 0: facing==4?2: 1;
			int zMin= facing==3?-2: facing==2?0:-1;
			int zMax= facing==3? 0: facing==2?2: 1;
			for(int yy=-1;yy<=1;yy++)
				for(int xx=xMin;xx<=xMax;xx++)
					for(int zz=zMin;zz<=zMax;zz++)
					{
						ItemStack s = null;
						TileEntity te = worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz);
						if(te instanceof TileEntityBlastFurnace)
						{
							s = ((TileEntityBlastFurnace)te).getOriginalBlock();
							((TileEntityBlastFurnace)te).formed=false;
						}
						if(startX+xx==xCoord && startY+yy==yCoord && startZ+zz==zCoord)
							s = this.getOriginalBlock();
						if(s!=null && Block.getBlockFromItem(s.getItem())!=null)
						{
							if(startX+xx==xCoord && startY+yy==yCoord && startZ+zz==zCoord)
								worldObj.spawnEntityInWorld(new EntityItem(worldObj, xCoord+.5,yCoord+.5,zCoord+.5, s));
							else
							{
								if(Block.getBlockFromItem(s.getItem())==IEContent.blockStoneDevice)
									worldObj.setBlockToAir(startX+xx,startY+yy,startZ+zz);
								worldObj.setBlock(startX+xx,startY+yy,startZ+zz, Block.getBlockFromItem(s.getItem()), s.getItemDamage(), 0x3);
							}
						}
					}
		}
	}
}