/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker.managers;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.CrTIngredientUtil;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.logger.ILogger;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

/**
 * Allows you to add or remove blueprint recipes.
 * <p>
 * Blueprint recipes consist of a variable number of inputs and one output.
 * They are grouped by categories, where each category is one blueprint item ingame.
 * <p>
 * You can find all existing categories using `/ct ieBlueprintCategories`
 *
 * @docParam this <recipetype:immersiveengineering:blueprint>
 */
@ZenRegister
@Document("mods/immersiveengineering/Blueprint")
@ZenCodeType.Name("mods.immersiveengineering.Blueprint")
public class BlueprintCraftingRecipeManager implements IRecipeManager
{

	@Override
	public IRecipeType<BlueprintCraftingRecipe> getRecipeType()
	{
		return BlueprintCraftingRecipe.TYPE;
	}


	/**
	 * Adds a new recipe.
	 * Make sure that the category exists before calling this method!
	 * Currently it is not possible to register new Blueprint categories.
	 *
	 * @param recipePath        The recipe name, without the resource location
	 * @param blueprintCategory The category name. The category must exist!
	 * @param inputs            The recipe's ingredients
	 * @param output            The recipe's output item
	 * @docParam recipePath "some_test"
	 * @docParam blueprintCategory "bullet"
	 * @docParam inputs [<item:minecraft:bedrock>]
	 * @docParam output <item:minecraft:bedrock> * 2
	 */
	@ZenCodeType.Method
	public void addRecipe(String recipePath, String blueprintCategory, IIngredient[] inputs, IItemStack output)
	{
		final ResourceLocation resourceLocation = new ResourceLocation(Lib.MODID, recipePath);
		final IngredientWithSize[] ingredients = CrTIngredientUtil.getIngredientsWithSize(inputs);
		final ItemStack results = output.getInternal();
		final BlueprintCraftingRecipe recipe = new BlueprintCraftingRecipe(resourceLocation, blueprintCategory, results, ingredients);

		CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, null)
		{
			@Override
			public boolean validate(ILogger logger)
			{
				if(!BlueprintCraftingRecipe.recipeCategories.contains(blueprintCategory))
				{
					final String format = "Blueprint Category '%s' does not exist yet. You can add it with '<recipetype:immersiveengineering:blueprint>.addBlueprintCategory(\"%s\");'";
					logger.error(String.format(format, blueprintCategory, blueprintCategory));
					return false;
				}
				return true;
			}
		});
	}
}
