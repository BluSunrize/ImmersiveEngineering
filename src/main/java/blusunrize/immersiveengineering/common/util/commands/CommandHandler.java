/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.commands;

import blusunrize.immersiveengineering.common.network.MessageClientCommand.Type;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class CommandHandler
{
	public static void registerServer(CommandDispatcher<CommandSource> dispatcher)
	{
		//TODO do all subcommands have proper permission requirements?
		LiteralArgumentBuilder<CommandSource> main = Commands.literal("ie");
		main.then(CommandMineral.create())
				.then(CommandShaders.create())
				.then(RemoteClientCommand.clientComamnd("resetrender", Type.RESET_SHADER_CACHES))
				.then(RemoteClientCommand.clientComamnd("resetmanual", Type.RESET_MANUAL));
		dispatcher.register(main);
	}
}