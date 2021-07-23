package blusunrize.immersiveengineering.api.tool.assembler;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class ItemStackRecipeQuery extends RecipeQuery
{
	private final ItemStack stack;

	public ItemStackRecipeQuery(ItemStack stack)
	{
		this.stack = stack;
	}

	@Override
	public boolean matchesIgnoringSize(ItemStack stack)
	{
		return ItemStack.isSame(this.stack, stack)&&ItemStack.tagMatches(this.stack, stack);
	}

	@Override
	public boolean matchesFluid(FluidStack fluid)
	{
		throw new RuntimeException("Not a fluid ingredient!");
	}

	@Override
	public int getFluidSize()
	{
		throw new RuntimeException("Not a fluid ingredient!");
	}

	@Override
	public int getItemCount()
	{
		return stack.getCount();
	}

	@Override
	public boolean isFluid()
	{
		return false;
	}
}
