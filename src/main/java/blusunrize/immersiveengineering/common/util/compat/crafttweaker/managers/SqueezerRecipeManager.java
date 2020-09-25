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
import com.blamejared.crafttweaker_annotations.annotations.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.util.*;
import net.minecraftforge.fluids.*;
import org.openzen.zencode.java.*;

/**
 * Allows you to add or remove Squeezer recipes.
 * <p>
 * Squeezer Recipes consist of an input, a fluid output and an item output.
 *
 * @docParam this <recipetype:immersiveengineering:squeezer>
 */
@ZenRegister
@Document("mods/immersiveengineering/Squeezer")
@ZenCodeType.Name("mods.immersiveengineering.Squeezer")
public class SqueezerRecipeManager implements IRecipeManager {
    
    @Override
    public IRecipeType<SqueezerRecipe> getRecipeType() {
        return SqueezerRecipe.TYPE;
    }
    
    /**
     * Removes all recipes that return this given fluid Stack.
     * Only removes if the fluid and the fluid amount match.
     *
     * @param fluidStack The output to remove
     * @docParam fluidStack <fluid:immersiveengineering:plantoil> * 60
     */
    @ZenCodeType.Method
    public void removeRecipe(IFluidStack fluidStack) {
        final AbstractActionGenericRemoveRecipe<SqueezerRecipe> action = new AbstractActionGenericRemoveRecipe<SqueezerRecipe>(this, fluidStack) {
            @Override
            public boolean shouldRemove(SqueezerRecipe recipe) {
                return recipe.fluidOutput.isFluidStackIdentical(fluidStack.getInternal());
            }
        };
        
        CraftTweakerAPI.apply(action);
    }
    
    /**
     * Removes all recipes that return this given fluid.
     * Since it's only the fluid, it does not check amounts.
     *
     * @param fluid The fluid output to remove
     * @docParam fluid <fluid:immersiveengineering:plantoil>.fluid
     */
    @ZenCodeType.Method
    public void removeRecipe(MCFluid fluid) {
        final AbstractActionGenericRemoveRecipe<SqueezerRecipe> action = new AbstractActionGenericRemoveRecipe<SqueezerRecipe>(this, fluid) {
            @Override
            public boolean shouldRemove(SqueezerRecipe recipe) {
                return recipe.fluidOutput.getFluid() == fluid.getInternal();
            }
        };
        
        CraftTweakerAPI.apply(action);
    }
    
    /**
     * Adds a recipe to the Squeezer.
     * The item output is optional.
     *
     * @param recipePath  The recipe name, without the resource location
     * @param input       The input item
     * @param energy      The total energy required for this recipe
     * @param fluidOutput The fluid output
     * @param itemOutput  The item output
     * @docParam recipePath "pressure_creates_diamonds"
     * @docParam input <item:minecraft:coal_block> * 8
     * @docParam energy 6000
     * @docParam fluidOutput <fluid:immersiveengineering:creosote> * 2500
     * @docParam itemOutput <item:minecraft:diamond>
     */
    @ZenCodeType.Method
    public void addRecipe(String recipePath, IIngredient input, int energy, IFluidStack fluidOutput, @ZenCodeType.Optional("<item:minecraft:air>") IItemStack itemOutput) {
        final ResourceLocation resourceLocation = new ResourceLocation(Lib.MODID, recipePath);
        final IngredientWithSize inputWithSize = CrTIngredientUtil.getIngredientWithSize(input);
        final FluidStack fluidOut = CrTIngredientUtil.getFluidStack(fluidOutput);
        final ItemStack itemOut = itemOutput.getInternal();
        
        final SqueezerRecipe recipe = new SqueezerRecipe(resourceLocation, fluidOut, itemOut, inputWithSize, energy);
        CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, null));
    }
    
    /**
     * Adds a recipe to the Squeezer.
     * Short form if you don't want a fluid output.
     * Does the same as if you provided `<fluid:minecraft:empty> * 0` to the other addRecipe Method.
     *
     * @param recipePath  The recipe name, without the resource location
     * @param input       The input item
     * @param energy      The total energy required for this recipe
     * @param itemOutput  The item output
     * @docParam recipePath "slag_off"
     * @docParam input <item:immersiveengineering:slag> * 9
     * @docParam energy 5000
     * @docParam itemOutput <item:minecraft:dirt>
     */
    @ZenCodeType.Method
    public void addRecipe(String recipePath, IIngredient input, int energy, IItemStack itemOutput) {
        addRecipe(recipePath, input, energy, new MCFluidStackMutable(FluidStack.EMPTY), itemOutput);
    }
}
