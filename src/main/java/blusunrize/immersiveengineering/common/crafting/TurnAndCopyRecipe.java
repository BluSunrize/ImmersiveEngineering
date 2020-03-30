/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

public class TurnAndCopyRecipe extends ShapedRecipe
{
	protected NonNullList<Ingredient> ingredientsQuarterTurn;
	protected NonNullList<Ingredient> ingredientsEighthTurn;
	protected int[] nbtCopyTargetSlot = null;
	protected Pattern nbtCopyPredicate = null;
	protected int lastMatch = 0;
	protected int lastStartX = 0;
	protected int lastStartY = 0;

	public TurnAndCopyRecipe(ResourceLocation id, String group, int width, int height, NonNullList<Ingredient> ingr,
							 ItemStack output)
	{
		super(id, group, width, height, ingr, output);
	}

	public void allowQuarterTurn()
	{
		ingredientsQuarterTurn = NonNullList.withSize(getIngredients().size(), Ingredient.EMPTY);
		int maxH = (getRecipeHeight()-1);
		for(int h = 0; h < getRecipeHeight(); h++)
			for(int w = 0; w < getRecipeWidth(); w++)
				ingredientsQuarterTurn.set(w*getRecipeHeight()+(maxH-h), getIngredients().get(h*getRecipeWidth()+w));
	}

	private static int[] eighthTurnMap = {3, -1, -1, 3, 0, -3, 1, 1, -3};

	public void allowEighthTurn()
	{
		if(getRecipeWidth()!=3||getRecipeHeight()!=3)//Recipe won't allow 8th turn when not a 3x3 square
			return;
		ingredientsEighthTurn = NonNullList.withSize(getIngredients().size(), Ingredient.EMPTY);
		int maxH = (getRecipeHeight()-1);
		for(int h = 0; h < getRecipeHeight(); h++)
			for(int w = 0; w < getRecipeWidth(); w++)
			{
				int i = h*getRecipeWidth()+w;
				ingredientsEighthTurn.set(i+eighthTurnMap[i], getIngredients().get(i));
			}
	}

	public void setNBTCopyTargetRecipe(int... slot)
	{
		this.nbtCopyTargetSlot = slot;
	}

	public void setNBTCopyPredicate(String pattern)
	{
		this.nbtCopyPredicate = Pattern.compile(pattern);
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(CraftingInventory matrix)
	{
		if(nbtCopyTargetSlot!=null)
		{
			ItemStack out = getRecipeOutput().copy();
			CompoundNBT tag = out.getOrCreateTag();
			for(int targetSlot : nbtCopyTargetSlot)
			{
				ItemStack s = matrix.getStackInSlot(targetSlot);
				if(!s.isEmpty()&&s.hasTag())
					tag = ItemNBTHelper.combineTags(tag, s.getOrCreateTag(), nbtCopyPredicate);
			}
			out.setTag(tag);
			return out;
		}
		else
			return super.getCraftingResult(matrix);
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv)
	{
		NonNullList<ItemStack> remains = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
		for(int yy = 0; yy < this.getRecipeHeight(); yy++)
			for(int xx = 0; xx < this.getRecipeWidth(); xx++)
			{
				int i = this.getRecipeWidth()*yy+xx;
				int transposedI = inv.getWidth()*(yy+lastStartY)+(xx+lastStartX);
				ItemStack s = inv.getStackInSlot(transposedI);
				NonNullList<Ingredient> matchedIngr = lastMatch==1?ingredientsQuarterTurn: lastMatch==2?ingredientsEighthTurn: this.getIngredients();
				if(matchedIngr.get(i) instanceof IngredientFluidStack)
				{
					LazyOptional<IFluidHandlerItem> handlerLazy = FluidUtil.getFluidHandler(s.getCount() > 1?Utils.copyStackWithAmount(s, 1): s);
					handlerLazy.ifPresent(handler ->
					{
						FluidStack fluid = ((IngredientFluidStack)matchedIngr.get(i)).getFluid();
						handler.drain(fluid.getAmount(), FluidAction.EXECUTE);
						remains.set(transposedI, handler.getContainer().copy());
					});
					if(!handlerLazy.isPresent())
						remains.set(transposedI, ForgeHooks.getContainerItem(s));
				}
			}
		return remains;
	}

	@Override
	public boolean checkMatch(CraftingInventory inv, int startX, int startY, boolean mirror)
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

	private boolean checkMatchDo(CraftingInventory inv, NonNullList<Ingredient> ingredients, int startX, int startY, boolean mirror, boolean rotate)
	{
		for(int x = 0; x < inv.getWidth(); x++)
			for(int y = 0; y < inv.getHeight(); y++)
			{
				int subX = x-startX;
				int subY = y-startY;
				Ingredient target = Ingredient.EMPTY;

				if(!rotate)
				{
					if(subX >= 0&&subY >= 0&&subX < getRecipeWidth()&&subY < getRecipeHeight())
						if(mirror)
							target = ingredients.get(getRecipeWidth()-subX-1+subY*getRecipeWidth());
						else
							target = ingredients.get(subX+subY*getRecipeWidth());
				}
				else
				{
					if(subX >= 0&&subY >= 0&&subX < getRecipeHeight()&&subY < getRecipeWidth())
						if(mirror)
							target = ingredients.get(getRecipeHeight()-subX-1+subY*getRecipeWidth());
						else
							target = ingredients.get(subY+subX*getRecipeHeight());
				}

				ItemStack slot = inv.getStackInSlot(x+y*inv.getWidth());
				if(!target.test(slot))
					return false;
			}
		return true;
	}

	@Nonnull
	@Override
	public IRecipeSerializer<?> getSerializer()
	{
		return RecipeSerializers.TURN_AND_COPY_SERIALIZER.get();
	}

	public boolean isQuarterTurn()
	{
		return ingredientsQuarterTurn!=null;
	}

	public boolean isEightTurn()
	{
		return ingredientsEighthTurn!=null;
	}

	public int[] getCopyTargets()
	{
		return nbtCopyTargetSlot;
	}

	public boolean hasCopyPredicate()
	{
		return nbtCopyPredicate!=null;
	}

	public String getBufferPredicate()
	{
		return nbtCopyPredicate.pattern();
	}
}