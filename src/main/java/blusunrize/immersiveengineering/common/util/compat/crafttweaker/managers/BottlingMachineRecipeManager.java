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
import com.blamejared.crafttweaker.api.*;
import com.blamejared.crafttweaker.api.annotations.*;
import com.blamejared.crafttweaker.api.item.*;
import com.blamejared.crafttweaker.api.managers.*;
import com.blamejared.crafttweaker.impl.actions.recipes.*;
import com.blamejared.crafttweaker.impl.tag.*;
import com.blamejared.crafttweaker_annotations.annotations.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.util.*;
import org.openzen.zencode.java.*;

/**
 * Allows you to add or remove Bottling Machine recipes.
 * <p>
 * Bottling Machine recipes consist of an item ingredient, a fluid input and an item output.
 *
 * @docParam this <recipetype:immersiveengineering:bottling_machine>
 */
@ZenRegister
@Document("mods/immersiveengineering/BottlingMachine")
@ZenCodeType.Name("mods.immersiveengineering.BottlingMachine")
public class BottlingMachineRecipeManager implements IRecipeManager {
    
    @Override
    public IRecipeType<BottlingMachineRecipe> getRecipeType() {
        return BottlingMachineRecipe.TYPE;
    }
    
    /**
     * Adds a recipe to the Bottling Machine.
     * The bottling Machine only goes via Fluid tag!
     *
     * @param recipePath The recipe name, without the resource location
     * @param itemInput  The item input (the item to be filled)
     * @param fluidTag   The fluid tag of the fluid
     * @param amount     The amount of the liquid that is required for the recipe (in mB)
     * @param output     The resulting "filled" item.
     * @docParam recipePath "grow_a_pick"
     * @docParam itemInput <item:minecraft:stick>
     * @docParam fluidTag <tag:minecraft:water>
     * @docParam amount 250
     * @docParam output <item:minecraft:wooden_pickaxe>
     */
    @ZenCodeType.Method
    public void addRecipe(String recipePath, IIngredient itemInput, MCTag fluidTag, int amount, IItemStack output) {
        final ResourceLocation resourceLocation = new ResourceLocation(Lib.MODID, recipePath);
        
        if(!fluidTag.isFluidTag()) {
            throw new IllegalArgumentException("The provided Tag needs to be a Fluid Tag!");
        }
        
        final FluidTagInput fluidTagInput = new FluidTagInput(fluidTag.getId().getInternal(), amount);
        
        final ItemStack itemOutput = output.getInternal();
        final Ingredient input = itemInput.asVanillaIngredient();
        
        final BottlingMachineRecipe recipe = new BottlingMachineRecipe(resourceLocation, itemOutput, input, fluidTagInput);
        
        CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, null));
    }
}
