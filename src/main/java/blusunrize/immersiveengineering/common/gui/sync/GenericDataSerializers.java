/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.gui.sync;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class GenericDataSerializers
{
	private static final List<DataSerializer<?>> SERIALIZERS = new ArrayList<>();
	public static final DataSerializer<Integer> INT32 = register(
			FriendlyByteBuf::readVarInt, FriendlyByteBuf::writeVarInt
	);
	public static final DataSerializer<FluidStack> FLUID_STACK = register(
			FriendlyByteBuf::readFluidStack, FriendlyByteBuf::writeFluidStack,
			FluidStack::copy, FluidStack::isFluidStackIdentical
	);
	public static final DataSerializer<Boolean> BOOLEAN = register(
			FriendlyByteBuf::readBoolean, FriendlyByteBuf::writeBoolean
	);
	public static final DataSerializer<Float> FLOAT = register(
			FriendlyByteBuf::readFloat, FriendlyByteBuf::writeFloat
	);

	private static <T> DataSerializer<T> register(
			Function<FriendlyByteBuf, T> read, BiConsumer<FriendlyByteBuf, T> write
	)
	{
		return register(read, write, t -> t, Objects::equals);
	}

	private static <T> DataSerializer<T> register(
			Function<FriendlyByteBuf, T> read, BiConsumer<FriendlyByteBuf, T> write,
			UnaryOperator<T> copy, BiPredicate<T, T> equals
	)
	{
		DataSerializer<T> serializer = new DataSerializer<>(read, write, copy, equals, SERIALIZERS.size());
		SERIALIZERS.add(serializer);
		return serializer;
	}

	public static DataPair<?> read(FriendlyByteBuf buffer)
	{
		DataSerializer<?> serializer = SERIALIZERS.get(buffer.readVarInt());
		return serializer.read(buffer);
	}

	public record DataSerializer<T>(
			Function<FriendlyByteBuf, T> read,
			BiConsumer<FriendlyByteBuf, T> write,
			UnaryOperator<T> copy,
			BiPredicate<T, T> equals,
			int id
	)
	{
		public DataPair<T> read(FriendlyByteBuf from)
		{
			return new DataPair<>(this, read().apply(from));
		}
	}

	public static record DataPair<T>(DataSerializer<T> serializer, T data)
	{
		public void write(FriendlyByteBuf to)
		{
			to.writeVarInt(serializer.id());
			serializer.write().accept(to, data);
		}
	}
}
