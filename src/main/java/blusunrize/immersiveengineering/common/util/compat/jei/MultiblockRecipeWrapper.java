package blusunrize.immersiveengineering.common.util.compat.jei;

import java.util.List;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class MultiblockRecipeWrapper extends BlankRecipeWrapper
{
	public final List<ItemStack>[] recipeInputs;
	private final List<ItemStack> inputs;
	public final List<ItemStack>[] recipeOutputs;
	private final List<ItemStack> outputs;
	private final List<FluidStack> fluidInputs;
	private final List<FluidStack> fluidOutputs;
	public MultiblockRecipeWrapper(MultiblockRecipe recipe)
	{
		recipe.setupJEI();
		this.inputs = recipe.getJEITotalItemInputs();
		this.recipeInputs = recipe.jeiItemInputList;
		this.outputs = recipe.getJEITotalItemOutputs();
		this.recipeOutputs = recipe.jeiItemOutputList;
		this.fluidInputs = recipe.getJEITotalFluidInputs();
		this.fluidOutputs = recipe.getJEITotalFluidOutputs();
	}
	@Override
	public List<ItemStack> getInputs()
	{
		return inputs;
	}
	@Override
	public List<ItemStack> getOutputs()
	{
		return outputs;
	}
	@Override
	public List<FluidStack> getFluidInputs()
	{
		return fluidInputs;
	}
	@Override
	public List<FluidStack> getFluidOutputs()
	{
		return fluidOutputs;
	}
}