package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPipe;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author BluSunrize - 27.05.2018
 */
public class IEIMCHandler
{
	private static final HashMap<String, Pair<Predicate<IMCMessage>, Consumer<IMCMessage>>> MESSAGE_HANDLERS = new HashMap<>();

	public static void init()
	{
		MESSAGE_HANDLERS.put("fluidpipe_cover", Pair.of(IMCMessage::isFunctionMessage, imcMessage -> {
			Optional<Function<ItemStack, Boolean>> opFunc = imcMessage.getFunctionValue(ItemStack.class, Boolean.class);
			opFunc.ifPresent(itemStackBooleanFunction -> TileEntityFluidPipe.validPipeCovers.add(itemStackBooleanFunction));
		}));

		MESSAGE_HANDLERS.put("fluidpipe_cover_climb", Pair.of(IMCMessage::isFunctionMessage, imcMessage -> {
			Optional<Function<ItemStack, Boolean>> opFunc = imcMessage.getFunctionValue(ItemStack.class, Boolean.class);
			opFunc.ifPresent(itemStackBooleanFunction -> TileEntityFluidPipe.climbablePipeCovers.add(itemStackBooleanFunction));
		}));

		MESSAGE_HANDLERS.put("shaderbag_exclude", Pair.of(IMCMessage::isStringMessage, imcMessage -> {
			String s = imcMessage.getStringValue();
			try
			{
				Class clazz = Class.forName(s);
				if(EntityLiving.class.isAssignableFrom(clazz))
					EventHandler.listOfBoringBosses.add(clazz);
				else
					IELogger.error("IMC Handling: "+s+" is not an instance of EntityLiving.");
			} catch(ClassNotFoundException e)
			{
				IELogger.error("IMC Handling: "+s+" is not a valid classname.");
			}
		}));
	}

	public static void handleIMCMessages(ImmutableList<IMCMessage> messages)
	{
		for(IMCMessage message : messages)
			if(MESSAGE_HANDLERS.containsKey(message.key))
			{
				Pair<Predicate<IMCMessage>, Consumer<IMCMessage>> handler = MESSAGE_HANDLERS.get(message.key);
				if(handler.getLeft().test(message))
					handler.getRight().accept(message);
			}
	}

}
