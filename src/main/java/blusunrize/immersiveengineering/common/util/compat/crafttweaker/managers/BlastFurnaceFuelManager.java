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
import com.blamejared.crafttweaker_annotations.annotations.*;
import net.minecraft.item.crafting.*;
import net.minecraft.util.*;
import org.openzen.zencode.java.*;

/**
 * Allows you to add or remove Blast Furnace fuel items.
 * <p>
 *
 * @docParam this <recipetype:immersiveengineering:blast_furnace_fuel>
 */
@ZenRegister
@Document("mods/immersiveengineering/BlastFurnaceFuel")
@ZenCodeType.Name("mods.immersiveengineering.BlastFurnaceFuel")
public class BlastFurnaceFuelManager implements IRecipeManager {
    
    @Override
    public IRecipeType<BlastFurnaceFuel> getRecipeType() {
        return BlastFurnaceFuel.TYPE;
    }
    
    /**
     * Adds a fuel to the Blast Furnace
     * @param recipePath The recipe name, without the resource location
     * @param fuel The fuel to be added
     * @param burnTime The fuel's burntime
     * @docParam recipePath "the_sungods_sword_can_burn"
     * @docParam fuel <item:minecraft:golden_sword>.withTag({RepairCost: 0 as int, Damage: 0 as int, display: {Name: "{\"text\":\"Sword of the Sungod\"}" as string}})
     * @docParam burnTime 100000
     */
    @ZenCodeType.Method
    public void addFuel(String recipePath, IIngredient fuel, int burnTime) {
        final ResourceLocation resourceLocation = new ResourceLocation(Lib.MODID, recipePath);
        final BlastFurnaceFuel recipe = new BlastFurnaceFuel(resourceLocation, fuel.asVanillaIngredient(), burnTime);
        CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, null));
    }
}
