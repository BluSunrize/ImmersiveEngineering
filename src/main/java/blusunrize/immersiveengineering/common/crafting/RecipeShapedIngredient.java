package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class RecipeShapedIngredient extends ShapedOreRecipe
{
	IngredientStack[] ingredients;
	static IngredientStack[] tempIngredients;
	public RecipeShapedIngredient(ItemStack result, Object... recipe)
	{
		super(result, saveIngredients(recipe));
		ingredients = tempIngredients;
		for(int i=0; i<input.length; i++)
			if(ingredients[i]!=null)
				input[i] = ingredients[i].getShapedRecipeInput();
		tempIngredients = null;
	}

	public static Object[] saveIngredients(Object... recipe)
	{
		Object[] converted = new Object[recipe.length];
		String shape = "";
		boolean shapeDone = false;
		for(int i=0; i<converted.length; i++)
		{
			converted[i] = recipe[i];
			if(!shapeDone)
				if(recipe[i] instanceof String[])
				{
					String[] parts = ((String[])recipe[i]);
					for(String s : parts)
						shape += s;
				}
				else if(recipe[i] instanceof String)
					shape += (String)recipe[i];

			if(recipe[i] instanceof Character)
			{
				if(!shapeDone)
				{
					shapeDone = true;
					tempIngredients = new IngredientStack[shape.length()];
				}
				Character chr = (Character)recipe[i];
				Object in = recipe[i+1];
				IngredientStack ingredient = ApiUtils.createIngredientStack(in);
				if(ingredient!=null)
				{
					recipe[i+1] = Blocks.FIRE;//Temp Replacement, fixed in constructor
					for(int j=0; j<shape.length(); j++)
						if(chr.charValue()==shape.charAt(j))
							tempIngredients[j] = ingredient;
				}
			}
		}
		return converted;
	}

	@Override
	protected boolean checkMatch(InventoryCrafting inv, int startX, int startY, boolean mirror)
	{
		for(int x=0; x<MAX_CRAFT_GRID_WIDTH; x++)
			for(int y=0; y<MAX_CRAFT_GRID_HEIGHT; y++)
			{
				int subX = x-startX;
				int subY = y-startY;
				IngredientStack target = null;

				if(subX>=0 && subY>=0 && subX<width && subY<height)
					if(mirror)
						target = ingredients[width - subX - 1 + subY * width];
					else
						target = ingredients[subX + subY * width];

				ItemStack slot = inv.getStackInRowAndColumn(x, y);
				if((target==null)!=(slot==null))
					return false;
				else if(target!=null && !target.matchesItemStack(slot))
					return false;
			}
		return true;
	}
}