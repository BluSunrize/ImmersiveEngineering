/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.crafting.CraftingHelper.ShapedPrimer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.regex.Pattern;

public class RecipeShapedIngredient extends ShapedOreRecipe
{
	NonNullList<Ingredient> ingredientsQuarterTurn;
	NonNullList<Ingredient> ingredientsEighthTurn;
	int[] nbtCopyTargetSlot = null;
	Pattern nbtCopyPredicate = null;
	boolean nbtCopyMultiplyDecimals = false;
	int lastMatch = 0;
	int lastStartX = 0;
	int lastStartY = 0;

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
		for(int i = 0; i < recipe.length; i++)
			if(recipe[i] instanceof IngredientStack)
				out[i] = new IngredientIngrStack((IngredientStack)recipe[i]);
			else
				out[i] = recipe[i];
		return out;
	}

	public RecipeShapedIngredient allowQuarterTurn()
	{
		ingredientsQuarterTurn = NonNullList.withSize(getIngredients().size(), Ingredient.EMPTY);
		int maxH = (height-1);
		for(int h = 0; h < height; h++)
			for(int w = 0; w < width; w++)
				ingredientsQuarterTurn.set(w*height+(maxH-h), getIngredients().get(h*width+w));
		return this;
	}

	static int[] eighthTurnMap = {3, -1, -1, 3, 0, -3, 1, 1, -3};

	public RecipeShapedIngredient allowEighthTurn()
	{
		if(width!=3||height!=3)//Recipe won't allow 8th turn when not a 3x3 square
			return this;
		ingredientsEighthTurn = NonNullList.withSize(getIngredients().size(), Ingredient.EMPTY);
		int maxH = (height-1);
		for(int h = 0; h < height; h++)
			for(int w = 0; w < width; w++)
			{
				int i = h*width+w;
				ingredientsEighthTurn.set(i+eighthTurnMap[i], getIngredients().get(i));
			}
		return this;
	}

	public RecipeShapedIngredient setNBTCopyTargetRecipe(int... slot)
	{
		this.nbtCopyTargetSlot = slot;
		return this;
	}

	public RecipeShapedIngredient setNBTCopyPredicate(String pattern)
	{
		this.nbtCopyPredicate = Pattern.compile(pattern);
		return this;
	}

	public RecipeShapedIngredient setNBTCopyMultiplyDecimals(boolean multiply)
	{
		this.nbtCopyMultiplyDecimals = multiply;
		return this;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting matrix)
	{
		if(nbtCopyTargetSlot!=null)
		{
			ItemStack out = output.copy();
			NBTTagCompound tag = out.hasTagCompound()?out.getTagCompound(): new NBTTagCompound();
			for(int targetSlot : nbtCopyTargetSlot)
			{
				ItemStack s = matrix.getStackInSlot(targetSlot);
				if(!s.isEmpty()&&s.hasTagCompound())
					tag = ItemNBTHelper.combineTags(tag, s.getTagCompound(), nbtCopyPredicate, nbtCopyMultiplyDecimals);
			}
			if(!tag.isEmpty())
				out.setTagCompound(tag);
			return out;
		}
		else
			return super.getCraftingResult(matrix);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) //getRecipeLeftovers
	{
		NonNullList<ItemStack> remains = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
		for(int yy = 0; yy < this.height; yy++)
			for(int xx = 0; xx < this.width; xx++)
			{
				int i = this.width*yy+xx;
				int transposedI = inv.getWidth()*(yy+lastStartY)+(xx+lastStartX);
				ItemStack s = inv.getStackInSlot(transposedI);
				NonNullList<Ingredient> matchedIngr = lastMatch==1?ingredientsQuarterTurn: lastMatch==2?ingredientsEighthTurn: this.input;
				if(matchedIngr.get(i) instanceof IngredientFluidStack)
				{
					IFluidHandlerItem handler = FluidUtil.getFluidHandler(s.getCount() > 1?Utils.copyStackWithAmount(s, 1): s);
					if(handler!=null)
					{
						FluidStack fluid = ((IngredientFluidStack)matchedIngr.get(i)).getFluid();
						handler.drain(fluid.amount, true);
						remains.set(transposedI, handler.getContainer().copy());
					}
					else
						remains.set(transposedI, ForgeHooks.getContainerItem(s));
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
			lastStartX = startX;
			lastStartY = startY;
			return true;
		}
		else if(ingredientsQuarterTurn!=null&&checkMatchDo(inv, ingredientsQuarterTurn, startX, startY, mirror, true))
		{
			lastMatch = 1;
			lastStartX = startX;
			lastStartY = startY;
			return true;
		}
		else if(ingredientsEighthTurn!=null&&checkMatchDo(inv, ingredientsEighthTurn, startX, startY, mirror, false))
		{
			lastMatch = 2;
			lastStartX = startX;
			lastStartY = startY;
			return true;
		}
		return false;
	}

	protected boolean checkMatchDo(InventoryCrafting inv, NonNullList<Ingredient> ingredients, int startX, int startY, boolean mirror, boolean rotate)
	{
		for(int x = 0; x < inv.getWidth(); x++)
			for(int y = 0; y < inv.getHeight(); y++)
			{
				int subX = x-startX;
				int subY = y-startY;
				Ingredient target = Ingredient.EMPTY;

				if(!rotate)
				{
					if(subX >= 0&&subY >= 0&&subX < width&&subY < height)
						if(mirror)
							target = ingredients.get(width-subX-1+subY*width);
						else
							target = ingredients.get(subX+subY*width);
				}
				else
				{
					if(subX >= 0&&subY >= 0&&subX < height&&subY < width)
						if(mirror)
							target = ingredients.get(height-subX-1+subY*width);
						else
							target = ingredients.get(subY+subX*height);
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