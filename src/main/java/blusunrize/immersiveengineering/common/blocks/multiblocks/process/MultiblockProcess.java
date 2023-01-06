/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.process;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;

public abstract class MultiblockProcess<R extends MultiblockRecipe, CTX extends ProcessContext<R>>
{
	private final ResourceLocation recipeId;
	private final BiFunction<Level, ResourceLocation, R> getRecipe;
	public int processTick;
	private LevelDependentData<R> levelData;
	public boolean clearProcess = false;

	private int extraProcessTicks = -1;

	public MultiblockProcess(ResourceLocation recipeId, BiFunction<Level, ResourceLocation, R> getRecipe)
	{
		this.recipeId = recipeId;
		this.getRecipe = getRecipe;
		this.processTick = 0;
	}

	public MultiblockProcess(R recipe)
	{
		this.recipeId = recipe.getId();
		this.getRecipe = ($, $1) -> {
			throw new RuntimeException("A process initialized with a recipe should never query recipes");
		};
		this.processTick = 0;
		populateLevelData(recipe);
	}

	public MultiblockProcess(
			BiFunction<Level, ResourceLocation, R> getRecipe, CompoundTag data
	)
	{
		this(new ResourceLocation(data.getString("recipe")), getRecipe);
		this.processTick = data.getInt("process_processTick");
	}

	protected List<ItemStack> getRecipeItemOutputs(Level level, CTX context)
	{
		R recipe = getLevelData(level).recipe;
		if(recipe==null)
			return List.of();
		return recipe.getActualItemOutputs();
	}

	protected List<FluidStack> getRecipeFluidOutputs(Level level)
	{
		R recipe = getLevelData(level).recipe;
		if(recipe==null)
			return List.of();
		return recipe.getActualFluidOutputs();
	}

	public boolean canProcess(CTX context, Level level)
	{
		LevelDependentData<R> levelData = getLevelData(level);
		if(levelData.recipe==null)
			return true;
		if(context.getEnergy().extractEnergy(levelData.energyPerTick, true)==levelData.energyPerTick)
		{
			List<ItemStack> outputs = getRecipeItemOutputs(level, context);
			if(outputs!=null)
				for(ItemStack output : outputs)
					if(!output.isEmpty()&&!canOutputItem(context, output))
						return false;
			List<FluidStack> fluidOutputs = levelData.recipe.getFluidOutputs();
			if(fluidOutputs!=null)
				for(FluidStack output : fluidOutputs)
					if(!canOutputFluid(context, output))
						return false;
			return context.additionalCanProcessCheck(this, level);
		}
		return false;
	}

	public void doProcessTick(CTX context, IMultiblockLevel level)
	{
		final Level rawLevel = level.getRawLevel();
		LevelDependentData<R> levelData = getLevelData(rawLevel);
		if(levelData.recipe==null)
		{
			this.clearProcess = true;
			return;
		}
		// perform initial tick
		context.getEnergy().extractEnergy(levelData.energyPerTick, false);
		this.processTick += 1;

		// initialize if not yet happened
		if(extraProcessTicks < 0)
			extraProcessTicks = levelData.recipe.getMultipleProcessTicks();

		if(extraProcessTicks >= 0)
		{
			int averageInsertion = context.getEnergy().getAverageInsertion();
			int averageExtraction = context.getEnergy().getAverageExtraction();
			if(averageInsertion < averageExtraction)
				extraProcessTicks = Math.max(0, extraProcessTicks-1);
			else if(averageInsertion > averageExtraction)
				extraProcessTicks = Math.min(levelData.recipe.getMultipleProcessTicks(), extraProcessTicks+1);

			int possibleTicks = Math.min(
					Math.min(
							extraProcessTicks, // extra ticks that can step up and down
							levelData.maxTicks-this.processTick // ticks remaining in the recipe
					),
					Math.min(
							averageInsertion/levelData.energyPerTick, // max ticks possible with current insertion
							context.getEnergy().getEnergyStored()/levelData.energyPerTick // max ticks possible with current stored power
					)
			);
			if(possibleTicks > 0)
			{
				context.getEnergy().extractEnergy(levelData.energyPerTick*possibleTicks, false);
				this.processTick += possibleTicks;
			}
		}

		if(this.processTick >= levelData.maxTicks)
			this.processFinish(context, level);
	}

	protected void processFinish(CTX context, IMultiblockLevel level)
	{
		final Level rawLevel = level.getRawLevel();
		List<ItemStack> outputs = getRecipeItemOutputs(rawLevel, context);
		if(outputs!=null)
			for(ItemStack output : outputs)
				outputItem(context, output, level);
		List<FluidStack> fluidOutputs = getRecipeFluidOutputs(rawLevel);
		if(fluidOutputs!=null)
			for(FluidStack output : fluidOutputs)
				outputFluid(context, output);

		context.onProcessFinish(this, level.getRawLevel());
		this.clearProcess = true;
	}

	public abstract void writeExtraDataToNBT(CompoundTag nbt);

	protected abstract boolean canOutputItem(CTX context, ItemStack output);

	protected abstract boolean canOutputFluid(CTX context, FluidStack output);

	protected abstract void outputItem(CTX context, ItemStack output, IMultiblockLevel level);

	protected abstract void outputFluid(CTX context, FluidStack output);

	protected LevelDependentData<R> getLevelData(Level level)
	{
		if(levelData==null)
			populateLevelData(getRecipe.apply(level, recipeId));
		return levelData;
	}

	private void populateLevelData(R recipe)
	{
		if(recipe!=null)
		{
			int maxTicks = recipe.getTotalProcessTime();
			int energyPerTick = recipe.getTotalProcessEnergy()/maxTicks;
			this.levelData = new LevelDependentData<>(recipe, maxTicks, energyPerTick);
		}
		else
			this.levelData = new LevelDependentData<>(null, 20, 0);
	}

	public ResourceLocation getRecipeId()
	{
		return recipeId;
	}

	public int getMaxTicks(Level level)
	{
		return getLevelData(level).maxTicks;
	}

	@Nullable
	public R getRecipe(Level level)
	{
		return getLevelData(level).recipe;
	}

	protected record LevelDependentData<R extends MultiblockRecipe>(@Nullable R recipe, int maxTicks, int energyPerTick)
	{
	}
}
