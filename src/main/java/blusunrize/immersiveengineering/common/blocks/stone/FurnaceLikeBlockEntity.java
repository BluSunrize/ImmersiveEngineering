/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IActiveState;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IProcessBE;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public abstract class FurnaceLikeBlockEntity<R, T extends FurnaceLikeBlockEntity<R, T>> extends MultiblockPartBlockEntity<T>
		implements IIEInventory, IActiveState, IInteractionObjectIE<T>, IProcessBE, IBlockBounds
{
	protected final NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
	private int process = 0;
	private int processMax = 0;
	private int burnTime = 0;
	private int lastBurnTime = 0;
	public final StateView stateView = new StateView();
	private final int fuelSlot;
	private final List<InputSlot<R>> inputs;
	private final List<OutputSlot<R>> outputs;
	private final ToIntFunction<R> getProcessingTime;

	protected FurnaceLikeBlockEntity(
			IETemplateMultiblock mb, BlockEntityType<T> type, int fuelSlot, List<InputSlot<R>> inputs, List<OutputSlot<R>> outputs, ToIntFunction<R> getProcessingTime,
			BlockPos pos, BlockState state
	)
	{
		super(mb, type, false, pos, state);
		this.fuelSlot = fuelSlot;
		this.inputs = inputs;
		this.outputs = outputs;
		this.getProcessingTime = getProcessingTime;
	}

	@Override
	public boolean canUseGui(Player player)
	{
		return formed;
	}

	@Override
	public T getGuiMaster()
	{
		return master();
	}

	@Nonnull
	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return Shapes.block();
	}

	@Override
	public void tickServer()
	{
		final boolean activeBeforeTick = getIsActive();

		if(burnTime > 0)
		{
			int processSpeed = 1;
			if(process > 0)
				processSpeed = getProcessSpeed();
			burnTime -= processSpeed;
			if(process > 0)
			{
				if(isAnyInputEmpty())
				{
					process = 0;
					processMax = 0;
				}
				else
				{
					R recipe = getRecipe();
					if(recipe!=null&&getProcessTime(recipe)!=processMax)
					{
						processMax = 0;
						process = 0;
						setActive(false);
					}
					else
					{
						process -= processSpeed;
						processSpeed = 0;//Process speed is "used up"
						if(!activeBeforeTick)
							setActive(true);
					}
				}
				markContainingBlockForUpdate(null);
			}

			if(process <= 0)
			{
				if(processMax > 0)
				{
					doRecipeIO();
					processMax = 0;
					burnTime -= process;
				}
				R recipe = getRecipe();
				if(recipe!=null)
				{
					final int time = getProcessTime(recipe);
					this.process = time-processSpeed;
					this.processMax = time;
					setActive(true);
				}
			}
		}
		else
		{
			if(activeBeforeTick)
				setActive(false);
		}

		if(burnTime <= 0&&getRecipe()!=null)
		{
			final int addedBurntime = getBurnTimeOf(inventory.get(fuelSlot));
			if(addedBurntime > 0)
			{
				lastBurnTime = addedBurntime;
				burnTime += lastBurnTime;
				Utils.modifyInvStackSize(inventory, fuelSlot, -1);
				markContainingBlockForUpdate(null);
			}
		}

		final boolean activeAfterTick = getIsActive();
		if(activeBeforeTick!=activeAfterTick)
		{
			this.setChanged();
			for(StructureBlockInfo info : multiblockInstance.getStructure(level))
			{
				T te = getEntityForPos(info.pos);
				if(te!=null)
					te.setActive(activeAfterTick);
			}
		}
	}

	@Nullable
	public R getRecipe()
	{
		R recipe = getRecipeForInput();
		if(recipe==null)
			return null;
		for(OutputSlot<R> out : outputs)
		{
			ItemStack currentStack = inventory.get(out.slotIndex);
			ItemStack outputSlot = out.get(recipe);
			if(!currentStack.isEmpty())
			{
				if(!ItemStack.isSame(currentStack, outputSlot))
					return null;
				else if(currentStack.getCount()+outputSlot.getCount() > getSlotLimit(out.slotIndex))
					return null;
			}
		}
		return recipe;
	}

	@Nullable
	protected abstract R getRecipeForInput();

	protected int getProcessSpeed()
	{
		return 1;
	}

	@Override
	public int[] getCurrentProcessesStep()
	{
		T master = master();
		if(master!=this&&master!=null)
			return master.getCurrentProcessesStep();
		return new int[]{processMax-process};
	}

	@Override
	public int[] getCurrentProcessesMax()
	{
		T master = master();
		if(master!=this&&master!=null)
			return master.getCurrentProcessesMax();
		return new int[]{processMax};
	}

	@Override
	public boolean triggerEvent(int id, int arg)
	{
		if(id==0)
			this.formed = arg==1;
		setChanged();
		markContainingBlockForUpdate(null);
		return true;
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		if(!descPacket)
		{
			process = nbt.getInt("process");
			processMax = nbt.getInt("processMax");
			burnTime = nbt.getInt("burnTime");
			lastBurnTime = nbt.getInt("lastBurnTime");
			ContainerHelper.loadAllItems(nbt, inventory);
		}
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		if(!descPacket)
		{
			nbt.putInt("process", process);
			nbt.putInt("processMax", processMax);
			nbt.putInt("burnTime", burnTime);
			nbt.putInt("lastBurnTime", lastBurnTime);
			ContainerHelper.saveAllItems(nbt, inventory);
		}
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return this.inventory;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}

	@Override
	public void doGraphicalUpdates()
	{
	}

	public StateView getGuiInts()
	{
		return stateView;
	}

	private boolean isAnyInputEmpty()
	{
		for(InputSlot<R> i : inputs)
			if(inventory.get(i.slotIndex).isEmpty())
				return true;
		return false;
	}

	private int getProcessTime(R recipe)
	{
		return getProcessingTime.applyAsInt(recipe);
	}

	private void doRecipeIO()
	{
		R recipe = getRecipe();
		if(recipe!=null)
		{
			for(InputSlot<R> slot : inputs)
				Utils.modifyInvStackSize(inventory, slot.slotIndex, -slot.get(recipe).getCount());
			for(OutputSlot<R> slot : outputs)
			{
				ItemStack result = slot.get(recipe);
				if(!result.isEmpty())
				{
					if(!inventory.get(slot.slotIndex).isEmpty())
						inventory.get(slot.slotIndex).grow(result.copy().getCount());
					else
						inventory.set(slot.slotIndex, result.copy());
				}
			}
		}
	}

	protected abstract int getBurnTimeOf(ItemStack fuel);

	public class StateView implements ContainerData
	{
		public static final int LAST_BURN_TIME = 0;
		public static final int BURN_TIME = 1;
		public static final int PROCESS_MAX = 2;
		public static final int CURRENT_PROCESS = 3;

		public int getLastBurnTime()
		{
			return get(LAST_BURN_TIME);
		}

		public int getBurnTime()
		{
			return get(BURN_TIME);
		}

		public int getMaxProcess()
		{
			return get(PROCESS_MAX);
		}

		public int getProcess()
		{
			return get(CURRENT_PROCESS);
		}

		@Override
		public int get(int index)
		{
			switch(index)
			{
				case LAST_BURN_TIME:
					return lastBurnTime;
				case BURN_TIME:
					return burnTime;
				case PROCESS_MAX:
					return processMax;
				case CURRENT_PROCESS:
					return process;
				default:
					throw new IllegalArgumentException("Unknown index "+index);
			}
		}

		@Override
		public void set(int index, int value)
		{
			switch(index)
			{
				case LAST_BURN_TIME:
					lastBurnTime = value;
					break;
				case BURN_TIME:
					burnTime = value;
					break;
				case PROCESS_MAX:
					processMax = value;
					break;
				case CURRENT_PROCESS:
					process = value;
					break;
				default:
					throw new IllegalArgumentException("Unknown index "+index);
			}
		}

		@Override
		public int getCount()
		{
			return 4;
		}
	}

	protected static class InputSlot<R>
	{
		private final Function<R, IngredientWithSize> getFromRecipe;
		private final int slotIndex;

		public InputSlot(Function<R, IngredientWithSize> getFromRecipe, int slotIndex)
		{
			this.getFromRecipe = getFromRecipe;
			this.slotIndex = slotIndex;
		}

		public IngredientWithSize get(R recipe)
		{
			return getFromRecipe.apply(recipe);
		}
	}

	protected static class OutputSlot<R>
	{
		private final Function<R, ItemStack> getFromRecipe;
		private final int slotIndex;

		public OutputSlot(Function<R, ItemStack> getFromRecipe, int slotIndex)
		{
			this.getFromRecipe = getFromRecipe;
			this.slotIndex = slotIndex;
		}

		public ItemStack get(R recipe)
		{
			return getFromRecipe.apply(recipe);
		}
	}
}
