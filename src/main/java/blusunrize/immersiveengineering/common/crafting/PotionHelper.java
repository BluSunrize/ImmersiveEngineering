/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.mixin.accessors.PotionBrewingAccess;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class PotionHelper
{
	public static FluidTagInput getFluidTagForType(Holder<Potion> type, int amount)
	{
		if(type==Potions.WATER||type==null)
			return new FluidTagInput(FluidTags.WATER, amount);
		else
		{
			CompoundTag nbt = new CompoundTag();
			nbt.putString("Potion", type.unwrapKey().orElseThrow().location().toString());
			return new FluidTagInput(IETags.fluidPotion, amount, DataComponentPredicate.builder()
					.expect(DataComponents.POTION_CONTENTS, new PotionContents(type))
					.build());
		}
	}

	public static void applyToAllPotionRecipes(PotionRecipeProcessor out)
	{
		final PotionBrewing brewingData;
		if(ServerLifecycleHooks.getCurrentServer()!=null)
			brewingData = ServerLifecycleHooks.getCurrentServer().potionBrewing();
		else
			brewingData = ImmersiveEngineering.proxy.getClientWorld().potionBrewing();
		// Vanilla
		for(var mixPredicate : ((PotionBrewingAccess)brewingData).getConversions())
			if(mixPredicate.getTo()!=Potions.MUNDANE&&mixPredicate.getTo()!=Potions.THICK)
				out.apply(
						mixPredicate.getTo(), mixPredicate.getFrom(),
						new IngredientWithSize(mixPredicate.getIngredient())
				);

		// Modded
		for(IBrewingRecipe recipe : brewingData.getRecipes())
			if(recipe instanceof BrewingRecipe brewingRecipe)
			{
				IngredientWithSize ingredient = new IngredientWithSize(brewingRecipe.getIngredient());
				Ingredient input = brewingRecipe.getInput();
				ItemStack output = brewingRecipe.getOutput();
				if(output.getItem()==Items.POTION&&input.getItems().length > 0)
					out.apply(getPotion(output), getPotion(input.getItems()[0]), ingredient);
			}
	}

	private static Holder<Potion> getPotion(ItemStack potion)
	{
		PotionContents potionData = potion.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
		return potionData.potion().orElse(Potions.WATER);
	}

	public interface PotionRecipeProcessor
	{
		void apply(Holder<Potion> output, Holder<Potion> input, IngredientWithSize reagent);
	}
}
