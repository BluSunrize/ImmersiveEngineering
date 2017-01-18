package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.PropertyBoolInverted;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class TileEntityMultiblockMetal<T extends TileEntityMultiblockMetal<T, R>, R extends IMultiblockRecipe> extends TileEntityMultiblockPart<T> implements IIEInventory, IIEInternalFluxHandler, IHammerInteraction, IMirrorAble, IProcessTile
{
	/**H L W*/
	protected final int[] structureDimensions;
	public final FluxStorageAdvanced energyStorage;
	protected final boolean hasRedstoneControl;
	protected final IMultiblock mutliblockInstance;
	protected boolean redstoneControlInverted = false;
	public int controllingComputers = 0;
	public boolean computerOn = true;

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
		if(descPacket)
		{
			controllingComputers = nbt.getBoolean("computerControlled") ? 1 : 0;
			computerOn = nbt.getBoolean("computerOn");
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
		if(descPacket)
		{
			nbt.setBoolean("computerControlled", controllingComputers > 0);
			nbt.setBoolean("computerOn", computerOn);
		}
	}
	protected abstract R readRecipeFromNBT(NBTTagCompound tag);
	protected MultiblockProcess loadProcessFromNBT(NBTTagCompound tag)
	{
		IMultiblockRecipe recipe = readRecipeFromNBT(tag);
		if(recipe!=null)
			if(isInWorldProcessingMachine())
				return new MultiblockProcessInWorld(recipe, tag.getFloat("process_transformationPoint"), Utils.loadItemStacksFromNBT(tag.getTag("process_inputItem")));
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


	@Nonnull
	@Override
	public FluxStorage getFluxStorage()
	{
		T master = this.master();
		if(master!=null)
			return master.energyStorage;
		return energyStorage;
	}
	@Nonnull
	@Override
	public SideConfig getEnergySideConfig(EnumFacing facing)
	{
		return this.formed&&this.isEnergyPos()?SideConfig.INPUT:SideConfig.NONE;
	}
	IEForgeEnergyWrapper wrapper = new IEForgeEnergyWrapper(this, null);
	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing)
	{
		if(this.formed&&this.isEnergyPos())
			return wrapper;
		return null;
	}
	@Override
	public void postEnergyTransferUpdate(int energy, boolean simulate)
	{
		if(!simulate)
			this.updateMasterBlock(null, energy!=0);
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
			TileEntityMultiblockMetal<T, R> master = master();
			master.redstoneControlInverted = !master.redstoneControlInverted;
			ChatUtils.sendServerNoSpamMessages(player, new TextComponentTranslation(Lib.CHAT_INFO+"rsControl."+(master.redstoneControlInverted?"invertedOn":"invertedOff")));
			this.updateMasterBlock(null, true);
			return true;
		}
		return false;
	}
	public boolean isRSDisabled()
	{
		if(controllingComputers > 0 && !computerOn)
			return true;
		int[] rsPositions = getRedstonePos();
		if(rsPositions==null || rsPositions.length<1)
			return false;
		for(int rsPos : rsPositions)
		{
			T tile = this.getTileForPos(rsPos);
			if(tile!=null)
			{
				boolean b = worldObj.isBlockIndirectlyGettingPowered(tile.getPos())>0;
				return redstoneControlInverted != b;
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
								worldObj.spawnEntityInWorld(new EntityItem(worldObj, pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5, s));
							else
								replaceStructureBlock(pos, state, s, yy,ll,ww);
						}
					}
		}
	}
	public void replaceStructureBlock(BlockPos pos, IBlockState state, ItemStack stack, int h, int l, int w)
	{
		if(state.getBlock()==this.getBlockType())
			worldObj.setBlockToAir(pos);
		worldObj.setBlockState(pos, state);
		TileEntity tile = worldObj.getTileEntity(pos);
		if(tile instanceof ITileDrop)
			((ITileDrop)tile).readOnPlacement(null, stack);
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
	public List<MultiblockProcess<R>> processQueue = new ArrayList<MultiblockProcess<R>>();
	public int tickedProcesses = 0;
	@Override
	public void update()
	{
		tickedProcesses = 0;
		if(worldObj.isRemote || isDummy() || isRSDisabled())
			return;

		int max = getMaxProcessPerTick();
		int i = 0;
		Iterator<MultiblockProcess<R>> processIterator = processQueue.iterator();
		tickedProcesses = 0;
		while(processIterator.hasNext() && i++<max)
		{
			MultiblockProcess<R> process = processIterator.next();
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
	public abstract float getMinProcessDistance(MultiblockProcess<R> process);
	public abstract boolean isInWorldProcessingMachine();
	public boolean addProcessToQueue(MultiblockProcess<R> process, boolean simulate)
	{
		return addProcessToQueue(process, simulate, false);
	}
	public boolean addProcessToQueue(MultiblockProcess<R> process, boolean simulate, boolean addToPrevious)
	{
		if(addToPrevious&&process instanceof MultiblockProcessInWorld)
		{
			for(MultiblockProcess<R> curr:processQueue)
				if(curr instanceof MultiblockProcessInWorld && process.recipe.equals(curr.recipe))
				{
					MultiblockProcessInWorld p = (MultiblockProcessInWorld)curr;
					boolean canStack = true;
					for(ItemStack old : (List<ItemStack>)p.inputItems)
					{
						for(ItemStack in : (List<ItemStack>)((MultiblockProcessInWorld)process).inputItems)
							if(OreDictionary.itemMatches(old, in, true) && ItemStack.areItemStackTagsEqual(old, in))
								if(old.stackSize+in.stackSize>old.getMaxStackSize())
								{
									canStack = false;
									break;
								}
						if(!canStack)
							break;
					}
					if(canStack)
					{
						if(!simulate)
							for(ItemStack old : (List<ItemStack>)p.inputItems)
							{
								for(ItemStack in : (List<ItemStack>)((MultiblockProcessInWorld)process).inputItems)
									if(OreDictionary.itemMatches(old, in, true) && ItemStack.areItemStackTagsEqual(old, in))
									{
										old.stackSize+=in.stackSize;
										break;
									}
							}
						return true;
					}
				}
		}
		if(getProcessQueueMaxLength()<0 || processQueue.size() < getProcessQueueMaxLength())
		{
			float dist = 1;
			MultiblockProcess<R> p = null;
			if(processQueue.size()>0)
			{
				p = processQueue.get(processQueue.size() - 1);
				if(p != null)
					dist = p.processTick / (float) p.maxTicks;
			}
			if(p != null && dist < getMinProcessDistance(p))
				return false;

			if(!simulate)
				processQueue.add(process);
			return true;
		}
		return false;
	}
	@Override
	public int[] getCurrentProcessesStep()
	{
		T master = master();
		if(master!=this && master!=null)
			return master.getCurrentProcessesStep();
		int[] ia = new int[processQueue.size()];
		for(int i=0; i<ia.length; i++)
			ia[i] = processQueue.get(i).processTick;
		return ia;
	}
	@Override
	public int[] getCurrentProcessesMax()
	{
		T master = master();
		if(master!=this && master!=null)
			return master.getCurrentProcessesMax();
		int[] ia = new int[processQueue.size()];
		for(int i=0; i<ia.length; i++)
			ia[i] = processQueue.get(i).maxTicks;
		return ia;
	}

	public boolean shouldRenderAsActive()
	{
		return (controllingComputers <= 0 || computerOn) && getEnergyStored(null) > 0 && !isRSDisabled() && !processQueue.isEmpty();
	}

	public abstract static class MultiblockProcess<R extends IMultiblockRecipe>
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
			return recipe.getActualItemOutputs(multiblock);
		}

		public boolean canProcess(TileEntityMultiblockMetal multiblock)
		{
			if(multiblock.energyStorage.extractEnergy(energyPerTick, true)==energyPerTick)
			{
				List<ItemStack> outputs = recipe.getItemOutputs();
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
			List<FluidStack> fluidOutputs = recipe.getActualFluidOutputs(multiblock);
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
			super.processFinish(multiblock);
			ItemStack[] inv = multiblock.getInventory();
			List<IngredientStack> itemInputList = this.recipe.getItemInputs();
			if(inv != null && this.inputSlots != null && itemInputList != null)
			{
				Iterator<IngredientStack> iterator = new ArrayList(itemInputList).iterator();
				while(iterator.hasNext())
				{
					IngredientStack ingr = iterator.next();
					int ingrSize = ingr.inputSize;
					for(int slot : this.inputSlots)
						if(inv[slot] != null && ingr.matchesItemStackIgnoringSize(inv[slot]))
						{
							int taken = Math.min(inv[slot].stackSize, ingrSize);
							if((inv[slot].stackSize -= taken) <= 0)
								inv[slot] = null;
							if((ingrSize -= taken) <= 0)
								break;
						}
				}
			}
			FluidTank[] tanks = multiblock.getInternalTanks();
			List<FluidStack> fluidInputList = this.recipe.getFluidInputs();
			if(tanks != null && this.inputTanks != null && fluidInputList != null)
			{
				Iterator<FluidStack> iterator = new ArrayList(fluidInputList).iterator();
				while(iterator.hasNext())
				{
					FluidStack ingr = iterator.next();
					int ingrSize = ingr.amount;
					for(int tank : this.inputTanks)
						if(tanks[tank] != null && tanks[tank].getFluid() != null && tanks[tank].getFluid().isFluidEqual(ingr))
						{
							int taken = Math.min(tanks[tank].getFluidAmount(), ingrSize);
							tanks[tank].drain(taken, true);
							if((ingrSize -= taken) <= 0)
								break;
						}
				}
			}
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
		public List<ItemStack> inputItems;
		protected float transformationPoint;
		public MultiblockProcessInWorld(R recipe, float transformationPoint, ItemStack... inputItem)
		{
			super(recipe);
			this.inputItems = new ArrayList<>(inputItem.length);
			for(ItemStack s : inputItem)
				this.inputItems.add(s);
			this.transformationPoint = transformationPoint;
		}

		public List<ItemStack> getDisplayItem()
		{
			if(processTick / (float)maxTicks > transformationPoint)
			{
				List<ItemStack> list = this.recipe.getItemOutputs();
				if(!list.isEmpty())
					return list;
			}
			return inputItems;
		}

		@Override
		protected void writeExtraDataToNBT(NBTTagCompound nbt)
		{
			nbt.setTag("process_inputItem", Utils.writeInventory(inputItems));
			nbt.setFloat("process_transformationPoint", transformationPoint);
		}
		@Override
		protected void processFinish(TileEntityMultiblockMetal multiblock)
		{
			super.processFinish(multiblock);
			int size = -1;

			for(ItemStack inputItem : this.inputItems)
			{
				for(IngredientStack s : recipe.getItemInputs())
					if(s.matchesItemStackIgnoringSize(inputItem))
					{
						size = s.inputSize;
						break;
					}
				if(size>0 && inputItem.stackSize>size)
				{
					inputItem.splitStack(size);
					processTick = 0;
					clearProcess = false;
				}
			}
		}
	}

	public static class MultiblockInventoryHandler_DirectProcessing implements IItemHandlerModifiable
	{
		TileEntityMultiblockMetal multiblock;
		float transformationPoint = .5f;
		boolean doProcessStacking = false;
		public MultiblockInventoryHandler_DirectProcessing(TileEntityMultiblockMetal multiblock)
		{
			this.multiblock = multiblock;
		}

		public MultiblockInventoryHandler_DirectProcessing setTransformationPoint(float point)
		{
			this.transformationPoint = point;
			return this;
		}
		public MultiblockInventoryHandler_DirectProcessing setProcessStacking(boolean stacking)
		{
			this.doProcessStacking = stacking;
			return this;
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
			stack = stack.copy();
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
			if(multiblock.addProcessToQueue(new MultiblockProcessInWorld(recipe, transformationPoint, displayStack), simulate, doProcessStacking))
			{
				multiblock.markDirty();
				multiblock.markContainingBlockForUpdate(null);
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