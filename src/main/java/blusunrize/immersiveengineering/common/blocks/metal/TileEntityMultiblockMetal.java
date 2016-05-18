package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.IEProperties.PropertyBoolInverted;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHammerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IMirrorAble;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IUsesBooleanProperty;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

public abstract class TileEntityMultiblockMetal<T extends TileEntityMultiblockMetal<T, R>, R extends IMultiblockRecipe> extends TileEntityMultiblockPart<T> implements IIEInventory, IFluxReceiver,IEnergyReceiver, IHammerInteraction, IMirrorAble
{
	/**H L W*/
	protected final int[] structureDimensions;
	protected final FluxStorageAdvanced energyStorage;
	protected final boolean hasRedstoneControl;
	protected final IMultiblock mutliblockInstance;
	protected boolean redstoneControlInverted = false;

	public TileEntityMultiblockMetal(IMultiblock mutliblockInstance, int[] structureDimensions, int energyCapacity, boolean redstoneControl)
	{
		this.structureDimensions = structureDimensions;
		this.energyStorage = new FluxStorageAdvanced(energyCapacity);
		this.hasRedstoneControl = redstoneControl;
		this.mutliblockInstance = mutliblockInstance;
	}

	//	=================================
	//		DATA MANAGEMENT
	//	=================================
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		energyStorage.readFromNBT(nbt);
		redstoneControlInverted = nbt.getBoolean("redstoneControlInverted");
		NBTTagList processNBT = nbt.getTagList("processQueue", 10);
		processQueue.clear();
		for(int i=0; i<processNBT.tagCount(); i++)
		{
			NBTTagCompound tag = processNBT.getCompoundTagAt(i);
			IMultiblockRecipe recipe = readRecipeFromNBT(tag);
			if(recipe!=null)
			{
				int processTick = tag.getInteger("process_processTick");
				MultiblockProcess process = loadProcessFromNBT(tag);
				if(process!=null)
				{
					process.processTick = processTick;
					processQueue.add(process);
				}
			}
		}

	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		energyStorage.writeToNBT(nbt);
		nbt.setBoolean("redstoneControlInverted", redstoneControlInverted);
		NBTTagList processNBT = new NBTTagList();
		for(MultiblockProcess process : this.processQueue)
			processNBT.appendTag(writeProcessToNBT(process));
		nbt.setTag("processQueue", processNBT);
	}
	protected abstract R readRecipeFromNBT(NBTTagCompound tag);
	protected MultiblockProcess loadProcessFromNBT(NBTTagCompound tag)
	{
		IMultiblockRecipe recipe = readRecipeFromNBT(tag);
		if(recipe!=null)
			if(isInWorldProcessingMachine())
				return new MultiblockProcessInWorld(recipe, ItemStack.loadItemStackFromNBT(tag.getCompoundTag("process_inputItem")), tag.getFloat("process_transformationPoint"));
			else
				return new MultiblockProcessInMachine(recipe, tag.getIntArray("process_inputSlots")).setInputTanks(tag.getIntArray("process_inputTanks"));
		return null;
	}
	protected NBTTagCompound writeProcessToNBT(MultiblockProcess process)
	{
		NBTTagCompound tag = process.recipe.writeToNBT(new NBTTagCompound());
		tag.setInteger("process_processTick", process.processTick);
		process.writeExtraDataToNBT(tag);
		return tag;
	}


	//	=================================
	//		ENERGY MANAGEMENT
	//	=================================
	public abstract int[] getEnergyPos();
	public boolean isEnergyPos()
	{
		for(int i : getEnergyPos())
			if(pos==i)
				return true;
		return false;
	}
	@Override
	public boolean canConnectEnergy(EnumFacing from)
	{
		return this.formed&&this.isEnergyPos();
	}
	@Override
	public int receiveEnergy(EnumFacing from, int energy, boolean simulate)
	{
		if(canConnectEnergy(from))
		{
			TileEntityMultiblockMetal master = (TileEntityMultiblockMetal)this.master();
			if(master==null)
				return 0;
			int rec = master.energyStorage.receiveEnergy(energy, simulate);
			master.markDirty();
			if(rec>0)
				worldObj.markBlockForUpdate(master.getPos());
			return rec;
		}
		return 0;
	}
	@Override
	public int getEnergyStored(EnumFacing from)
	{
		return ((TileEntityMultiblockMetal)this.master()).energyStorage.getEnergyStored();
	}
	@Override
	public int getMaxEnergyStored(EnumFacing from)
	{
		return ((TileEntityMultiblockMetal)this.master()).energyStorage.getMaxEnergyStored();
	}


	//	=================================
	//		REDSTONE CONTROL
	//	=================================
	public abstract int[] getRedstonePos();
	public boolean isRedstonePos()
	{
		if(!hasRedstoneControl || getRedstonePos()==null)
			return false;
		for(int i : getRedstonePos())
			if(pos==i)
				return true;
		return false;
	}
	@Override
	public boolean hammerUseSide(EnumFacing side, EntityPlayer player, float hitX, float hitY, float hitZ)
	{
		if(this.isRedstonePos())
		{
			TileEntityMultiblockMetal master = master();
			master.redstoneControlInverted = !master.redstoneControlInverted;
			ChatUtils.sendServerNoSpamMessages(player, new ChatComponentTranslation(Lib.CHAT_INFO+"rsControl."+(master.redstoneControlInverted?"invertedOn":"invertedOff")));
			master.markDirty();
			worldObj.markBlockForUpdate(master.getPos());
			return true;
		}
		return false;
	}
	public boolean isRSDisabled()
	{
		int[] rsPositions = getRedstonePos();
		if(rsPositions==null || rsPositions.length<1)
			return false;
		for(int rsPos : rsPositions)
		{
			T tile = this.getTileForPos(rsPos);
			if(tile!=null)
			{
				boolean b = worldObj.isBlockIndirectlyGettingPowered(tile.getPos())>0;
				return redstoneControlInverted?!b:b;
			}
		}
		return false;
	}


	//	=================================
	//		POSITION MANAGEMENT
	//	=================================
	public BlockPos getBlockPosForPos(int targetPos)
	{
		int blocksPerLevel = structureDimensions[1]*structureDimensions[2];
		// dist = target position - current position
		int distH = (targetPos/blocksPerLevel)-(pos/blocksPerLevel);
		int distL = (targetPos%blocksPerLevel / structureDimensions[2])-(pos%blocksPerLevel / structureDimensions[2]);
		int distW = (targetPos%structureDimensions[2])-(pos%structureDimensions[2]);
		int w = mirrored?-distW:distW;
		return getPos().offset(facing, distL).offset(facing.rotateY(), w).add(0, distH, 0);
	}
	public T getTileForPos(int targetPos)
	{
		BlockPos target = getBlockPosForPos(targetPos);
		TileEntity tile = worldObj.getTileEntity(target);
		if(this.getClass().isInstance(tile))
			return (T)tile;
		return null;
	}
	@Override
	public ItemStack getOriginalBlock()
	{
		if(pos<0)
			return null;
		ItemStack s = null;
		try{
			int blocksPerLevel = structureDimensions[1]*structureDimensions[2];
			int h = (pos/blocksPerLevel);
			int l = (pos%blocksPerLevel / structureDimensions[2]);
			int w = (pos%structureDimensions[2]);
			s = this.mutliblockInstance.getStructureManual()[h][l][w];
		}catch(Exception e){e.printStackTrace();}
		return s!=null?s.copy():null;
	}
	@Override
	public void disassemble()
	{
		if(formed && !worldObj.isRemote)
		{
			BlockPos startPos = this.getBlockPosForPos(0);
			for(int yy=0;yy<structureDimensions[0];yy++)
				for(int ll=0;ll<structureDimensions[1];ll++)
					for(int ww=0;ww<structureDimensions[2];ww++)
					{
						int w = mirrored?-ww:ww;
						BlockPos pos = startPos.offset(facing, ll).offset(facing.rotateY(), w).add(0, yy, 0);

						ItemStack s = null;

						TileEntity te = worldObj.getTileEntity(pos);
						if(te instanceof TileEntityMultiblockMetal)
						{
							s = ((TileEntityMultiblockMetal)te).getOriginalBlock();
							((TileEntityMultiblockMetal)te).formed=false;
						}

						if(pos.equals(getPos()))
							s = this.getOriginalBlock();
						IBlockState state = Utils.getStateFromItemStack(s);
						if(state!=null)
						{
							if(pos.equals(getPos()))
							{
								worldObj.spawnEntityInWorld(new EntityItem(worldObj, pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5, s));
							}
							else
							{
								if(state.getBlock()==this.getBlockType())
									worldObj.setBlockToAir(pos);
								worldObj.setBlockState(pos, state);
							}
						}
					}
		}
	}
	@Override
	public boolean getIsMirrored()
	{
		return this.mirrored;
	}
	@Override
	public PropertyBoolInverted getBoolProperty(Class<? extends IUsesBooleanProperty> inf)
	{
		return IEProperties.BOOLEANS[0];
	}

	//	=================================
	//		PROCESS MANAGEMENT
	//	=================================
	public List<MultiblockProcess> processQueue = new ArrayList<MultiblockProcess>();
	public int tickedProcesses = 0;
	@Override
	public void update()
	{
		if(isDummy() || isRSDisabled() || worldObj.isRemote)
			return;

		int max = getMaxProcessPerTick();
		int i = 0;
		Iterator<MultiblockProcess> processIterator = processQueue.iterator();
		tickedProcesses = 0;
		while(processIterator.hasNext() && i++<max)
		{
			MultiblockProcess process = processIterator.next();
			if(process.canProcess(this))
			{
				process.doProcessTick(this);
				tickedProcesses++;
			}
			if(process.clearProcess)
				processIterator.remove();
		}
	}
	public abstract FluidTank[] getInternalTanks();
	public abstract R findRecipeForInsertion(ItemStack inserting);
	public abstract int[] getOutputSlots();
	public abstract int[] getOutputTanks();
	public abstract boolean additionalCanProcessCheck(MultiblockProcess<R> process);
	public abstract void doProcessOutput(ItemStack output);
	public abstract void doProcessFluidOutput(FluidStack output);
	public abstract void onProcessFinish(MultiblockProcess<R> process);
	public abstract int getMaxProcessPerTick();
	public abstract int getProcessQueueMaxLength();
	public abstract float getMinProcessDistance();
	public abstract boolean isInWorldProcessingMachine();
	public boolean addProcessToQueue(MultiblockProcess<R> process, boolean simulate)
	{
		if(getProcessQueueMaxLength()<0 || processQueue.size() < getProcessQueueMaxLength())
		{
			float dist = 1;
			if(processQueue.size()>0)
			{
				MultiblockProcess p = processQueue.get(processQueue.size()-1);
				if(p!=null)
					dist = p.processTick/(float)p.maxTicks;
			}
			if(dist<getMinProcessDistance())
				return false;

			if(!simulate)
				processQueue.add(process);
			return true;
		}
		return false;
	}

	public static abstract class MultiblockProcess<R extends IMultiblockRecipe>
	{
		public R recipe;
		public int processTick;
		public int maxTicks;
		public int energyPerTick;
		public boolean clearProcess = false;

		public MultiblockProcess(R recipe)
		{
			this.recipe = recipe;
			this.processTick = 0;
			this.maxTicks = this.recipe.getTotalProcessTime();
			this.energyPerTick = this.recipe.getTotalProcessEnergy()/this.maxTicks;
		}
		
		protected List<ItemStack> getRecipeItemOutputs(TileEntityMultiblockMetal multiblock)
		{
			return recipe.getItemOutputs();
		}

		public boolean canProcess(TileEntityMultiblockMetal multiblock)
		{
			if(multiblock.energyStorage.extractEnergy(energyPerTick, true)==energyPerTick)
			{
				List<ItemStack> outputs = getRecipeItemOutputs(multiblock);
				if(outputs!=null && !outputs.isEmpty())
				{
					int[] outputSlots = multiblock.getOutputSlots();
					for(ItemStack output : outputs)
						if(output!=null)
						{
							boolean canOutput = false;
							if(outputSlots==null)
								canOutput = true;
							else
							{
								for(int iOutputSlot : outputSlots)
								{
									ItemStack s = multiblock.getInventory()[iOutputSlot];
									if(s==null || (ItemHandlerHelper.canItemStacksStack(s, output) && s.stackSize+output.stackSize<= multiblock.getSlotLimit(iOutputSlot)))
									{
										canOutput = true;
										break;
									}
								}
							}
							if(!canOutput)
								return false;
						}
				}
				List<FluidStack> fluidOutputs = recipe.getFluidOutputs();
				if(fluidOutputs!=null && !fluidOutputs.isEmpty())
				{
					FluidTank[] tanks = multiblock.getInternalTanks();
					int[] outputTanks = multiblock.getOutputTanks();
					for(FluidStack output : fluidOutputs)
						if(output!=null && output.amount>0)
						{
							boolean canOutput = false;
							if(tanks==null || outputTanks==null)
								canOutput = true;
							else
							{
								for(int iOutputTank : outputTanks)
									if(iOutputTank>=0&&iOutputTank<tanks.length && tanks[iOutputTank]!=null && tanks[iOutputTank].fill(output,false)==output.amount)
									{
										canOutput = true;
										break;
									}
							}
							if(!canOutput)
								return false;
						}
				}
				return multiblock.additionalCanProcessCheck(this);
			}
			return false;
		}

		public void doProcessTick(TileEntityMultiblockMetal multiblock)
		{
			int energyExtracted = energyPerTick;
			int ticksAdded = 1;
			if(this.recipe.getMultipleProcessTicks()>1)
			{
				//Average Insertion, tracked by the advanced flux storage
				int averageInsertion = multiblock.energyStorage.getAverageInsertion();
				//Average Insertion musn'T be greater than possible extraction
				averageInsertion = multiblock.energyStorage.extractEnergy(averageInsertion, true);
				if(averageInsertion>energyExtracted)
				{
					int possibleTicks = Math.min(averageInsertion/energyPerTick, Math.min(this.recipe.getMultipleProcessTicks(), this.maxTicks-this.processTick));
					if(possibleTicks>1)
					{
						ticksAdded = possibleTicks;
						energyExtracted *= ticksAdded;
					}
				}
			}
			multiblock.energyStorage.extractEnergy(energyExtracted, false);
			this.processTick+=ticksAdded;

			if(this.processTick>=this.maxTicks)
			{
				this.processFinish(multiblock);
			}
		}

		protected void processFinish(TileEntityMultiblockMetal multiblock)
		{
			List<ItemStack> outputs = getRecipeItemOutputs(multiblock);
			if(outputs!=null && !outputs.isEmpty())
			{
				int[] outputSlots = multiblock.getOutputSlots();
				for(ItemStack output : outputs)
					if(output!=null)
						if(outputSlots==null || multiblock.getInventory()==null)
							multiblock.doProcessOutput(output.copy());
						else
						{
							for(int iOutputSlot:outputSlots)
							{
								ItemStack s = multiblock.getInventory()[iOutputSlot];
								if(s==null)
								{	
									multiblock.getInventory()[iOutputSlot] = output.copy();
									break;
								}
								else if(ItemHandlerHelper.canItemStacksStack(s, output) && s.stackSize+output.stackSize<= multiblock.getSlotLimit(iOutputSlot))
								{	
									multiblock.getInventory()[iOutputSlot].stackSize += output.stackSize;
									break;
								}
							}
						}
			}
			List<FluidStack> fluidOutputs = recipe.getFluidOutputs();
			if(fluidOutputs!=null && !fluidOutputs.isEmpty())
			{
				FluidTank[] tanks = multiblock.getInternalTanks();
				int[] outputTanks = multiblock.getOutputTanks();
				for(FluidStack output : fluidOutputs)
					if(output!=null && output.amount>0)
					{
						if(tanks==null || outputTanks==null)
							multiblock.doProcessFluidOutput(output);
						else
						{
							for(int iOutputTank : outputTanks)
								if(iOutputTank>=0&&iOutputTank<tanks.length && tanks[iOutputTank]!=null && tanks[iOutputTank].fill(output,false)==output.amount)
								{
									tanks[iOutputTank].fill(output, true);
									break;
								}
						}
					}
			}

			multiblock.onProcessFinish(this);
			this.clearProcess = true;
		}

		protected abstract void writeExtraDataToNBT(NBTTagCompound nbt);
	}

	public static class MultiblockProcessInMachine<R extends IMultiblockRecipe> extends MultiblockProcess<R>
	{
		protected int[] inputSlots = new int[0];
		protected int[] inputTanks = new int[0];
		public MultiblockProcessInMachine(R recipe, int... inputSlots)
		{
			super(recipe);
			this.inputSlots = inputSlots;
		}
		public MultiblockProcessInMachine setInputTanks(int... inputTanks)
		{
			this.inputTanks = inputTanks;
			return this;
		}
		
		public int[] getInputSlots()
		{
			return this.inputSlots;
		}
		public int[] getInputTanks()
		{
			return this.inputTanks;
		}

		@Override
		public void doProcessTick(TileEntityMultiblockMetal multiblock)
		{
			ItemStack[] inv = multiblock.getInventory();
			if(recipe.getItemInputs()!=null && inv!=null)
			{
				ItemStack[] query = new ItemStack[inputSlots.length];
				for(int i=0; i<inputSlots.length; i++)
					if(inputSlots[i]>=0&&inputSlots[i]<inv.length)
						query[i] = multiblock.getInventory()[inputSlots[i]];
				if(!ApiUtils.stacksMatchIngredientList(recipe.getItemInputs(), query))
				{
					this.clearProcess = true;
					return;
				}
			}
			//			FluidTank[] tanks = multiblock.getInternalTanks();
			//			if(tanks!=null)
			//			{
			//				ItemStack[] query = new ItemStack[inputSlots.length];
			//				for(int i=0; i	inputSlots.length; i++)
			//					if(inputSlots[i]>=0&&inputSlots[i]<inv.length)
			//						query[i] = multiblock.getInventory()[inputSlots[i]];
			//				if(!ApiUtils.stacksMatchIngredientList(recipe.getItemInputs(), query))
			//				{
			//					this.clearProcess = true;
			//					return;
			//				}
			//			}
			super.doProcessTick(multiblock);
		}
		@Override
		protected void processFinish(TileEntityMultiblockMetal multiblock)
		{
			ItemStack[] inv = multiblock.getInventory();
			if(inv!=null && this.inputSlots!=null)
			{
				for(int slot : this.inputSlots)
					if(inv[slot]!=null)
					{
						for(IngredientStack ingr : this.recipe.getItemInputs())
							if(ingr.matchesItemStack(inv[slot]))
							{
								inv[slot].stackSize -= ingr.inputSize;
								if(inv[slot].stackSize<=0)
									inv[slot] = null;
								break;
							}
					}
			}
			FluidTank[] tanks = multiblock.getInternalTanks();
			if(tanks!=null && this.inputTanks!=null)
			{
				for(int tank : this.inputTanks)
					if(tanks[tank]!=null && tanks[tank].getFluid()!=null)
					{
						for(FluidStack ingr : this.recipe.getFluidInputs())
							if(tanks[tank].getFluid().containsFluid(ingr))
							{
								tanks[tank].drain(ingr.amount, true);
								break;
							}
					}
			}
			super.processFinish(multiblock);
		}
		@Override
		protected void writeExtraDataToNBT(NBTTagCompound nbt)
		{
			if(inputSlots!=null)
				nbt.setIntArray("process_inputSlots", inputSlots);
			if(inputTanks!=null)
				nbt.setIntArray("process_inputTanks", inputTanks);
		}
	}

	public static class MultiblockProcessInWorld<R extends IMultiblockRecipe> extends MultiblockProcess<R>
	{
		protected ItemStack inputItem;
		protected float transformationPoint;
		public MultiblockProcessInWorld(R recipe, ItemStack inputItem, float transformationPoint)
		{
			super(recipe);
			this.inputItem = inputItem;
			this.transformationPoint = transformationPoint;
		}

		public ItemStack getDisplayItem()
		{
			if(processTick/(float)maxTicks > transformationPoint) 
			{
				List<ItemStack> list = this.recipe.getItemOutputs();
				if(!list.isEmpty())
					return list.get(0);
			}
			return inputItem;
		}

		@Override
		protected void writeExtraDataToNBT(NBTTagCompound nbt)
		{
			nbt.setTag("process_inputItem", inputItem.writeToNBT(new NBTTagCompound()));
			nbt.setFloat("process_transformationPoint", transformationPoint);
		}
	}

	public static class MultiblockInventoryHandler_DirectProcessing implements IItemHandlerModifiable
	{
		TileEntityMultiblockMetal multiblock;
		float transformationPoint = .5f;
		public MultiblockInventoryHandler_DirectProcessing(TileEntityMultiblockMetal multiblock)
		{
			this.multiblock = multiblock;
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
			IMultiblockRecipe recipe = this.multiblock.findRecipeForInsertion(stack);
			if(recipe==null)
				return stack;
			ItemStack displayStack = null;
			for(IngredientStack ingr : recipe.getItemInputs())
				if(ingr.matchesItemStack(stack))
				{
					displayStack = Utils.copyStackWithAmount(stack, ingr.inputSize);
					break;
				}
			if(multiblock.addProcessToQueue(new MultiblockProcessInWorld(recipe, displayStack, transformationPoint), simulate))
			{
				multiblock.markDirty();
				multiblock.worldObj.markBlockForUpdate(multiblock.getPos());
				stack.stackSize -= displayStack.stackSize;
				if(stack.stackSize<=0)
					stack = null;
			}
			return stack;
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
}