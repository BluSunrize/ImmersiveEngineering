/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker.managers;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.CrTIngredientUtil;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.openzen.zencode.java.ZenCodeType;

/**
 * Allows you to add or remove Coke Oven recipes.
 * <p>
 * Coke Oven recipes consist of an input, an output and the amount of creosote produced
 *
 * @docParam this <recipetype:immersiveengineering:coke_oven>
 */
@ZenRegister
@Document("mods/immersiveengineering/CokeOven")
@ZenCodeType.Name("mods.immersiveengineering.CokeOven")
public class CokeOvenRecipeManager implements IRecipeManager
{

	@Override
	public RecipeType<CokeOvenRecipe> getRecipeType()
	{
		return CokeOvenRecipe.TYPE;
	}

	/**
	 * Adds a coke oven recipe
	 *
	 * @param recipePath       RecipePath The recipe name, without the resource location
	 * @param ingredient       The recipe's input
	 * @param time             The time the recipe requires, in ticks
	 * @param output           The produced item
	 * @param creosoteProduced The amount of creosote produced
	 * @docParam recipePath "burn_a_stick"
	 * @docParam ingredient <item:minecraft:stick>
	 * @docParam time 100
	 * @docParam output <item:immersiveengineering:stick_treated>
	 * @docParam creosoteProduced 1
	 */
	@ZenCodeType.Method
	public void addRecipe(String recipePath, IIngredientWithAmount ingredient, int time, IItemStack output, @ZenCodeType.OptionalInt int creosoteProduced)
	{
		final ResourceLocation resourceLocation = new ResourceLocation("crafttweaker", recipePath);
		final IngredientWithSize ingredientWithSize = CrTIngredientUtil.getIngredientWithSize(ingredient);
		final ItemStack result = output.getInternal();

		final CokeOvenRecipe recipe = new CokeOvenRecipe(resourceLocation, result, ingredientWithSize, time, creosoteProduced);
		CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, null));
	}
}
