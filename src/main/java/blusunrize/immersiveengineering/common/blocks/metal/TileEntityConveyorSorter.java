package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.ArrayList;

import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityConveyorSorter extends TileEntityIEBase implements ISidedInventory
{
	public SorterInventory filter;
	public int[] sideFilter = {0,0,0,0,0,0};//OreDict,nbt,fuzzy
	public static final int filterSlotsPerSide = 8;
	private boolean isRouting = false;


	public TileEntityConveyorSorter()
	{
		filter = new SorterInventory(this);
	}

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	public void routeItem(int inputSide, ItemStack stack)
	{
		if(!worldObj.isRemote)
		{
			Integer[][] validOutputs = getValidOutputs(inputSide, stack, true, false);
			outputting:
			{
				if(validOutputs[0].length>0)
				{
					int rand = worldObj.rand.nextInt(validOutputs[0].length);
					stack = this.outputItemToInv(stack, validOutputs[0][rand]);
					if(stack!=null)
						for(int i=0; i<validOutputs[0].length; i++)
							if(i!=rand)
							{
								stack = this.outputItemToInv(stack, validOutputs[0][i]);
								if(stack==null)
									break outputting;
							}

				}
				if(stack!=null && validOutputs[1].length>0)
				{
					int rand = worldObj.rand.nextInt(validOutputs[1].length);
					ForgeDirection fd = ForgeDirection.getOrientation(validOutputs[1][rand]);
					EntityItem ei = new EntityItem(worldObj, xCoord+.5+fd.offsetX, yCoord+.5+fd.offsetY, zCoord+.5+fd.offsetZ, stack.copy());
					ei.motionX = (0.075F * fd.offsetX);
					ei.motionY = 0.025000000372529D;
					ei.motionZ = (0.075F * fd.offsetZ);
					this.worldObj.spawnEntityInWorld(ei);
					break outputting;
				}
				if(validOutputs[2].length>0)
				{
					int rand = worldObj.rand.nextInt(validOutputs[2].length);
					stack = this.outputItemToInv(stack, validOutputs[2][rand]);
					if(stack!=null)
						for(int i=0; i<validOutputs[2].length; i++)
							if(i!=rand)
							{
								stack = this.outputItemToInv(stack, validOutputs[2][rand]);
								if(stack==null)
									break outputting;
							}

				}
				if(stack!=null && validOutputs[3].length>0)
				{
					int rand = worldObj.rand.nextInt(validOutputs[3].length);
					ForgeDirection fd = ForgeDirection.getOrientation(validOutputs[1][rand]);
					EntityItem ei = new EntityItem(worldObj, xCoord+.5+fd.offsetX, yCoord+.5+fd.offsetY, zCoord+.5+fd.offsetZ, stack.copy());
					ei.motionX = (0.075F * fd.offsetX);
					ei.motionY = 0.025000000372529D;
					ei.motionZ = (0.075F * fd.offsetZ);
					this.worldObj.spawnEntityInWorld(ei);
					break outputting;
				}

			}
		}
	}

	public boolean doOredict(int side)
	{
		if(side>=0 && side<this.sideFilter.length)
			return (this.sideFilter[side]&1)!=0;
		return false;		
	}
	public boolean doNBT(int side)
	{
		if(side>=0 && side<this.sideFilter.length)
			return (this.sideFilter[side]&2)!=0;
		return false;		
	}
	public boolean doFuzzy(int side)
	{
		if(side>=0 && side<this.sideFilter.length)
			return (this.sideFilter[side]&4)!=0;
		return false;		
	}

	@Override
	public void receiveMessageFromClient(NBTTagCompound message)
	{
		if(message.hasKey("sideConfig"))
			this.sideFilter = message.getIntArray("sideConfig");
	}

	public Integer[][] getValidOutputs(int inputSide, ItemStack stack, boolean allowUnmapped, boolean allowThrowing)
	{
		if(isRouting || stack==null)
			return new Integer[][]{{},{},{},{}};
		this.isRouting = true;
		ArrayList<Integer> validFilteredInvOuts = new ArrayList<Integer>(6);
		ArrayList<Integer> validFilteredEntityOuts = new ArrayList<Integer>(6);
		ArrayList<Integer> validUnfilteredInvOuts = new ArrayList<Integer>(6);
		ArrayList<Integer> validUnfilteredEntityOuts = new ArrayList<Integer>(6);
		for(int side=0; side<6; side++)
			if(side!=inputSide)
			{
				boolean unmapped = true;
				boolean allowed = false;
				filterIteration:
				{
					for(ItemStack filterStack : filter.filters[side])
						if(filterStack!=null)
						{
							unmapped = false;
							
							boolean b = OreDictionary.itemMatches(filterStack, stack, true);
							
							if(!b && doFuzzy(side))
								b = filterStack.getItem().equals(stack.getItem());
							
							if(!b && doOredict(side))
								for (String name:OreDictionary.getOreNames())
									if (Utils.compareToOreName(stack, name)&&Utils.compareToOreName(filterStack, name))
									{
										b = true;
										break;
									}
							
							if(doNBT(side))
								b &= ItemStack.areItemStackTagsEqual(filterStack, stack);
							if(b)
							{
								allowed=true;
								break filterIteration;
							}

						}
				}
				if(allowed)
				{
					ForgeDirection fd = ForgeDirection.getOrientation(side);
					TileEntity inventory = this.worldObj.getTileEntity(xCoord+fd.offsetX, yCoord+fd.offsetY, zCoord+fd.offsetZ);
					if(isInventory(inventory, ForgeDirection.OPPOSITES[side]) && Utils.canInsertStackIntoInventory((IInventory)inventory, stack, ForgeDirection.OPPOSITES[side]))
						validFilteredInvOuts.add(side);
					else if(allowThrowing)
						validFilteredEntityOuts.add(side);
				}
				else if(allowUnmapped&&unmapped)
				{
					ForgeDirection fd = ForgeDirection.getOrientation(side);
					TileEntity inventory = this.worldObj.getTileEntity(xCoord+fd.offsetX, yCoord+fd.offsetY, zCoord+fd.offsetZ);
					if(isInventory(inventory, ForgeDirection.OPPOSITES[side]) && Utils.canInsertStackIntoInventory((IInventory)inventory, stack, ForgeDirection.OPPOSITES[side]))
						validUnfilteredInvOuts.add(side);
					else if(allowThrowing)
						validUnfilteredEntityOuts.add(side);
				}
			}
		this.isRouting = false;
		return new Integer[][]{ 
				validFilteredInvOuts.toArray(new Integer[validFilteredInvOuts.size()]),
				validFilteredEntityOuts.toArray(new Integer[validFilteredEntityOuts.size()]),
				validUnfilteredInvOuts.toArray(new Integer[validUnfilteredInvOuts.size()]),
				validUnfilteredEntityOuts.toArray(new Integer[validUnfilteredEntityOuts.size()])
		};
	}


	public void outputItem(ItemStack stack, int side)
	{
		ForgeDirection fd = ForgeDirection.getOrientation(side);
		TileEntity inventory = this.worldObj.getTileEntity(xCoord+fd.offsetX, yCoord+fd.offsetY, zCoord+fd.offsetZ);
		if(isInventory(inventory, ForgeDirection.OPPOSITES[side]) && Utils.canInsertStackIntoInventory((IInventory)inventory, stack, ForgeDirection.OPPOSITES[side]))
			stack = Utils.insertStackIntoInventory((IInventory)inventory, stack, ForgeDirection.OPPOSITES[side]);

		if(stack != null)
		{
			EntityItem ei = new EntityItem(worldObj, xCoord+.5+fd.offsetX, yCoord+.5+fd.offsetY, zCoord+.5+fd.offsetZ, stack.copy());
			ei.motionX = (0.075F * fd.offsetX);
			ei.motionY = 0.025000000372529D;
			ei.motionZ = (0.075F * fd.offsetZ);
			this.worldObj.spawnEntityInWorld(ei);
		}
	}
	public ItemStack outputItemToInv(ItemStack stack, int side)
	{
		ForgeDirection fd = ForgeDirection.getOrientation(side);
		TileEntity inventory = this.worldObj.getTileEntity(xCoord+fd.offsetX, yCoord+fd.offsetY, zCoord+fd.offsetZ);
		if(isInventory(inventory, ForgeDirection.OPPOSITES[side]))
		{
			stack = Utils.insertStackIntoInventory((IInventory)inventory, stack, ForgeDirection.OPPOSITES[side]);
		}
		return stack;
	}
	boolean isInventory(TileEntity tile, int side)
	{
		if(tile instanceof ISidedInventory && ((ISidedInventory)tile).getAccessibleSlotsFromSide(side).length>0)
			return true;
		if(tile instanceof IInventory && ((IInventory)tile).getSizeInventory()>0)
			return true;
		return false;
	}


	@Override
	public int getSizeInventory()
	{
		return 6;
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
		this.routeItem(slot, stack);
		//			master().addStackToInputs(stack);
	}
	@Override
	public String getInventoryName()
	{
		return "IERouter";
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
		return worldObj.getTileEntity(xCoord,yCoord,zCoord)!=this?false:player.getDistanceSq(xCoord+.5D,yCoord+.5D,zCoord+.5D)<=64;
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
		Integer[][] outputs = getValidOutputs(slot, stack, true, false);
		if(outputs[0].length>0 || outputs[1].length>0 || outputs[2].length>0 || outputs[3].length>0)
			return true;

		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return new int[]{side};
	}
	@Override
	public boolean canInsertItem(int slot, ItemStack item, int side)
	{
		return side==slot;
	}
	@Override
	public boolean canExtractItem(int slot, ItemStack item, int side)
	{
		return false;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		sideFilter = nbt.getIntArray("sideFilter");
		if(!descPacket)
		{
			NBTTagList filterList = nbt.getTagList("filter", 10);
			filter = new SorterInventory(this);
			filter.readFromNBT(filterList);


		}
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setIntArray("sideFilter", sideFilter);
		if(!descPacket)
		{
			NBTTagList filterList = new NBTTagList();
			filter.writeToNBT(filterList);
			nbt.setTag("filter", filterList);
		}
	}


	public static class SorterInventory implements IInventory
	{
		public ItemStack[][] filters = new ItemStack[6][filterSlotsPerSide];
		final TileEntityConveyorSorter tile;

		public SorterInventory(TileEntityConveyorSorter tile)
		{
			this.tile = tile;
		}

		@Override
		public int getSizeInventory()
		{
			return 6*filterSlotsPerSide;
		}
		@Override
		public ItemStack getStackInSlot(int slot)
		{
			return filters[slot/filterSlotsPerSide][slot%filterSlotsPerSide];
		}

		@Override
		public ItemStack decrStackSize(int slot, int amount)
		{
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
			ItemStack stack = getStackInSlot(slot);
			if (stack != null)
				setInventorySlotContents(slot, null);
			return stack;
		}
		@Override
		public void setInventorySlotContents(int slot, ItemStack stack)
		{
			filters[slot/filterSlotsPerSide][slot%filterSlotsPerSide] = stack;
			if (stack != null && stack.stackSize > getInventoryStackLimit())
				stack.stackSize = getInventoryStackLimit();
		}

		@Override
		public String getInventoryName()
		{
			return "IESorterLayout";
		}

		@Override
		public boolean hasCustomInventoryName()
		{
			return false;
		}

		@Override
		public int getInventoryStackLimit()
		{
			return 1;
		}

		@Override
		public boolean isUseableByPlayer(EntityPlayer player)
		{
			return true;
		}

		@Override
		public void openInventory(){}
		@Override
		public void closeInventory(){}

		@Override
		public boolean isItemValidForSlot(int slot, ItemStack stack)
		{
			return true;
		}
		@Override
		public void markDirty()
		{
			this.tile.markDirty();
		}

		public void writeToNBT(NBTTagList list)
		{
			for(int i=0; i<this.filters.length; i++)
				for(int j=0; j<this.filters[i].length; j++)
					if(this.filters[i][j] != null)
					{
						NBTTagCompound itemTag = new NBTTagCompound();
						itemTag.setByte("Slot", (byte)(i*filterSlotsPerSide+j));
						this.filters[i][j].writeToNBT(itemTag);
						list.appendTag(itemTag);
					}

		}
		public void readFromNBT(NBTTagList list)
		{
			for (int i=0; i<list.tagCount(); i++)
			{
				NBTTagCompound itemTag = list.getCompoundTagAt(i);
				int slot = itemTag.getByte("Slot") & 255;
				if(slot>=0 && slot<getSizeInventory())
					this.filters[slot/filterSlotsPerSide][slot%filterSlotsPerSide] = ItemStack.loadItemStackFromNBT(itemTag);
			}
		}
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==0)
		{
			this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			return true;
		}
		return false;
	}
}