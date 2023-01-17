/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.process;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext.ProcessContextInWorld;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.function.BiFunction;

public class MultiblockProcessInWorld<R extends MultiblockRecipe>
		extends MultiblockProcess<R, ProcessContextInWorld<R>>
{
	public NonNullList<ItemStack> inputItems;
	protected float transformationPoint;

	public MultiblockProcessInWorld(
			ResourceLocation recipeId, BiFunction<Level, ResourceLocation, R> getRecipe,
			float transformationPoint, NonNullList<ItemStack> inputItem
	)
	{
		super(recipeId, getRecipe);
		this.inputItems = inputItem;
		this.transformationPoint = transformationPoint;
	}

	public MultiblockProcessInWorld(
			R recipe, float transformationPoint, NonNullList<ItemStack> inputItem
	)
	{
		super(recipe);
		this.inputItems = inputItem;
		this.transformationPoint = transformationPoint;
	}

	public MultiblockProcessInWorld(
			BiFunction<Level, ResourceLocation, R> getRecipe, CompoundTag data
	)
	{
		super(getRecipe, data);
		this.inputItems = NonNullList.withSize(data.getInt("numInputs"), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(data, this.inputItems);
		this.transformationPoint = data.getFloat("process_transformationPoint");
	}

	public MultiblockProcessInWorld(R recipe, ItemStack input)
	{
		this(recipe, 0.5f, Utils.createNonNullItemStackListFromItemStack(input));
	}

	public List<ItemStack> getDisplayItem(Level level)
	{
		LevelDependentData<R> levelData = getLevelData(level);
		if(processTick/(float)levelData.maxTicks() > transformationPoint&&levelData.recipe()!=null)
		{
			List<ItemStack> list = levelData.recipe().getItemOutputs();
			if(!list.isEmpty())
				return list;
		}
		return inputItems;
	}

	@Override
	public void writeExtraDataToNBT(CompoundTag nbt)
	{
		ContainerHelper.saveAllItems(nbt, inputItems);
		nbt.putInt("numInputs", inputItems.size());
		nbt.putFloat("process_transformationPoint", transformationPoint);
	}

	@Override
	protected boolean canOutputItem(ProcessContextInWorld<R> context, ItemStack output)
	{
		return true;
	}

	@Override
	protected boolean canOutputFluid(ProcessContextInWorld<R> context, FluidStack output)
	{
		return false;
	}

	@Override
	protected void outputItem(ProcessContextInWorld<R> context, ItemStack output, IMultiblockLevel level)
	{
		context.doProcessOutput(output, level);
	}

	@Override
	protected void outputFluid(ProcessContextInWorld<R> context, FluidStack output)
	{
		context.doProcessFluidOutput(output);
	}

	@Override
	protected void processFinish(ProcessContextInWorld<R> context, IMultiblockLevel level)
	{
		super.processFinish(context, level);
		int size = -1;

		R recipe = getLevelData(level.getRawLevel()).recipe();
		if(recipe==null)
			return;
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
