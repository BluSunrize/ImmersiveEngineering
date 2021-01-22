package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.tool.AssemblerHandler;
import blusunrize.immersiveengineering.api.tool.AssemblerHandler.IRecipeAdapter;
import blusunrize.immersiveengineering.api.tool.AssemblerHandler.RecipeQuery;
import blusunrize.immersiveengineering.common.util.FakePlayerUtil;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.RecipeMatcher;

import java.util.List;
import java.util.function.Predicate;

public class DefaultAssemblerAdapter implements IRecipeAdapter<IRecipe<CraftingInventory>>
{
	@Override
	public RecipeQuery[] getQueriedInputs(IRecipe<CraftingInventory> recipe, NonNullList<ItemStack> input, World world)
	{
		NonNullList<Ingredient> ingred = recipe.getIngredients();
		// Check that the ingredients roughly match what the recipe actually requires.
		// This is necessary to prevent infinite crafting for recipes like FireworkRocketRecipe which don't return
		// meaningful values in getIngredients.
		NonNullList<Predicate<ItemStack>> ingredientsForMatching = NonNullList.create();
		List<ItemStack> inputList = input.subList(0, input.size()-1);
		for(Ingredient i : ingred)
			if(!i.hasNoMatchingItems())
				ingredientsForMatching.add(i);
		final int numNonEmpty = ingredientsForMatching.size();
		while(ingredientsForMatching.size() < inputList.size())
			ingredientsForMatching.add(ItemStack::isEmpty);
		ForgeHooks.setCraftingPlayer(FakePlayerUtil.getFakePlayer(world));
		int[] ingredientAssignment = RecipeMatcher.findMatches(inputList, ingredientsForMatching);
		ForgeHooks.setCraftingPlayer(null);

		// - 1: Input list contains the output slot
		RecipeQuery[] query = new RecipeQuery[input.size()-1];
		if(ingredientAssignment!=null)
			// If the ingredients provided by the recipe are plausible request those
			// Try to request each ingredient at the index where it is in the input pattern, this is needed for
			// some CraftTweaker recipes
			for(int stackIndex = 0; stackIndex < ingredientAssignment.length; stackIndex++)
			{
				int ingredIndex = ingredientAssignment[stackIndex];
				if(ingredIndex < numNonEmpty)
					query[stackIndex] = AssemblerHandler.createQueryFromIngredient(
							(Ingredient)ingredientsForMatching.get(ingredIndex)
					);
			}
		else
			// Otherwise request the exact stacks used in the input
			for(int i = 0; i < query.length; i++)
				if(!input.get(i).isEmpty())
					query[i] = AssemblerHandler.createQueryFromItemStack(input.get(i));
		return query;
	}
}
