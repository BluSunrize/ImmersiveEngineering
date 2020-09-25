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
import com.blamejared.crafttweaker.api.managers.*;
import com.blamejared.crafttweaker.impl.actions.recipes.*;
import com.blamejared.crafttweaker.impl.item.*;
import com.blamejared.crafttweaker_annotations.annotations.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.util.*;
import org.openzen.zencode.java.*;

import java.util.*;

/**
 * Allows you to add or remove Crusher recipes.
 * <p>
 * Crusher Recipes consist of an input, an output and a list of possible secondary outputs.
 *
 * @docParam this <recipetype:immersiveengineering:crusher>
 */
@ZenRegister
@Document("mods/immersiveengineering/Crusher")
@ZenCodeType.Name("mods.immersiveengineering.Crusher")
public class CrusherRecipeManager implements IRecipeManager {
    
    @Override
    public IRecipeType<CrusherRecipe> getRecipeType() {
        return CrusherRecipe.TYPE;
    }
    
    @Override
    public void removeRecipe(IItemStack output) {
        removeRecipe((IIngredient) output);
    }
    
    /**
     * Removes all recipes that output the given IIngredient.
     * Removes the recipe as soon as any of the recipe's possible outputs matches the given IIngredient.
     * Includes secondary outputs and chance-based outputs.
     *
     * @param output The output whose recipes should be removed
     * @docParam output <item:immersiveengineering:dust_iron>
     * @docParam output <tag:forge:dusts>
     */
    @ZenCodeType.Method
    public void removeRecipe(IIngredient output) {
        CraftTweakerAPI.apply(new AbstractActionRemoveMultipleOutputs<CrusherRecipe>(this, output) {
            
            @Override
            public List<ItemStack> getAllOutputs(CrusherRecipe recipe) {
                final ArrayList<ItemStack> itemStacks = new ArrayList<>();
                itemStacks.add(recipe.output);
                for(StackWithChance secondaryOutput : recipe.secondaryOutputs) {
                    itemStacks.add(secondaryOutput.getStack());
                }
                return itemStacks;
            }
        });
    }
    
    /**
     * Adds a Crusher recipe.
     *
     * @param recipePath        The recipe name, without the resource location
     * @param input             The input ingredient
     * @param energy            The total energy required
     * @param mainOutput        The main item that this recipe will return
     * @param additionalOutputs All secondary items that can be returned
     * @docParam recipePath "tnt_discharge"
     * @docParam input <item:minecraft:tnt>
     * @docParam energy 500
     * @docParam mainOutput <item:minecraft:gunpowder> * 4
     * @docParam additionalOutputs <item:minecraft:coal> % 50, <item:minecraft:diamond> % 1
     */
    @ZenCodeType.Method
    public void addRecipe(String recipePath, IIngredient input, int energy, IItemStack mainOutput, MCWeightedItemStack... additionalOutputs) {
        final ResourceLocation resourceLocation = new ResourceLocation(Lib.MODID, recipePath);
        
        final CrusherRecipe recipe = new CrusherRecipe(resourceLocation, mainOutput.getInternal(), input
                .asVanillaIngredient(), energy);
        for(MCWeightedItemStack additionalOutput : additionalOutputs) {
            recipe.addToSecondaryOutput(CrTIngredientUtil.getStackWithChance(additionalOutput));
        }
        CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, null));
    }
}
