/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic.arcfurnace;

import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext.ProcessContextInMachine;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.BiFunction;

import static blusunrize.immersiveengineering.common.blocks.multiblocks.logic.arcfurnace.ArcFurnaceLogic.ADDITIVE_SLOT_COUNT;
import static blusunrize.immersiveengineering.common.blocks.multiblocks.logic.arcfurnace.ArcFurnaceLogic.FIRST_ADDITIVE_SLOT;

public class ArcFurnaceProcess extends MultiblockProcessInMachine<ArcFurnaceRecipe>
{
	private final long seed;

	public ArcFurnaceProcess(BiFunction<Level, ResourceLocation, ArcFurnaceRecipe> getRecipe, CompoundTag data)
	{
		super(getRecipe, data);
		this.seed = data.getLong("seed");
	}

	public ArcFurnaceProcess(ArcFurnaceRecipe recipe, long seed, int... inputSlots)
	{
		super(recipe, inputSlots);
		this.seed = seed;
	}

	@Override
	public void writeExtraDataToNBT(CompoundTag nbt)
	{
		super.writeExtraDataToNBT(nbt);
		nbt.putLong("seed", seed);
	}

	@Override
	protected List<ItemStack> getRecipeItemOutputs(Level level, ProcessContextInMachine<ArcFurnaceRecipe> context)
	{
		ArcFurnaceRecipe recipe = getRecipe(level);
		if(recipe==null)
			return NonNullList.create();
		ItemStack input = context.getInventory().getStackInSlot(this.inputSlots[0]);
		NonNullList<ItemStack> additives = NonNullList.withSize(ADDITIVE_SLOT_COUNT, ItemStack.EMPTY);
		for(int i = 0; i < ADDITIVE_SLOT_COUNT; i++)
			additives.set(i, context.getInventory().getStackInSlot(FIRST_ADDITIVE_SLOT+i).copy());
		return recipe.generateActualOutput(input, additives, seed);
	}

	@Override
	protected void processFinish(ProcessContextInMachine<ArcFurnaceRecipe> context, IMultiblockLevel level)
	{
		super.processFinish(context, level);
		if(context instanceof ArcFurnaceLogic.State state)
			state.pouringMetal = 40;
	}
}
