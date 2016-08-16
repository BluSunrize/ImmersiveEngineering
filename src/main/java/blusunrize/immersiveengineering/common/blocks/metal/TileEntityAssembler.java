package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockAssembler;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.ArrayList;
import java.util.Iterator;

public class TileEntityAssembler extends TileEntityMultiblockMetal<TileEntityAssembler,IMultiblockRecipe> implements IGuiTile// IAdvancedSelectionBounds,IAdvancedCollisionBounds
{
	public boolean[] computerOn = new boolean[3];
	public TileEntityAssembler()
	{
		super(MultiblockAssembler.instance, new int[]{3,3,3}, 32000, true);
	}

	public FluidTank[] tanks = {new FluidTank(8000),new FluidTank(8000),new FluidTank(8000)};
	public ItemStack[] inventory = new ItemStack[18+3];
	public CrafterPatternInventory[] patterns = {new CrafterPatternInventory(this),new CrafterPatternInventory(this),new CrafterPatternInventory(this)};

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
		tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
		tanks[2].readFromNBT(nbt.getCompoundTag("tank2"));
		if(!descPacket)
		{
			inventory = Utils.readInventory(nbt.getTagList("inventory", 10), 18+3);
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
		nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
		nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
		nbt.setTag("tank2", tanks[2].writeToNBT(new NBTTagCompound()));
		if(!descPacket)
		{
			nbt.setTag("inventory", Utils.writeInventory(inventory));
			for(int iPattern=0; iPattern<patterns.length; iPattern++)
			{
				NBTTagList patternList = new NBTTagList();
				patterns[iPattern].writeToNBT(patternList);
				nbt.setTag("pattern"+iPattern, patternList);
			}
		}
	}
	@Override
	public void receiveMessageFromClient(NBTTagCompound message)
	{
		if (message.hasKey("buttonID"))
		{
			int id = message.getInteger("buttonID");
			if (id >= 0 && id < patterns.length)
			{
				CrafterPatternInventory pattern = patterns[id];
				for (int i = 0; i < pattern.inv.length; i++)
					pattern.inv[i] = null;
			}
		}
    }
	@Override
	public void update()
	{
		super.update();

		if(isDummy() || isRSDisabled() || worldObj.isRemote || worldObj.getTotalWorldTime()%16!=((getPos().getX()^getPos().getZ())&15))
			return;
		boolean update = false;
		ItemStack[][] outputBuffer = new ItemStack[patterns.length][0];
		for(int p=0; p<patterns.length; p++)
		{
			CrafterPatternInventory pattern = patterns[p];
			if ((controllingComputers != 0) && !computerOn[p])
			    return;
			if(pattern.inv[9] != null && canOutput(pattern.inv[9], p))
			{
				ItemStack output = pattern.inv[9].copy();
				ArrayList<ItemStack> queryList = new ArrayList<>();//List of all available inputs in the inventory
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
					ArrayList<Integer> usedOreSlots = new ArrayList<>();
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
										if(Utils.stackMatchesObject((ItemStack)query, oreInputs[iOre], true))
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
		TileEntity inventoryTile = this.worldObj.getTileEntity(getPos().offset(facing,2));
		for(int buffer=0; buffer<outputBuffer.length; buffer++)
			if(outputBuffer[buffer]!=null && outputBuffer[buffer].length>0)
				for(int iOutput=0; iOutput<outputBuffer[buffer].length; iOutput++)
				{
					ItemStack output = outputBuffer[buffer][iOutput];
					if(output!=null && output.stackSize>0)
					{
						if(!isRecipeIngredient(output, buffer) && inventoryTile!=null)
						{
							output = Utils.insertStackIntoInventory(inventoryTile, output, facing.getOpposite());
							if(output==null || output.stackSize<=0)
								continue;
						}
						int free = -1;
						if(iOutput==0)//Main recipe output
						{
							if(this.inventory[18+buffer]==null && free<0)
								free = 18+buffer;
							else if(this.inventory[18+buffer]!=null && OreDictionary.itemMatches(output, this.inventory[18+buffer], true) && this.inventory[18+buffer].stackSize+output.stackSize<=this.inventory[18+buffer].getMaxStackSize())
							{
								this.inventory[18+buffer].stackSize += output.stackSize;
								free = -1;
								break;
							}
						}
						else
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
				}
		for (int i=0;i<3;i++)
			if(!isRecipeIngredient(this.inventory[18+i], i) && inventoryTile!=null)
				this.inventory[18+i] = Utils.insertStackIntoInventory(inventoryTile, this.inventory[18+i], facing.getOpposite());
		if(update)
		{
			this.markDirty();
			this.markContainingBlockForUpdate(null);
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
					this.markContainingBlockForUpdate(null);
					return true;
				}
		for(int i=0; i<inventory.length; i++)
			if(inventory[i]!=null && Utils.stackMatchesObject(inventory[i], query, true))
			{
				int taken = Math.min(querySize, inventory[i].stackSize);
				boolean doTake = true;
				ItemStack container = ForgeHooks.getContainerItem(inventory[i]);
				if(container!=null)
				{
					if(inventory[i].stackSize-taken<=0)
					{
						inventory[i]=container;
						doTake=false;
					}
					else
						containerItems.add(container.copy());
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
							if(Utils.stackMatchesObject((ItemStack)query, oreInputs[iOre], true))
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
					if(Utils.stackMatchesObject(next, query, true))
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
	public boolean canOutput(ItemStack output, int iPattern)
	{
		if(this.inventory[18+iPattern]==null)
			return true;
		else if(OreDictionary.itemMatches(output, this.inventory[18+iPattern], true) && ItemStack.areItemStackTagsEqual(output, this.inventory[18+iPattern]) && this.inventory[18+iPattern].stackSize+output.stackSize<=this.inventory[18+iPattern].getMaxStackSize())
			return true;
		return false;
	}
	public boolean isRecipeIngredient(ItemStack stack, int slot)
	{
		if(slot-1<patterns.length)
		{
			for(int p=slot+1; p<patterns.length; p++)
			{
				CrafterPatternInventory pattern = patterns[p];
				for(int i=0; i<9; i++)
					if(pattern.inv[i]!=null && OreDictionary.itemMatches(pattern.inv[i], stack, false))
						return true;
			}
		}
		return false;
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
		if((pos%9<3 && facing==EnumFacing.SOUTH)||(pos%9>=6 && facing==EnumFacing.NORTH))
			zMin = .25f;
		else if((pos%9<3 && facing==EnumFacing.NORTH)||(pos%9>=6 && facing==EnumFacing.SOUTH))
			zMax = .75f;
		else if((pos%9<3 && facing==EnumFacing.EAST)||(pos%9>=6 && facing==EnumFacing.WEST))
			xMin = .25f;
		else if((pos%9<3 && facing==EnumFacing.WEST)||(pos%9>=6 && facing==EnumFacing.EAST))
			xMax = .75f;
		if((pos%3==0 && facing==EnumFacing.EAST)||(pos%3==2 && facing==EnumFacing.WEST))
			zMin = .1875f;
		else if((pos%3==0 && facing==EnumFacing.WEST)||(pos%3==2 && facing==EnumFacing.EAST))
			zMax = .8125f;
		else if((pos%3==0 && facing==EnumFacing.NORTH)||(pos%3==2 && facing==EnumFacing.SOUTH))
			xMin = .1875f;
		else if((pos%3==0 && facing==EnumFacing.SOUTH)||(pos%3==2 && facing==EnumFacing.NORTH))
			xMax = .8125f;
		return new float[]{xMin,yMin,zMin, xMax,yMax,zMax};
	}

	@Override
	public int[] getEnergyPos()
	{
		return new int[]{22};
	}
	@Override
	public int[] getRedstonePos()
	{
		return new int[]{3, 5};
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return false;
	}
	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<IMultiblockRecipe> process)
	{
		return false;
	}
	@Override
	public void doProcessOutput(ItemStack output)
	{
		BlockPos pos = getPos().offset(facing,-1);
		TileEntity inventoryTile = this.worldObj.getTileEntity(pos);
		if(inventoryTile!=null)
			output = Utils.insertStackIntoInventory(inventoryTile, output, facing.getOpposite());
		if(output!=null)
			Utils.dropStackAtPos(worldObj, pos, output, facing);
	}
	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}
	@Override
	public void onProcessFinish(MultiblockProcess<IMultiblockRecipe> process)
	{
	}
	@Override
	public int getMaxProcessPerTick()
	{
		return 0;
	}
	@Override
	public int getProcessQueueMaxLength()
	{
		return 0;
	}
	@Override
	public float getMinProcessDistance(MultiblockProcess<IMultiblockRecipe> process)
	{
		return 0;
	}


	@Override
	public ItemStack[] getInventory()
	{
		return this.inventory;
	}
	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return true;
	}
	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}
	@Override
	public int[] getOutputSlots()
	{
		return new int[0];
	}
	@Override
	public int[] getOutputTanks()
	{
		return new int[0];
	}
	@Override
	public FluidTank[] getInternalTanks()
	{
		return this.tanks;
	}
	@Override
	public void doGraphicalUpdates(int slot)
	{
		this.markDirty();
		this.markContainingBlockForUpdate(null);
	}


	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if((pos==10||pos==16)&&capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return (pos==10&&facing==this.facing.getOpposite())||(pos==16&&facing==this.facing);
		return super.hasCapability(capability, facing);
	}
	IItemHandler insertionHandler = new IEInventoryHandler(18, this, 0, true, false);
	IItemHandler extractionHandler = new IEInventoryHandler(3, this, 18, false, true);
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if((pos==10||pos==16)&&capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			TileEntityAssembler master = master();
			if(master==null)
				return null;
			if(pos==10&&facing==this.facing.getOpposite())
				return (T)master.insertionHandler;
			if(pos==16&&facing==this.facing)
				return (T)master.extractionHandler;
			return null;
		}
		return super.getCapability(capability, facing);
	}


	@Override
	public IMultiblockRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return null;
	}
	@Override
	protected IMultiblockRecipe readRecipeFromNBT(NBTTagCompound tag)
	{
		return null;
	}
	

	@Override
	public boolean canOpenGui()
	{
		return formed;
	}
	@Override
	public int getGuiID()
	{
		return Lib.GUIID_Assembler;
	}
	@Override
	public TileEntity getGuiMaster()
	{
		return master();
	}



	@Override
	protected FluidTank[] getAccessibleFluidTanks(EnumFacing side)
	{
		TileEntityAssembler master = master();
		if(master!=null && pos==1&&(side==null||side==facing.getOpposite()))
			return master.tanks;
		return new FluidTank[0];
	}
	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource)
	{
		return true;
	}
	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side)
	{
		return true;
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
			if(slot<9)
			{
				inv[slot] = stack;
				if (stack != null && stack.stackSize > getInventoryStackLimit())
					stack.stackSize = getInventoryStackLimit();
			}
			recalculateOutput();
		}
		@Override
		public void clear()
		{
			for(int i=0; i<this.inv.length; i++)
				this.inv[i] = null;
		}

		public void recalculateOutput()
		{
			InventoryCrafting invC = new Utils.InventoryCraftingFalse(3, 3);
			for(int j=0; j<9; j++)
				invC.setInventorySlotContents(j, inv[j]);
			this.recipe = Utils.findRecipe(invC, tile.getWorld());
			this.inv[9] = recipe!=null?recipe.getCraftingResult(invC):null;
		}
		public ArrayList<ItemStack> getTotalPossibleOutputs()
		{
			ArrayList<ItemStack> outputList = new ArrayList<ItemStack>();
			outputList.add(inv[9].copy());
			for(int i=0; i<9; i++)
			{
				if(FluidContainerRegistry.getFluidForFilledItem(inv[i])!=null)
				{
					FluidStack fs = FluidContainerRegistry.getFluidForFilledItem(inv[i]);
					boolean hasFluid = false;
					for(FluidTank tank : tile.tanks)
						if(tank.getFluid()!=null && tank.getFluid().containsFluid(fs))
						{
							hasFluid=true;
							break;
						}
					if(hasFluid)
						continue;
				}
				//				ItemStack container = inv[i].getItem().getContainerItem(inv[i]);
				//				if(container!=null && inv[i].getItem().doesContainerItemLeaveCraftingGrid(inv[i]))
				//					outputList.add(container.copy());
			}
			InventoryCrafting invC = new Utils.InventoryCraftingFalse(3, 3);
			for(int j=0; j<9; j++)
				invC.setInventorySlotContents(j, inv[j]);
			for(ItemStack ss : this.recipe.getRemainingItems(invC))
				if(ss!=null)
					outputList.add(ss);
			return outputList;
		}
		@Override
		public String getName()
		{
			return "IECrafterPattern";
		}
		@Override
		public boolean hasCustomName()
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

		@Override
		public ITextComponent getDisplayName()
		{
			return new TextComponentString(this.getName());
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
}