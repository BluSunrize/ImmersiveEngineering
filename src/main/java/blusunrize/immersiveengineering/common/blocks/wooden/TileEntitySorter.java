package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;

public class TileEntitySorter extends TileEntityIEBase implements IGuiTile
{
	public SorterInventory filter;
	public int[] sideFilter = {0,0,0,0,0,0};//OreDict,nbt,fuzzy
	public static final int filterSlotsPerSide = 8;
	private boolean isRouting = false;


	public TileEntitySorter()
	{
		filter = new SorterInventory(this);
	}


	public ItemStack routeItem(EnumFacing inputSide, ItemStack stack, boolean simulate)
	{
		if(!worldObj.isRemote)
		{
			Integer[][] validOutputs = getValidOutputs(inputSide, stack, true, false);

			if(validOutputs[0].length>0)
			{
				int rand = worldObj.rand.nextInt(validOutputs[0].length);
				stack = this.outputItemToInv(stack, EnumFacing.getFront(validOutputs[0][rand]), simulate);
				if(stack!=null)
					for(int i=0; i<validOutputs[0].length; i++)
						if(i!=rand)
						{
							stack = this.outputItemToInv(stack, EnumFacing.getFront(validOutputs[0][i]), simulate);
							if(stack==null)
								return null;
						}

			}
			if(stack!=null && validOutputs[1].length>0)
			{
				if(!simulate)
				{
					int rand = worldObj.rand.nextInt(validOutputs[1].length);
					EnumFacing fd = EnumFacing.getFront(validOutputs[1][rand]);
					EntityItem ei = new EntityItem(worldObj, getPos().getX()+.5+fd.getFrontOffsetX(), getPos().getY()+.5+fd.getFrontOffsetY(), getPos().getZ()+.5+fd.getFrontOffsetZ(), stack.copy());
					ei.motionX = (0.075F * fd.getFrontOffsetX());
					ei.motionY = 0.025000000372529D;
					ei.motionZ = (0.075F * fd.getFrontOffsetZ());
					this.worldObj.spawnEntityInWorld(ei);
				}
				return null;
			}
			if(validOutputs[2].length>0)
			{
				int rand = worldObj.rand.nextInt(validOutputs[2].length);
				stack = this.outputItemToInv(stack, EnumFacing.getFront(validOutputs[2][rand]), simulate);
				if(stack!=null)
					for(int i=0; i<validOutputs[2].length; i++)
						if(i!=rand)
						{
							stack = this.outputItemToInv(stack, EnumFacing.getFront(validOutputs[2][i]), simulate);
							if(stack==null)
								return null;
						}

			}
			if(stack!=null && validOutputs[3].length>0)
			{
				if(!simulate)
				{
					int rand = worldObj.rand.nextInt(validOutputs[3].length);
					EnumFacing fd = EnumFacing.getFront(validOutputs[1][rand]);
					EntityItem ei = new EntityItem(worldObj, getPos().getX()+.5+fd.getFrontOffsetX(), getPos().getY()+.5+fd.getFrontOffsetY(), getPos().getZ()+.5+fd.getFrontOffsetZ(), stack.copy());
					ei.motionX = (0.075F * fd.getFrontOffsetX());
					ei.motionY = 0.025000000372529D;
					ei.motionZ = (0.075F * fd.getFrontOffsetZ());
					this.worldObj.spawnEntityInWorld(ei);
				}
				return null;
			}
		}
		return stack;
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
	public boolean canOpenGui()
	{
		return true;
	}
	@Override
	public int getGuiID()
	{
		return Lib.GUIID_Sorter;
	}
	@Override
	public TileEntity getGuiMaster()
	{
		return this;
	}

	@Override
	public void receiveMessageFromClient(NBTTagCompound message)
	{
		if(message.hasKey("sideConfig"))
			this.sideFilter = message.getIntArray("sideConfig");
	}

	public Integer[][] getValidOutputs(EnumFacing inputSide, ItemStack stack, boolean allowUnmapped, boolean allowThrowing)
	{
		if(isRouting || stack==null)
			return new Integer[][]{{},{},{},{}};
			this.isRouting = true;
			ArrayList<Integer> validFilteredInvOuts = new ArrayList<Integer>(6);
			ArrayList<Integer> validFilteredEntityOuts = new ArrayList<Integer>(6);
			ArrayList<Integer> validUnfilteredInvOuts = new ArrayList<Integer>(6);
			ArrayList<Integer> validUnfilteredEntityOuts = new ArrayList<Integer>(6);
			for(EnumFacing side : EnumFacing.values())
				if(side!=inputSide)
				{
					boolean unmapped = true;
					boolean allowed = false;
					filterIteration:
					{
						for(ItemStack filterStack : filter.filters[side.ordinal()])
							if(filterStack!=null)
							{
								unmapped = false;

								boolean b = OreDictionary.itemMatches(filterStack, stack, true);

								if(!b && doFuzzy(side.ordinal()))
									b = filterStack.getItem().equals(stack.getItem());

								if(!b && doOredict(side.ordinal()))
									for (String name:OreDictionary.getOreNames())
										if (Utils.compareToOreName(stack, name)&&Utils.compareToOreName(filterStack, name))
										{
											b = true;
											break;
										}

								if(doNBT(side.ordinal()))
									b &= Utils.compareItemNBT(filterStack, stack);
								if(b)
								{
									allowed=true;
									break filterIteration;
								}

							}
					}
					if(allowed)
					{
						TileEntity inventory = this.worldObj.getTileEntity(getPos().offset(side));
						if(Utils.canInsertStackIntoInventory(inventory, stack, side.getOpposite()))
							validFilteredInvOuts.add(side.ordinal());
						else if(allowThrowing)
							validFilteredEntityOuts.add(side.ordinal());
					}
					else if(allowUnmapped&&unmapped)
					{
						TileEntity inventory = this.worldObj.getTileEntity(getPos().offset(side));
						if(Utils.canInsertStackIntoInventory(inventory, stack, side.getOpposite()))
							validUnfilteredInvOuts.add(side.ordinal());
						else if(allowThrowing)
							validUnfilteredEntityOuts.add(side.ordinal());
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


	//	public void outputItem(ItemStack stack, EnumFacing side)
	//	{
	//		TileEntity inventory = this.worldObj.getTileEntity(getPos().offset(side));
	//		stack = Utils.insertStackIntoInventory(inventory, stack, side.getOpposite());
	//		if(stack != null)
	//		{
	//			EntityItem ei = new EntityItem(worldObj, getPos().getX()+.5+side.getFrontOffsetX(), getPos().getY()+.5+side.getFrontOffsetY(), getPos().getZ()+.5+side.getFrontOffsetZ(), stack.copy());
	//			ei.motionX = (0.075F * side.getFrontOffsetX());
	//			ei.motionY = 0.025000000372529D;
	//			ei.motionZ = (0.075F * side.getFrontOffsetZ());
	//			this.worldObj.spawnEntityInWorld(ei);
	//		}
	//	}
	public ItemStack outputItemToInv(ItemStack stack, EnumFacing side, boolean simulate)
	{
		TileEntity inventory = this.worldObj.getTileEntity(getPos().offset(side));
		return Utils.insertStackIntoInventory(inventory, stack, side.getOpposite(), simulate);
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


	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return true;
		return super.hasCapability(capability, facing);
	}
	IItemHandler[] insertionHandlers = {
			new SorterInventoryHandler(this,EnumFacing.DOWN),
			new SorterInventoryHandler(this,EnumFacing.UP),
			new SorterInventoryHandler(this,EnumFacing.NORTH),
			new SorterInventoryHandler(this,EnumFacing.SOUTH),
			new SorterInventoryHandler(this,EnumFacing.WEST),
			new SorterInventoryHandler(this,EnumFacing.EAST)};

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing!=null)
			return (T)insertionHandlers[facing.ordinal()];
		return super.getCapability(capability, facing);
	}

	public static class SorterInventoryHandler implements IItemHandlerModifiable
	{
		TileEntitySorter sorter;
		EnumFacing side;
		public SorterInventoryHandler(TileEntitySorter sorter, EnumFacing side)
		{
			this.sorter = sorter;
			this.side = side;
		}

		@Override
		public int getSlots()
		{
			return 1;
		}
		@Override
		public ItemStack getStackInSlot(int slot)
		{
			return null;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			return sorter.routeItem(this.side, stack, simulate);
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			return null;
		}
		@Override
		public void setStackInSlot(int slot, ItemStack stack)
		{
		}
	}


	public static class SorterInventory implements IInventory
	{
		public ItemStack[][] filters = new ItemStack[6][filterSlotsPerSide];
		final TileEntitySorter tile;

		public SorterInventory(TileEntitySorter tile)
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
		public ItemStack removeStackFromSlot(int slot)
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
		public void clear()
		{
			for(int i=0; i<filters.length; i++)
				for(int j=0; j<filters[i].length; j++)
					filters[i][j] = null;
		}

		@Override
		public String getName()
		{
			return "IESorterLayout";
		}
		@Override
		public boolean hasCustomName()
		{
			return false;
		}
		@Override
		public ITextComponent getDisplayName()
		{
			return new TextComponentString(getName());
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
		public void openInventory(EntityPlayer player){}
		@Override
		public void closeInventory(EntityPlayer player){}

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


		@Override
		public int getField(int id)
		{
			return 0;
		}
		@Override
		public void setField(int id, int value)
		{
		}
		@Override
		public int getFieldCount()
		{
			return 0;
		}
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		return id == 0;
	}
}