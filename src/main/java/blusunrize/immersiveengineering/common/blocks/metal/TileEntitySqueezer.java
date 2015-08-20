package blusunrize.immersiveengineering.common.blocks.metal;

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
import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.api.energy.DieselHandler.SqueezerRecipe;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockSqueezer;
import blusunrize.immersiveengineering.common.util.Utils;
import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyReceiver;

public class TileEntitySqueezer extends TileEntityMultiblockPart implements IFluidHandler, ISidedInventory, IEnergyReceiver
{
	public int facing = 2;
	public FluidTank tank = new FluidTank(12000);
	public EnergyStorage energyStorage = new EnergyStorage(32000,256,32000);
	ItemStack[] inventory = new ItemStack[12];
	public int tick=0;
	int processMaxTime = 0;


	public TileEntitySqueezer master()
	{
		if(offset[0]==0&&offset[1]==0&&offset[2]==0)
			return null;
		TileEntity te = worldObj.getTileEntity(xCoord-offset[0], yCoord-offset[1], zCoord-offset[2]);
		return te instanceof TileEntitySqueezer?(TileEntitySqueezer)te : null;
	}
	public static boolean _Immovable()
	{
		return true;
	}

	@Override
	public ItemStack getOriginalBlock()
	{
		if(!formed)
			return new ItemStack(IEContent.blockMetalMultiblocks,1,BlockMetalMultiblocks.META_squeezer);
		return MultiblockSqueezer.instance.getStructureManual()[(pos%9/3)][pos/9][pos%3].copy();
	}
	@Override
	public float[] getBlockBounds()
	{
		return new float[]{0,0,0,1,1,1};
	}


	@Override
	public void updateEntity()
	{
		if(!formed || pos!=13)
			return;

		if(!worldObj.isRemote)
		{
			boolean update = false;
			int[] valid = getValidInputs();
			int inputs = Math.min(9, valid[0]);
			this.processMaxTime = valid[1];
			if(inputs>0 )//&& hasTankSpace())
			{
				int consumed = Config.getInt("squeezer_consumption");
				if(energyStorage.extractEnergy(consumed, true)==consumed)
				{
					energyStorage.extractEnergy(consumed, false);
					tick++;
				}
				if(tick>=valid[1])
				{
					SqueezerRecipe recipe=null;
					for(int i=0; i<9; i++)
					{
						ItemStack stack = this.getStackInSlot(i);
						if(stack!=null)
						{
							SqueezerRecipe rr = getRecipe(stack);
							if(rr==null)
								continue;
							if(recipe==null || rr==recipe || (rr.fluid!=null?rr.fluid.isFluidEqual(recipe.fluid): recipe.fluid!=null?recipe.fluid.isFluidEqual(rr.fluid): false) )
								recipe=rr;
							else
								continue;

							if(recipe!=null)
							{
								int recipeInputSize = recipe.input instanceof ItemStack?((ItemStack)recipe.input).stackSize:1;
								int outLimit = recipe.output==null?9: ((64-(inventory[11]!=null?inventory[11].stackSize:0))/recipe.output.stackSize);
								int fluidLimit = recipe.fluid==null?9: ((tank.getCapacity()-tank.getFluidAmount())/recipe.fluid.amount);
								int taken = Math.min(Math.min(inputs,stack.stackSize/recipeInputSize), Math.min(outLimit,fluidLimit));
								//								
								if(taken>0)
								{
									this.decrStackSize(i, taken*recipeInputSize);
									if(recipe.output!=null)
										if(inventory[11]!=null)
											inventory[11].stackSize+= taken*recipe.output.copy().stackSize;
										else if(inventory[11]==null)
											inventory[11] = Utils.copyStackWithAmount(recipe.output, taken*recipe.output.stackSize);
									if(recipe.fluid!=null)
										this.tank.fill( new FluidStack(recipe.fluid, taken*recipe.fluid.amount), true);
									inputs-=taken;
									update = true;
								}
							}

							//							int f = DieselHandler.getPlantoilOutput(stack);
							//							if(f>0)
							//							{
							//								int fSpace = tank.getCapacity()-tank.getFluidAmount();
							//								int taken = Math.min(inputs, Math.min(stack.stackSize, fSpace/f));
							//								if(taken>0)
							//								{
							//									tank.fill(new FluidStack(IEContent.fluidPlantoil,f*taken), true);
							//									this.decrStackSize(i, taken);
							//									inputs-=taken;
							//									update = true;
							//								}
							//								else
							//									continue;
							//							}
							if(inputs<=0 || tank.getFluidAmount()>=tank.getCapacity())
								break;
						}
					}
					tick=0;
				}
			}
			else if(tick>0)
				tick=0;
			if(tank.getFluidAmount()>0 && tank.getFluid()!=null && (inventory[10]==null||inventory[10].stackSize+1<inventory[10].getMaxStackSize()))
			{
				ItemStack filledContainer = Utils.fillFluidContainer(tank, inventory[9], inventory[10]);
				if(filledContainer!=null)
				{
					if(inventory[10]!=null && OreDictionary.itemMatches(inventory[10], filledContainer, true))
						inventory[10].stackSize+=filledContainer.stackSize;
					else if(inventory[10]==null)
						inventory[10] = filledContainer.copy();
					this.decrStackSize(9, filledContainer.stackSize);
					update = true;
				}

				if(tank.getFluidAmount()>0 && tank.getFluid()!=null)
				{
					int connected=0;
					for(int f=2; f<6; f++)
					{
						TileEntity te = worldObj.getTileEntity(xCoord+(f==4?-2:f==5?2:0),yCoord-1,zCoord+(f==2?-2:f==3?2:0));
						if(te!=null && te instanceof IFluidHandler && ((IFluidHandler)te).canFill(ForgeDirection.getOrientation(f).getOpposite(), this.tank.getFluid().getFluid()))
							connected++;
					}
					if(connected!=0)
					{
						int out = Math.min(144,tank.getFluidAmount())/connected;
						for(int f=2; f<6; f++)
						{
							TileEntity te = worldObj.getTileEntity(xCoord+(f==4?-2:f==5?2:0),yCoord-1,zCoord+(f==2?-2:f==3?2:0));
							if(te!=null && te instanceof IFluidHandler && this.tank.getFluid()!=null && ((IFluidHandler)te).canFill(ForgeDirection.getOrientation(f).getOpposite(), this.tank.getFluid().getFluid()))
							{
								int accepted = ((IFluidHandler)te).fill(ForgeDirection.getOrientation(f).getOpposite(), new FluidStack(this.tank.getFluid().getFluid(),out), false);
								FluidStack drained = this.tank.drain(accepted, true);
								((IFluidHandler)te).fill(ForgeDirection.getOrientation(f).getOpposite(), drained, true);
								update = true;
							}
						}
					}
				}
			}
			if(update)
			{
				this.markDirty();
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
		}
	}

	public SqueezerRecipe getRecipe(ItemStack input)
	{
		SqueezerRecipe recipe = DieselHandler.findSqueezerRecipe(input);
		if(recipe==null)
			return null;

		if(inventory[11]==null || recipe.output==null || (OreDictionary.itemMatches(inventory[11],recipe.output,false) && inventory[11].stackSize+recipe.output.stackSize<=recipe.output.getMaxStackSize()) )
			if(tank.getFluid()==null || recipe.fluid==null || (tank.getFluid().isFluidEqual(recipe.fluid) && tank.getFluidAmount()+recipe.fluid.amount<=tank.getCapacity()))
				return recipe;
		return null;
	}

	int[] getValidInputs()
	{
		int in=0;
		int time = 0;
		for(int i=0; i<9; i++)
		{
			ItemStack stack = this.getStackInSlot(i);
			SqueezerRecipe r = getRecipe(stack);
			if(r!=null)
			{
				in+=stack.stackSize;
				if(r.time>time)
					time = r.time;
			}
		}
		return new int[]{in,time};
	}
	public int getScaledProgress(int scale)
	{
		if(processMaxTime<=0)
			return 0;
		return (int) (scale*(tick/(float)processMaxTime));
	}

	//	boolean hasTankSpace()
	//	{
	//		for(int i=0; i<9; i++)
	//		{
	//			ItemStack stack = this.getStackInSlot(i);
	//			if(stack!=null)
	//			{
	//				int f = DieselHandler.getPlantoilOutput(stack);
	//				if(f>0 && tank.getFluidAmount()+f<tank.getCapacity())
	//					return true;
	//			}
	//		}
	//		return false;
	//	}


	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = nbt.getInteger("facing");
		tank.readFromNBT(nbt.getCompoundTag("tank"));
		energyStorage.readFromNBT(nbt);
		tick = nbt.getInteger("tick");
		processMaxTime = nbt.getInteger("processMaxTime");
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
		NBTTagCompound tankTag = tank.writeToNBT(new NBTTagCompound());
		nbt.setTag("tank", tankTag);
		energyStorage.writeToNBT(nbt);
		nbt.setInteger("tick", tick);
		nbt.setInteger("processMaxTime", processMaxTime);
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
		{
			if(pos!=1&&pos!=9&&pos!=11&&pos!=19)
				return null;
			return master().drain(from,resource,doDrain);
		}
		else if(resource!=null)
			return drain(from, resource.amount, doDrain);
		return null;
	}
	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		if(!formed)
			return null;
		if(master()!=null)
		{
			if(pos!=1&&pos!=9&&pos!=11&&pos!=19)
				return null;
			return master().drain(from,maxDrain,doDrain);
		}
		else
		{
			FluidStack fs = tank.drain(maxDrain, doDrain);
			markDirty();
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			return fs;
		}
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


	@Override
	public void invalidate()
	{
		super.invalidate();
		if(formed && !worldObj.isRemote)
		{
			int f = facing;
			int il = pos/9;
			int ih = (pos%9/3)-1;
			int iw = (pos%3)-1;
			int startX = xCoord-(f==4?il: f==5?-il: f==2?-iw: iw);
			int startY = yCoord-ih;
			int startZ = zCoord-(f==2?il: f==3?-il: f==5?-iw: iw);
			for(int l=0;l<3;l++)
				for(int w=-1;w<=1;w++)
					for(int h=-1;h<=1;h++)
					{
						int xx = (f==4?l: f==5?-l: f==2?-w: w);
						int yy = h;
						int zz = (f==2?l: f==3?-l: f==5?-w: w);

						ItemStack s = null;
						if(worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz) instanceof TileEntitySqueezer)
						{
							s = ((TileEntitySqueezer)worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz)).getOriginalBlock();
							((TileEntitySqueezer)worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz)).formed=false;
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
		return "IESqueezer";
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
		return slot<9?DieselHandler.findSqueezerRecipe(stack)!=null:
			(slot==9 && FluidContainerRegistry.isEmptyContainer(stack));
	}
	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		if(!formed)
			return new int[0];
		if(master()!=null)
			return master().getAccessibleSlotsFromSide(side);
		return new int[]{0,1,2,3,4,5,6,7,8,9,10,11};
	}
	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side)
	{
		if(!formed)
			return false;
		if(master()!=null)
			return master().canInsertItem(slot,stack,side);
		return isItemValidForSlot(slot,stack);
	}
	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side)
	{
		if(!formed)
			return false;
		if(master()!=null)
			return master().canExtractItem(slot,stack,side);
		return slot==10&&slot==11;
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return formed&&((pos==10 && from==ForgeDirection.DOWN)||(pos==16 && from==ForgeDirection.UP));
	}
	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		if(formed && this.master()!=null &&((pos==10 && from==ForgeDirection.DOWN)||(pos==16 && from==ForgeDirection.UP)))
		{
			TileEntitySqueezer master = master();
			int rec = master.energyStorage.receiveEnergy(maxReceive, simulate);
			master.markDirty();
			return rec;
		}
		return 0;
	}
	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		if(this.master()!=null)
			this.master().energyStorage.getEnergyStored();
		return energyStorage.getEnergyStored();
	}
	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		if(this.master()!=null)
			this.master().energyStorage.getMaxEnergyStored();
		return energyStorage.getMaxEnergyStored();
	}
}