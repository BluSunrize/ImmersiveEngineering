/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker.managers;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.CrTIngredientUtil;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.openzen.zencode.java.ZenCodeType;

/**
 * Allows you to add or remove Metal Press recipes.
 * <p>
 * Metal Press recipes consist of an input, a mold item and an output.
 *
 * @docParam this <recipetype:immersiveengineering:metal_press>
 */
@ZenRegister
@Document("mods/immersiveengineering/MetalPress")
@ZenCodeType.Name("mods.immersiveengineering.MetalPress")
public class MetalPressRecipeManager implements IRecipeManager<MetalPressRecipe>
{

	@Override
	public RecipeType<MetalPressRecipe> getRecipeType()
	{
		return MetalPressRecipe.TYPE;
	}

	/**
	 * Adds a new metal press recipe
	 *
	 * @param recipePath The recipe name, without the resource location
	 * @param input      The recipe's input
	 * @param mold       The mold to be used
	 * @param energy     The total energy required for this recipe
	 * @param output     The recipe result
	 * @docParam recipePath "book_press"
	 * @docParam input <item:minecraft:paper> * 2
	 * @docParam mold <item:immersiveengineering:manual>
	 * @docParam energy 1000
	 * @docParam output <item:immersiveengineering:manual>
	 */
	@ZenCodeType.Method
	public void addRecipe(String recipePath, IIngredientWithAmount input, IItemStack mold, int energy, IItemStack output)
	{
		final ResourceLocation resourceLocation = new ResourceLocation("crafttweaker", recipePath);
		final IngredientWithSize ingredient = CrTIngredientUtil.getIngredientWithSize(input);
		final ItemStack outputStack = output.getInternal();

		final MetalPressRecipe recipe = new MetalPressRecipe(resourceLocation, outputStack, ingredient, mold.getDefinition(), energy);
		CraftTweakerAPI.apply(new ActionAddRecipe<>(this, recipe, null));
	}
}
