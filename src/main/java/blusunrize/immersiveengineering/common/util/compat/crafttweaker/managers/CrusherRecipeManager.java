/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker.managers;

import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.CrTIngredientUtil;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.actions.AbstractActionRemoveMultipleOutputs;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.blamejared.crafttweaker.api.util.random.Percentaged;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import org.openzen.zencode.java.ZenCodeType;

import java.util.ArrayList;
import java.util.List;

import static blusunrize.immersiveengineering.api.crafting.IESerializableRecipe.of;

/**
 * Allows you to add or remove Crusher recipes.
 * <p>
 * Crusher Recipes consist of an input, an output and a list of possible secondary outputs.
 *
 * @docParam this <recipetype:immersiveengineering:crusher>
 */
@ZenRegister
@Document("mods/immersiveengineering/Crusher")
@ZenCodeType.Name("mods.immersiveengineering.Crusher")
public class CrusherRecipeManager implements IRecipeManager<CrusherRecipe>
{

	@Override
	public RecipeType<CrusherRecipe> getRecipeType()
	{
		return IERecipeTypes.CRUSHER.get();
	}

	@Override
	public void remove(IIngredient output)
	{
		removeRecipe(output);
	}

	/**
	 * Removes all recipes that output the given IIngredient.
	 * Removes the recipe as soon as any of the recipe's possible outputs matches the given IIngredient.
	 * Includes secondary outputs and chance-based outputs.
	 *
	 * @param output The output whose recipes should be removed
	 * @docParam output <item:immersiveengineering:dust_iron>
	 * @docParam output <tag:forge:dusts>
	 */
	@ZenCodeType.Method
	public void removeRecipe(IIngredient output)
	{
		CraftTweakerAPI.apply(new AbstractActionRemoveMultipleOutputs<CrusherRecipe>(this, output)
		{

			@Override
			public List<ItemStack> getAllOutputs(CrusherRecipe recipe)
			{
				final List<ItemStack> itemStacks = new ArrayList<>();
				itemStacks.add(recipe.output.get());
				for(StackWithChance secondaryOutput : recipe.secondaryOutputs)
					itemStacks.add(secondaryOutput.stack().get());
				return itemStacks;
			}
		});
	}

	/**
	 * Adds a Crusher recipe.
	 *
	 * @param recipePath        The recipe name, without the resource location
	 * @param input             The input ingredient
	 * @param energy            The total energy required
	 * @param mainOutput        The main item that this recipe will return
	 * @param additionalOutputs All secondary items that can be returned
	 * @docParam recipePath "tnt_discharge"
	 * @docParam input <item:minecraft:tnt>
	 * @docParam energy 500
	 * @docParam mainOutput <item:minecraft:gunpowder> * 4
	 * @docParam additionalOutputs <item:minecraft:coal> % 50, <item:minecraft:diamond> % 1
	 */
	@SafeVarargs
	@ZenCodeType.Method
	public final void addRecipe(String recipePath, IIngredient input, int energy, IItemStack mainOutput, Percentaged<IItemStack>... additionalOutputs)
	{
		final ResourceLocation resourceLocation = new ResourceLocation("crafttweaker", recipePath);

		final ItemStack result = mainOutput.getInternal();
		final Ingredient ingredient = input.asVanillaIngredient();
		final CrusherRecipe recipe = new CrusherRecipe(resourceLocation, of(result), ingredient, energy);

		for(Percentaged<IItemStack> additionalOutput : additionalOutputs)
		{
			final StackWithChance stackWithChance = CrTIngredientUtil.getStackWithChance(additionalOutput);
			recipe.addToSecondaryOutput(stackWithChance);
		}

		CraftTweakerAPI.apply(new ActionAddRecipe<>(this, recipe, null));
	}
}
