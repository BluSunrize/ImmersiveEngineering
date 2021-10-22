/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.blocks.metal.FluidPipeBlockEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.InterModComms.IMCMessage;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author BluSunrize - 27.05.2018
 */
public class IEIMCHandler
{
	private static final HashMap<String, Consumer<IMCMessage>> MESSAGE_HANDLERS = new HashMap<>();

	public static void init()
	{
		MESSAGE_HANDLERS.put("fluidpipe_cover", imcMessage -> {
			Predicate<Block> func = (Predicate<Block>)imcMessage.messageSupplier().get();
			FluidPipeBlockEntity.validPipeCovers.add(func);
		});

		MESSAGE_HANDLERS.put("fluidpipe_cover_climb", imcMessage -> {
			Predicate<Block> func = (Predicate<Block>)imcMessage.messageSupplier().get();
			FluidPipeBlockEntity.climbablePipeCovers.add(func);
		});

		MESSAGE_HANDLERS.put("shaderbag_exclude", imcMessage -> {
			String s = (String)imcMessage.messageSupplier().get();
			try
			{
				Class<?> clazz = Class.forName(s);
				if(Mob.class.isAssignableFrom(clazz))
					EventHandler.listOfBoringBosses.add((Class<? extends Mob>)clazz);
				else
					IELogger.error("IMC Handling: "+s+" is not an instance of EntityLiving.");
			} catch(ClassNotFoundException e)
			{
				IELogger.error("IMC Handling: "+s+" is not a valid classname.");
			}
		});
	}

	public static void handleIMCMessages(Stream<IMCMessage> messages)
	{
		messages.forEach(message -> {
			if(MESSAGE_HANDLERS.containsKey(message.method()))
			{
				Consumer<IMCMessage> handler = MESSAGE_HANDLERS.get(message.method());
				handler.accept(message);
			}
		});
	}

}
