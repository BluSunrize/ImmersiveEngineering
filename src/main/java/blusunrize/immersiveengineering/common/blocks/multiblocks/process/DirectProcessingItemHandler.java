package blusunrize.immersiveengineering.common.blocks.multiblocks.process;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext.ProcessContextInWorld;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class DirectProcessingItemHandler<R extends MultiblockRecipe> implements IItemHandlerModifiable
{
	private static final float TRANSFORMATION_POINT = .5f;
	private boolean doProcessStacking = false;
	private final Supplier<Level> level;
	private final MultiblockProcessor<R, ProcessContextInWorld<R>> processor;
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
		R recipe = getRecipeOnInsert.apply(level.get(), stack);
		if(recipe==null)
			return stack;
		ItemStack displayStack = recipe.getDisplayStack(stack);
		if(processor.addProcessToQueue(new MultiblockProcessInWorld<>(
				recipe,
				TRANSFORMATION_POINT,
				Utils.createNonNullItemStackListFromItemStack(displayStack)
		), level.get(), simulate, doProcessStacking))
			stack.shrink(displayStack.getCount());
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
