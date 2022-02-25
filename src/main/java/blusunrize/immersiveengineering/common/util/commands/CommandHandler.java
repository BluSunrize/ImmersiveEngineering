/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CommandHandler
{
	public static void registerServer(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		//TODO do all subcommands have proper permission requirements?
		LiteralArgumentBuilder<CommandSourceStack> main = Commands.literal("ie");
		main.then(CommandMineral.create())
				.then(CommandShaders.create());
		dispatcher.register(main);
	}
}