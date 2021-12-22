package blusunrize.immersiveengineering.api.tool.assembler;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class FluidTagRecipeQuery extends RecipeQuery
{
	private final FluidTagInput tag;

	public FluidTagRecipeQuery(FluidTagInput stack)
	{
		this.tag = stack;
	}

	@Override
	public boolean matchesIgnoringSize(ItemStack stack)
	{
		return FluidUtil.getFluidContained(stack)
				.map(tag::testIgnoringAmount)
				.orElse(false);
	}

	@Override
	public boolean matchesFluid(FluidStack fluid)
	{
		return tag.test(fluid);
	}

	@Override
	public int getFluidSize()
	{
		return tag.getAmount();
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
