/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.network;

import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class PacketUtils
{
	public static <T> List<T> readList(FriendlyByteBuf buffer, Function<FriendlyByteBuf, T> readElement)
	{
		int numElements = buffer.readVarInt();
		List<T> result = new ArrayList<>(numElements);
		for(int i = 0; i < numElements; ++i)
			result.add(readElement.apply(buffer));
		return result;
	}

	public static <T> void writeListReverse(FriendlyByteBuf buffer, List<T> toWrite, BiConsumer<FriendlyByteBuf, T> writeElement)
	{
		writeList(buffer, toWrite, (t, buf) -> writeElement.accept(buf, t));
	}

	public static <T> void writeList(FriendlyByteBuf buffer, List<T> toWrite, BiConsumer<T, FriendlyByteBuf> writeElement)
	{
		buffer.writeVarInt(toWrite.size());
		for(T element : toWrite)
			writeElement.accept(element, buffer);
	}
}
