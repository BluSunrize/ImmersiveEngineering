/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker.managers;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import blusunrize.immersiveengineering.api.crafting.IESerializableRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
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
public class BlueprintCraftingRecipeManager implements IRecipeManager<BlueprintCraftingRecipe>
{

	@Override
	public RecipeType<BlueprintCraftingRecipe> getRecipeType()
	{
		return IERecipeTypes.BLUEPRINT.get();
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
	public void addRecipe(String recipePath, String blueprintCategory, IIngredientWithAmount[] inputs, IItemStack output)
	{
		final ResourceLocation resourceLocation = new ResourceLocation("crafttweaker", recipePath);
		final IngredientWithSize[] ingredients = CrTIngredientUtil.getIngredientsWithSize(inputs);
		final ItemStack results = output.getInternal();
		final BlueprintCraftingRecipe recipe = new BlueprintCraftingRecipe(
				resourceLocation, blueprintCategory, IESerializableRecipe.of(results), ingredients
		);

		CraftTweakerAPI.apply(new ActionAddRecipe<>(this, recipe, null));
	}
}
