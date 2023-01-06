/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic.sawmill;

import blusunrize.immersiveengineering.api.crafting.SawmillRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.Set;

public class SawmillProcess
{
	private static final double STRIP_THRESHOLD = .3125;
	private static final double SAWING_THRESHOLD = .8625;

	private final ItemStack input;
	private RecipeDependentData recipeDependentData;
	private int processTick;
	private boolean stripped = false;
	private boolean sawed = false;
	private boolean processFinished = false;

	public SawmillProcess(ItemStack input)
	{
		this.input = input;
	}

	private RecipeDependentData getRecipeDependentData(Level level)
	{
		if(this.recipeDependentData==null)
		{
			SawmillRecipe recipe = SawmillRecipe.findRecipe(level, input);
			if(recipe!=null)
				this.recipeDependentData = new RecipeDependentData(
						recipe,
						recipe.getTotalProcessTime(),
						recipe.getTotalProcessEnergy()/recipe.getTotalProcessTime()
				);
			else
				this.recipeDependentData = new RecipeDependentData(null, 80, 40);
		}
		return this.recipeDependentData;
	}

	public boolean processStep(
			Level level, IEnergyStorage energy, ItemStack sawblade, Set<ItemStack> secondaries
	)
	{
		RecipeDependentData data = getRecipeDependentData(level);
		if(energy.extractEnergy(data.energyPerTick, true) < data.energyPerTick)
			return false;
		energy.extractEnergy(data.energyPerTick, false);
		this.processTick++;
		float relative = getRelativeProcessStep(level);
		if(data.recipe!=null)
		{
			if(!this.stripped&&relative >= STRIP_THRESHOLD)
			{
				this.stripped = true;
				data.recipe.secondaryStripping.stream()
						.map(Lazy::get)
						.forEach(secondaries::add);
			}
			if(!this.sawed&&relative >= SAWING_THRESHOLD)
			{
				this.sawed = true;
				if(!sawblade.isEmpty())
					data.recipe.secondaryOutputs.stream()
							.map(Lazy::get)
							.forEach(secondaries::add);
			}
		}
		if(relative >= 1)
			this.processFinished = true;
		return true;
	}

	public void incrementProcessOnClient()
	{
		++this.processTick;
	}

	public float getRelativeProcessStep(Level level)
	{
		return this.processTick/getRecipeDependentData(level).maxProcessTicks;
	}

	public ItemStack getCurrentStack(Level level, boolean sawblade)
	{
		RecipeDependentData data = getRecipeDependentData(level);
		if(data.recipe==null)
			return this.input;
		final double relativeProgress = this.processTick/(double)data.maxProcessTicks;
		// Moved past sawblade and the blade was/is there
		if(relativeProgress > SAWING_THRESHOLD&&sawblade)
			return data.recipe.output.get();
		// Not stripped yet
		if(relativeProgress < STRIP_THRESHOLD)
			return this.input;
		// After stripping, before sawing (or no blade)
		ItemStack stripped = data.recipe.stripped.get();
		if(stripped.isEmpty())
			stripped = this.input;
		return stripped;
	}

	public boolean isSawing(Level level)
	{
		return getRelativeProcessStep(level) > .5375&&!this.sawed;
	}

	public CompoundTag writeToNBT()
	{
		CompoundTag nbt = new CompoundTag();
		nbt.put("input", this.input.save(new CompoundTag()));
		nbt.putInt("processTick", this.processTick);
		nbt.putBoolean("stripped", this.stripped);
		nbt.putBoolean("sawed", this.sawed);
		return nbt;
	}

	public boolean isProcessFinished()
	{
		return processFinished;
	}

	public ItemStack getInput()
	{
		return input;
	}

	public static SawmillProcess readFromNBT(CompoundTag nbt)
	{
		ItemStack input = ItemStack.of(nbt.getCompound("input"));
		SawmillProcess process = new SawmillProcess(input);
		process.processTick = nbt.getInt("processTick");
		process.stripped = nbt.getBoolean("stripped");
		process.sawed = nbt.getBoolean("sawed");
		return process;
	}

	private record RecipeDependentData(SawmillRecipe recipe, float maxProcessTicks, int energyPerTick)
	{
	}
}
