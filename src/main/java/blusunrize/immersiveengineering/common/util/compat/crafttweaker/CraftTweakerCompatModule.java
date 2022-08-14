/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.plugin.CraftTweakerPlugin;
import com.blamejared.crafttweaker.api.plugin.ICommandRegistrationHandler;
import com.blamejared.crafttweaker.api.plugin.ICraftTweakerPlugin;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;


@CraftTweakerPlugin(Lib.MODID+":ct_module")
public class CraftTweakerCompatModule implements ICraftTweakerPlugin
{
	@Override
	public void registerCommands(ICommandRegistrationHandler handler)
	{
		handler.registerDump(
				"ieBlueprintCategories",
				new TextComponent("Lists the different blueprint categories for the IE workbench"),
				CraftTweakerCompatModule::buildDumpCommand
		);
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
