/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.command.boilerplate.CommandImpl;
import com.blamejared.crafttweaker.api.event.type.CTCommandRegisterEvent;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CrafttweakerEventHandlers
{

	@SubscribeEvent
	public static void onCommandCollection(CTCommandRegisterEvent event)
	{
		event.registerDump(new CommandImpl(
				"ieBlueprintCategories",
				new TextComponent("Lists the different blueprint categories for the IE workbench"),
				CrafttweakerEventHandlers::buildDumpCommand
		));
	}

	private static void buildDumpCommand(LiteralArgumentBuilder<CommandSourceStack> builder)
	{
		builder.executes(ctx -> {
			CraftTweakerAPI.LOGGER.info("List of all known blueprint categories: ");
			for(String recipeCategory : BlueprintCraftingRecipe.recipeCategories)
				CraftTweakerAPI.LOGGER.info("- {}", recipeCategory);
			final TextComponent message = new TextComponent(ChatFormatting.GREEN+"Categories written to the log"+ChatFormatting.RESET);
			ctx.getSource().sendSuccess(message, true);
			return 0;
		});
	}
}
