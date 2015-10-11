package blusunrize.immersiveengineering.common.blocks.stone;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.util.Utils;

public class TileEntityCokeOven extends TileEntityMultiblockPart implements ISidedInventory, IFluidHandler
{
	public FluidTank tank = new FluidTank(12000);
	ItemStack[] inventory = new ItemStack[4];
	public int facing = 2;
	public int process = 0;
	public int processMax = 0;
	public boolean active = false;

	@Override
	public TileEntityCokeOven master()
	{
		if(offset[0]==0&&offset[1]==0&&offset[2]==0)
			return null;
		TileEntity te = worldObj.getTileEntity(xCoord-offset[0], yCoord-offset[1], zCoord-offset[2]);
		return te instanceof TileEntityCokeOven?(TileEntityCokeOven)te : null;
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
		return new ItemStack(IEContent.blockStoneDecoration,1,1);
	}

	@Override
	public void updateEntity()
	{
		if(!worldObj.isRemote&&formed&&master()==null)
		{
			boolean a = active;
			boolean b = false;
			if(process>0)
			{
				if(inventory[0]==null)
				{
					process=0;
					processMax=0;
				}
				else
				{
					CokeOvenRecipe recipe = getRecipe();
					if(recipe==null || recipe.time!=processMax)
					{
						process=0;
						processMax=0;
						active=false;
					}
					else
						process--;
				}
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
			else
			{
				if(active)
				{
					CokeOvenRecipe recipe = getRecipe();
					if(recipe!=null)
					{
						this.decrStackSize(0, 1);
						if(inventory[1]!=null)
							inventory[1].stackSize+=recipe.output.copy().stackSize;
						else if(inventory[1]==null)
							inventory[1] = recipe.output.copy();
						this.tank.fill(new FluidStack(IEContent.fluidCreosote,recipe.creosoteOutput), true);
					}
					processMax=0;
					active=false;
				}
				else
				{
					CokeOvenRecipe recipe = getRecipe();
					if(recipe!=null)
					{
						this.process=recipe.time;
						this.processMax=process;
						this.active=true;
					}
				}
			}

			if(tank.getFluidAmount()>0 && tank.getFluid()!=null && (inventory[3]==null||inventory[3].stackSize+1<=inventory[3].getMaxStackSize()))
			{
				ItemStack filledContainer = Utils.fillFluidContainer(tank, inventory[2], inventory[3]);
				if(filledContainer!=null)
				{
					if(inventory[3]!=null && OreDictionary.itemMatches(inventory[3], filledContainer, true))
						inventory[3].stackSize+=filledContainer.stackSize;
					else if(inventory[3]==null)
						inventory[3] = filledContainer.copy();
					this.decrStackSize(2, filledContainer.stackSize);
					b=true;
				}
			}

			if(a!=active || b)
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
	public CokeOvenRecipe getRecipe()
	{
		CokeOvenRecipe recipe = CokeOvenRecipe.findRecipe(inventory[0]);
		if(recipe==null)
			return null;

		if(inventory[1]==null || (OreDictionary.itemMatches(inventory[1],recipe.output,false) && inventory[1].stackSize+recipe.output.stackSize<=getInventoryStackLimit()) )
			if(tank.getFluidAmount()+recipe.creosoteOutput<=tank.getCapacity())
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
		return "IECokeOven";
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
		if(slot==0)
			return stack!=null && CokeOvenRecipe.findRecipe(stack)!=null;
		if(slot==2)
			return stack!=null && FluidContainerRegistry.isEmptyContainer(stack);
		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		if(!formed)
			return new int[0];
		if(master()!=null)
			return master().getAccessibleSlotsFromSide(side);
		return new int[]{0,1,2,3};
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side)
	{
		if(!formed)
			return false;
		if(master()!=null)
			return master().canInsertItem(slot,stack,side);
		return (slot==0 || slot==2)&&this.isItemValidForSlot(slot, stack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side)
	{
		if(!formed)
			return false;
		if(master()!=null)
			return master().canExtractItem(slot,stack,side);
		return slot==1 || slot==3;
	}



	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = nbt.getInteger("facing");

		process = nbt.getInteger("process");
		processMax = nbt.getInteger("processMax");
		active = nbt.getBoolean("active");

		tank.readFromNBT(nbt.getCompoundTag("tank"));
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

		NBTTagCompound tankTag = tank.writeToNBT(new NBTTagCompound());
		nbt.setTag("tank", tankTag);
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
			if(!(offset[0]==0&&offset[1]==0&&offset[2]==0) && !(worldObj.getTileEntity(startX, startY, startZ) instanceof TileEntityCokeOven))
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
						if(te instanceof TileEntityCokeOven)
						{
							s = ((TileEntityCokeOven)te).getOriginalBlock();
							((TileEntityCokeOven)te).formed=false;
						}
						if(startX+xx==xCoord && startY+yy==yCoord && startZ+zz==zCoord)
							s = this.getOriginalBlock();
						if(s!=null && Block.getBlockFromItem(s.getItem())!=null)
						{
							if(startX+xx==xCoord && startY+yy==yCoord && startZ+zz==zCoord)
								worldObj.spawnEntityInWorld(new EntityItem(worldObj, xCoord+.5,yCoord+.5,zCoord+.5, s));
							else
							{
								if(Block.getBlockFromItem(s.getItem())==IEContent.blockMetalMultiblocks)
									worldObj.setBlockToAir(startX+xx,startY+yy,startZ+zz);
								worldObj.setBlock(startX+xx,startY+yy,startZ+zz, Block.getBlockFromItem(s.getItem()), s.getItemDamage(), 0x3);
							}
						}
					}
		}
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		return 0;
	}
	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if(!formed)
			return null;
		if(master()!=null)
			return master().drain(from,resource,doDrain);
		if(resource!=null)
		{
			FluidStack fs = drain(from, resource.amount, doDrain);
			markDirty();
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			return fs;
		}
		return null;
	}
	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		if(!formed)
			return null;
		if(master()!=null)
			return master().drain(from,maxDrain,doDrain);
		return tank.drain(maxDrain, doDrain);
	}
	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return false;
	}
	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		if(!formed)
			return false;
		return true;
	}
	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		if(!formed)
			return new FluidTankInfo[]{};
		if(master()!=null)
			return master().getTankInfo(from);
		return new FluidTankInfo[]{tank.getInfo()};
	}
}