/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockOrientation;
import blusunrize.immersiveengineering.api.multiblocks.blocks.RelativeBlockFace;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IProcessBE;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process_old.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process_old.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process_old.MultiblockProcessInWorld;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.MultiblockCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
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
	protected final MultiblockCapability<IEnergyStorage> energyCap;

	private final MutableInt cachedComparatorValue = new MutableInt(-1);

	public PoweredMultiblockBlockEntity(IETemplateMultiblock multiblockInstance, int energyCapacity, boolean redstoneControl,
										BlockEntityType<? extends T> type, BlockPos pos, BlockState state)
	{
		super(multiblockInstance, type, redstoneControl, pos, state);
		this.energyStorage = new AveragingEnergyStorage(energyCapacity);
		this.energyCap = MultiblockCapability.make(
				this, be -> be.energyCap, PoweredMultiblockBlockEntity::master, registerEnergyInput(this.energyStorage)
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
		if(!descPacket||shouldSyncProcessQueue())
		{
			EnergyHelper.serializeTo(energyStorage, nbt);
			ListTag processNBT = new ListTag();
			for(MultiblockProcess<?> process : this.processQueue)
				processNBT.add(writeProcessToNBT(process));
			nbt.put("processQueue", processNBT);
		}
		if(descPacket)
			nbt.putBoolean("renderActive", renderAsActiveClient);
	}

	@Nullable
	protected abstract R getRecipeForId(Level level, ResourceLocation id);

	@Nullable
	protected MultiblockProcess<R> loadProcessFromNBT(CompoundTag tag)
	{
		ResourceLocation id = new ResourceLocation(tag.getString("recipe"));
		if(isInWorldProcessingMachine())
			return MultiblockProcessInWorld.load(id, this::getRecipeForId, tag);
		else
			return MultiblockProcessInMachine.load(id, this::getRecipeForId, tag);
	}

	protected CompoundTag writeProcessToNBT(MultiblockProcess<?> process)
	{
		CompoundTag tag = new CompoundTag();
		tag.putString("recipe", process.getRecipeId().toString());
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
		return getEnergyPos().contains(asRelativeFace(absoluteFace));
	}

	@Nonnull
	@Override
	public <C> LazyOptional<C> getCapability(@Nonnull Capability<C> capability, @Nullable Direction side)
	{
		if(capability==ForgeCapabilities.ENERGY&&(side==null||isEnergyPos(side)))
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
	private boolean renderAsActiveClient = false;

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
		energyStorage.updateAverage();
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
				if(curr instanceof MultiblockProcessInWorld<R> existingProcess&&process.getRecipeId().equals(curr.getRecipeId()))
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
					dist = p.processTick/(float)p.getMaxTicks(level);
			}
			if(p!=null&&dist < getMinProcessDistance(p))
				return false;

			if(!simulate)
				processQueue.add(process);
			markContainingBlockForUpdate(null);
			markChunkDirty();
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
			ia[i] = processQueue.get(i).getMaxTicks(level);
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

	protected final MultiblockFace asRelativeFace(Direction absoluteFace)
	{
		return new MultiblockFace(posInMultiblock, RelativeBlockFace.from(
				new MultiblockOrientation(getFacing().getOpposite(), getIsMirrored()), absoluteFace
		));
	}

	protected record MultiblockFace(BlockPos posInMultiblock, RelativeBlockFace face)
	{
		public MultiblockFace(int x, int y, int z, RelativeBlockFace face)
		{
			this(new BlockPos(x, y, z), face);
		}
	}

	public static class MultiblockInventoryHandler_DirectProcessing
			<T extends PoweredMultiblockBlockEntity<T, R>, R extends MultiblockRecipe>
			implements IItemHandlerModifiable
	{
		protected T multiblock;
		float transformationPoint = .5f;
		boolean doProcessStacking = false;

		public MultiblockInventoryHandler_DirectProcessing(T multiblock)
		{
			this.multiblock = multiblock;
		}

		public MultiblockInventoryHandler_DirectProcessing<T, R> setTransformationPoint(float point)
		{
			this.transformationPoint = point;
			return this;
		}

		public MultiblockInventoryHandler_DirectProcessing<T, R> setProcessStacking(boolean stacking)
		{
			this.doProcessStacking = stacking;
			return this;
		}

		@Override
		public int getSlots()
		{
			return 1;
		}

		@Nonnull
		@Override
		public ItemStack getStackInSlot(int slot)
		{
			return ItemStack.EMPTY;
		}

		@Nonnull
		@Override
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
		{
			stack = stack.copy();
			R recipe = this.multiblock.findRecipeForInsertion(stack);
			if(recipe==null)
				return stack;
			ItemStack displayStack = recipe.getDisplayStack(stack);
			if(multiblock.addProcessToQueue(new MultiblockProcessInWorld<>(
					recipe, multiblock::getRecipeForId, transformationPoint,
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

		@Nonnull
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
		public void setStackInSlot(int slot, @Nonnull ItemStack stack)
		{
		}
	}
}