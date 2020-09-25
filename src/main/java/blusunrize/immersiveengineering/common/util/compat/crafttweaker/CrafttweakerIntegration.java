/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.*;
import blusunrize.immersiveengineering.api.crafting.*;
import com.blamejared.crafttweaker.api.*;
import com.blamejared.crafttweaker.impl.commands.*;
import net.minecraft.util.text.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.*;

@Mod.EventBusSubscriber(modid = Lib.MODID)
public class CrafttweakerIntegration {
    
    @SubscribeEvent
    public static void onCommandCollection(CTCommandCollectionEvent event) {
        event.registerDump("ieBlueprintCategories", "Lists the different blueprint categories for the IE workbench", commandContext -> {
            
            for(String recipeCategory : BlueprintCraftingRecipe.recipeCategories) {
                CraftTweakerAPI.logDump(recipeCategory);
            }
            final StringTextComponent message = new StringTextComponent(TextFormatting.GREEN + "Categories written to the log" + TextFormatting.RESET);
            commandContext.getSource().sendFeedback(message, true);
            return 0;
        });
    }
}
