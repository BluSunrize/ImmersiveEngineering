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
import blusunrize.immersiveengineering.common.util.compat.crafttweaker.actions.*;
import com.blamejared.crafttweaker.api.*;
import com.blamejared.crafttweaker.api.annotations.*;
import com.blamejared.crafttweaker.api.item.*;
import com.blamejared.crafttweaker.api.managers.*;
import com.blamejared.crafttweaker.impl.actions.recipes.*;
import com.blamejared.crafttweaker_annotations.annotations.*;
import net.minecraft.item.crafting.*;
import net.minecraft.util.*;
import org.openzen.zencode.java.*;

/**
 * Allows you to add or remove Fertilizers from the Garden Cloche
 * <p>
 * A Fertilizer consists of an ingredient and a fertilizer value
 *
 * @docParam this <recipetype:immersiveengineering:fertilizer>
 */
@ZenRegister
@Document("mods/immersiveengineering/Fertilizer")
@ZenCodeType.Name("mods.immersiveengineering.Fertilizer")
public class ClocheFertilizerManager implements IRecipeManager {
    
    @Override
    public IRecipeType<ClocheFertilizer> getRecipeType() {
        return ClocheFertilizer.TYPE;
    }
    
    /**
     * Adds the fertilizer as possible fertilizer
     *
     * @param recipePath      The recipe name, without the resource location
     * @param fertilizer      The fertilizer to be added
     * @param fertilizerValue The value this fertilizer gives in the garden cloche
     * @docParam recipePath "sulfur_grow"
     * @docParam fertilizer <tag:forge:dusts/sulfur>
     * @docParam fertilizerValue 6.0F
     */
    @ZenCodeType.Method
    public void addFertilizer(String recipePath, IIngredient fertilizer, float fertilizerValue) {
        final ResourceLocation resourceLocation = new ResourceLocation(Lib.MODID, recipePath);
        final ClocheFertilizer recipe = new ClocheFertilizer(resourceLocation, fertilizer.asVanillaIngredient(), fertilizerValue);
        CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, null));
    }
    
    /**
     * Removes a given fertilizer.
     * Will remove all fertilizers for which this IItemStack matches
     * <p>
     * In other words, if a fertilizer uses a Tag ingredient, you can remove it by providing any item with that tag.
     *
     * @param fertilizer The fertilizer to be removed
     * @docParam fertilizer <item:minecraft:bone_meal>
     */
    @ZenCodeType.Method
    public void removeFertilizer(IItemStack fertilizer) {
        CraftTweakerAPI.apply(new ActionRemoveFertilizer(this, fertilizer));
    }
    
    @Override
    public void removeRecipe(IItemStack fertilizer) {
        removeFertilizer(fertilizer);
    }
}
