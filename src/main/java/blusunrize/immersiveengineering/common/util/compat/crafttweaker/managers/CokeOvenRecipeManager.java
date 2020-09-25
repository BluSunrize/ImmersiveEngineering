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
import com.blamejared.crafttweaker.api.*;
import com.blamejared.crafttweaker.api.annotations.*;
import com.blamejared.crafttweaker.api.item.*;
import com.blamejared.crafttweaker.api.managers.*;
import com.blamejared.crafttweaker.impl.actions.recipes.*;
import com.blamejared.crafttweaker_annotations.annotations.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.util.*;
import org.openzen.zencode.java.*;

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
public class CokeOvenRecipeManager implements IRecipeManager {
    
    @Override
    public IRecipeType<CokeOvenRecipe> getRecipeType() {
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
     */
    @ZenCodeType.Method
    public void addRecipe(String recipePath, IIngredient ingredient, int time, IItemStack output, @ZenCodeType.OptionalInt int creosoteProduced) {
        final ResourceLocation resourceLocation = new ResourceLocation(Lib.MODID, recipePath);
        final IngredientWithSize ingredientWithSize = CrTIngredientUtil.getIngredientWithSize(ingredient);
        final ItemStack result = output.getInternal();
        
        final CokeOvenRecipe recipe = new CokeOvenRecipe(resourceLocation, result, ingredientWithSize, time, creosoteProduced);
        CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, null));
    }
}
