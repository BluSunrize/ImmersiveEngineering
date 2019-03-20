/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.tool.AssemblerHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorAttachable;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockAssembler;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;

public class TileEntityAssembler extends TileEntityMultiblockMetal<TileEntityAssembler, IMultiblockRecipe> implements IGuiTile, IConveyorAttachable// IAdvancedSelectionBounds,IAdvancedCollisionBounds
{
	public boolean[] computerOn = new boolean[3];
	public boolean isComputerControlled = false;

	public TileEntityAssembler()
	{
		super(MultiblockAssembler.instance, new int[]{3, 3, 3}, 32000, true);
	}

	public FluidTank[] tanks = {new FluidTank(8000), new FluidTank(8000), new FluidTank(8000)};
	public NonNullList<ItemStack> inventory = NonNullList.withSize(18+3, ItemStack.EMPTY);
	public CrafterPatternInventory[] patterns = {new CrafterPatternInventory(this), new CrafterPatternInventory(this), new CrafterPatternInventory(this)};
	public boolean recursiveIngredients = false;

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
		tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
		tanks[2].readFromNBT(nbt.getCompoundTag("tank2"));
		recursiveIngredients = nbt.getBoolean("recursiveIngredients");
		if(!descPacket)
		{
			inventory = Utils.readInventory(nbt.getTagList("inventory", 10), 18+3);
			for(int iPattern = 0; iPattern < patterns.length; iPattern++)
			{
				NBTTagList patternList = nbt.getTagList("pattern"+iPattern, 10);
				patterns[iPattern] = new CrafterPatternInventory(this);
				patterns[iPattern].readFromNBT(patternList);
			}
		}
		{
			byte cOn = nbt.getByte("computerControlled");
			isComputerControlled = (cOn&1)!=0;
			if(isComputerControlled)
			{
				for(int i = 0; i < 3; i++)
					computerOn[i] = (cOn&(2<<i))!=0;
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
		nbt.setBoolean("recursiveIngredients", recursiveIngredients);
		if(!descPacket)
		{
			nbt.setTag("inventory", Utils.writeInventory(inventory));
			for(int iPattern = 0; iPattern < patterns.length; iPattern++)
			{
				NBTTagList patternList = new NBTTagList();
				patterns[iPattern].writeToNBT(patternList);
				nbt.setTag("pattern"+iPattern, patternList);
			}
		}
		if(isComputerControlled)
		{
			byte cOn = 1;
			for(int i = 0; i < 3; i++)
				if(computerOn[i])
					cOn |= 2<<i;
			nbt.setByte("computerControlled", cOn);
		}
	}

	@Override
	public void receiveMessageFromClient(NBTTagCompound message)
	{
		if(message.hasKey("buttonID"))
		{
			int id = message.getInteger("buttonID");
			if(id >= 0&&id < patterns.length)
			{
				CrafterPatternInventory pattern = patterns[id];
				for(int i = 0; i < pattern.inv.size(); i++)
					pattern.inv.set(i, ItemStack.EMPTY);
			}
			else if(id==3)
			{
				recursiveIngredients = !recursiveIngredients;
			}
		}
		else if(message.hasKey("patternSync"))
		{
			int r = message.getInteger("recipe");
			NBTTagList list = message.getTagList("patternSync", 10);
			CrafterPatternInventory pattern = patterns[r];
			for(int i = 0; i < list.tagCount(); i++)
			{
				NBTTagCompound itemTag = list.getCompoundTagAt(i);
				pattern.inv.set(itemTag.getInteger("slot"), new ItemStack(itemTag));
			}
		}
	}

	@Override
	public void update()
	{
		super.update();

		if(isDummy()||isRSDisabled()||world.isRemote||world.getTotalWorldTime()%16!=((getPos().getX()^getPos().getZ())&15))
			return;
		boolean update = false;
		NonNullList<ItemStack>[] outputBuffer = new NonNullList[patterns.length];
		for(int p = 0; p < patterns.length; p++)
		{
			CrafterPatternInventory pattern = patterns[p];
			if(isComputerControlled&&!computerOn[p])
				continue;
			if(!pattern.inv.get(9).isEmpty()&&canOutput(pattern.inv.get(9), p))
			{
				ItemStack output = pattern.inv.get(9).copy();
				ArrayList<ItemStack> availableStacks = new ArrayList<>();//List of all available inputs in the inventory
				for(NonNullList<ItemStack> bufferedStacks : outputBuffer)
					if(bufferedStacks!=null)
						for(ItemStack stack : bufferedStacks)
							if(!stack.isEmpty())
								availableStacks.add(stack);
				for(ItemStack stack : this.inventory)
					if(!stack.isEmpty())
						availableStacks.add(stack);
				int consumed = IEConfig.Machines.assembler_consumption;

				AssemblerHandler.IRecipeAdapter adapter = AssemblerHandler.findAdapter(pattern.recipe);
				AssemblerHandler.RecipeQuery[] queries = adapter.getQueriedInputs(pattern.recipe, pattern.inv);
				if(queries==null)
					continue;
				if(this.energyStorage.extractEnergy(consumed, true)==consumed&&this.consumeIngredients(queries, availableStacks, false, null))
				{
					this.energyStorage.extractEnergy(consumed, false);
					NonNullList<ItemStack> outputList = NonNullList.create();//List of all outputs for the current recipe. This includes discarded containers
					outputList.add(output);

					NonNullList<ItemStack> gridItems = NonNullList.withSize(9, ItemStack.EMPTY);
					this.consumeIngredients(queries, availableStacks, true, gridItems);

					NonNullList<ItemStack> remainingItems = pattern.recipe.getRemainingItems(Utils.InventoryCraftingFalse.createFilledCraftingInventory(3, 3, gridItems));
					for(ItemStack rem : remainingItems)
						if(!rem.isEmpty())
							outputList.add(rem);

					outputBuffer[p] = outputList;
					update = true;
				}
			}
		}
		BlockPos outputPos = getPos().offset(facing, 2);
		TileEntity inventoryTile = Utils.getExistingTileEntity(world, outputPos);
		for(int buffer = 0; buffer < outputBuffer.length; buffer++)
			if(outputBuffer[buffer]!=null&&outputBuffer[buffer].size() > 0)
				for(int iOutput = 0; iOutput < outputBuffer[buffer].size(); iOutput++)
				{
					ItemStack output = outputBuffer[buffer].get(iOutput);
					if(!output.isEmpty()&&output.getCount() > 0)
					{
						if(!isRecipeIngredient(output, buffer)&&inventoryTile!=null)
						{
							output = Utils.insertStackIntoInventory(inventoryTile, output, facing.getOpposite());
							if(output.isEmpty()||output.getCount() <= 0)
								continue;
						}
						int free = -1;
						if(iOutput==0)//Main recipe output
						{
							if(this.inventory.get(18+buffer).isEmpty()&&free < 0)
								free = 18+buffer;
							else if(!this.inventory.get(18+buffer).isEmpty()&&OreDictionary.itemMatches(output, this.inventory.get(18+buffer), true)&&this.inventory.get(18+buffer).getCount()+output.getCount() <= this.inventory.get(18+buffer).getMaxStackSize())
							{
								this.inventory.get(18+buffer).grow(output.getCount());
								free = -1;
								continue;
							}
						}
						else
							for(int i = 0; i < this.inventory.size(); i++)
							{
								if(this.inventory.get(i).isEmpty()&&free < 0)
									free = i;
								else if(!this.inventory.get(i).isEmpty()&&OreDictionary.itemMatches(output, this.inventory.get(i), true)&&this.inventory.get(i).getCount()+output.getCount() <= this.inventory.get(i).getMaxStackSize())
								{
									this.inventory.get(i).grow(output.getCount());
									free = -1;
									break;
								}
							}
						if(free >= 0)
							this.inventory.set(free, output.copy());
					}
				}
		for(int i = 0; i < 3; i++)
			if(!isRecipeIngredient(this.inventory.get(18+i), i)&&inventoryTile!=null)
				this.inventory.set(18+i, Utils.insertStackIntoInventory(inventoryTile, this.inventory.get(18+i), facing.getOpposite()));

		if(update)
		{
			this.markDirty();
			this.markContainingBlockForUpdate(null);
		}
	}

	public boolean consumeIngredients(AssemblerHandler.RecipeQuery[] queries, ArrayList<ItemStack> itemStacks, boolean doConsume, @Nullable NonNullList<ItemStack> gridItems)
	{
		if(!doConsume)
		{
			ArrayList<ItemStack> dupeList = new ArrayList<>(itemStacks.size());
			for(ItemStack stack : itemStacks)
				dupeList.add(stack.copy());
			itemStacks = dupeList;
		}
		for(int i = 0; i < queries.length; i++)
		{
			AssemblerHandler.RecipeQuery recipeQuery = queries[i];
			if(recipeQuery!=null&&recipeQuery.query!=null)
			{
				FluidStack fs = recipeQuery.query instanceof FluidStack?(FluidStack)recipeQuery.query: (recipeQuery.query instanceof IngredientStack&&((IngredientStack)recipeQuery.query).fluid!=null)?((IngredientStack)recipeQuery.query).fluid: null;
				int querySize = recipeQuery.querySize;
				if(fs!=null)
				{
					boolean hasFluid = false;
					for(FluidTank tank : tanks)
						if(tank.getFluid()!=null&&tank.getFluid().containsFluid(fs))
						{
							hasFluid = true;
							if(doConsume)
								tank.drain(fs.amount, true);
							break;
						}
					if(hasFluid)
						continue;
					else
						querySize = 1;
				}
				Iterator<ItemStack> it = itemStacks.iterator();
				while(it.hasNext())
				{
					ItemStack next = it.next();
					if(!next.isEmpty()&&ApiUtils.stackMatchesObject(next, recipeQuery.query, true))
					{
						int taken = Math.min(querySize, next.getCount());
						ItemStack forGrid = next.splitStack(taken);
						if(gridItems!=null)
							gridItems.set(i, forGrid);
						if(next.getCount() <= 0)
							it.remove();
						querySize -= taken;
						if(querySize <= 0)
							break;
					}
				}
				if(querySize > 0)
					return false;
			}
		}
		return true;
	}

	public boolean canOutput(ItemStack output, int iPattern)
	{
		if(this.inventory.get(18+iPattern).isEmpty())
			return true;
		else
			return OreDictionary.itemMatches(output, this.inventory.get(18+iPattern), true)&&Utils.compareItemNBT(output, this.inventory.get(18+iPattern))&&this.inventory.get(18+iPattern).getCount()+output.getCount() <= this.inventory.get(18+iPattern).getMaxStackSize();
	}

	public boolean isRecipeIngredient(ItemStack stack, int slot)
	{
		if(stack.isEmpty())
			return false;
		if(slot-1 < patterns.length||recursiveIngredients)
			for(int p = recursiveIngredients?0: slot; p < patterns.length; p++)
			{
				CrafterPatternInventory pattern = patterns[p];
				for(int i = 0; i < 9; i++)
					if(!pattern.inv.get(i).isEmpty())
					{
						if(OreDictionary.itemMatches(pattern.inv.get(i), stack, false))
							return true;
						else if(pattern.inv.get(i).getItem()==stack.getItem()&&!pattern.inv.get(i).getHasSubtypes()&&pattern.inv.get(i).isItemStackDamageable())
							return true;
					}
			}
		return false;
	}

	@Override
	public float[] getBlockBounds()
	{
		if(pos < 9||pos==10||pos==13||pos==16||pos==22)
			return new float[]{0, 0, 0, 1, 1, 1};
		float xMin = 0;
		float yMin = 0;
		float zMin = 0;
		float xMax = 1;
		float yMax = 1;
		float zMax = 1;
		if((pos%9 < 3&&facing==EnumFacing.SOUTH)||(pos%9 >= 6&&facing==EnumFacing.NORTH))
			zMin = .25f;
		else if((pos%9 < 3&&facing==EnumFacing.NORTH)||(pos%9 >= 6&&facing==EnumFacing.SOUTH))
			zMax = .75f;
		else if((pos%9 < 3&&facing==EnumFacing.EAST)||(pos%9 >= 6&&facing==EnumFacing.WEST))
			xMin = .25f;
		else if((pos%9 < 3&&facing==EnumFacing.WEST)||(pos%9 >= 6&&facing==EnumFacing.EAST))
			xMax = .75f;
		if((pos%3==0&&facing==EnumFacing.EAST)||(pos%3==2&&facing==EnumFacing.WEST))
			zMin = .1875f;
		else if((pos%3==0&&facing==EnumFacing.WEST)||(pos%3==2&&facing==EnumFacing.EAST))
			zMax = .8125f;
		else if((pos%3==0&&facing==EnumFacing.NORTH)||(pos%3==2&&facing==EnumFacing.SOUTH))
			xMin = .1875f;
		else if((pos%3==0&&facing==EnumFacing.SOUTH)||(pos%3==2&&facing==EnumFacing.NORTH))
			xMax = .8125f;
		return new float[]{xMin, yMin, zMin, xMax, yMax, zMax};
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
	public void replaceStructureBlock(BlockPos pos, IBlockState state, ItemStack stack, int h, int l, int w)
	{
		super.replaceStructureBlock(pos, state, stack, h, l, w);
		if(h==1&&w==1&&l!=1)
		{
			TileEntity tile = world.getTileEntity(pos);
			if(tile instanceof TileEntityConveyorBelt)
				((TileEntityConveyorBelt)tile).setFacing(this.facing);
		}
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
		BlockPos pos = getPos().offset(facing, -1);
		TileEntity inventoryTile = this.world.getTileEntity(pos);
		if(inventoryTile!=null)
			output = Utils.insertStackIntoInventory(inventoryTile, output, facing.getOpposite());
		if(!output.isEmpty())
			Utils.dropStackAtPos(world, pos, output, facing);
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
	public NonNullList<ItemStack> getInventory()
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
	public IFluidTank[] getInternalTanks()
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
			return ((pos==10&&facing==this.facing.getOpposite())||(pos==16&&facing==this.facing))&&master()!=null;
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
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side)
	{
		TileEntityAssembler master = master();
		if(master!=null&&pos==1&&(side==null||side==facing.getOpposite()))
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

	@Override
	public EnumFacing[] sigOutputDirections()
	{
		if(pos==16)
			return new EnumFacing[]{this.facing};
		return new EnumFacing[0];
	}

	public static class CrafterPatternInventory implements IInventory
	{
		public NonNullList<ItemStack> inv = NonNullList.withSize(10, ItemStack.EMPTY);
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
		public boolean isEmpty()
		{
			for(ItemStack stack : inv)
			{
				if(!stack.isEmpty())
					return false;
			}
			return true;
		}

		@Override
		public ItemStack getStackInSlot(int slot)
		{
			return inv.get(slot);
		}

		@Override
		public ItemStack decrStackSize(int slot, int amount)
		{
			ItemStack stack = getStackInSlot(slot);
			if(slot < 9&&!stack.isEmpty())
				if(stack.getCount() <= amount)
					setInventorySlotContents(slot, ItemStack.EMPTY);
				else
				{
					stack = stack.splitStack(amount);
					if(stack.getCount()==0)
						setInventorySlotContents(slot, ItemStack.EMPTY);
				}
			return stack;
		}

		@Override
		public ItemStack removeStackFromSlot(int slot)
		{
			ItemStack stack = getStackInSlot(slot);
			if(!stack.isEmpty())
				setInventorySlotContents(slot, ItemStack.EMPTY);
			return stack;
		}

		@Override
		public void setInventorySlotContents(int slot, ItemStack stack)
		{
			if(slot < 9)
			{
				inv.set(slot, stack);
				if(!stack.isEmpty()&&stack.getCount() > getInventoryStackLimit())
					stack.setCount(getInventoryStackLimit());
			}
			recalculateOutput();
		}

		@Override
		public void clear()
		{
			for(int i = 0; i < this.inv.size(); i++)
				this.inv.set(i, ItemStack.EMPTY);
		}

		public void recalculateOutput()
		{
			InventoryCrafting invC = Utils.InventoryCraftingFalse.createFilledCraftingInventory(3, 3, inv);
			this.recipe = Utils.findRecipe(invC, tile.getWorld());
			this.inv.set(9, recipe!=null?recipe.getCraftingResult(invC): ItemStack.EMPTY);
		}

		public ArrayList<ItemStack> getTotalPossibleOutputs()
		{
			ArrayList<ItemStack> outputList = new ArrayList<ItemStack>();
			outputList.add(inv.get(9).copy());
			for(int i = 0; i < 9; i++)
			{
				FluidStack fs = FluidUtil.getFluidContained(inv.get(i));
				if(fs!=null)
				{
					boolean hasFluid = false;
					for(FluidTank tank : tile.tanks)
						if(tank.getFluid()!=null&&tank.getFluid().containsFluid(fs))
						{
							hasFluid = true;
							break;
						}
					if(hasFluid)
						continue;
				}
				//				ItemStack container = inv[i].getItem().getContainerItem(inv[i]);
				//				if(container!=null && inv[i].getItem().doesContainerItemLeaveCraftingGrid(inv[i]))
				//					outputList.add(container.copy());
			}
			InventoryCrafting invC = Utils.InventoryCraftingFalse.createFilledCraftingInventory(3, 3, inv);
			for(ItemStack ss : this.recipe.getRemainingItems(invC))
				if(!ss.isEmpty())
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
		public boolean isUsableByPlayer(EntityPlayer player)
		{
			return true;
		}

		@Override
		public void openInventory(EntityPlayer player)
		{
		}

		@Override
		public void closeInventory(EntityPlayer player)
		{
		}

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
			for(int i = 0; i < this.inv.size(); i++)
				if(!this.inv.get(i).isEmpty())
				{
					NBTTagCompound itemTag = new NBTTagCompound();
					itemTag.setByte("Slot", (byte)i);
					this.inv.get(i).writeToNBT(itemTag);
					list.appendTag(itemTag);
				}
		}

		public void readFromNBT(NBTTagList list)
		{
			for(int i = 0; i < list.tagCount(); i++)
			{
				NBTTagCompound itemTag = list.getCompoundTagAt(i);
				int slot = itemTag.getByte("Slot")&255;
				if(slot >= 0&&slot < getSizeInventory())
					this.inv.set(slot, new ItemStack(itemTag));
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