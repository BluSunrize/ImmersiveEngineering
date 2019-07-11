/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.DirectionalBlockPos;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.tool.AssemblerHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorAttachable;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.generic.TileEntityPoweredMultiblock;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockAssembler;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;

//TODO powered MB or not?
public class TileEntityAssembler extends TileEntityPoweredMultiblock<TileEntityAssembler, IMultiblockRecipe>
		implements IInteractionObjectIE, IConveyorAttachable
{
	public static TileEntityType<TileEntityAssembler> TYPE;

	public boolean[] computerOn = new boolean[3];
	public boolean isComputerControlled = false;

	public TileEntityAssembler()
	{
		super(MultiblockAssembler.instance, 32000, true, TYPE);
	}

	public FluidTank[] tanks = {new FluidTank(8000), new FluidTank(8000), new FluidTank(8000)};
	public NonNullList<ItemStack> inventory = NonNullList.withSize(18+3, ItemStack.EMPTY);
	public CrafterPatternInventory[] patterns = {new CrafterPatternInventory(this), new CrafterPatternInventory(this), new CrafterPatternInventory(this)};
	public boolean recursiveIngredients = false;

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompound("tank0"));
		tanks[1].readFromNBT(nbt.getCompound("tank1"));
		tanks[2].readFromNBT(nbt.getCompound("tank2"));
		recursiveIngredients = nbt.getBoolean("recursiveIngredients");
		if(!descPacket)
		{
			inventory = Utils.readInventory(nbt.getList("inventory", 10), 18+3);
			for(int iPattern = 0; iPattern < patterns.length; iPattern++)
			{
				ListNBT patternList = nbt.getList("pattern"+iPattern, 10);
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
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.put("tank0", tanks[0].writeToNBT(new CompoundNBT()));
		nbt.put("tank1", tanks[1].writeToNBT(new CompoundNBT()));
		nbt.put("tank2", tanks[2].writeToNBT(new CompoundNBT()));
		nbt.putBoolean("recursiveIngredients", recursiveIngredients);
		if(!descPacket)
		{
			nbt.put("inventory", Utils.writeInventory(inventory));
			for(int iPattern = 0; iPattern < patterns.length; iPattern++)
			{
				ListNBT patternList = new ListNBT();
				patterns[iPattern].writeToNBT(patternList);
				nbt.put("pattern"+iPattern, patternList);
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
	public void receiveMessageFromClient(CompoundNBT message)
	{
		if(message.hasKey("buttonID"))
		{
			int id = message.getInt("buttonID");
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
			int r = message.getInt("recipe");
			ListNBT list = message.getList("patternSync", 10);
			CrafterPatternInventory pattern = patterns[r];
			for(int i = 0; i < list.size(); i++)
			{
				CompoundNBT itemTag = list.getCompound(i);
				pattern.inv.set(itemTag.getInt("slot"), ItemStack.read(itemTag));
			}
		}
	}

	private CapabilityReference<IItemHandler> output = CapabilityReference.forTileEntity(this,
			() -> new DirectionalBlockPos(pos.offset(facing, 2), facing.getOpposite()),
			CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);

	@Override
	public void tick()
	{
		super.tick();

		if(isDummy()||isRSDisabled()||world.isRemote||world.getGameTime()%16!=((getPos().getX()^getPos().getZ())&15))
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
		for(int buffer = 0; buffer < outputBuffer.length; buffer++)
			if(outputBuffer[buffer]!=null&&outputBuffer[buffer].size() > 0)
				for(int iOutput = 0; iOutput < outputBuffer[buffer].size(); iOutput++)
				{
					ItemStack output = outputBuffer[buffer].get(iOutput);
					if(!output.isEmpty()&&output.getCount() > 0)
					{
						if(!isRecipeIngredient(output, buffer))
						{
							output = Utils.insertStackIntoInventory(this.output, output, false);
							if(output.isEmpty()||output.getCount() <= 0)
								continue;
						}
						int free = -1;
						if(iOutput==0)//Main recipe output
						{
							if(this.inventory.get(18+buffer).isEmpty()&&free < 0)
								free = 18+buffer;
							else if(!this.inventory.get(18+buffer).isEmpty()&&
									ItemStack.areItemStacksEqual(output, this.inventory.get(18+buffer))&&
									this.inventory.get(18+buffer).getCount()+output.getCount() <= this.inventory.get(18+buffer).getMaxStackSize())
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
								else if(!this.inventory.get(i).isEmpty()&&
										ItemStack.areItemStacksEqual(output, this.inventory.get(i))&&
										this.inventory.get(i).getCount()+output.getCount() <= this.inventory.get(i).getMaxStackSize())
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
			if(!isRecipeIngredient(this.inventory.get(18+i), i))
				this.inventory.set(18+i, Utils.insertStackIntoInventory(output, this.inventory.get(18+i), false));

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
						ItemStack forGrid = next.split(taken);
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
			return ItemStack.areItemStacksEqual(output, this.inventory.get(18+iPattern))&&
					this.inventory.get(18+iPattern).getCount()+output.getCount() <= this.inventory.get(18+iPattern).getMaxStackSize();
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
						if(ItemStack.areItemsEqual(pattern.inv.get(i), stack))
							return true;
					}
			}
		return false;
	}

	@Override
	public float[] getBlockBounds()
	{
		if(posInMultiblock < 9||posInMultiblock==10||posInMultiblock==13||posInMultiblock==16||posInMultiblock==22)
			return new float[]{0, 0, 0, 1, 1, 1};
		float xMin = 0;
		float yMin = 0;
		float zMin = 0;
		float xMax = 1;
		float yMax = 1;
		float zMax = 1;
		if((posInMultiblock%9 < 3&&facing==Direction.SOUTH)||(posInMultiblock%9 >= 6&&facing==Direction.NORTH))
			zMin = .25f;
		else if((posInMultiblock%9 < 3&&facing==Direction.NORTH)||(posInMultiblock%9 >= 6&&facing==Direction.SOUTH))
			zMax = .75f;
		else if((posInMultiblock%9 < 3&&facing==Direction.EAST)||(posInMultiblock%9 >= 6&&facing==Direction.WEST))
			xMin = .25f;
		else if((posInMultiblock%9 < 3&&facing==Direction.WEST)||(posInMultiblock%9 >= 6&&facing==Direction.EAST))
			xMax = .75f;
		if((posInMultiblock%3==0&&facing==Direction.EAST)||(posInMultiblock%3==2&&facing==Direction.WEST))
			zMin = .1875f;
		else if((posInMultiblock%3==0&&facing==Direction.WEST)||(posInMultiblock%3==2&&facing==Direction.EAST))
			zMax = .8125f;
		else if((posInMultiblock%3==0&&facing==Direction.NORTH)||(posInMultiblock%3==2&&facing==Direction.SOUTH))
			xMin = .1875f;
		else if((posInMultiblock%3==0&&facing==Direction.SOUTH)||(posInMultiblock%3==2&&facing==Direction.NORTH))
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
	public void replaceStructureBlock(BlockPos pos, BlockState state, ItemStack stack, int h, int l, int w)
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
		//TODO this seems both unnecessary and wrong
		//BlockPos pos = getPos().offset(facing, -1);
		//TileEntity inventoryTile = this.world.getTileEntity(pos);
		//if(inventoryTile!=null)
		//	output = Utils.insertStackIntoInventory(inventoryTile, output, facing.getOpposite());
		//if(!output.isEmpty())
		//	Utils.dropStackAtPos(world, pos, output, facing);
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

	private LazyOptional<IItemHandler> insertionHandler = registerConstantCap(
			new IEInventoryHandler(18, this, 0, true, false));
	private LazyOptional<IItemHandler> extractionHandler = registerConstantCap(
			new IEInventoryHandler(3, this, 18, false, true));

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing)
	{
		if((posInMultiblock==10||posInMultiblock==16)&&capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			TileEntityAssembler master = master();
			if(master==null)
				return LazyOptional.empty();
			if(posInMultiblock==10&&facing==this.facing.getOpposite())
				return master.insertionHandler.cast();
			if(posInMultiblock==16&&facing==this.facing)
				return master.extractionHandler.cast();
			return LazyOptional.empty();
		}
		return super.getCapability(capability, facing);
	}


	@Override
	public IMultiblockRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return null;
	}

	@Override
	protected IMultiblockRecipe readRecipeFromNBT(CompoundNBT tag)
	{
		return null;
	}

	@Override
	public boolean canUseGui(PlayerEntity player)
	{
		return formed;
	}

	@Override
	public ResourceLocation getGuiName()
	{
		return Lib.GUIID_Assembler;
	}

	@Override
	public IInteractionObjectIE getGuiMaster()
	{
		return master();
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(Direction side)
	{
		TileEntityAssembler master = master();
		if(master!=null&&posInMultiblock==1&&(side==null||side==facing.getOpposite()))
			return master.tanks;
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resource)
	{
		return true;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, Direction side)
	{
		return true;
	}

	@Override
	public Direction[] sigOutputDirections()
	{
		if(posInMultiblock==16)
			return new Direction[]{this.facing};
		return new Direction[0];
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
					stack = stack.split(amount);
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
			CraftingInventory invC = Utils.InventoryCraftingFalse.createFilledCraftingInventory(3, 3, inv);
			this.recipe = Utils.findCraftingRecipe(invC, tile.getWorld());
			this.inv.set(9, recipe!=null?recipe.getCraftingResult(invC): ItemStack.EMPTY);
		}

		@Nullable
		@Override
		public ITextComponent getCustomName()
		{
			return getName();
		}

		@Override
		public ITextComponent getName()
		{
			return new StringTextComponent("IECrafterPattern");
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
		public boolean isUsableByPlayer(PlayerEntity player)
		{
			return true;
		}

		@Override
		public void openInventory(PlayerEntity player)
		{
		}

		@Override
		public void closeInventory(PlayerEntity player)
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

		public void writeToNBT(ListNBT list)
		{
			for(int i = 0; i < this.inv.size(); i++)
				if(!this.inv.get(i).isEmpty())
				{
					CompoundNBT itemTag = new CompoundNBT();
					itemTag.setByte("Slot", (byte)i);
					this.inv.get(i).write(itemTag);
					list.add(itemTag);
				}
		}

		public void readFromNBT(ListNBT list)
		{
			for(int i = 0; i < list.size(); i++)
			{
				CompoundNBT itemTag = list.getCompound(i);
				int slot = itemTag.getByte("Slot")&255;
				if(slot < getSizeInventory())
					this.inv.set(slot, ItemStack.read(itemTag));
			}
			recalculateOutput();
		}

		@Override
		public ITextComponent getDisplayName()
		{
			return getName();
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