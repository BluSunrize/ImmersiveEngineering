/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.gui.sync;

import blusunrize.immersiveengineering.common.gui.ArcFurnaceMenu.ProcessSlot;
import blusunrize.immersiveengineering.common.gui.MixerMenu;
import blusunrize.immersiveengineering.common.gui.MixerMenu.SlotProgress;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.UnaryOperator;

public class GenericDataSerializers
{
	private static final List<DataSerializer<?>> SERIALIZERS = new ArrayList<>();
	public static final DataSerializer<Integer> INT32 = register(ByteBufCodecs.INT);
	public static final DataSerializer<FluidStack> FLUID_STACK = register(
			FluidStack.STREAM_CODEC, FluidStack::copy, FluidStack::matches
	);
	public static final DataSerializer<Boolean> BOOLEAN = register(ByteBufCodecs.BOOL);
	public static final DataSerializer<Float> FLOAT = register(ByteBufCodecs.FLOAT);
	public static final DataSerializer<List<ProcessSlot>> ARC_PROCESS_SLOTS = register(
			ProcessSlot.STREAM_CODEC.apply(ByteBufCodecs.list())
	);
	// Allows items to be synced without requiring a slot
	public static final DataSerializer<ItemStack> ITEM_STACK = register(
			ItemStack.STREAM_CODEC, ItemStack::copy, ItemStack::matches
	);
	public static final DataSerializer<byte[]> BYTE_ARRAY = register(
			ByteBufCodecs.BYTE_ARRAY, arr -> Arrays.copyOf(arr, arr.length), Arrays::equals
	);
	public static final DataSerializer<List<FluidStack>> FLUID_STACKS = register(
			FluidStack.STREAM_CODEC.apply(ByteBufCodecs.list()),
			l -> l.stream().map(FluidStack::copy).toList(),
			(l1, l2) -> {
				if(l1.size()!=l2.size())
					return false;
				for(int i = 0; i < l1.size(); ++i)
					if(!FluidStack.matches(l1.get(i), l2.get(i)))
						return false;
				return true;
			}
	);
	public static final DataSerializer<List<MixerMenu.SlotProgress>> MIXER_SLOTS = register(
			SlotProgress.STREAM_CODEC.apply(ByteBufCodecs.list())
	);
	public static final DataSerializer<List<String>> STRINGS = register(
			ByteBufCodecs.stringUtf8(512).apply(ByteBufCodecs.list()),
			ArrayList::new, List::equals
	);

	private static <T> DataSerializer<T> register(StreamCodec<? super RegistryFriendlyByteBuf, T> codec)
	{
		return register(codec, t -> t, Objects::equals);
	}

	private static <T> DataSerializer<T> register(
			StreamCodec<? super RegistryFriendlyByteBuf, T> codec, UnaryOperator<T> copy, BiPredicate<T, T> equals
	)
	{
		DataSerializer<T> serializer = new DataSerializer<>(codec, copy, equals, SERIALIZERS.size());
		SERIALIZERS.add(serializer);
		return serializer;
	}

	public record DataSerializer<T>(
			StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
			UnaryOperator<T> copy,
			BiPredicate<T, T> equals,
			int id
	)
	{
		private DataPair<T> read(RegistryFriendlyByteBuf from)
		{
			return new DataPair<>(this, codec.decode(from));
		}
	}

	public record DataPair<T>(DataSerializer<T> serializer, T data)
	{
		public static final StreamCodec<RegistryFriendlyByteBuf, DataPair<?>> CODEC = new StreamCodec<>()
		{
			@Override
			public DataPair<?> decode(RegistryFriendlyByteBuf buffer)
			{
				DataSerializer<?> serializer = SERIALIZERS.get(buffer.readVarInt());
				return serializer.read(buffer);
			}

			@Override
			public void encode(RegistryFriendlyByteBuf buffer, DataPair<?> data)
			{
				data.write(buffer);
			}
		};

		private void write(RegistryFriendlyByteBuf to)
		{
			to.writeVarInt(serializer.id());
			serializer.codec.encode(to, data);
		}
	}
}
