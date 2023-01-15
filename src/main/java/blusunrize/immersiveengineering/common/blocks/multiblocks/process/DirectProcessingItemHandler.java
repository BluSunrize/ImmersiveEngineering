/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.process;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext.ProcessContextInWorld;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.InsertOnlyInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class DirectProcessingItemHandler<R extends MultiblockRecipe> extends InsertOnlyInventory
{
	private static final float TRANSFORMATION_POINT = .5f;
	private boolean doProcessStacking = false;
	protected final Supplier<Level> level;
	protected final MultiblockProcessor<R, ProcessContextInWorld<R>> processor;
	private final BiFunction<Level, ItemStack, @Nullable R> getRecipeOnInsert;

	public DirectProcessingItemHandler(
			Supplier<Level> level,
			MultiblockProcessor<R, ProcessContextInWorld<R>> processor,
			BiFunction<Level, ItemStack, @Nullable R> getRecipeOnInsert
	)
	{
		this.level = level;
		this.processor = processor;
		this.getRecipeOnInsert = getRecipeOnInsert;
	}

	public DirectProcessingItemHandler<R> setProcessStacking(boolean stacking)
	{
		this.doProcessStacking = stacking;
		return this;
	}

	@Override
	protected ItemStack insert(ItemStack stack, boolean simulate)
	{
		stack = stack.copy();
		R recipe = getRecipeOnInsert.apply(level.get(), stack);
		if(recipe==null)
			return stack;
		ItemStack displayStack = recipe.getDisplayStack(stack);
		displayStack = ItemHandlerHelper.copyStackWithSize(stack, displayStack.getCount());
		if(processor.addProcessToQueue(new MultiblockProcessInWorld<>(
				recipe,
				TRANSFORMATION_POINT,
				Utils.createNonNullItemStackListFromItemStack(displayStack)
		), level.get(), simulate, doProcessStacking))
			stack.shrink(displayStack.getCount());
		return stack;
	}
}
