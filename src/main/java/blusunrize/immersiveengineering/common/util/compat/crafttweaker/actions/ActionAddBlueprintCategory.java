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
import com.blamejared.crafttweaker.api.logger.*;

public class ActionAddBlueprintCategory implements IUndoableAction {
    
    private final String blueprintCategory;
    
    public ActionAddBlueprintCategory(String blueprintCategory) {
        this.blueprintCategory = blueprintCategory;
    }
    
    @Override
    public void undo() {
        BlueprintCraftingRecipe.recipeCategories.remove(blueprintCategory);
    }
    
    @Override
    public String describeUndo() {
        return "Removing previously added Blueprint Category '" + blueprintCategory + "'";
    }
    
    @Override
    public void apply() {
        BlueprintCraftingRecipe.recipeCategories.add(blueprintCategory);
    }
    
    @Override
    public String describe() {
        return "Adding Blueprint Category '" + blueprintCategory + "'";
    }
    
    @Override
    public boolean validate(ILogger logger) {
        if(BlueprintCraftingRecipe.recipeCategories.contains(blueprintCategory)) {
            logger.error("Blueprint Category '" + blueprintCategory + "' already exists!");
            return false;
        }
        
        return true;
    }
}
