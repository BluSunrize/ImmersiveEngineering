package blusunrize.immersiveengineering.common.util.compat.crafttweaker.actions;

import com.blamejared.crafttweaker.api.brackets.CommandStringDisplayable;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.blamejared.crafttweaker.impl.fluid.MCFluidStackMutable;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fluids.FluidStack;

public class ActionAddRecipeCustomOutput extends ActionAddRecipe
{
	private final String output;

	public ActionAddRecipeCustomOutput(IRecipeManager recipeManager, IRecipe<?> recipe, FluidStack output) {
		this(recipeManager, recipe, new MCFluidStackMutable(output).getCommandString());
	}

	public ActionAddRecipeCustomOutput(IRecipeManager recipeManager, IRecipe<?> recipe, CommandStringDisplayable output) {
		this(recipeManager, recipe, output.getCommandString());
	}

	public ActionAddRecipeCustomOutput(IRecipeManager recipeManager, IRecipe<?> recipe, String output)
	{
		super(recipeManager, recipe, null);
		this.output = output;
	}

	@Override
	protected String describeOutputs()
	{
		return output;
	}
}
