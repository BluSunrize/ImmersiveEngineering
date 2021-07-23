package blusunrize.immersiveengineering.api.tool.assembler;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

public class IngredientRecipeQuery extends RecipeQuery
{
	private final Ingredient ingredient;
	private final int size;

	public IngredientRecipeQuery(Ingredient ingredient, int size)
	{
		this.ingredient = ingredient;
		this.size = size;
	}

	@Override
	public boolean matchesIgnoringSize(ItemStack stack)
	{
		return ingredient.test(stack);
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
		return size;
	}

	@Override
	public boolean isFluid()
	{
		return false;
	}
}
