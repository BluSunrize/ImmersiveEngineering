/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.fluidaware;

import blusunrize.immersiveengineering.common.crafting.fluidaware.TurnAndCopyRecipe.MatchLocation;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import blusunrize.immersiveengineering.mixin.accessors.ShapedRecipeAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class TurnAndCopyRecipe extends AbstractShapedRecipe<MatchLocation>
{
	protected boolean allowQuarter;
	protected boolean allowEighth;
	@Nonnull
	protected final List<Integer> nbtCopyTargetSlot;
	protected Pattern nbtCopyPredicate = null;

	public TurnAndCopyRecipe(ShapedRecipe vanilla)
	{
		this(vanilla, List.of());
	}

	public TurnAndCopyRecipe(ShapedRecipe vanilla, List<Integer> copySlots)
	{
		super(
				vanilla.getGroup(),
				vanilla.getWidth(), vanilla.getHeight(),
				vanilla.getIngredients(), vanilla.getResultItem(null), vanilla.category(),
				((ShapedRecipeAccess)vanilla).getPattern().data()
		);
		this.nbtCopyTargetSlot = copySlots;
	}

	public TurnAndCopyRecipe allowQuarterTurn()
	{
		allowQuarter = true;
		return this;
	}

	public TurnAndCopyRecipe allowEighthTurn()
	{
		if(getWidth()==3&&getHeight()==3)//Recipe won't allow 8th turn when not a 3x3 square
			allowEighth = true;
		return this;
	}

	public void setNBTCopyPredicate(String pattern)
	{
		this.nbtCopyPredicate = Pattern.compile(pattern);
	}

	@Nonnull
	@Override
	public ItemStack assemble(@Nonnull CraftingContainer matrix, RegistryAccess access)
	{
		ItemStack out = super.assemble(matrix, access);
		CompoundTag tag = new CompoundTag();
		for(int targetSlot : nbtCopyTargetSlot)
		{
			ItemStack s = matrix.getItem(targetSlot);
			if(!s.isEmpty()&&s.hasTag())
				tag = ItemNBTHelper.combineTags(tag, s.getOrCreateTag(), nbtCopyPredicate);
		}
		if(!tag.isEmpty())
			out.setTag(tag);
		return out;
	}

	@Nullable
	@Override
	protected MatchLocation findMatch(CraftingContainer inv)
	{
		for(int xOffset = 0; xOffset <= inv.getWidth()-this.getWidth(); ++xOffset)
			for(int yOffset = 0; yOffset <= inv.getHeight()-this.getHeight(); ++yOffset)
				for(boolean mirror : BOOLEANS)
					for(Rotation rot : Rotation.values())
					{
						if(!rot.allowed.test(this))
							continue;
						MatchLocation loc = new MatchLocation(xOffset, yOffset, mirror, rot, getWidth(), getHeight());
						if(checkMatchDo(inv, loc))
							return loc;
					}
		return null;
	}

	private boolean checkMatchDo(CraftingContainer inv, MatchLocation loc)
	{
		for(int x = 0; x < inv.getWidth(); x++)
			for(int y = 0; y < inv.getHeight(); y++)
			{
				Ingredient target = Ingredient.EMPTY;

				int index = loc.getListIndex(x, y);
				if(index >= 0)
					target = getIngredients().get(index);

				ItemStack slot = inv.getItem(x+y*inv.getWidth());
				if(!target.test(slot))
					return false;
			}
		return true;
	}

	@Nonnull
	@Override
	public RecipeSerializer<?> getSerializer()
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

	public List<Integer> getCopyTargets()
	{
		return nbtCopyTargetSlot;
	}

	public boolean hasCopyPredicate()
	{
		return nbtCopyPredicate!=null;
	}

	public String getBufferPredicate()
	{
		if(nbtCopyPredicate!=null)
			return nbtCopyPredicate.pattern();
		else
			return null;
	}

	public static class MatchLocation implements AbstractFluidAwareRecipe.IMatchLocation
	{
		private final int offsetX;
		private final int offsetY;
		private final boolean mirrored;
		private final Rotation rotation;
		private final int recipeWidth;
		private final int recipeHeight;

		public MatchLocation(
				int offsetX, int offsetY, boolean mirrored, Rotation rotation, int recipeWidth, int recipeHeight
		)
		{
			this.offsetX = offsetX;
			this.offsetY = offsetY;
			this.mirrored = mirrored;
			this.rotation = rotation;
			this.recipeWidth = recipeWidth;
			this.recipeHeight = recipeHeight;
		}

		@Override
		public int getListIndex(int x, int y)
		{
			x -= offsetX;
			y -= offsetY;
			if(mirrored)
				x = recipeWidth-1-x;
			if(rotation.isValid(x, y, recipeWidth, recipeHeight))
				return rotation.getIndex(x, y, recipeWidth, recipeHeight);
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