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

/**
 * Allows you to add or remove Blast Furnace recipes.
 * <p>
 * Blast furnace recipes consist of an ingredient, an output and an optional slag result.
 *
 * @docParam this <recipetype:immersiveengineering:blast_furnace>
 */
@ZenRegister
@Document("mods/immersiveengineering/BlastFurnace")
@ZenCodeType.Name("mods.immersiveengineering.BlastFurnace")
public class BlastFurnaceRecipeManager implements IRecipeManager {
    
    @Override
    public IRecipeType<BlastFurnaceRecipe> getRecipeType() {
        return BlastFurnaceRecipe.TYPE;
    }
    
    /**
     * Adds a Blast furnace recipe
     *
     * @param recipePath The recipe name, without the resource location
     * @param ingredient The item input
     * @param time       The time this recipe needs, in ticks
     * @param output     The recipe output
     * @param slag       The item that should appear in the slag slot, optional
     * @docParam recipePath "wool_to_charcoal"
     * @docParam ingredient <tag:minecraft:wool>
     * @docParam time 1000
     * @docParam output <item:minecraft:charcoal>
     * @docParam slag <item:minecraft:string>
     */
    @ZenCodeType.Method
    public void addRecipe(String recipePath, IIngredient ingredient, int time, IItemStack output, @ZenCodeType.Optional("<item:minecraft:air>") IItemStack slag) {
        final ResourceLocation resourceLocation = new ResourceLocation(Lib.MODID, recipePath);
        final IngredientWithSize ingredientWithSize = CrTIngredientUtil.getIngredientWithSize(ingredient);
        final ItemStack outputItem = output.getInternal();
        final ItemStack slagItem = slag.getInternal();
        final BlastFurnaceRecipe blastFurnaceRecipe = new BlastFurnaceRecipe(resourceLocation, outputItem, ingredientWithSize, time, slagItem);
        CraftTweakerAPI.apply(new ActionAddRecipe(this, blastFurnaceRecipe, null));
    }
}
