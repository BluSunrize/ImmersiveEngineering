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
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockBlockEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MultiblockProcessInWorld<R extends MultiblockRecipe> extends MultiblockProcess<R>
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
	public void writeExtraDataToNBT(CompoundTag nbt)
	{
		ContainerHelper.saveAllItems(nbt, inputItems);
		nbt.putInt("numInputs", inputItems.size());
		nbt.putFloat("process_transformationPoint", transformationPoint);
	}

	@Override
	protected void processFinish(PoweredMultiblockBlockEntity<?, R> multiblock)
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
