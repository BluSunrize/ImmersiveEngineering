package blusunrize.immersiveengineering.common.util.inventory;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public abstract class InsertOnlyInventory implements IItemHandler
{
	@Override
	public final int getSlots()
	{
		return 1;
	}

	@Override
	public final @NotNull ItemStack getStackInSlot(int slot)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public final @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate)
	{
		return insert(stack, simulate);
	}

	@Override
	public final @NotNull ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public final int getSlotLimit(int slot)
	{
		return 64;
	}

	@Override
	public final boolean isItemValid(int slot, @NotNull ItemStack stack)
	{
		return true;
	}

	protected abstract ItemStack insert(ItemStack toInsert, boolean simulate);
}
