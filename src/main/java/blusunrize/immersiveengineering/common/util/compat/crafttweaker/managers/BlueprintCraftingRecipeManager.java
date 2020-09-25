/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker.managers;

import blusunrize.immersiveengineering.api.*;
import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.*;
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.actions.*;
import com.blamejared.crafttweaker.api.*;
import com.blamejared.crafttweaker.api.annotations.*;
import com.blamejared.crafttweaker.api.item.*;
import com.blamejared.crafttweaker.api.logger.*;
import com.blamejared.crafttweaker.api.managers.*;
import com.blamejared.crafttweaker.impl.actions.recipes.*;
import com.blamejared.crafttweaker_annotations.annotations.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.util.*;
import org.openzen.zencode.java.*;

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
public class BlueprintCraftingRecipeManager implements IRecipeManager {
    
    @Override
    public IRecipeType<BlueprintCraftingRecipe> getRecipeType() {
        return BlueprintCraftingRecipe.TYPE;
    }
    
    /**
     * Adds a blueprint category. You need to call this method before adding any recipes that use a blueprint category that is not added by IE.
     * May only be called for a nonexistent category, otherwise it will error.
     *
     * Adding a blueprint category will also create a new blueprint item in JEI.
     * For technical reasons it is not possible to also add one to the creative menu, so stick to JEI for getting the item.
     *
     * To localize the generated blueprint's name, you should add a lang file entry "desc.immersiveengineering.info.blueprint.<blueprintCategory>"
     *
     * In the example below it would be "desc.immersiveengineering.info.blueprint.badabim"
     * @param blueprintCategory The category name to be added
     * @docParam blueprintCategory "badabim"
     */
    @ZenCodeType.Method
    public void addBlueprintCategory(String blueprintCategory) {
        CraftTweakerAPI.apply(new ActionAddBlueprintCategory(blueprintCategory));
    }
    
    
    /**
     * Adds a new recipe.
     * Make sure that the category exists before calling this method!
     *
     *
     * @param recipePath The recipe name, without the resource location
     * @param blueprintCategory The category name. The category must exist!
     * @param inputs The recipe's ingredients
     * @param output The recipe's output item
     *
     * @docParam recipePath "some_test"
     * @docParam blueprintCategory "bullet"
     * @docParam inputs [<item:minecraft:bedrock>]
     * @docParam output <item:minecraft:bedrock> * 2
     */
    @ZenCodeType.Method
    public void addRecipe(String recipePath, String blueprintCategory, IIngredient[] inputs, IItemStack output) {
        final ResourceLocation resourceLocation = new ResourceLocation(Lib.MODID, recipePath);
        final IngredientWithSize[] ingredients = CrTIngredientUtil.getIngredientsWithSize(inputs);
        final ItemStack results = output.getInternal();
        final BlueprintCraftingRecipe recipe = new BlueprintCraftingRecipe(resourceLocation, blueprintCategory, results, ingredients);
        
        CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, null) {
            @Override
            public boolean validate(ILogger logger) {
                if(!BlueprintCraftingRecipe.recipeCategories.contains(blueprintCategory)) {
                    final String format = "Blueprint Category '%s' does not exist yet. You can add it with '<recipetype:immersiveengineering:blueprint>.addBlueprintCategory(\"%s\");'";
                    logger.error(String.format(format, blueprintCategory, blueprintCategory));
                    return false;
                }
                return true;
            }
        });
    }
}
