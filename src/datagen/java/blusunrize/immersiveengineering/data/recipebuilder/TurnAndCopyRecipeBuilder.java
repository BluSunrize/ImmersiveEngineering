/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.data.recipebuilder;

import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;
import java.util.regex.Pattern;

public class TurnAndCopyRecipeBuilder extends ShapedRecipeBuilder
{
	private boolean allowQuarterTurn = false;
	private boolean allowEighthTurn = false;
	private int[] nbtCopyTargetSlot = null;
	private Pattern nbtCopyPredicate = null;

	public TurnAndCopyRecipeBuilder(IItemProvider result, int count)
	{
		super(result, count);
	}

	public static TurnAndCopyRecipeBuilder builder(IItemProvider result, int count)
	{
		return new TurnAndCopyRecipeBuilder(result, count);
	}

	public static TurnAndCopyRecipeBuilder builder(IItemProvider result)
	{
		return new TurnAndCopyRecipeBuilder(result, 1);
	}

	public TurnAndCopyRecipeBuilder allowQuarterTurn()
	{
		this.allowQuarterTurn = true;
		return this;
	}

	public TurnAndCopyRecipeBuilder allowEighthTurn()
	{
		this.allowEighthTurn = true;
		return this;
	}

	public TurnAndCopyRecipeBuilder setNBTCopyTargetRecipe(int... slot)
	{
		this.nbtCopyTargetSlot = slot;
		return this;
	}

	public TurnAndCopyRecipeBuilder setNBTCopyPredicate(String pattern)
	{
		this.nbtCopyPredicate = Pattern.compile(pattern);
		return this;
	}

	@Override
	public void build(Consumer<IFinishedRecipe> consumerIn, ResourceLocation id)
	{
		Consumer<IFinishedRecipe> dummyConsumer = iFinishedRecipe -> {
			TurnAndCopyResult result = new TurnAndCopyResult(iFinishedRecipe, allowQuarterTurn, allowEighthTurn, nbtCopyTargetSlot, nbtCopyPredicate);
			consumerIn.accept(result);
		};
		super.build(dummyConsumer, id);
	}

	public static class TurnAndCopyResult extends WrappedFinishedRecipe
	{
		private final boolean allowQuarterTurn;
		private final boolean allowEighthTurn;
		private final int[] nbtCopyTargetSlot;
		private final Pattern nbtCopyPredicate;

		public TurnAndCopyResult(IFinishedRecipe base, boolean allowQuarterTurn, boolean allowEighthTurn, int[] nbtCopyTargetSlot, Pattern nbtCopyPredicate)
		{
			super(base, RecipeSerializers.TURN_AND_COPY_SERIALIZER);
			this.allowQuarterTurn = allowQuarterTurn;
			this.allowEighthTurn = allowEighthTurn;
			this.nbtCopyTargetSlot = nbtCopyTargetSlot;
			this.nbtCopyPredicate = nbtCopyPredicate;
		}

		@Override
		public void serialize(JsonObject json)
		{
			super.serialize(json);
			if(allowQuarterTurn)
				json.addProperty("quarter_turn", true);
			if(allowEighthTurn)
				json.addProperty("eighth_turn", true);

			if(nbtCopyTargetSlot!=null)
			{
				if(nbtCopyTargetSlot.length > 1)
				{
					JsonArray jsonarray = new JsonArray();
					for(int slot : nbtCopyTargetSlot)
						jsonarray.add(slot);
					json.add("copy_nbt", jsonarray);
				}
				else
					json.addProperty("copy_nbt", nbtCopyTargetSlot[0]);
				if(nbtCopyPredicate!=null)
					json.addProperty("copy_nbt_predicate", nbtCopyPredicate.pattern());
			}
		}
	}
}