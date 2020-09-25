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
 * Allows you to add or remove Metal Press recipes.
 * <p>
 * Metal Press recipes consist of an input, a mold item and an output.
 *
 * @docParam this <recipetype:immersiveengineering:metal_press>
 */
@ZenRegister
@Document("mods/immersiveengineering/MetalPress")
@ZenCodeType.Name("mods.immersiveengineering.MetalPress")
public class MetalPressRecipeManager implements IRecipeManager {
    
    @Override
    public IRecipeType<MetalPressRecipe> getRecipeType() {
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
    public void addRecipe(String recipePath, IIngredient input, IItemStack mold, int energy, IItemStack output) {
        final ResourceLocation resourceLocation = new ResourceLocation(Lib.MODID, recipePath);
        final IngredientWithSize ingredient = CrTIngredientUtil.getIngredientWithSize(input);
        final ComparableItemStack moldStack = new ComparableItemStack(mold.getInternal(), mold.hasTag());
        final ItemStack outputStack = output.getInternal();
        
        final MetalPressRecipe recipe = new MetalPressRecipe(resourceLocation, outputStack, ingredient, moldStack, energy);
        CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, null));
    }
}
