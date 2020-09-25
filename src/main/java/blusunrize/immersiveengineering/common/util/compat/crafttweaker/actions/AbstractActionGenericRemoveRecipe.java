/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker.actions;

import blusunrize.immersiveengineering.common.crafting.*;
import com.blamejared.crafttweaker.api.*;
import com.blamejared.crafttweaker.api.actions.*;
import com.blamejared.crafttweaker.api.brackets.*;
import com.blamejared.crafttweaker.api.managers.*;
import net.minecraft.item.crafting.*;
import net.minecraft.util.*;

import java.util.*;

public abstract class AbstractActionGenericRemoveRecipe<T extends IRecipe<?>> implements IRuntimeAction {
    
    private final IRecipeManager manager;
    private final CommandStringDisplayable output;
    
    public AbstractActionGenericRemoveRecipe(IRecipeManager manager, CommandStringDisplayable output) {
        this.manager = manager;
        this.output = output;
    }
    
    @Override
    public void apply() {
        int count = 0;
        final Iterator<Map.Entry<ResourceLocation, IRecipe<?>>> iterator = manager.getRecipes()
                .entrySet()
                .iterator();
        
        try {
            while(iterator.hasNext()) {
                final IRecipe<?> recipe = iterator.next().getValue();
                if(recipe instanceof GeneratedListRecipe) {
                    CraftTweakerAPI.logDebug("Skipping GeneratedListRecipe '%s'", recipe.getId());
                    continue;
                }
                
                //noinspection unchecked
                if(shouldRemove((T) recipe)) {
                    iterator.remove();
                    count++;
                }
            }
        } catch(ClassCastException exception) {
            CraftTweakerAPI.logThrowing("There is an illegal entry in %s that caused an exception: ", exception, manager
                    .getCommandString());
        }
        
        CraftTweakerAPI.logInfo("Removed %s \"%s\" recipes", count, manager.getCommandString());
    }
    
    public abstract boolean shouldRemove(T recipe);
    
    @Override
    public String describe() {
        return "Removing all \"" + manager.getCommandString() + "\" recipes, that output: " + output
                .getCommandString();
    }
}
