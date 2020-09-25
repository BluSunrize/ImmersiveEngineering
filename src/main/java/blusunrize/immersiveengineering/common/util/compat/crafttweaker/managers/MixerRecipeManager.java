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
import com.blamejared.crafttweaker.api.fluid.*;
import com.blamejared.crafttweaker.api.item.*;
import com.blamejared.crafttweaker.api.managers.*;
import com.blamejared.crafttweaker.impl.actions.recipes.*;
import com.blamejared.crafttweaker.impl.fluid.*;
import com.blamejared.crafttweaker.impl.tag.*;
import com.blamejared.crafttweaker_annotations.annotations.*;
import net.minecraft.item.crafting.*;
import net.minecraft.util.*;
import net.minecraftforge.fluids.*;
import org.openzen.zencode.java.*;

/**
 * Allows you to add or remove Mixer recipes.
 * <p>
 * Mixer Recipes consist of a fluid input, multiple item inputs and a fluid output.
 *
 * @docParam this <recipetype:immersiveengineering:mixer>
 */
@ZenRegister
@Document("mods/immersiveengineering/Mixer")
@ZenCodeType.Name("mods.immersiveengineering.Mixer")
public class MixerRecipeManager implements IRecipeManager {
    
    @Override
    public IRecipeType<MixerRecipe> getRecipeType() {
        return MixerRecipe.TYPE;
    }
    
    @Override
    public void removeRecipe(IItemStack output) {
        throw new UnsupportedOperationException("Cannot remove a Mixer recipe by Item output because Mixer Recipes have no Item output!");
    }
    
    /**
     * Removes all recipes that return this given fluid Stack.
     * Only removes if the fluid and the fluid amount match.
     * Does not remove potion recipes!
     *
     * @param fluidStack The output to remove
     * @docParam fluidStack <fluid:immersiveengineering:concrete> * 500
     */
    @ZenCodeType.Method
    public void removeRecipe(IFluidStack fluidStack) {
        final AbstractActionGenericRemoveRecipe<MixerRecipe> action = new AbstractActionGenericRemoveRecipe<MixerRecipe>(this, fluidStack) {
            @Override
            public boolean shouldRemove(MixerRecipe recipe) {
                return recipe.fluidOutput.isFluidStackIdentical(fluidStack.getInternal());
            }
        };
        
        CraftTweakerAPI.apply(action);
    }
    
    /**
     * Removes all recipes that return this given fluid.
     * Since it's only the fluid, it does not check amounts.
     * Does not remove potion recipes!
     *
     * @param fluid The fluid output to remove
     * @docParam fluid <fluid:immersiveengineering:concrete>.fluid
     */
    @ZenCodeType.Method
    public void removeRecipe(MCFluid fluid) {
        final AbstractActionGenericRemoveRecipe<MixerRecipe> action = new AbstractActionGenericRemoveRecipe<MixerRecipe>(this, fluid) {
            @Override
            public boolean shouldRemove(MixerRecipe recipe) {
                return recipe.fluidOutput.getFluid() == fluid.getInternal();
            }
        };
        
        CraftTweakerAPI.apply(action);
    }
    
    /**
     * Adds a recipe to the Mixer.
     * Make sure that the provided Tag is a valid fluid tag.
     *
     * Mixer recipes will always convert 1mB of the input fluid to 1mB of the output fluid.
     * The `amount` parameter specifies for how many mB the given ingredients last
     *
     * @param recipePath The recipe name, without the resource location
     * @param fluidInput The fluid input as Tag
     * @param inputItems The required input items
     * @param energy     The total energy required
     * @param output     The produced output fluidStack
     * @param amount     The amount of fluid that can be converted per set of input items (in mB)
     * @docParam recipePath "grow_creosote_oil"
     * @docParam fluidInput <tag:minecraft:water>
     * @docParam inputItems [<item:minecraft:oak_sapling>, <item:minecraft:bone_meal> * 4, <item:immersiveengineering:creosote_bucket>]
     * @docParam energy 5000
     * @docParam output <fluid:immersiveengineering:creosote>.fluid
     * @docParam amount 8000
     */
    @ZenCodeType.Method
    public void addRecipe(String recipePath, MCTag fluidInput, IIngredient[] inputItems, int energy, MCFluid output, int amount) {
        final ResourceLocation resourceLocation = new ResourceLocation(Lib.MODID, recipePath);
        if(!fluidInput.isFluidTag()) {
            throw new IllegalArgumentException("Provided tag is not a fluid tag: " + fluidInput.getCommandString());
        }
        
        final ResourceLocation fluidTagId = fluidInput.getId().getInternal();
        final FluidTagInput fluidTagInput = new FluidTagInput(fluidTagId, amount);
        final IngredientWithSize[] ingredientsWithSize = CrTIngredientUtil.getIngredientsWithSize(inputItems);
        final FluidStack outputFluidStack = new FluidStack(output.getInternal(), amount);
        
        final MixerRecipe recipe = new MixerRecipe(resourceLocation, outputFluidStack, fluidTagInput, ingredientsWithSize, energy);
        CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, null));
    }
}
