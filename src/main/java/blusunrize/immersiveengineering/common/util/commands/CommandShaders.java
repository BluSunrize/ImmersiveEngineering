/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.commands;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class CommandShaders
{
	public static LiteralArgumentBuilder<CommandSourceStack> create()
	{
		LiteralArgumentBuilder<CommandSourceStack> main = Commands.literal("clearshaders");
		main.requires(source -> source.hasPermission(4));
		main.executes(source -> clearShaders(source, source.getSource().getPlayerOrException()));
		main.then(Commands.argument("player", EntityArgument.player()).executes(
				context -> clearShaders(context,  EntityArgument.getPlayer(context, "player"))));
		return main;
	}

	private static int clearShaders(CommandContext<CommandSourceStack> context, ServerPlayer player)
	{
		UUID uuid = player.getUUID();
		if(ShaderRegistry.receivedShaders.containsKey(uuid))
			ShaderRegistry.receivedShaders.get(uuid).clear();
		ShaderRegistry.recalculatePlayerTotalWeight(uuid);
		context.getSource().sendSuccess(
				() -> Component.translatable(Lib.CHAT_COMMAND+"shaders.clear.sucess", player.getName()),
				true);
		return Command.SINGLE_SUCCESS;
	}
}