/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public abstract class TileEntityMultiblockMetal<T extends TileEntityMultiblockMetal<T, R>, R extends IMultiblockRecipe> extends TileEntityMultiblockPart<T> implements IIEInventory, IIEInternalFluxHandler, IHammerInteraction, IMirrorAble, IProcessTile, IComparatorOverride
{
	public final FluxStorageAdvanced energyStorage;
	protected final boolean hasRedstoneControl;
	protected final IMultiblock mutliblockInstance;
	protected boolean redstoneControlInverted = false;
	//Absent means no controlling computers
	public Optional<Boolean> computerOn = Optional.empty();

	public TileEntityMultiblockMetal(IMultiblock mutliblockInstance, int[] structureDimensions, int energyCapacity, boolean redstoneControl)
	{
		super(structureDimensions);
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
		for(int i = 0; i < processNBT.tagCount(); i++)
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
		if(nbt.hasKey("computerOn", Constants.NBT.TAG_BYTE)&&Loader.isModLoaded("opencomputers"))
		{
			byte cOn = nbt.getByte("computerOn");
			switch(cOn)
			{
				case 0:
					computerOn = Optional.of(false);
					break;
				case 1:
					computerOn = Optional.of(true);
					break;
				case 2:
					computerOn = Optional.empty();
					break;
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
		if(computerOn.isPresent())
			nbt.setBoolean("computerOn", computerOn.get());
		else
			nbt.setByte("computerOn", (byte)2);
	}

	protected abstract R readRecipeFromNBT(NBTTagCompound tag);

	protected MultiblockProcess loadProcessFromNBT(NBTTagCompound tag)
	{
		IMultiblockRecipe recipe = readRecipeFromNBT(tag);
		if(recipe!=null)
			if(isInWorldProcessingMachine())
				return new MultiblockProcessInWorld<>(recipe, tag.getFloat("process_transformationPoint"), Utils.loadItemStacksFromNBT(tag.getTag("process_inputItem")));
			else
				return new MultiblockProcessInMachine<>(recipe, tag.getIntArray("process_inputSlots")).setInputTanks(tag.getIntArray("process_inputTanks"));
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
		return this.formed&&this.isEnergyPos()?SideConfig.INPUT: SideConfig.NONE;
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

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(!isDummy())
		{
			BlockPos nullPos = this.getBlockPosForPos(0);
			return new AxisAlignedBB(nullPos, nullPos.offset(facing, structureDimensions[1]).offset(mirrored?facing.rotateYCCW(): facing.rotateY(), structureDimensions[2]).up(structureDimensions[0]));
		}
		return super.getRenderBoundingBox();
	}

	//	=================================
	//		REDSTONE CONTROL
	//	=================================
	public abstract int[] getRedstonePos();

	public boolean isRedstonePos()
	{
		if(!hasRedstoneControl||getRedstonePos()==null)
			return false;
		for(int i : getRedstonePos())
			if(pos==i)
				return true;
		return false;
	}

	@Override
	public int getComparatorInputOverride()
	{
		if(!this.isRedstonePos())
			return 0;
		TileEntityMultiblockMetal master = master();
		if(master==null)
			return 0;
		return Utils.calcRedstoneFromInventory(master);
	}

	@Override
	public boolean hammerUseSide(EnumFacing side, EntityPlayer player, float hitX, float hitY, float hitZ)
	{
		if(this.isRedstonePos())
		{
			TileEntityMultiblockMetal<T, R> master = master();
			master.redstoneControlInverted = !master.redstoneControlInverted;
			ChatUtils.sendServerNoSpamMessages(player, new TextComponentTranslation(Lib.CHAT_INFO+"rsControl."+(master.redstoneControlInverted?"invertedOn": "invertedOff")));
			this.updateMasterBlock(null, true);
			return true;
		}
		return false;
	}

	public boolean isRSDisabled()
	{
		if(computerOn.isPresent())
			return !computerOn.get();
		int[] rsPositions = getRedstonePos();
		if(rsPositions==null||rsPositions.length < 1)
			return false;
		for(int rsPos : rsPositions)
		{
			T tile = this.getTileForPos(rsPos);
			if(tile!=null)
			{
				boolean b = world.getRedstonePowerFromNeighbors(tile.getPos()) > 0;
				return redstoneControlInverted!=b;
			}
		}
		return false;
	}


	//	=================================
	//		POSITION MANAGEMENT
	//	=================================
	@Nullable
	public T getTileForPos(int targetPos)
	{
		BlockPos target = getBlockPosForPos(targetPos);
		TileEntity tile = Utils.getExistingTileEntity(world, target);
		if(this.getClass().isInstance(tile))
			return (T)tile;
		return null;
	}

	@Override
	public ItemStack getOriginalBlock()
	{
		if(pos < 0)
			return ItemStack.EMPTY;
		ItemStack s = ItemStack.EMPTY;
		try
		{
			int blocksPerLevel = structureDimensions[1]*structureDimensions[2];
			int h = (pos/blocksPerLevel);
			int l = (pos%blocksPerLevel/structureDimensions[2]);
			int w = (pos%structureDimensions[2]);
			s = this.mutliblockInstance.getStructureManual()[h][l][w];
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		return s.copy();
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
		ApiUtils.checkForNeedlessTicking(this);
		tickedProcesses = 0;
		if(world.isRemote||isDummy()||isRSDisabled())
			return;

		int max = getMaxProcessPerTick();
		int i = 0;
		Iterator<MultiblockProcess<R>> processIterator = processQueue.iterator();
		tickedProcesses = 0;
		while(processIterator.hasNext()&&i++ < max)
		{
			MultiblockProcess<R> process = processIterator.next();
			if(process.canProcess(this))
			{
				process.doProcessTick(this);
				tickedProcesses++;
				updateMasterBlock(null, true);
			}
			if(process.clearProcess)
				processIterator.remove();
		}
	}

	public abstract IFluidTank[] getInternalTanks();

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
			for(MultiblockProcess<R> curr : processQueue)
				if(curr instanceof MultiblockProcessInWorld&&process.recipe.equals(curr.recipe))
				{
					MultiblockProcessInWorld p = (MultiblockProcessInWorld)curr;
					boolean canStack = true;
					for(ItemStack old : (List<ItemStack>)p.inputItems)
					{
						for(ItemStack in : (List<ItemStack>)((MultiblockProcessInWorld)process).inputItems)
							if(OreDictionary.itemMatches(old, in, true)&&Utils.compareItemNBT(old, in))
								if(old.getCount()+in.getCount() > old.getMaxStackSize())
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
									if(OreDictionary.itemMatches(old, in, true)&&Utils.compareItemNBT(old, in))
									{
										old.grow(in.getCount());
										break;
									}
							}
						return true;
					}
				}
		}
		if(getProcessQueueMaxLength() < 0||processQueue.size() < getProcessQueueMaxLength())
		{
			float dist = 1;
			MultiblockProcess<R> p = null;
			if(processQueue.size() > 0)
			{
				p = processQueue.get(processQueue.size()-1);
				if(p!=null)
					dist = p.processTick/(float)p.maxTicks;
			}
			if(p!=null&&dist < getMinProcessDistance(p))
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
		if(master!=this&&master!=null)
			return master.getCurrentProcessesStep();
		int[] ia = new int[processQueue.size()];
		for(int i = 0; i < ia.length; i++)
			ia[i] = processQueue.get(i).processTick;
		return ia;
	}

	@Override
	public int[] getCurrentProcessesMax()
	{
		T master = master();
		if(master!=this&&master!=null)
			return master.getCurrentProcessesMax();
		int[] ia = new int[processQueue.size()];
		for(int i = 0; i < ia.length; i++)
			ia[i] = processQueue.get(i).maxTicks;
		return ia;
	}

	public boolean shouldRenderAsActive()
	{
		return getEnergyStored(null) > 0&&!isRSDisabled()&&!processQueue.isEmpty();
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

		protected List<FluidStack> getRecipeFluidOutputs(TileEntityMultiblockMetal multiblock)
		{
			return recipe.getActualFluidOutputs(multiblock);
		}

		public boolean canProcess(TileEntityMultiblockMetal multiblock)
		{
			if(multiblock.energyStorage.extractEnergy(energyPerTick, true)==energyPerTick)
			{
				List<ItemStack> outputs = recipe.getItemOutputs();
				if(outputs!=null&&!outputs.isEmpty())
				{
					int[] outputSlots = multiblock.getOutputSlots();
					for(ItemStack output : outputs)
						if(!output.isEmpty())
						{
							boolean canOutput = false;
							if(outputSlots==null)
								canOutput = true;
							else
							{
								for(int iOutputSlot : outputSlots)
								{
									ItemStack s = multiblock.getInventory().get(iOutputSlot);
									if(s.isEmpty()||(ItemHandlerHelper.canItemStacksStack(s, output)&&s.getCount()+output.getCount() <= multiblock.getSlotLimit(iOutputSlot)))
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
				if(fluidOutputs!=null&&!fluidOutputs.isEmpty())
				{
					IFluidTank[] tanks = multiblock.getInternalTanks();
					int[] outputTanks = multiblock.getOutputTanks();
					for(FluidStack output : fluidOutputs)
						if(output!=null&&output.amount > 0)
						{
							boolean canOutput = false;
							if(tanks==null||outputTanks==null)
								canOutput = true;
							else
							{
								for(int iOutputTank : outputTanks)
									if(iOutputTank >= 0&&iOutputTank < tanks.length&&tanks[iOutputTank]!=null&&tanks[iOutputTank].fill(output, false)==output.amount)
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
			if(this.recipe.getMultipleProcessTicks() > 1)
			{
				//Average Insertion, tracked by the advanced flux storage
				int averageInsertion = multiblock.energyStorage.getAverageInsertion();
				//Average Insertion musn'T be greater than possible extraction
				averageInsertion = multiblock.energyStorage.extractEnergy(averageInsertion, true);
				if(averageInsertion > energyExtracted)
				{
					int possibleTicks = Math.min(averageInsertion/energyPerTick, Math.min(this.recipe.getMultipleProcessTicks(), this.maxTicks-this.processTick));
					if(possibleTicks > 1)
					{
						ticksAdded = possibleTicks;
						energyExtracted *= ticksAdded;
					}
				}
			}
			multiblock.energyStorage.extractEnergy(energyExtracted, false);
			this.processTick += ticksAdded;

			if(this.processTick >= this.maxTicks)
			{
				this.processFinish(multiblock);
			}
		}

		protected void processFinish(TileEntityMultiblockMetal multiblock)
		{
			List<ItemStack> outputs = getRecipeItemOutputs(multiblock);
			if(outputs!=null&&!outputs.isEmpty())
			{
				int[] outputSlots = multiblock.getOutputSlots();
				for(ItemStack output : outputs)
					if(!output.isEmpty())
						if(outputSlots==null||multiblock.getInventory()==null)
							multiblock.doProcessOutput(output.copy());
						else
						{
							for(int iOutputSlot : outputSlots)
							{
								ItemStack s = multiblock.getInventory().get(iOutputSlot);
								if(s.isEmpty())
								{
									multiblock.getInventory().set(iOutputSlot, output.copy());
									break;
								}
								else if(ItemHandlerHelper.canItemStacksStack(s, output)&&s.getCount()+output.getCount() <= multiblock.getSlotLimit(iOutputSlot))
								{
									multiblock.getInventory().get(iOutputSlot).grow(output.getCount());
									break;
								}
							}
						}
			}
			List<FluidStack> fluidOutputs = getRecipeFluidOutputs(multiblock);
			if(fluidOutputs!=null&&!fluidOutputs.isEmpty())
			{
				IFluidTank[] tanks = multiblock.getInternalTanks();
				int[] outputTanks = multiblock.getOutputTanks();
				for(FluidStack output : fluidOutputs)
					if(output!=null&&output.amount > 0)
					{
						if(tanks==null||outputTanks==null)
							multiblock.doProcessFluidOutput(output);
						else
						{
							for(int iOutputTank : outputTanks)
								if(iOutputTank >= 0&&iOutputTank < tanks.length&&tanks[iOutputTank]!=null&&tanks[iOutputTank].fill(output, false)==output.amount)
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
		protected int[] inputAmounts = null;
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

		public MultiblockProcessInMachine setInputAmounts(int... inputAmounts)
		{
			this.inputAmounts = inputAmounts;
			return this;
		}

		public int[] getInputSlots()
		{
			return this.inputSlots;
		}

		@Nullable
		public int[] getInputAmounts()
		{
			return this.inputAmounts;
		}

		public int[] getInputTanks()
		{
			return this.inputTanks;
		}

		protected List<IngredientStack> getRecipeItemInputs(TileEntityMultiblockMetal multiblock)
		{
			return recipe.getItemInputs();
		}

		protected List<FluidStack> getRecipeFluidInputs(TileEntityMultiblockMetal multiblock)
		{
			return recipe.getFluidInputs();
		}

		@Override
		public void doProcessTick(TileEntityMultiblockMetal multiblock)
		{
			NonNullList<ItemStack> inv = multiblock.getInventory();
			if(recipe.shouldCheckItemAvailability()&&recipe.getItemInputs()!=null&&inv!=null)
			{
				NonNullList<ItemStack> query = NonNullList.withSize(inputSlots.length, ItemStack.EMPTY);
				for(int i = 0; i < inputSlots.length; i++)
					if(inputSlots[i] >= 0&&inputSlots[i] < inv.size())
						query.set(i, multiblock.getInventory().get(inputSlots[i]));
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
			NonNullList<ItemStack> inv = multiblock.getInventory();
			List<IngredientStack> itemInputList = this.getRecipeItemInputs(multiblock);
			if(inv!=null&&this.inputSlots!=null&&itemInputList!=null)
			{
				if(this.inputAmounts!=null&&this.inputSlots.length==this.inputAmounts.length)
				{
					for(int i = 0; i < this.inputSlots.length; i++)
						if(this.inputAmounts[i] > 0)
							inv.get(this.inputSlots[i]).shrink(this.inputAmounts[i]);

				}
				else
				{
					Iterator<IngredientStack> iterator = new ArrayList(itemInputList).iterator();
					while(iterator.hasNext())
					{
						IngredientStack ingr = iterator.next();
						int ingrSize = ingr.inputSize;
						for(int slot : this.inputSlots)
							if(!inv.get(slot).isEmpty()&&ingr.matchesItemStackIgnoringSize(inv.get(slot)))
							{
								int taken = Math.min(inv.get(slot).getCount(), ingrSize);
								inv.get(slot).shrink(taken);
								if(inv.get(slot).getCount() <= 0)
									inv.set(slot, ItemStack.EMPTY);
								if((ingrSize -= taken) <= 0)
									break;
							}
					}
				}
			}
			IFluidTank[] tanks = multiblock.getInternalTanks();
			List<FluidStack> fluidInputList = this.getRecipeFluidInputs(multiblock);
			if(tanks!=null&&this.inputTanks!=null&&fluidInputList!=null)
			{
				Iterator<FluidStack> iterator = new ArrayList(fluidInputList).iterator();
				while(iterator.hasNext())
				{
					FluidStack ingr = iterator.next();
					int ingrSize = ingr.amount;
					for(int tank : this.inputTanks)
						if(tanks[tank]!=null)
						{
							if(tanks[tank] instanceof IFluidHandler&&((IFluidHandler)tanks[tank]).drain(ingr, false)!=null)
							{
								FluidStack taken = ((IFluidHandler)tanks[tank]).drain(ingr, true);
								if((ingrSize -= taken.amount) <= 0)
									break;
							}
							else if(tanks[tank].getFluid()!=null&&tanks[tank].getFluid().isFluidEqual(ingr))
							{
								int taken = Math.min(tanks[tank].getFluidAmount(), ingrSize);
								tanks[tank].drain(taken, true);
								if((ingrSize -= taken) <= 0)
									break;
							}
						}
				}
			}
		}

		@Override
		protected void writeExtraDataToNBT(NBTTagCompound nbt)
		{
			if(inputSlots!=null)
				nbt.setIntArray("process_inputSlots", inputSlots);
			if(inputAmounts!=null)
				nbt.setIntArray("process_inputAmounts", inputAmounts);
			if(inputTanks!=null)
				nbt.setIntArray("process_inputTanks", inputTanks);
		}
	}

	public static class MultiblockProcessInWorld<R extends IMultiblockRecipe> extends MultiblockProcess<R>
	{
		public List<ItemStack> inputItems;
		protected float transformationPoint;

		public MultiblockProcessInWorld(R recipe, float transformationPoint, NonNullList<ItemStack> inputItem)
		{
			super(recipe);
			this.inputItems = new ArrayList<>(inputItem.size());
			for(ItemStack s : inputItem)
				this.inputItems.add(s);
			this.transformationPoint = transformationPoint;
		}

		public List<ItemStack> getDisplayItem()
		{
			if(processTick/(float)maxTicks > transformationPoint)
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

				if(size > 0&&inputItem.getCount() > size)
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
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			stack = stack.copy();
			IMultiblockRecipe recipe = this.multiblock.findRecipeForInsertion(stack);
			if(recipe==null)
				return stack;
			ItemStack displayStack = recipe.getDisplayStack(stack);
			if(multiblock.addProcessToQueue(new MultiblockProcessInWorld(recipe, transformationPoint, Utils.createNonNullItemStackListFromItemStack(displayStack)), simulate, doProcessStacking))
			{
				multiblock.markDirty();
				multiblock.markContainingBlockForUpdate(null);
				stack.shrink(displayStack.getCount());
				if(stack.getCount() <= 0)
					stack = ItemStack.EMPTY;
			}
			return stack;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot)
		{
			return 64;
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack)
		{
		}
	}
}