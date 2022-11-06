package blusunrize.immersiveengineering.common.util.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public final class SlotwiseItemHandler implements IItemHandlerModifiable
{
	private final ItemStackHandler rawHandler;
	private final List<IOConstraint> slotConstraints;

	public SlotwiseItemHandler(List<IOConstraint> slotConstraints, Runnable onChanged)
	{
		this.rawHandler = new ItemStackHandler(slotConstraints.size())
		{
			@Override
			protected void onContentsChanged(int slot)
			{
				super.onContentsChanged(slot);
				onChanged.run();
			}
		};
		this.slotConstraints = slotConstraints;
	}

	@Override
	public int getSlots()
	{
		return rawHandler.getSlots();
	}

	@Override
	public @NotNull ItemStack getStackInSlot(int slot)
	{
		return rawHandler.getStackInSlot(slot);
	}

	@Override
	@NotNull
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate)
	{
		if(!this.slotConstraints.get(slot).allowInsert.test(stack))
			return stack;
		return rawHandler.insertItem(slot, stack, simulate);
	}

	@Override
	@NotNull
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		if(!this.slotConstraints.get(slot).allowExtract())
			return ItemStack.EMPTY;
		return rawHandler.extractItem(slot, amount, simulate);
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return rawHandler.getSlotLimit(slot);
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack)
	{
		// TODO may not be entirely correct
		return rawHandler.isItemValid(slot, stack);
	}

	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack)
	{
		rawHandler.setStackInSlot(slot, stack);
	}

	public Tag serializeNBT()
	{
		return rawHandler.serializeNBT();
	}

	public void deserializeNBT(CompoundTag nbt)
	{
		rawHandler.deserializeNBT(nbt);
	}

	public ItemStackHandler getRawHandler()
	{
		return rawHandler;
	}

	public record IOConstraint(boolean allowExtract, Predicate<ItemStack> allowInsert)
	{
		public static final IOConstraint OUTPUT = new IOConstraint(true, $ -> false);

		public static IOConstraint input(Predicate<ItemStack> allow)
		{
			return new IOConstraint(false, allow);
		}
	}
}
