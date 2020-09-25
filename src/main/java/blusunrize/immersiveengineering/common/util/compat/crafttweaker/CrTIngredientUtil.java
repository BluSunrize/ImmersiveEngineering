/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.crafting.*;
import com.blamejared.crafttweaker.api.fluid.*;
import com.blamejared.crafttweaker.api.item.*;
import com.blamejared.crafttweaker.impl.item.*;
import net.minecraft.fluid.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraftforge.fluids.*;

public class CrTIngredientUtil {
    
    private CrTIngredientUtil() {
    }
    
    /**
     * So far, only IItemStack supports setting an amount.
     * So we can at least check for that, I guess?
     *
     * @param crafttweakerIngredient The CrT ingredient
     * @return The IE ingredient
     */
    public static IngredientWithSize getIngredientWithSize(IIngredient crafttweakerIngredient) {
        final Ingredient basePredicate = crafttweakerIngredient.asVanillaIngredient();
        if(crafttweakerIngredient instanceof IItemStack) {
            return IngredientWithSize.of(((IItemStack) crafttweakerIngredient).getInternal());
        }
        return new IngredientWithSize(basePredicate);
    }
    
    /**
     * Same as {@link #getIngredientWithSize(IIngredient)} but for an array
     */
    public static IngredientWithSize[] getIngredientsWithSize(IIngredient[] crafttweakerIngredients) {
        final IngredientWithSize[] result = new IngredientWithSize[crafttweakerIngredients.length];
        for(int i = 0; i < crafttweakerIngredients.length; i++) {
            result[i] = getIngredientWithSize(crafttweakerIngredients[i]);
        }
        return result;
    }
    
    /**
     * {@link com.blamejared.crafttweaker.impl.helper.CraftTweakerHelper} only allows to get a List, not a NonNullList
     */
    public static NonNullList<ItemStack> getNonNullList(IItemStack[] itemStacks) {
        final NonNullList<ItemStack> result = NonNullList.create();
        for(IItemStack itemStack : itemStacks) {
            result.add(itemStack.getInternal());
        }
        return result;
    }
    
    /**
     * Creates a StackWithChance, and clamps the chance to [0..1]
     */
    public static StackWithChance getStackWithChance(MCWeightedItemStack weightedStack) {
        final ItemStack stack = weightedStack.getItemStack().getInternal();
        final float weight = MathHelper.clamp((float) weightedStack.getWeight(), 0, 1);
        return new StackWithChance(stack, weight);
    }
    
    /**
     * Just a simple input sanitation, since I don't know how machines would handle a FluidStack with Fluid.EMPTY and stackSize == 1
     */
    public static FluidStack getFluidStack(IFluidStack stack) {
        final FluidStack internal = stack.getInternal();
        if(internal.getFluid() == Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }
        return internal;
    }
}
