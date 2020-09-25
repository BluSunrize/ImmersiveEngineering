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
import com.blamejared.crafttweaker_annotations.annotations.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.util.*;
import org.openzen.zencode.java.*;

import java.util.*;

/**
 * Allows you to add or remove arc furnace smelter recipes.
 * <p>
 * Arc Furnace recipes consist of one base ingredident, a list of additives, and a list of outputs.
 * Optionally, they can also have an item as slag output.
 *
 * @docParam this <recipetype:immersiveengineering:arc_furnace>
 */
@ZenRegister
@Document("mods/immersiveengineering/ArcFurnace")
@ZenCodeType.Name("mods.immersiveengineering.ArcFurnace")
public class ArcFurnaceRecipeManager implements IRecipeManager {
    
    @Override
    public IRecipeType<ArcFurnaceRecipe> getRecipeType() {
        return ArcFurnaceRecipe.TYPE;
    }
    
    /**
     * Adds a recipe to the Arc Furnace
     *
     * @param recipePath     The recipe name, without the resource location
     * @param mainIngredient The main ingredient
     * @param additives      The additives
     * @param time           The time the recipe takes, in ticks
     * @param energy         The total energy the recipe requires
     * @param outputs        The recipe result(s)
     * @param slag           The item that should appear as slag
     * @docParam recipePath "coal_to_bedrock"
     * @docParam mainIngredient <item:minecraft:coal_block> * 2
     * @docParam additives [<item:minecraft:diamond> * 1, <tag:minecraft:wool>]
     * @docParam time 2000
     * @docParam energy 100000
     * @docParam outputs [<item:minecraft:bedrock>]
     * @docParam slag <item:minecraft:gold_nugget>
     */
    @ZenCodeType.Method
    public void addRecipe(String recipePath, IIngredient mainIngredient, IIngredient[] additives, int time, int energy, IItemStack[] outputs, @ZenCodeType.Optional("<item:minecraft:air>") IItemStack slag) {
        final ResourceLocation resourceLocation = new ResourceLocation(Lib.MODID, recipePath);
        final NonNullList<ItemStack> outputList = CrTIngredientUtil.getNonNullList(outputs);
        final IngredientWithSize main = CrTIngredientUtil.getIngredientWithSize(mainIngredient);
        final IngredientWithSize[] additivesWithSize = CrTIngredientUtil.getIngredientsWithSize(additives);
        
        final ArcFurnaceRecipe recipe = new ArcFurnaceRecipe(resourceLocation, outputList, main, slag
                .getInternal(), time, energy, additivesWithSize);
        
        CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, null));
    }
    
    @Override
    public void removeRecipe(IItemStack output) {
        removeRecipe(output, false);
    }
    
    /**
     * Removes a recipe based on its outputs.
     * Removes the recipe as long as one of the recipe's outputs matches the ingredient given.
     * TODO: Allow for removal of Recycling recipes
     *
     * @param output    The recipe result
     * @param checkSlag If the slag output should be included in the check or not
     * @docParam output <item:minecraft:iron_ore>
     * @docParam checkSlag true
     */
    @ZenCodeType.Method
    public void removeRecipe(IIngredient output, boolean checkSlag) {
        CraftTweakerAPI.apply(new AbstractActionRemoveMultipleOutputs<ArcFurnaceRecipe>(this, output) {
            @Override
            public List<ItemStack> getAllOutputs(ArcFurnaceRecipe recipe) {
                final List<ItemStack> itemStacks = new ArrayList<>(recipe.output);
                if(checkSlag) {
                    itemStacks.add(recipe.slag);
                }
                return itemStacks;
            }
            
            @Override
            public String describe() {
                return super.describe() + ", " + (checkSlag ? "including" : "excluding") + " slag outputs";
            }
        });
    }
}
