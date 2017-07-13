package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.crafting.CraftingHelper.ShapedPrimer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class RecipeShapedIngredient extends ShapedOreRecipe
{
	NonNullList<Ingredient> ingredientsQuarterTurn;
	NonNullList<Ingredient> ingredientsEighthTurn;
	int nbtCopyTargetSlot = -1;
	int lastMatch = 0;
	public RecipeShapedIngredient(ResourceLocation group, ItemStack result, Object... recipe)
	{
		super(group, result, wrapIngredients(recipe));
	}
	public RecipeShapedIngredient(ResourceLocation group, ItemStack result, ShapedPrimer primer)
	{
		super(group, result, primer);
	}

	private static Object[] wrapIngredients(Object... recipe)
	{
		Object[] out = new Object[recipe.length];
		for(int i=0; i<recipe.length; i++)
			if(recipe[i] instanceof IngredientStack)
				out[i] = new IngredientIngrStack((IngredientStack)recipe[i]);
			else
				out[i] = recipe[i];
		return out;
	}

	public RecipeShapedIngredient allowQuarterTurn()
	{
		ingredientsQuarterTurn = NonNullList.withSize(getIngredients().size(), Ingredient.EMPTY);
		int maxH = (height - 1);
		for(int h = 0; h < height; h++)
			for(int w = 0; w < width; w++)
				ingredientsQuarterTurn.set(w * height + (maxH - h), getIngredients().get(h * width + w));
		return this;
	}

	static int[] eighthTurnMap = {3, -1, -1, 3, 0, -3, 1, 1, -3};

	public RecipeShapedIngredient allowEighthTurn()
	{
		if(width != 3 || height != 3)//Recipe won't allow 8th turn when not a 3x3 square
			return this;
		ingredientsEighthTurn = NonNullList.withSize(getIngredients().size(), Ingredient.EMPTY);
		int maxH = (height - 1);
		for(int h = 0; h < height; h++)
			for(int w = 0; w < width; w++)
			{
				int i = h * width + w;
				ingredientsEighthTurn.set(i + eighthTurnMap[i], getIngredients().get(i));
			}
		return this;
	}

	public RecipeShapedIngredient setNBTCopyTargetRecipe(int slot)
	{
		this.nbtCopyTargetSlot = slot;
		return this;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting matrix)
	{
		if(nbtCopyTargetSlot >= 0)
		{
			ItemStack out = output.copy();
			if(!matrix.getStackInSlot(nbtCopyTargetSlot).isEmpty() && matrix.getStackInSlot(nbtCopyTargetSlot).hasTagCompound())
				out.setTagCompound(matrix.getStackInSlot(nbtCopyTargetSlot).getTagCompound().copy());
			return out;
		}
		else
			return super.getCraftingResult(matrix);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) //getRecipeLeftovers
	{
		NonNullList<ItemStack> remains = ForgeHooks.defaultRecipeGetRemainingItems(inv);
		for(int i = 0; i < height*width; i++)
		{
			ItemStack s = inv.getStackInSlot(i);
			NonNullList<Ingredient> matchedIngr = lastMatch==1?ingredientsQuarterTurn: lastMatch==2?ingredientsEighthTurn: this.input;
			if((!remains.get(i).isEmpty() || !s.isEmpty()) && matchedIngr.get(i) instanceof IngredientFluidStack)
			{
				if(remains.get(i).isEmpty() && !s.isEmpty())
					remains.set(i, s.copy());
				System.out.println("Had fluid ingredient for "+remains.get(i));
				IFluidHandler handler = FluidUtil.getFluidHandler(remains.get(i));
				if(handler!=null)
				{
					FluidStack fluid = ((IngredientFluidStack)matchedIngr.get(i)).getFluid();
					handler.drain(fluid.amount, true);
				}
				if(remains.get(i).getCount()<=0)
					remains.set(i, ItemStack.EMPTY);
			}
		}
		return remains;
	}

	@Override
	protected boolean checkMatch(InventoryCrafting inv, int startX, int startY, boolean mirror)
	{
		if(checkMatchDo(inv, getIngredients(), startX, startY, mirror, false))
		{
			lastMatch = 0;
			return true;
		}
		else if(ingredientsQuarterTurn != null && checkMatchDo(inv, ingredientsQuarterTurn, startX, startY, mirror, true))
		{
			lastMatch = 1;
			return true;
		}
		else if(ingredientsEighthTurn != null && checkMatchDo(inv, ingredientsEighthTurn, startX, startY, mirror, false))
		{
			lastMatch = 2;
			return true;
		}
		return false;
	}

	protected boolean checkMatchDo(InventoryCrafting inv, NonNullList<Ingredient> ingredients, int startX, int startY, boolean mirror, boolean rotate)
	{
		for(int x = 0; x < MAX_CRAFT_GRID_WIDTH; x++)
			for(int y = 0; y < MAX_CRAFT_GRID_HEIGHT; y++)
			{
				int subX = x - startX;
				int subY = y - startY;
				Ingredient target = null;

				if(!rotate)
				{
					if(subX >= 0 && subY >= 0 && subX < width && subY < height)
						if(mirror)
							target = ingredients.get(width - subX - 1 + subY * width);
						else
							target = ingredients.get(subX + subY * width);
				} else
				{
					if(subX >= 0 && subY >= 0 && subX < height && subY < width)
						if(mirror)
							target = ingredients.get(height - subX - 1 + subY * width);
						else
							target = ingredients.get(subY + subX * height);
				}

				ItemStack slot = inv.getStackInRowAndColumn(x, y);
//				if((target == null) != (slot.isEmpty()))
//					return false;
//				else if(target != null && !target.apply(slot))
//					return false;
				if(!target.apply(slot))
					return false;
			}
		return true;
	}
}