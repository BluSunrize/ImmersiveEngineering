package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.ArrayList;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.Utils;

public class TileEntityConveyorSorter extends TileEntityIEBase implements ISidedInventory, IBlockOverlayText
{
	public SorterInventory filter;
	public int[] oreDictFilter = {-1,-1,-1,-1,-1,-1};
	public int[] sideFilter = {0,0,0,0,0,0};
	public static final int filterSlotsPerSide = 8;


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

	public Integer[][] getValidOutputs(int inputSide, ItemStack stack, boolean allowUnmapped, boolean allowThrowing)
	{
		ArrayList<Integer> validFilteredInvOuts = new ArrayList<Integer>();
		ArrayList<Integer> validFilteredEntityOuts = new ArrayList<Integer>();
		ArrayList<Integer> validUnfilteredInvOuts = new ArrayList<Integer>();
		ArrayList<Integer> validUnfilteredEntityOuts = new ArrayList<Integer>();
		for(int side=0; side<6; side++)
			if(side!=inputSide && sideFilter[side]==0)
			{
				boolean unmapped = true;
				boolean allowed = false;
				filterIteration:
				{
					for(ItemStack filterStack : filter.filters[side])
						if(filterStack!=null)
						{
							unmapped = false;
							if(oreDictFilter[side]==0)
							{
								for(int allowedOid : OreDictionary.getOreIDs(filterStack))
									for(int oid : OreDictionary.getOreIDs(stack))
										if(oid==allowedOid)
										{
											allowed=true;
											break filterIteration;
										}
							}
							else
							{
								if(OreDictionary.itemMatches(filterStack, stack, true))
								{
									allowed=true;
									break filterIteration;
								}
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

	public void toggleSide(int side)
	{
		oreDictFilter[side]++;
		if(oreDictFilter[side]>0)
			oreDictFilter[side]=-1;
		worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), 0, 0);
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

	@Override
	public String[] getOverlayText(EntityPlayer player, MovingObjectPosition mop, boolean hammer)
	{
		if(hammer)
			return new String []{
				StatCollector.translateToLocal("desc.ImmersiveEngineering.info.blockSide."+ForgeDirection.getOrientation(mop.sideHit)),
				StatCollector.translateToLocal("desc.ImmersiveEngineering.info.oreDict."+(oreDictFilter[mop.sideHit]==-1?"off":"on"))
		};
		return null;
	}
}
