/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.utils.IngredientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IProcessBE;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.MultiblockCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import blusunrize.immersiveengineering.common.util.orientation.RelativeBlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class PoweredMultiblockBlockEntity<T extends PoweredMultiblockBlockEntity<T, R>, R extends MultiblockRecipe>
		extends MultiblockPartBlockEntity<T> implements IIEInventory, IProcessBE, IComparatorOverride
{
	public final AveragingEnergyStorage energyStorage;
	protected final MultiblockCapability<?, IEnergyStorage> energyCap;

	private final MutableInt cachedComparatorValue = new MutableInt(-1);

	public PoweredMultiblockBlockEntity(IETemplateMultiblock multiblockInstance, int energyCapacity, boolean redstoneControl,
										BlockEntityType<? extends T> type, BlockPos pos, BlockState state)
	{
		super(multiblockInstance, type, redstoneControl, pos, state);
		this.energyStorage = new AveragingEnergyStorage(energyCapacity);
		this.energyCap = new MultiblockCapability<>(
				be -> be.energyCap, PoweredMultiblockBlockEntity::master, this, registerEnergyInput(this.energyStorage)
		);
	}

	//	=================================
	//		DATA MANAGEMENT
	//	=================================
	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		EnergyHelper.deserializeFrom(energyStorage, nbt);
		if(!descPacket||shouldSyncProcessQueue())
		{
			ListTag processNBT = nbt.getList("processQueue", Tag.TAG_COMPOUND);
			processQueue.clear();
			for(int i = 0; i < processNBT.size(); i++)
			{
				CompoundTag tag = processNBT.getCompound(i);
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
		}
		if(descPacket)
			renderAsActiveClient = nbt.getBoolean("renderActive");
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		EnergyHelper.serializeTo(energyStorage, nbt);
		if(!descPacket||shouldSyncProcessQueue())
		{
			ListTag processNBT = new ListTag();
			for(MultiblockProcess<?> process : this.processQueue)
				processNBT.add(writeProcessToNBT(process));
			nbt.put("processQueue", processNBT);
		}
		if(descPacket)
			nbt.putBoolean("renderActive", renderAsActiveClient);
	}

	@Nullable
	protected abstract R getRecipeForId(ResourceLocation id);

	@Nullable
	protected MultiblockProcess<R> loadProcessFromNBT(CompoundTag tag)
	{
		String id = tag.getString("recipe");
		R recipe = getRecipeForId(new ResourceLocation(id));
		if(recipe!=null)
			if(isInWorldProcessingMachine())
				return MultiblockProcessInWorld.load(recipe, tag);
			else
				return MultiblockProcessInMachine.load(recipe, tag);
		return null;
	}

	protected CompoundTag writeProcessToNBT(MultiblockProcess<?> process)
	{
		CompoundTag tag = new CompoundTag();
		tag.putString("recipe", process.recipe.getId().toString());
		tag.putInt("process_processTick", process.processTick);
		process.writeExtraDataToNBT(tag);
		return tag;
	}

	//	=================================
	//		ENERGY MANAGEMENT
	//	=================================
	public abstract Set<MultiblockFace> getEnergyPos();

	public boolean isEnergyPos(Direction absoluteFace)
	{
		return getEnergyPos().contains(new MultiblockFace(
				posInMultiblock, RelativeBlockFace.from(getFacing().getOpposite(), getIsMirrored(), absoluteFace)
		));
	}

	@Nonnull
	@Override
	public <C> LazyOptional<C> getCapability(@Nonnull Capability<C> capability, @Nullable Direction side)
	{
		if(capability==CapabilityEnergy.ENERGY&&(side==null||isEnergyPos(side)))
			return energyCap.getAndCast();
		return super.getCapability(capability, side);
	}

	@Override
	public AABB getRenderBoundingBox()
	{
		if(!isDummy())
		{
			BlockPos nullPos = this.getOrigin();
			return new AABB(nullPos,
					TemplateMultiblock.withSettingsAndOffset(
							nullPos, new BlockPos(structureDimensions.get()), getIsMirrored(),
							multiblockInstance.untransformDirection(getFacing())
					));
		}
		return super.getRenderBoundingBox();
	}

	@Override
	public int getComparatorInputOverride()
	{
		if(!this.isRedstonePos())
			return 0;
		PoweredMultiblockBlockEntity<?, ?> master = master();
		if(master==null)
			return 0;
		return master.getComparatorValueOnMaster();
	}

	protected int getComparatorValueOnMaster()
	{
		return Utils.calcRedstoneFromInventory(this);
	}

	//	=================================
	//		PROCESS MANAGEMENT
	//	=================================
	public final List<MultiblockProcess<R>> processQueue = new ArrayList<>();
	public int tickedProcesses = 0;
	private boolean renderAsActiveClient = true;

	private void syncRenderActive()
	{
		boolean renderActive = shouldRenderAsActive();
		if(renderAsActiveClient==renderActive)
			return;
		renderAsActiveClient = renderActive;
		updateMasterBlock(null, true);
	}

	@Override
	public void tickServer()
	{
		syncRenderActive();
		if(isRSDisabled())
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
		updateComparators(this, getRedstonePos(), cachedComparatorValue, getComparatorValueOnMaster());
	}

	protected boolean shouldSyncProcessQueue()
	{
		return true;
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
		if(addToPrevious&&process instanceof MultiblockProcessInWorld<R> newProcess)
		{
			for(MultiblockProcess<R> curr : processQueue)
				if(curr instanceof MultiblockProcessInWorld<R> existingProcess&&process.recipe.equals(curr.recipe))
				{
					boolean canStack = true;
					for(ItemStack old : existingProcess.inputItems)
					{
						for(ItemStack in : newProcess.inputItems)
							if(ItemStack.isSame(old, in)&&Utils.compareItemNBT(old, in))
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
									if(ItemStack.isSame(old, in)&&Utils.compareItemNBT(old, in))
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

	public final boolean shouldRenderAsActive()
	{
		if(level!=null&&!level.isClientSide)
			return shouldRenderAsActiveImpl();
		else
			return renderAsActiveClient;
	}

	protected boolean shouldRenderAsActiveImpl()
	{
		return energyStorage.getEnergyStored() > 0&&!isRSDisabled()&&!processQueue.isEmpty();
	}

	protected record MultiblockFace(BlockPos posInMultiblock, RelativeBlockFace face)
	{
		public MultiblockFace(int x, int y, int z, RelativeBlockFace face)
		{
			this(new BlockPos(x, y, z), face);
		}
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

		protected List<ItemStack> getRecipeItemOutputs(PoweredMultiblockBlockEntity<?, R> multiblock)
		{
			return recipe.getActualItemOutputs(multiblock);
		}

		protected List<FluidStack> getRecipeFluidOutputs(PoweredMultiblockBlockEntity<?, R> multiblock)
		{
			return recipe.getActualFluidOutputs(multiblock);
		}

		public boolean canProcess(PoweredMultiblockBlockEntity<?, R> multiblock)
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

		public void doProcessTick(PoweredMultiblockBlockEntity<?, R> multiblock)
		{
			int energyExtracted = energyPerTick;
			int ticksAdded = 1;
			if(this.recipe.getMultipleProcessTicks() > 1)
			{
				//Average Insertion, tracked by the advanced flux storage
				int averageInsertion = multiblock.energyStorage.getAverageInsertion();
				//Average Insertion mustn't be greater than possible extraction
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

		protected void processFinish(PoweredMultiblockBlockEntity<?, R> multiblock)
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

		protected abstract void writeExtraDataToNBT(CompoundTag nbt);
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

		protected List<IngredientWithSize> getRecipeItemInputs(PoweredMultiblockBlockEntity<?, R> multiblock)
		{
			return recipe.getItemInputs();
		}

		protected List<FluidTagInput> getRecipeFluidInputs(PoweredMultiblockBlockEntity<?, R> multiblock)
		{
			return recipe.getFluidInputs();
		}

		@Override
		public void doProcessTick(PoweredMultiblockBlockEntity<?, R> multiblock)
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
		protected void processFinish(PoweredMultiblockBlockEntity<?, R> multiblock)
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

		public static <R extends MultiblockRecipe>
		MultiblockProcessInMachine<R> load(R recipe, CompoundTag data)
		{
			return new MultiblockProcessInMachine<>(recipe, data.getIntArray("process_inputSlots"))
					.setInputAmounts(data.getIntArray("process_inputAmounts"))
					.setInputTanks(data.getIntArray("process_inputTanks"));
		}

		@Override
		protected void writeExtraDataToNBT(CompoundTag nbt)
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
		public NonNullList<ItemStack> inputItems;
		protected float transformationPoint;

		public MultiblockProcessInWorld(R recipe, float transformationPoint, NonNullList<ItemStack> inputItem)
		{
			super(recipe);
			this.inputItems = inputItem;
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

		public static <R extends MultiblockRecipe>
		MultiblockProcessInWorld<R> load(R recipe, CompoundTag data)
		{
			NonNullList<ItemStack> inputs = NonNullList.withSize(data.getInt("numInputs"), ItemStack.EMPTY);
			ContainerHelper.loadAllItems(data, inputs);
			float transformationPoint = data.getFloat("process_transformationPoint");
			return new MultiblockProcessInWorld<>(recipe, transformationPoint, inputs);
		}

		@Override
		protected void writeExtraDataToNBT(CompoundTag nbt)
		{
			ContainerHelper.saveAllItems(nbt, inputItems);
			nbt.putInt("numInputs", inputItems.size());
			nbt.putFloat("process_transformationPoint", transformationPoint);
		}

		@Override
		protected void processFinish(PoweredMultiblockBlockEntity multiblock)
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
			<T extends PoweredMultiblockBlockEntity<T, R>, R extends MultiblockRecipe>
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
				multiblock.setChanged();
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