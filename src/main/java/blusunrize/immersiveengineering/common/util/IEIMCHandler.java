package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.blocks.metal.FluidPipeTileEntity;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.InterModComms.IMCMessage;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author BluSunrize - 27.05.2018
 */
public class IEIMCHandler
{
	private static final HashMap<String, Consumer<IMCMessage>> MESSAGE_HANDLERS = new HashMap<>();

	public static void init()
	{
		MESSAGE_HANDLERS.put("fluidpipe_cover", imcMessage -> {
			Predicate<Block> func = (Predicate<Block>)imcMessage.getMessageSupplier().get();
			FluidPipeTileEntity.validPipeCovers.add(func);
		});

		MESSAGE_HANDLERS.put("fluidpipe_cover_climb", imcMessage -> {
			Predicate<Block> func = (Predicate<Block>)imcMessage.getMessageSupplier().get();
			FluidPipeTileEntity.climbablePipeCovers.add(func);
		});

		MESSAGE_HANDLERS.put("shaderbag_exclude", imcMessage -> {
			String s = (String)imcMessage.getMessageSupplier().get();
			try
			{
				Class clazz = Class.forName(s);
				if(MobEntity.class.isAssignableFrom(clazz))
					EventHandler.listOfBoringBosses.add(clazz);
				else
					IELogger.error("IMC Handling: "+s+" is not an instance of EntityLiving.");
			} catch(ClassNotFoundException e)
			{
				IELogger.error("IMC Handling: "+s+" is not a valid classname.");
			}
		});
	}

	public static void handleIMCMessages(ImmutableList<IMCMessage> messages)
	{
		for(IMCMessage message : messages)
			if(MESSAGE_HANDLERS.containsKey(message.getMethod()))
			{
				Consumer<IMCMessage> handler = MESSAGE_HANDLERS.get(message.getMethod());
				handler.accept(message);
			}
	}

}
