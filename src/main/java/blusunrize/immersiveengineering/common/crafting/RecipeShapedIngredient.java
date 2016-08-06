package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class RecipeShapedIngredient extends ShapedOreRecipe
{
	static IngredientStack[] tempIngredients;

	IngredientStack[] ingredients;
	IngredientStack[] ingredientsQuarterTurn;
	IngredientStack[] ingredientsEighthTurn;
	int nbtCopyTargetSlot = -1;
	public RecipeShapedIngredient(ItemStack result, Object... recipe)
	{
		super(result, saveIngredients(recipe));
		setIngredients(tempIngredients);
		tempIngredients = null;
	}

	public RecipeShapedIngredient setIngredients(IngredientStack[] ingr)
	{
		ingredients = ingr;
		for(int i=0; i<input.length; i++)
			if(ingredients[i]!=null)
				input[i] = ingredients[i].getShapedRecipeInput();
		return this;
	}

	public RecipeShapedIngredient allowQuarterTurn()
	{
		ingredientsQuarterTurn = new IngredientStack[ingredients.length];
		int maxH = (height - 1);
		for(int h = 0; h < height; h++)
			for(int w = 0; w < width; w++)
				ingredientsQuarterTurn[w * height + (maxH - h)] = ingredients[h * width + w];
		return this;
	}

	static int[] eighthTurnMap = {3, -1, -1, 3, 0, -3, 1, 1, -3};

	public RecipeShapedIngredient allowEighthTurn()
	{
		if(width != 3 || height != 3)//Recipe won't allow 8th turn when not a 3x3 square
			return this;
		ingredientsEighthTurn = new IngredientStack[ingredients.length];
		int maxH = (height - 1);
		for(int h = 0; h < height; h++)
			for(int w = 0; w < width; w++)
			{
				int i = h * width + w;
				ingredientsEighthTurn[i + eighthTurnMap[i]] = ingredients[i];
			}
		return this;
	}

	public RecipeShapedIngredient setNBTCopyTargetRecipe(int slot)
	{
		this.nbtCopyTargetSlot = slot;
		return this;
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
	public ItemStack getCraftingResult(InventoryCrafting matrix)
	{
		if(nbtCopyTargetSlot >= 0)
		{
			ItemStack out = output.copy();
			if(matrix.getStackInSlot(nbtCopyTargetSlot) != null && matrix.getStackInSlot(nbtCopyTargetSlot).hasTagCompound())
				out.setTagCompound(matrix.getStackInSlot(nbtCopyTargetSlot).getTagCompound().copy());
			return out;
		} else
			return super.getCraftingResult(matrix);
	}

	@Override
	public ItemStack[] getRemainingItems(InventoryCrafting inv) //getRecipeLeftovers
	{
		ItemStack[] remains = ForgeHooks.defaultRecipeGetRemainingItems(inv);
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack s = inv.getStackInSlot(i);
			System.out.println("TEST " + i + " - " + s + " ~ " + s.getItem());
			if(s != null && remains[i] == null && s.getItem() instanceof UniversalBucket)
			{
				ItemStack empty = ((UniversalBucket) s.getItem()).getEmpty();
				if(empty != null)
					remains[i] = empty.copy();
			}
		}
		return remains;
	}

	@Override
	protected boolean checkMatch(InventoryCrafting inv, int startX, int startY, boolean mirror)
	{
		if(checkMatchDo(inv, ingredients, startX, startY, mirror, false))
			return true;
		else if(ingredientsQuarterTurn != null && checkMatchDo(inv, ingredientsQuarterTurn, startX, startY, mirror, true))
			return true;
		else if(ingredientsEighthTurn != null && checkMatchDo(inv, ingredientsEighthTurn, startX, startY, mirror, false))
			return true;
		return false;
	}

	protected boolean checkMatchDo(InventoryCrafting inv, IngredientStack[] ingredients, int startX, int startY, boolean mirror, boolean rotate)
	{
		for(int x = 0; x < MAX_CRAFT_GRID_WIDTH; x++)
			for(int y = 0; y < MAX_CRAFT_GRID_HEIGHT; y++)
			{
				int subX = x - startX;
				int subY = y - startY;
				IngredientStack target = null;

				if(!rotate)
				{
					if(subX >= 0 && subY >= 0 && subX < width && subY < height)
						if(mirror)
							target = ingredients[width - subX - 1 + subY * width];
						else
							target = ingredients[subX + subY * width];
				} else
				{
					if(subX >= 0 && subY >= 0 && subX < height && subY < width)
						if(mirror)
							target = ingredients[height - subX - 1 + subY * width];
						else
							target = ingredients[subY + subX * height];
				}

				ItemStack slot = inv.getStackInRowAndColumn(x, y);
				if((target == null) != (slot == null))
					return false;
				else if(target != null && !target.matchesItemStack(slot))
					return false;
			}
		return true;
	}
}