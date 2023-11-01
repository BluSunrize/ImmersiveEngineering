/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.commands;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = Lib.MODID, value = Dist.CLIENT)
public class ClientCommands
{
	@SubscribeEvent
	public static void registerClientCommands(RegisterClientCommandsEvent ev)
	{
		LiteralArgumentBuilder<CommandSourceStack> main = Commands.literal("cie");
		main.then(createResetRender())
				.then(createResetManual());
		ev.getDispatcher().register(main);
	}

	public static LiteralArgumentBuilder<CommandSourceStack> createResetRender()
	{
		LiteralArgumentBuilder<CommandSourceStack> ret = Commands.literal("resetrender");
		ret.executes(context -> {
			ImmersiveEngineering.proxy.clearRenderCaches();
			return Command.SINGLE_SUCCESS;
		});
		return ret;
	}

	public static LiteralArgumentBuilder<CommandSourceStack> createResetManual()
	{
		LiteralArgumentBuilder<CommandSourceStack> ret = Commands.literal("resetmanual");
		ret.executes(context -> {
			ImmersiveEngineering.proxy.resetManual();
			return Command.SINGLE_SUCCESS;
		});
		return ret;
	}
}
