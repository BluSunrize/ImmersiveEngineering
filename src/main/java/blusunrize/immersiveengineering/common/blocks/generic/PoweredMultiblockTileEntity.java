/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.utils.IngredientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IProcessTile;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public abstract class PoweredMultiblockTileEntity<T extends PoweredMultiblockTileEntity<T, R>, R extends MultiblockRecipe>
		extends MultiblockPartTileEntity<T> implements IIEInventory, IIEInternalFluxHandler,
		IProcessTile, IComparatorOverride
{
	public final FluxStorageAdvanced energyStorage;

	public PoweredMultiblockTileEntity(IETemplateMultiblock multiblockInstance, int energyCapacity, boolean redstoneControl,
									   TileEntityType<? extends T> type)
	{
		super(multiblockInstance, type, redstoneControl);
		this.energyStorage = new FluxStorageAdvanced(energyCapacity);
	}

	//	=================================
	//		DATA MANAGEMENT
	//	=================================
	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		energyStorage.readFromNBT(nbt);
		ListNBT processNBT = nbt.getList("processQueue", 10);
		processQueue.clear();
		for(int i = 0; i < processNBT.size(); i++)
		{
			CompoundNBT tag = processNBT.getCompound(i);
			if(tag.contains("recipe"))
			{
				int processTick = tag.getInt("process_processTick");
				MultiblockProcess<R> process = loadProcessFromNBT(tag);
				if(process!=null)
				{
					process.processTick = processTick;
					processQueue.add(process);
				}
			}
		}
		if(nbt.contains("computerOn", Constants.NBT.TAG_BYTE)&&ModList.get().isLoaded("opencomputers"))
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
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		energyStorage.writeToNBT(nbt);
		ListNBT processNBT = new ListNBT();
		for(MultiblockProcess process : this.processQueue)
			processNBT.add(writeProcessToNBT(process));
		nbt.put("processQueue", processNBT);
		if(computerOn.isPresent())
			nbt.putBoolean("computerOn", computerOn.get());
		else
			nbt.putByte("computerOn", (byte)2);
	}

	ArrayDeque<R> lastCachedRecipes = new ArrayDeque(5);

	@Nullable
	protected abstract R getRecipeForId(ResourceLocation id);
	//{
//		for(R recipe : lastCachedRecipes)
//			if(id.equals(recipe.getId()))
//				return recipe;
//		Optional<R> opt = (Optional<R>)this.world.getRecipeManager().getRecipe(id);
//		if(!opt.isPresent())
//			return null;
//		R recipe = opt.get();
//		if(lastCachedRecipes.size()==5)
//			lastCachedRecipes.pop();
//		lastCachedRecipes.add(recipe);
//		return recipe;
//	}

	@Nullable
	protected MultiblockProcess<R> loadProcessFromNBT(CompoundNBT tag)
	{
		String id = tag.getString("recipe");
		R recipe = getRecipeForId(new ResourceLocation(id));
		if(recipe!=null)
			if(isInWorldProcessingMachine())
				return new MultiblockProcessInWorld<>(recipe, tag.getFloat("process_transformationPoint"),
						Utils.loadItemStacksFromNBT(tag.get("process_inputItem")));
			else
				return new MultiblockProcessInMachine<>(recipe, tag.getIntArray("process_inputSlots"))
						.setInputTanks(tag.getIntArray("process_inputTanks"));
		return null;
	}

	protected CompoundNBT writeProcessToNBT(MultiblockProcess process)
	{
		CompoundNBT tag = new CompoundNBT();
		tag.putString("recipe", process.recipe.getId().toString());
		tag.putInt("process_processTick", process.processTick);
		process.writeExtraDataToNBT(tag);
		return tag;
	}

	//	=================================
	//		ENERGY MANAGEMENT
	//	=================================
	public abstract Set<BlockPos> getEnergyPos();

	public boolean isEnergyPos()
	{
		return getEnergyPos().contains(posInMultiblock);
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
	public IOSideConfig getEnergySideConfig(Direction facing)
	{
		return this.formed&&this.isEnergyPos()?IOSideConfig.INPUT: IOSideConfig.NONE;
	}

	IEForgeEnergyWrapper wrapper = new IEForgeEnergyWrapper(this, null);

	@Override
	@Nullable
	public IEForgeEnergyWrapper getCapabilityWrapper(Direction facing)
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

	@OnlyIn(Dist.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(!isDummy())
		{
			BlockPos nullPos = this.getOrigin();
			return new AxisAlignedBB(nullPos, TemplateMultiblock.withSettingsAndOffset(nullPos, new BlockPos(structureDimensions),
					getIsMirrored(), getFacing()));
		}
		return super.getRenderBoundingBox();
	}

	@Override
	public int getComparatorInputOverride()
	{
		if(!this.isRedstonePos())
			return 0;
		PoweredMultiblockTileEntity master = master();
		if(master==null)
			return 0;
		return Utils.calcRedstoneFromInventory(master);
	}

	//	=================================
	//		PROCESS MANAGEMENT
	//	=================================
	public List<MultiblockProcess<R>> processQueue = new ArrayList<MultiblockProcess<R>>();
	public int tickedProcesses = 0;

	@Override
	public void tick()
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

	@Nullable
	public abstract IFluidTank[] getInternalTanks();

	@Nullable
	public abstract R findRecipeForInsertion(ItemStack inserting);

	@Nullable
	public abstract int[] getOutputSlots();

	@Nullable
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
			MultiblockProcessInWorld<R> newProcess = (MultiblockProcessInWorld<R>)process;
			for(MultiblockProcess<R> curr : processQueue)
				if(curr instanceof MultiblockProcessInWorld&&process.recipe.equals(curr.recipe))
				{
					MultiblockProcessInWorld<R> existingProcess = (MultiblockProcessInWorld<R>)curr;
					boolean canStack = true;
					for(ItemStack old : existingProcess.inputItems)
					{
						for(ItemStack in : newProcess.inputItems)
							if(ItemStack.areItemsEqual(old, in)&&Utils.compareItemNBT(old, in))
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
							for(ItemStack old : existingProcess.inputItems)
							{
								for(ItemStack in : newProcess.inputItems)
									if(ItemStack.areItemsEqual(old, in)&&Utils.compareItemNBT(old, in))
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

	@Nonnull
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

	@Nonnull
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

	public abstract static class MultiblockProcess<R extends MultiblockRecipe>
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

		protected List<ItemStack> getRecipeItemOutputs(PoweredMultiblockTileEntity<?, R> multiblock)
		{
			return recipe.getActualItemOutputs(multiblock);
		}

		protected List<FluidStack> getRecipeFluidOutputs(PoweredMultiblockTileEntity<?, R> multiblock)
		{
			return recipe.getActualFluidOutputs(multiblock);
		}

		public boolean canProcess(PoweredMultiblockTileEntity<?, R> multiblock)
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
						if(output!=null&&output.getAmount() > 0)
						{
							boolean canOutput = false;
							if(tanks==null||outputTanks==null)
								canOutput = true;
							else
							{
								for(int iOutputTank : outputTanks)
									if(iOutputTank >= 0&&iOutputTank < tanks.length&&tanks[iOutputTank]!=null
											&&tanks[iOutputTank].fill(output, FluidAction.SIMULATE)==output.getAmount())
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

		public void doProcessTick(PoweredMultiblockTileEntity<?, R> multiblock)
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

		protected void processFinish(PoweredMultiblockTileEntity<?, R> multiblock)
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
					if(output!=null&&output.getAmount() > 0)
					{
						if(tanks==null||outputTanks==null)
							multiblock.doProcessFluidOutput(output);
						else
						{
							for(int iOutputTank : outputTanks)
								if(iOutputTank >= 0&&iOutputTank < tanks.length&&tanks[iOutputTank]!=null
										&&tanks[iOutputTank].fill(output, FluidAction.SIMULATE)==output.getAmount())
								{
									tanks[iOutputTank].fill(output, FluidAction.EXECUTE);
									break;
								}
						}
					}
			}

			multiblock.onProcessFinish(this);
			this.clearProcess = true;
		}

		protected abstract void writeExtraDataToNBT(CompoundNBT nbt);
	}

	public static class MultiblockProcessInMachine<R extends MultiblockRecipe> extends MultiblockProcess<R>
	{
		protected int[] inputSlots = new int[0];
		protected int[] inputAmounts = null;
		protected int[] inputTanks = new int[0];

		public MultiblockProcessInMachine(R recipe, int... inputSlots)
		{
			super(recipe);
			this.inputSlots = inputSlots;
		}

		public MultiblockProcessInMachine<R> setInputTanks(int... inputTanks)
		{
			this.inputTanks = inputTanks;
			return this;
		}

		public MultiblockProcessInMachine<R> setInputAmounts(int... inputAmounts)
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

		protected List<IngredientWithSize> getRecipeItemInputs(PoweredMultiblockTileEntity<?, R> multiblock)
		{
			return recipe.getItemInputs();
		}

		protected List<FluidTagInput> getRecipeFluidInputs(PoweredMultiblockTileEntity<?, R> multiblock)
		{
			return recipe.getFluidInputs();
		}

		@Override
		public void doProcessTick(PoweredMultiblockTileEntity<?, R> multiblock)
		{
			NonNullList<ItemStack> inv = multiblock.getInventory();
			if(recipe.shouldCheckItemAvailability()&&recipe.getItemInputs()!=null&&inv!=null)
			{
				NonNullList<ItemStack> query = NonNullList.withSize(inputSlots.length, ItemStack.EMPTY);
				for(int i = 0; i < inputSlots.length; i++)
					if(inputSlots[i] >= 0&&inputSlots[i] < inv.size())
						query.set(i, multiblock.getInventory().get(inputSlots[i]));
				if(!IngredientUtils.stacksMatchIngredientWithSizeList(recipe.getItemInputs(), query))
				{
					this.clearProcess = true;
					return;
				}
			}
			super.doProcessTick(multiblock);
		}

		@Override
		protected void processFinish(PoweredMultiblockTileEntity<?, R> multiblock)
		{
			super.processFinish(multiblock);
			NonNullList<ItemStack> inv = multiblock.getInventory();
			List<IngredientWithSize> itemInputList = this.getRecipeItemInputs(multiblock);
			if(inv!=null&&this.inputSlots!=null&&itemInputList!=null)
			{
				if(this.inputAmounts!=null&&this.inputSlots.length==this.inputAmounts.length)
				{
					for(int i = 0; i < this.inputSlots.length; i++)
						if(this.inputAmounts[i] > 0)
							inv.get(this.inputSlots[i]).shrink(this.inputAmounts[i]);

				}
				else
					for(IngredientWithSize ingr : new ArrayList<>(itemInputList))
					{
						int ingrSize = ingr.getCount();
						for(int slot : this.inputSlots)
							if(!inv.get(slot).isEmpty()&&ingr.test(inv.get(slot)))
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
			IFluidTank[] tanks = multiblock.getInternalTanks();
			List<FluidTagInput> fluidInputList = this.getRecipeFluidInputs(multiblock);
			if(tanks!=null&&this.inputTanks!=null&&fluidInputList!=null)
			{
				for(FluidTagInput ingr : new ArrayList<>(fluidInputList))
				{
					int ingrSize = ingr.getAmount();
					for(int tank : this.inputTanks)
						if(tanks[tank]!=null&&ingr.testIgnoringAmount(tanks[tank].getFluid()))
						{
							int taken = Math.min(tanks[tank].getFluidAmount(), ingrSize);
							tanks[tank].drain(taken, FluidAction.EXECUTE);
							if((ingrSize -= taken) <= 0)
								break;
						}
				}
			}
		}

		@Override
		protected void writeExtraDataToNBT(CompoundNBT nbt)
		{
			if(inputSlots!=null)
				nbt.putIntArray("process_inputSlots", inputSlots);
			if(inputAmounts!=null)
				nbt.putIntArray("process_inputAmounts", inputAmounts);
			if(inputTanks!=null)
				nbt.putIntArray("process_inputTanks", inputTanks);
		}
	}

	public static class MultiblockProcessInWorld<R extends MultiblockRecipe> extends MultiblockProcess<R>
	{
		public List<ItemStack> inputItems;
		protected float transformationPoint;

		public MultiblockProcessInWorld(R recipe, float transformationPoint, NonNullList<ItemStack> inputItem)
		{
			super(recipe);
			this.inputItems = new ArrayList<>(inputItem);
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
		protected void writeExtraDataToNBT(CompoundNBT nbt)
		{
			nbt.put("process_inputItem", Utils.writeInventory(inputItems));
			nbt.putFloat("process_transformationPoint", transformationPoint);
		}

		@Override
		protected void processFinish(PoweredMultiblockTileEntity multiblock)
		{
			super.processFinish(multiblock);
			int size = -1;

			for(ItemStack inputItem : this.inputItems)
			{
				for(IngredientWithSize s : recipe.getItemInputs())
					if(s.test(inputItem))
					{
						size = s.getCount();
						break;
					}

				if(size > 0&&inputItem.getCount() > size)
				{
					inputItem.split(size);
					processTick = 0;
					clearProcess = false;
				}
			}
		}
	}

	public static class MultiblockInventoryHandler_DirectProcessing
			<T extends PoweredMultiblockTileEntity<T, R>, R extends MultiblockRecipe>
			implements IItemHandlerModifiable
	{
		T multiblock;
		float transformationPoint = .5f;
		boolean doProcessStacking = false;

		public MultiblockInventoryHandler_DirectProcessing(T multiblock)
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
			R recipe = this.multiblock.findRecipeForInsertion(stack);
			if(recipe==null)
				return stack;
			ItemStack displayStack = recipe.getDisplayStack(stack);
			if(multiblock.addProcessToQueue(new MultiblockProcessInWorld<>(recipe, transformationPoint,
					Utils.createNonNullItemStackListFromItemStack(displayStack)), simulate, doProcessStacking))
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
		public boolean isItemValid(int slot, @Nonnull ItemStack stack)
		{
			return true;//TODO
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack)
		{
		}
	}
}