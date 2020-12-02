/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.common.crafting.TurnAndCopyRecipe.MatchLocation;
import blusunrize.immersiveengineering.common.crafting.shaped.IEShapedRecipe;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class TurnAndCopyRecipe extends IEShapedRecipe<MatchLocation>
{
	protected boolean allowQuarter;
	protected boolean allowEighth;
	protected int[] nbtCopyTargetSlot = null;
	protected Pattern nbtCopyPredicate = null;

	public TurnAndCopyRecipe(ResourceLocation id, String group, int width, int height, NonNullList<Ingredient> ingr,
							 ItemStack output)
	{
		super(id, group, width, height, ingr, output);
	}

	public void allowQuarterTurn()
	{
		allowQuarter = true;
	}

	public void allowEighthTurn()
	{
		if(getWidth()==3&&getHeight()==3)//Recipe won't allow 8th turn when not a 3x3 square
			allowEighth = true;
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
	public ItemStack getCraftingResult(@Nonnull CraftingInventory matrix)
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

	@Nullable
	@Override
	protected MatchLocation findMatch(CraftingInventory inv)
	{
		for(int xOffset = 0; xOffset <= inv.getWidth()-this.getWidth(); ++xOffset)
			for(int yOffset = 0; yOffset <= inv.getHeight()-this.getHeight(); ++yOffset)
				for(boolean mirror : BOOLEANS)
					for(Rotation rot : Rotation.values())
					{
						if(!rot.allowed.test(this))
							continue;
						MatchLocation loc = new MatchLocation(xOffset, yOffset, mirror, rot);
						if(checkMatchDo(inv, loc))
							return loc;
					}
		return null;
	}

	private boolean checkMatchDo(CraftingInventory inv, MatchLocation loc)
	{
		for(int x = 0; x < inv.getWidth(); x++)
			for(int y = 0; y < inv.getHeight(); y++)
			{
				Ingredient target = Ingredient.EMPTY;

				int index = loc.getListIndex(x, y, getWidth(), getHeight());
				if(index >= 0)
					target = getIngredients().get(index);

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
		return allowQuarter;
	}

	public boolean isEightTurn()
	{
		return allowEighth;
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

	public static class MatchLocation implements IMatchLocation
	{
		private final int offsetX;
		private final int offsetY;
		private final boolean mirrored;
		private final Rotation rotation;

		public MatchLocation(int offsetX, int offsetY, boolean mirrored, Rotation rotation)
		{
			this.offsetX = offsetX;
			this.offsetY = offsetY;
			this.mirrored = mirrored;
			this.rotation = rotation;
		}

		@Override
		public int getListIndex(int x, int y, int width, int height)
		{
			x -= offsetX;
			y -= offsetY;
			if(mirrored)
				x = width-1-x;
			if(rotation.isValid(x, y, width, height))
				return rotation.getIndex(x, y, width, height);
			else
				return -1;
		}
	}

	private enum Rotation
	{
		NONE(s -> true),
		QUARTER(TurnAndCopyRecipe::isQuarterTurn),
		EIGHTH(TurnAndCopyRecipe::isEightTurn);

		private static final int[] eighthTurnMap = {3, -1, -1, 3, 0, -3, 1, 1, -3};
		private final Predicate<TurnAndCopyRecipe> allowed;

		Rotation(Predicate<TurnAndCopyRecipe> allowed)
		{
			this.allowed = allowed;
		}

		public boolean isValid(int x, int y, int width, int height)
		{
			if(this==QUARTER)
			{
				int temp = x;
				x = y;
				y = temp;
			}
			return x >= 0&&x < width&&y >= 0&&y < height;
		}

		public int getIndex(int x, int y, int width, int height)
		{
			switch(this)
			{
				case NONE:
					return y*width+x;
				case QUARTER:
					return x*width+((height-1)-y);
				case EIGHTH:
					int i = y*width+x;
					return i+eighthTurnMap[i];
			}
			throw new UnsupportedOperationException();
		}
	}
}