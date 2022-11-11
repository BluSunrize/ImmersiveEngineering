package blusunrize.immersiveengineering.common.blocks.multiblocks.process;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLevel;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

public class MultiblockProcessor<R extends MultiblockRecipe, CTX extends ProcessContext<R>>
{
	private final List<MultiblockProcess<R, CTX>> processQueue = new ArrayList<>();
	private final int maxQueueLength;
	private final int minProcessDelay;
	private final int maxProcessPerTick;
	private final Runnable markDirty;
	private final RecipeSource<R> recipeSource;

	public MultiblockProcessor(
			int maxQueueLength,
			int minProcessDelay,
			int maxProcessPerTick,
			Runnable markDirty,
			RecipeSource<R> recipeSource
	)
	{
		this.maxQueueLength = maxQueueLength;
		this.minProcessDelay = minProcessDelay;
		this.maxProcessPerTick = maxProcessPerTick;
		this.markDirty = markDirty;
		this.recipeSource = recipeSource;
	}

	public boolean tickServer(CTX ctx, IMultiblockLevel level, boolean canWork)
	{
		ctx.getEnergy().updateAverage();
		if(!canWork)
			return false;

		int i = 0;
		Iterator<MultiblockProcess<R, CTX>> processIterator = processQueue.iterator();
		boolean tickedAny = false;
		while(processIterator.hasNext()&&i++ < maxProcessPerTick)
		{
			MultiblockProcess<R, CTX> process = processIterator.next();
			if(process.canProcess(ctx, level.getRawLevel()))
			{
				process.doProcessTick(ctx, level);
				tickedAny = true;
			}
			if(process.clearProcess)
				processIterator.remove();
		}
		if(tickedAny)
			markDirty.run();
		return tickedAny;
	}

	// TODO write to/read from NBT
	public Tag toNBT()
	{
		ListTag processList = new ListTag();
		for(final var process : processQueue)
		{
			CompoundTag tag = new CompoundTag();
			tag.putString("recipe", process.getRecipeId().toString());
			tag.putInt("process_processTick", process.processTick);
			process.writeExtraDataToNBT(tag);
			processList.add(tag);
		}
		return processList;
	}

	public void fromNBT(Tag nbt, ProcessLoader<R, CTX> loader)
	{
		if(!(nbt instanceof ListTag list))
			return;
		this.processQueue.clear();
		for(final var tag : list)
			if(tag instanceof CompoundTag processTag)
			{
				final var loadedProcess = loader.fromNBT(this.recipeSource.getRecipeFromID, processTag);
				if(loadedProcess!=null)
					this.processQueue.add(loadedProcess);
			}
	}

	public boolean addProcessToQueue(MultiblockProcess<R, CTX> process, Level level, boolean simulate)
	{
		return addProcessToQueue(process, level, simulate, false);
	}

	public boolean addProcessToQueue(
			MultiblockProcess<R, CTX> process, Level level, boolean simulate, boolean addToPrevious
	)
	{
		if(addToPrevious&&process instanceof MultiblockProcessInWorld)
		{
			// Pattern variables look fine in IntelliJ, but cause the "real" compiler to complain. Probably related to
			// the CTX type parameter
			@SuppressWarnings("PatternVariableCanBeUsed") final MultiblockProcessInWorld<R> newProcess = (MultiblockProcessInWorld<R>)process;
			for(MultiblockProcess<R, CTX> curr : processQueue)
				if(curr instanceof MultiblockProcessInWorld&&process.getRecipeId().equals(curr.getRecipeId()))
				{
					boolean canStack = true;
					final MultiblockProcessInWorld<R> existingProcess = (MultiblockProcessInWorld<R>)curr;
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
		if(maxQueueLength < 0||processQueue.size() < maxQueueLength)
		{
			float dist = Float.POSITIVE_INFINITY;
			if(processQueue.size() > 0)
			{
				MultiblockProcess<R, CTX> lastBefore = processQueue.get(processQueue.size()-1);
				dist = lastBefore.processTick/(float)lastBefore.getMaxTicks(level);
			}
			if(dist < minProcessDelay)
				return false;

			if(!simulate)
				processQueue.add(process);
			markDirty.run();
			return true;
		}
		return false;
	}

	public RecipeSource<R> getRecipeSource()
	{
		return recipeSource;
	}

	public int getMaxQueueSize()
	{
		return maxQueueLength;
	}

	public int getQueueSize()
	{
		return processQueue.size();
	}

	public List<MultiblockProcess<R, CTX>> getQueue()
	{
		return Collections.unmodifiableList(processQueue);
	}

	public record RecipeSource<R extends MultiblockRecipe>(
			BiFunction<Level, ItemStack, @Nullable R> getRecipeOnInsert,
			BiFunction<Level, ResourceLocation, @Nullable R> getRecipeFromID
	)
	{
	}

	public interface ProcessLoader<R extends MultiblockRecipe, CTX extends ProcessContext<R>>
	{
		MultiblockProcess<R, CTX> fromNBT(BiFunction<Level, ResourceLocation, R> getRecipe, CompoundTag data);
	}
}
