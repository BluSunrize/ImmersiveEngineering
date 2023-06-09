/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.crafting.IESerializableRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class FurnaceHandler<R extends IESerializableRecipe>
{
	private int process = 0;
	private int processMax = 0;
	private int burnTime = 0;
	private int lastBurnTime = 0;
	public final StateView stateView = new StateView();

	private final int fuelSlot;
	private final List<InputSlot<R>> inputs;
	private final List<OutputSlot<R>> outputs;
	private final ToIntFunction<R> getProcessingTime;
	private final Runnable setChanged;

	public FurnaceHandler(
			int fuelSlot,
			List<InputSlot<R>> inputs,
			List<OutputSlot<R>> outputs,
			ToIntFunction<R> getProcessingTime,
			Runnable setChanged
	)
	{
		this.fuelSlot = fuelSlot;
		this.inputs = inputs;
		this.outputs = outputs;
		this.getProcessingTime = getProcessingTime;
		this.setChanged = setChanged;
	}

	public boolean tickServer(IMultiblockContext<? extends IFurnaceEnvironment<R>> ctx)
	{
		boolean active = false;
		final IFurnaceEnvironment<R> env = ctx.getState();

		if(burnTime > 0)
		{
			int processSpeed = 1;
			if(process > 0)
				processSpeed = env.getProcessSpeed(ctx.getLevel());
			burnTime -= processSpeed;
			if(process > 0)
			{
				if(isAnyInputEmpty(env.getInventory()))
				{
					process = 0;
					processMax = 0;
				}
				else
				{
					R recipe = getRecipe(env);
					if(recipe!=null&&getProcessTime(recipe)!=processMax)
					{
						processMax = 0;
						process = 0;
					}
					else
					{
						process -= processSpeed;
						processSpeed = 0;//Process speed is "used up"
						active = true;
					}
				}
				setChanged.run();
			}

			if(process <= 0)
			{
				if(processMax > 0)
				{
					doRecipeIO(env);
					processMax = 0;
					burnTime -= process;
				}
				R recipe = getRecipe(env);
				if(recipe!=null)
				{
					final int time = getProcessTime(recipe);
					this.process = time-processSpeed;
					this.processMax = time;
					active = true;
				}
			}
		}

		if(burnTime <= 0&&getRecipe(env)!=null)
		{
			final IItemHandlerModifiable inv = env.getInventory();
			final ItemStack fuel = inv.getStackInSlot(fuelSlot);
			final int addedBurntime = env.getBurnTimeOf(ctx.getLevel().getRawLevel(), fuel);
			if(addedBurntime > 0)
			{
				lastBurnTime = addedBurntime;
				burnTime += lastBurnTime;
				if(fuel.hasCraftingRemainingItem()&&fuel.getCount()==1)
					inv.setStackInSlot(fuelSlot, fuel.getCraftingRemainingItem());
				else
					fuel.shrink(1);
				setChanged.run();
			}
		}

		if(!active)
			env.turnOff(ctx.getLevel());
		return active;
	}

	public Tag toNBT()
	{
		final CompoundTag result = new CompoundTag();
		result.putInt("process", process);
		result.putInt("processMax", processMax);
		result.putInt("burnTime", burnTime);
		result.putInt("lastBurnTime", lastBurnTime);
		return result;
	}

	public void readNBT(Tag nbt)
	{
		if(!(nbt instanceof CompoundTag compound))
			return;
		process = compound.getInt("process");
		processMax = compound.getInt("processMax");
		burnTime = compound.getInt("burnTime");
		lastBurnTime = compound.getInt("lastBurnTime");
	}

	private boolean isAnyInputEmpty(IItemHandler inv)
	{
		for(InputSlot<R> i : inputs)
			if(inv.getStackInSlot(i.slotIndex).isEmpty())
				return true;
		return false;
	}

	@Nullable
	private R getRecipe(IFurnaceEnvironment<R> env)
	{
		R recipe = env.getRecipeForInput();
		if(recipe==null)
			return null;
		final IItemHandlerModifiable inv = env.getInventory();
		for(OutputSlot<R> out : outputs)
		{
			ItemStack currentStack = inv.getStackInSlot(out.slotIndex);
			ItemStack outputSlot = out.get(recipe);
			if(!currentStack.isEmpty())
			{
				if(!ItemStack.isSameItem(currentStack, outputSlot))
					return null;
				else if(currentStack.getCount()+outputSlot.getCount() > inv.getSlotLimit(out.slotIndex))
					return null;
			}
		}
		return recipe;
	}

	private void doRecipeIO(IFurnaceEnvironment<R> env)
	{
		R recipe = getRecipe(env);
		if(recipe==null)
			return;
		final IItemHandlerModifiable inv = env.getInventory();
		for(InputSlot<R> slot : inputs)
		{
			int reqSize = inputs.stream()
					.map(matchSlot -> matchSlot.get(recipe))
					.filter(ingr -> ingr.test(inv.getStackInSlot(slot.slotIndex)))
					.mapToInt(IngredientWithSize::getCount).findFirst().orElse(0);
			inv.getStackInSlot(slot.slotIndex).shrink(reqSize);
		}

		for(OutputSlot<R> slot : outputs)
		{
			ItemStack result = slot.get(recipe);
			if(!result.isEmpty())
			{
				if(!inv.getStackInSlot(slot.slotIndex).isEmpty())
					inv.getStackInSlot(slot.slotIndex).grow(result.getCount());
				else
					inv.setStackInSlot(slot.slotIndex, result.copy());
			}
		}
	}

	private int getProcessTime(R recipe)
	{
		return getProcessingTime.applyAsInt(recipe);
	}

	public interface IFurnaceEnvironment<R extends IESerializableRecipe>
	{
		IItemHandlerModifiable getInventory();

		@Nullable
		R getRecipeForInput();

		int getBurnTimeOf(Level level, ItemStack fuel);

		default int getProcessSpeed(IMultiblockLevel level)
		{
			return 1;
		}

		default void turnOff(IMultiblockLevel level)
		{
		}
	}

	public class StateView implements ContainerData
	{
		public static final int LAST_BURN_TIME = 0;
		public static final int BURN_TIME = 1;
		public static final int PROCESS_MAX = 2;
		public static final int CURRENT_PROCESS = 3;
		public static final int NUM_SLOTS = 4;

		public static int getLastBurnTime(ContainerData data)
		{
			return data.get(LAST_BURN_TIME);
		}

		public static int getBurnTime(ContainerData data)
		{
			return data.get(BURN_TIME);
		}

		public static int getMaxProcess(ContainerData data)
		{
			return data.get(PROCESS_MAX);
		}

		public static int getProcess(ContainerData data)
		{
			return data.get(CURRENT_PROCESS);
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
			return NUM_SLOTS;
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
		private final Function<R, Lazy<ItemStack>> getFromRecipe;
		private final int slotIndex;

		public OutputSlot(Function<R, Lazy<ItemStack>> getFromRecipe, int slotIndex)
		{
			this.getFromRecipe = getFromRecipe;
			this.slotIndex = slotIndex;
		}

		public ItemStack get(R recipe)
		{
			return getFromRecipe.apply(recipe).get();
		}
	}
}
