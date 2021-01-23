/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.impl.commands.CTCommandCollectionEvent;
import com.blamejared.crafttweaker.impl.commands.script_examples.ExampleCollectionEvent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CrafttweakerEventHandlers
{

	@SubscribeEvent
	public static void onCommandCollection(CTCommandCollectionEvent event)
	{
		event.registerDump("ieBlueprintCategories", "Lists the different blueprint categories for the IE workbench", commandContext -> {

			CraftTweakerAPI.logDump("List of all known blueprint categories: ");
			for(String recipeCategory : BlueprintCraftingRecipe.recipeCategories)
			{
				CraftTweakerAPI.logDump("- %s", recipeCategory);
			}
			final StringTextComponent message = new StringTextComponent(TextFormatting.GREEN+"Categories written to the log"+TextFormatting.RESET);
			commandContext.getSource().sendFeedback(message, true);
			return 0;
		});
	}

	@SubscribeEvent
	public static void onExampleEvent(ExampleCollectionEvent event)
	{
		addExampleFile(event, "alloy");
		addExampleFile(event, "arc_furnace");
		addExampleFile(event, "blast_furnace");
		addExampleFile(event, "blueprint");
		addExampleFile(event, "bottling_machine");
		addExampleFile(event, "cloche");
		addExampleFile(event, "coke_oven");
		addExampleFile(event, "crusher");
		addExampleFile(event, "fermenter");
		addExampleFile(event, "metal_press");
		addExampleFile(event, "mixer");
		addExampleFile(event, "refinery");
		addExampleFile(event, "sawmill");
		addExampleFile(event, "squeezer");
	}

	private static void addExampleFile(ExampleCollectionEvent event, String scriptName)
	{
		event.addResource(new ResourceLocation(Lib.MODID, Lib.MODID+"/"+scriptName));
	}
}
