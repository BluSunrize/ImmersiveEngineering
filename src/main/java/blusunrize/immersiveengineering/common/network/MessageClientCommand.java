/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * Serves as a workaround for implementing client-side commands while they are not provided by Forge.
 */
public class MessageClientCommand implements IMessage
{
	private final Type type;

	public MessageClientCommand(Type type)
	{
		this.type = type;
	}

	public MessageClientCommand(FriendlyByteBuf buf)
	{
		this(Type.values()[buf.readVarInt()]);
	}

	public static void send(CommandContext<CommandSourceStack> context, Type type) throws CommandSyntaxException
	{
		CommandSourceStack source = context.getSource();
		source.getEntityOrException();
		ServerPlayer entity = source.getPlayerOrException();
		ImmersiveEngineering.packetHandler.send(
				PacketDistributor.PLAYER.with(() -> entity), new MessageClientCommand(type)
		);
	}

	@Override
	public void toBytes(FriendlyByteBuf buf)
	{
		buf.writeVarInt(type.ordinal());
	}

	@Override
	public void process(Supplier<Context> context)
	{
		context.get().enqueueWork(type.run);
	}

	public enum Type
	{
		RESET_MANUAL(ImmersiveEngineering.proxy::resetManual),
		RESET_SHADER_CACHES(ImmersiveEngineering.proxy::clearRenderCaches);

		private final Runnable run;

		Type(Runnable run)
		{
			this.run = run;
		}
	}
}
