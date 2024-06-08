/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.LogicCircuitHandler.LogicCircuitInstruction;
import blusunrize.immersiveengineering.client.fx.FractalOptions.Color4;
import blusunrize.immersiveengineering.common.items.CoresampleItem;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class IEDataComponents
{
	public static final DeferredRegister<DataComponentType<?>> REGISTER = DeferredRegister.create(
			Registries.DATA_COMPONENT_TYPE, Lib.MODID
	);

	public static DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> BALLOON_OFFSET = REGISTER.register(
			"balloon_offset", () -> DataComponentType.<Integer>builder()
					.persistent(Codec.INT)
					.networkSynchronized(ByteBufCodecs.VAR_INT)
					.build()
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<LogicCircuitInstruction>> CIRCUIT_INSTRUCTION = REGISTER.register(
			"logic_instruction", () -> DataComponentType.<LogicCircuitInstruction>builder()
					.persistent(LogicCircuitInstruction.CODEC)
					.networkSynchronized(LogicCircuitInstruction.STREAM_CODEC)
					.build()
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<SimpleFluidContent>> GENERIC_FLUID = REGISTER.register(
			"fluid", () -> DataComponentType.<SimpleFluidContent>builder()
					.persistent(SimpleFluidContent.CODEC)
					.networkSynchronized(SimpleFluidContent.STREAM_CODEC)
					.build()
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<CoresampleItem.ItemData>> CORESAMPLE = REGISTER.register(
			"coresample_data", () -> DataComponentType.<CoresampleItem.ItemData>builder()
					.persistent(CoresampleItem.ItemData.CODEC)
					.networkSynchronized(CoresampleItem.ItemData.STREAM_CODEC)
					.build()
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<String>> BLUEPRINT = REGISTER.register(
			"blueprint", () -> DataComponentType.<String>builder()
					.persistent(Codec.STRING)
					.networkSynchronized(ByteBufCodecs.STRING_UTF8)
					.build()
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<Color4>> COLOR = REGISTER.register(
			"color", () -> DataComponentType.<Color4>builder()
					.persistent(Color4.CODEC)
					.networkSynchronized(Color4.STREAM_CODEC)
					.build()
	);
	// TODO probably just a massive hack? Does this need to be persistent?
	public static DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> JERRYCAN_DRAIN = REGISTER.register(
			"jerrycan_drain", () -> DataComponentType.<Integer>builder().build()
	);
}
