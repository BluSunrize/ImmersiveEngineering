package blusunrize.immersiveengineering.api.tool.assembler;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class FluidStackRecipeQuery extends RecipeQuery
{
	private final FluidStack fluidStack;

	public FluidStackRecipeQuery(FluidStack stack)
	{
		this.fluidStack = stack;
	}

	@Override
	public boolean matchesIgnoringSize(ItemStack stack)
	{
		return FluidUtil.getFluidContained(stack)
				.map(fs -> fs.containsFluid(fluidStack))
				.orElse(false);
	}

	@Override
	public boolean matchesFluid(FluidStack fluid)
	{
		return fluid.containsFluid(fluidStack);
	}

	@Override
	public int getFluidSize()
	{
		return fluidStack.getAmount();
	}

	@Override
	public int getItemCount()
	{
		return 1;
	}

	@Override
	public boolean isFluid()
	{
		return true;
	}
}
