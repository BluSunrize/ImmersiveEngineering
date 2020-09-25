/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker.actions;

import blusunrize.immersiveengineering.api.crafting.*;
import com.blamejared.crafttweaker.api.actions.*;
import com.blamejared.crafttweaker.api.item.*;
import com.blamejared.crafttweaker.api.managers.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.util.*;

import java.util.*;

public class ActionRemoveFertilizer implements IRuntimeAction {
    
    private final IRecipeManager recipeManager;
    private final IItemStack toBeRemoved;
    
    public ActionRemoveFertilizer(IRecipeManager recipeManager, IItemStack toBeRemoved) {
        this.recipeManager = recipeManager;
        this.toBeRemoved = toBeRemoved;
    }
    
    @Override
    public void apply() {
        final Iterator<Map.Entry<ResourceLocation, IRecipe<?>>> iterator = recipeManager.getRecipes()
                .entrySet()
                .iterator();
    
        final ItemStack internal = toBeRemoved.getInternal();
    
        while(iterator.hasNext()) {
            final IRecipe<?> recipe = iterator.next().getValue();
            if(!(recipe instanceof ClocheFertilizer)) {
                continue;
            }
            ClocheFertilizer clocheFertilizer = (ClocheFertilizer) recipe;
            if(clocheFertilizer.input.test(internal)) {
                iterator.remove();
            }
        }
    }
    
    @Override
    public String describe() {
        return "Removing all Fertilizers that match " + toBeRemoved.getCommandString();
    }
}
