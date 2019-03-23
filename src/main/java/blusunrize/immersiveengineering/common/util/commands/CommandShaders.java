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
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.UUID;

public class CommandShaders
{
	public static LiteralArgumentBuilder<CommandSource> create()
	{
		LiteralArgumentBuilder<CommandSource> main = Commands.literal("clearshaders");
		main.requires(source -> source.hasPermissionLevel(4));
		main.executes(source -> clearShaders(source, source.getSource().asPlayer()));
		main.then(Commands.argument("player", EntityArgument.singlePlayer()).executes(
				context -> clearShaders(context, context.getArgument("player", EntityPlayerMP.class))));
		return main;
	}

	private static int clearShaders(CommandContext<CommandSource> context, EntityPlayerMP player)
	{
		UUID uuid = player.getUniqueID();
		if(ShaderRegistry.receivedShaders.containsKey(uuid))
			ShaderRegistry.receivedShaders.get(uuid).clear();
		ShaderRegistry.recalculatePlayerTotalWeight(uuid);
		context.getSource().sendFeedback(
				new TextComponentTranslation(Lib.CHAT_COMMAND+"shaders.clear.sucess", player.getName()),
				true);
		return Command.SINGLE_SUCCESS;
	}
}