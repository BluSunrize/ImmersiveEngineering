package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockAssembler;
import blusunrize.immersiveengineering.common.util.Utils;
import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityAssembler extends TileEntityMultiblockPart implements ISidedInventory, IEnergyReceiver, IFluidHandler
{
	public int facing = 2;
	public EnergyStorage energyStorage = new EnergyStorage(16000);
	public ItemStack[] inventory = new ItemStack[18];
	public CrafterPatternInventory[] patterns = {new CrafterPatternInventory(this),new CrafterPatternInventory(this),new CrafterPatternInventory(this)};
	public FluidTank[] tanks = {new FluidTank(8000),new FluidTank(8000),new FluidTank(8000)};


	@Override
	public TileEntityAssembler master()
	{
		if(offset[0]==0&&offset[1]==0&&offset[2]==0)
			return null;
		TileEntity te = worldObj.getTileEntity(xCoord-offset[0], yCoord-offset[1], zCoord-offset[2]);
		return te instanceof TileEntityAssembler?(TileEntityAssembler)te : null;
	}

	@Override
	public ItemStack getOriginalBlock()
	{
		if(pos<0)
			return null;
		ItemStack s = MultiblockAssembler.instance.getStructureManual() [pos/9][pos%9/3][pos%3];
		return s!=null?s.copy():null;
	}
	@Override
	public void updateEntity()
	{
		if(!formed || pos!=13)
			return;

		if(worldObj.isRemote || worldObj.getTotalWorldTime()%16!=((xCoord^zCoord)&15))
			return;

		boolean update = false;
		ItemStack[][] outputBuffer = new ItemStack[patterns.length][0];
		for(int p=0; p<patterns.length; p++)
		{
			CrafterPatternInventory pattern = patterns[p];
			if(pattern.inv[9]!=null)
			{
				ItemStack output = pattern.inv[9].copy();
				ArrayList<ItemStack> queryList = new ArrayList();//List of all available inputs in the inventory
				for(ItemStack[] bufferedStacks : outputBuffer)
					for(ItemStack stack : bufferedStacks)
						if(stack!=null)
							queryList.add(stack.copy());
				for(ItemStack stack : this.inventory)
					if(stack!=null)
						queryList.add(stack.copy());

				int consumed = Config.getInt("assembler_consumption");
				if(this.hasIngredients(pattern, queryList) && this.energyStorage.extractEnergy(consumed, true)==consumed)
				{
					this.energyStorage.extractEnergy(consumed, false);
					ArrayList<ItemStack> outputList = new ArrayList<ItemStack>();//List of all outputs for the current recipe. This includes discarded containers
					outputList.add(output);

					Object[] oreInputs = null;
					ArrayList<Integer> usedOreSlots = new ArrayList();
					if(pattern.recipe instanceof ShapedOreRecipe||pattern.recipe instanceof ShapelessOreRecipe)
					{
						oreInputs = pattern.recipe instanceof ShapedOreRecipe?((ShapedOreRecipe)pattern.recipe).getInput():((ShapelessOreRecipe)pattern.recipe).getInput().toArray();
					}
					for(int i=0; i<9; i++)
						if(pattern.inv[i]!=null)
						{
							Object query = pattern.inv[i].copy();
							int querySize = pattern.inv[i].stackSize;
							if(FluidContainerRegistry.getFluidForFilledItem(pattern.inv[i])!=null)
							{
								FluidStack fs = FluidContainerRegistry.getFluidForFilledItem(pattern.inv[i]);
								fs.amount *= querySize;
								boolean hasFluid = false;
								for(FluidTank tank : tanks)
									if(tank.getFluid()!=null && tank.getFluid().containsFluid(fs))
									{
										hasFluid=true;
										break;
									}
								if(hasFluid)
									query = fs;
							}

							if(query instanceof ItemStack && oreInputs!=null)
								for(int iOre=0; iOre<oreInputs.length; iOre++)
									if(!usedOreSlots.contains(Integer.valueOf(iOre)))
										if(Utils.stackMatchesObject((ItemStack)query, oreInputs[iOre]))
										{
											query = oreInputs[iOre];
											querySize = 1;
											break;
										}
							boolean taken = false;
							for(int j=0; j<outputBuffer.length; j++)
							{
								if(taken = consumeItem(query, querySize, outputBuffer[j], outputList))
									break;
							}
							if(!taken)
								this.consumeItem(query, querySize, inventory, outputList);
						}
					outputBuffer[p]=outputList.toArray(new ItemStack[outputList.size()]);
					update = true;
				}
			}
		}
		TileEntity inventory = this.worldObj.getTileEntity(xCoord+(facing==4?2:facing==5?-2:0),yCoord,zCoord+(facing==2?2:facing==3?-2:0));
		for(int buffer=0; buffer<outputBuffer.length; buffer++)
			if(outputBuffer[buffer]!=null && outputBuffer[buffer].length>0)
				for(ItemStack output : outputBuffer[buffer])
					if(output!=null && output.stackSize>0)	
					{
						boolean isRecipeIngredient = false;
						if(buffer-1<outputBuffer.length)
						{
							for(int p=buffer+1; p<patterns.length; p++)
							{
								CrafterPatternInventory pattern = patterns[p];
								for(int i=0; i<9; i++)
									if(pattern.inv[i]!=null && OreDictionary.itemMatches(pattern.inv[i], output, false))
									{
										isRecipeIngredient=true;
										break;
									}
							}

						}

						if(!isRecipeIngredient)
							if((inventory instanceof ISidedInventory && ((ISidedInventory)inventory).getAccessibleSlotsFromSide(facing).length>0)
									||(inventory instanceof IInventory && ((IInventory)inventory).getSizeInventory()>0))
							{
								output = Utils.insertStackIntoInventory((IInventory)inventory, output, facing);
								if(output==null || output.stackSize<=0)
									continue;
							}

						int free = -1;
						for(int i=0; i<this.inventory.length; i++)
						{
							if(this.inventory[i]==null && free<0)
								free = i;
							else if(this.inventory[i]!=null && OreDictionary.itemMatches(output, this.inventory[i], true) && this.inventory[i].stackSize+output.stackSize<=this.inventory[i].getMaxStackSize())
							{
								this.inventory[i].stackSize += output.stackSize;
								free = -1;
								break;
							}
						}
						if(free>=0)
							this.inventory[free] = output.copy();
					}

		if(update)
		{
			this.markDirty();
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	public boolean consumeItem(Object query, int querySize, ItemStack[] inventory, ArrayList<ItemStack> containerItems)
	{
		if(query instanceof FluidStack)
			for(FluidTank tank : tanks)
				if(tank.getFluid()!=null && tank.getFluid().containsFluid((FluidStack)query))
				{
					tank.drain(((FluidStack)query).amount, true);
					markDirty();
					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
					return true;
				}

		for(int i=0; i<inventory.length; i++)
			if(inventory[i]!=null && Utils.stackMatchesObject(inventory[i], query))
			{
				int taken = Math.min(querySize, inventory[i].stackSize);
				boolean doTake = true;
				if(inventory[i].getItem().hasContainerItem(inventory[i]))
				{
					ItemStack container = inventory[i].getItem().getContainerItem(inventory[i]);
					if(container!=null && inventory[i].getItem().doesContainerItemLeaveCraftingGrid(inventory[i]))
					{
						containerItems.add(container.copy());
						if(inventory[i].stackSize-taken<=0)
						{
							inventory[i]=null;
							doTake=false;
						}
					}
					else if(inventory[i].stackSize-taken<=0)
					{
						inventory[i] = container;
						doTake=false;
					}
				}
				if(doTake)
				{
					inventory[i].stackSize -= taken;
					if(inventory[i].stackSize<=0)
						inventory[i]=null;
				}
				querySize -= taken;
				if(querySize<=0)
					break;
			}
		return query==null || querySize<=0;
	}
	public boolean hasIngredients(CrafterPatternInventory pattern, ArrayList<ItemStack> queryList)
	{
		Object[] oreInputs = null;
		ArrayList<Integer> usedOreSlots = new ArrayList();
		if(pattern.recipe instanceof ShapedOreRecipe||pattern.recipe instanceof ShapelessOreRecipe)
		{
			oreInputs = pattern.recipe instanceof ShapedOreRecipe?((ShapedOreRecipe)pattern.recipe).getInput():((ShapelessOreRecipe)pattern.recipe).getInput().toArray();
		}
		boolean match = true;
		for(int i=0; i<9; i++)
			if(pattern.inv[i]!=null)
			{
				if(FluidContainerRegistry.getFluidForFilledItem(pattern.inv[i])!=null)
				{
					FluidStack fs = FluidContainerRegistry.getFluidForFilledItem(pattern.inv[i]);
					boolean hasFluid = false;
					for(FluidTank tank : tanks)
						if(tank.getFluid()!=null && tank.getFluid().containsFluid(fs))
						{
							hasFluid=true;
							break;
						}
					if(hasFluid)
						continue;
				}

				Object query = pattern.inv[i].copy();
				int querySize = pattern.inv[i].stackSize;
				if(oreInputs!=null)
				{
					for(int iOre=0; iOre<oreInputs.length; iOre++)
						if(!usedOreSlots.contains(Integer.valueOf(iOre)))
							if(Utils.stackMatchesObject((ItemStack)query, oreInputs[iOre]))
							{
								query = oreInputs[iOre];
								querySize = 1;
								break;
							}
				}

				Iterator<ItemStack> it = queryList.iterator();
				while(it.hasNext())
				{
					ItemStack next = it.next();
					if(Utils.stackMatchesObject(next, query))
					{
						int taken = Math.min(querySize, next.stackSize);
						next.stackSize -= taken;
						if(next.stackSize<=0)
							it.remove();
						querySize -= taken;
						if(querySize<=0)
							break;
					}
				}
				if(querySize>0)
				{
					match = false;
					break;
				}
			}
		return match;
	}


	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = nbt.getInteger("facing");
		energyStorage.readFromNBT(nbt);

		tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
		tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
		tanks[2].readFromNBT(nbt.getCompoundTag("tank2"));

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
			for(int iPattern=0; iPattern<patterns.length; iPattern++)
			{
				NBTTagList patternList = nbt.getTagList("pattern"+iPattern, 10);
				patterns[iPattern] = new CrafterPatternInventory(this);
				patterns[iPattern].readFromNBT(patternList);
			}
		}
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", facing);
		energyStorage.writeToNBT(nbt);

		NBTTagCompound tankTag0 = tanks[0].writeToNBT(new NBTTagCompound());
		nbt.setTag("tank0", tankTag0);
		NBTTagCompound tankTag1 = tanks[1].writeToNBT(new NBTTagCompound());
		nbt.setTag("tank1", tankTag1);
		NBTTagCompound tankTag2 = tanks[2].writeToNBT(new NBTTagCompound());
		nbt.setTag("tank2", tankTag2);

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
			for(int iPattern=0; iPattern<patterns.length; iPattern++)
			{
				NBTTagList patternList = new NBTTagList();
				patterns[iPattern].writeToNBT(patternList);
				nbt.setTag("pattern"+iPattern, patternList);
			}
		}
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(pos==4)
			return AxisAlignedBB.getBoundingBox(xCoord-1,yCoord,zCoord-1, xCoord+2,yCoord+3,zCoord+2);
		return AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord, xCoord,yCoord,zCoord);
	}
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return super.getMaxRenderDistanceSquared()*Config.getDouble("increasedTileRenderdistance");
	}
	@Override
	public float[] getBlockBounds()
	{
		if(pos<9 || pos==10||pos==13||pos==16 || pos==22)
			return new float[]{0,0,0,1,1,1};

		float xMin = 0;
		float yMin = 0;
		float zMin = 0;
		float xMax = 1;
		float yMax = 1;
		float zMax = 1;

		if((pos%9<3 && facing==2)||(pos%9>=6 && facing==3))
			zMin = .25f;
		else if((pos%9<3 && facing==3)||(pos%9>=6 && facing==2))
			zMax = .75f;
		else if((pos%9<3 && facing==4)||(pos%9>=6 && facing==5))
			xMin = .25f;
		else if((pos%9<3 && facing==5)||(pos%9>=6 && facing==4))
			xMax = .75f;

		if((pos%3==0 && facing==4)||(pos%3==2 && facing==5))
			zMin = .1875f;
		else if((pos%3==0 && facing==5)||(pos%3==2 && facing==4))
			zMax = .8125f;
		else if((pos%3==0 && facing==3)||(pos%3==2 && facing==2))
			xMin = .1875f;
		else if((pos%3==0 && facing==2)||(pos%3==2 && facing==3))
			xMax = .8125f;

		return new float[]{xMin,yMin,zMin, xMax,yMax,zMax};
	}

	@Override
	public void invalidate()
	{
		super.invalidate();

		if(formed && !worldObj.isRemote)
		{
			//			int f = facing;
			TileEntity master = master();
			if(master==null)
				master = this;

			int startX = master.xCoord;
			int startY = master.yCoord;
			int startZ = master.zCoord;

			for(int yy=-1;yy<=1;yy++)
				for(int zz=-1;zz<=1;zz++)
					for(int xx=-1;xx<=1;xx++)
					{
						ItemStack s = null;
						TileEntity te = worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz);
						if(te instanceof TileEntityAssembler)
						{
							s = ((TileEntityAssembler)te).getOriginalBlock();
							((TileEntityAssembler)te).formed=false;
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
							TileEntity tile = worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz);
							if(tile instanceof TileEntityStructuralArm)
								((TileEntityStructuralArm)tile).facing = facing<4?(xx==-1?4:5):(zz==-1?2:3);
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
		this.markDirty();
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
		if(!formed||worldObj.isRemote)
			return;
		if(master()!=null)
		{
			master().setInventorySlotContents(slot,stack);
			return;
		}
		inventory[slot] = stack;
		if (stack != null && stack.stackSize > getInventoryStackLimit())
			stack.stackSize = getInventoryStackLimit();
		this.markDirty();
	}
	@Override
	public String getInventoryName()
	{
		return "IEAssembler";
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
		if(!formed||stack==null)
			return true;
		if(master()!=null)
			return master().isItemValidForSlot(slot,stack);
		return true;
	}
	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		if(!formed)
			return new int[0];
		if(pos==10||pos==16)
			return new int[]{0,1,2,3,4,5,6,7,8, 9,10,11,12,13,14,15,16,17};
		return new int[0];
	}
	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side)
	{
		if(!formed)
			return true;
		if(master()!=null)
			return master().canInsertItem(slot,stack,side);

		return isItemValidForSlot(slot,stack);
	}
	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side)
	{
		if(!formed)
			return true;
		if(master()!=null)
			return master().canExtractItem(slot,stack,side);
		return slot>=16&&slot<=22;
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return formed && pos==22 && from==ForgeDirection.UP;
	}
	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		if(formed && pos==22 && from==ForgeDirection.UP && this.master()!=null)
		{
			TileEntityAssembler master = master();
			int rec = master.energyStorage.receiveEnergy(maxReceive, simulate);
			master.markDirty();
			if(rec>0)
				worldObj.markBlockForUpdate(master().xCoord, master().yCoord, master().zCoord);
			return rec;
		}
		return 0;
	}
	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		if(this.master()!=null)
			return this.master().energyStorage.getEnergyStored();
		return energyStorage.getEnergyStored();
	}
	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		if(this.master()!=null)
			return this.master().energyStorage.getMaxEnergyStored();
		return energyStorage.getMaxEnergyStored();
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if(resource==null)
			return 0;
		if(master()!=null)
			return master().fill(from, resource, doFill);
		int fill = -1;
		for(FluidTank tank : tanks)
			if(tank.getFluid()!=null && tank.getFluid().isFluidEqual(resource))
			{
				fill = tank.fill(resource, doFill);
				break;
			}
		if(fill==-1)
			for(FluidTank tank : tanks)
			{
				fill = tank.fill(resource, doFill);
				if(fill>0)
					break;
			}
		if(fill>0)
		{
			markDirty();
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
		return fill<0?0:fill;
	}
	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return null;
	}
	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}
	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return formed && pos==1 && from==ForgeDirection.getOrientation(facing).getOpposite();
	}
	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return false;
	}
	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		if(this.master()!=null)
			return this.master().getTankInfo(from);
		return new FluidTankInfo[]{tanks[0].getInfo(),tanks[1].getInfo(),tanks[2].getInfo()};
	}

	public static class CrafterPatternInventory implements IInventory
	{
		public ItemStack[] inv = new ItemStack[10];
		public IRecipe recipe;
		final TileEntityAssembler tile;
		public CrafterPatternInventory(TileEntityAssembler tile)
		{
			this.tile = tile;
		}

		@Override
		public int getSizeInventory()
		{
			return 10;
		}
		@Override
		public ItemStack getStackInSlot(int slot)
		{
			return inv[slot];
		}

		@Override
		public ItemStack decrStackSize(int slot, int amount)
		{
			ItemStack stack = getStackInSlot(slot);

			if(slot<9 && stack != null)
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
			if(slot<9)
			{
				inv[slot] = stack;
				if (stack != null && stack.stackSize > getInventoryStackLimit())
					stack.stackSize = getInventoryStackLimit();
			}
			recalculateOutput();
		}

		public void recalculateOutput()
		{
			InventoryCrafting invC = new Utils.InventoryCraftingFalse(3, 3);
			for(int j=0; j<9; j++)
				invC.setInventorySlotContents(j, inv[j]);
			this.recipe = Utils.findRecipe(invC, tile.getWorldObj());
			this.inv[9] = recipe!=null?recipe.getCraftingResult(invC):null;
		}

		@Override
		public String getInventoryName()
		{
			return "IECrafterPattern";
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
			for(int i=0; i<this.inv.length; i++)
				if(this.inv[i] != null)
				{
					NBTTagCompound itemTag = new NBTTagCompound();
					itemTag.setByte("Slot", (byte)i);
					this.inv[i].writeToNBT(itemTag);
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
					this.inv[slot] = ItemStack.loadItemStackFromNBT(itemTag);
			}
			recalculateOutput();
		}
	}
}