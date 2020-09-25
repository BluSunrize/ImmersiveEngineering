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
 * Allows you to add or remove Refinery recipes.
 * <p>
 * Refinery Recipes consist of two fluid inputs and a fluid output.
 *
 * @docParam this <recipetype:immersiveengineering:refinery>
 */
@ZenRegister
@Document("mods/immersiveengineering/Refinery")
@ZenCodeType.Name("mods.immersiveengineering.Refinery")
public class RefineryRecipeManager implements IRecipeManager {
    
    @Override
    public IRecipeType<RefineryRecipe> getRecipeType() {
        return RefineryRecipe.TYPE;
    }
    
    @Override
    public void removeRecipe(IItemStack output) {
        throw new UnsupportedOperationException("Cannot remove a refinery recipe by item output, since it only has a fluid output");
    }
    
    /**
     * Removes all recipes that return this given fluid Stack.
     * Only removes if the fluid and the fluid amount match.
     *
     * @param fluidStack The output to remove
     * @docParam fluidStack <fluid:immersiveengineering:biodiesel> * 16
     */
    @ZenCodeType.Method
    public void removeRecipe(IFluidStack fluidStack) {
        final AbstractActionGenericRemoveRecipe<RefineryRecipe> action = new AbstractActionGenericRemoveRecipe<RefineryRecipe>(this, fluidStack) {
            @Override
            public boolean shouldRemove(RefineryRecipe recipe) {
                return recipe.output.isFluidStackIdentical(fluidStack.getInternal());
            }
        };
        
        CraftTweakerAPI.apply(action);
    }
    
    /**
     * Removes all recipes that return this given fluid.
     * Since it's only the fluid, it does not check amounts.
     *
     * @param fluid The fluid output to remove
     * @docParam fluid <fluid:immersiveengineering:biodiesel>.fluid
     */
    @ZenCodeType.Method
    public void removeRecipe(MCFluid fluid) {
        final AbstractActionGenericRemoveRecipe<RefineryRecipe> action = new AbstractActionGenericRemoveRecipe<RefineryRecipe>(this, fluid) {
            @Override
            public boolean shouldRemove(RefineryRecipe recipe) {
                return recipe.output.getFluid() == fluid.getInternal();
            }
        };
        
        CraftTweakerAPI.apply(action);
    }
    
    /**
     * Adds a recipe to the Refinery.
     * Make sure that the provided Tags are valid fluid tags.
     *
     * @param recipePath  The recipe name, without the resource location
     * @param fluidInput1 The first fluid input, as Tag
     * @param amount1     The amount of fluid that should be consumed
     * @param fluidInput2 The second fluid input, as Tag
     * @param amount2     The amount of fluid that should be consumed
     * @param energy      The total energy required
     * @param output      The output fluid
     * @docParam recipePath "refine_herbicide"
     * @docParam fluidInput1 <tag:minecraft:water>
     * @docParam amount1 10
     * @docParam fluidInput2 <tag:forge:ethanol>
     * @docParam amount2 1
     * @docParam energy 1000
     * @docParam output <fluid:immersiveengineering:herbicide> * 10
     */
    @ZenCodeType.Method
    public void addRecipe(String recipePath, MCTag fluidInput1, int amount1, MCTag fluidInput2, int amount2, int energy, IFluidStack output) {
        final ResourceLocation resourceLocation = new ResourceLocation(Lib.MODID, recipePath);
        final FluidStack outputStack = CrTIngredientUtil.getFluidStack(output);
        
        if(!fluidInput1.isFluidTag()) {
            throw new IllegalArgumentException("Provided tag is not a fluid tag: " + fluidInput1.getCommandString());
        }
        
        if(!fluidInput2.isFluidTag()) {
            throw new IllegalArgumentException("Provided tag is not a fluid tag: " + fluidInput2.getCommandString());
        }
        
        final ResourceLocation tag1Location = fluidInput1.getId().getInternal();
        final ResourceLocation tag2Location = fluidInput2.getId().getInternal();
        final FluidTagInput tagInput1 = new FluidTagInput(tag1Location, amount1);
        final FluidTagInput tagInput2 = new FluidTagInput(tag2Location, amount2);
        
        final RefineryRecipe recipe = new RefineryRecipe(resourceLocation, outputStack, tagInput1, tagInput2, energy);
        CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, null));
    }
}
