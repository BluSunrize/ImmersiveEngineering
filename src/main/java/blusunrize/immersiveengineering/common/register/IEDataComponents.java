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
import blusunrize.immersiveengineering.common.blocks.metal.CapacitorBlockEntity.CapacitorState;
import blusunrize.immersiveengineering.common.items.ChemthrowerItem.ChemthrowerData;
import blusunrize.immersiveengineering.common.items.CoresampleItem;
import blusunrize.immersiveengineering.common.items.EarmuffsItem.EarmuffData;
import blusunrize.immersiveengineering.common.items.components.AttachedItem;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;
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
	public static DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> GENERIC_ITEMS = REGISTER.register(
			"items", () -> DataComponentType.<ItemContainerContents>builder()
					.persistent(ItemContainerContents.CODEC)
					.networkSynchronized(ItemContainerContents.STREAM_CODEC)
					.build()
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> GENERIC_ENERGY = REGISTER.register(
			"energy", () -> DataComponentType.<Integer>builder()
					.persistent(Codec.INT)
					.networkSynchronized(ByteBufCodecs.VAR_INT)
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
	public static DeferredHolder<DataComponentType<?>, DataComponentType<Block>> DEFAULT_COVER = REGISTER.register(
			"default_cover", () -> DataComponentType.<Block>builder()
					.persistent(BuiltInRegistries.BLOCK.byNameCodec())
					.networkSynchronized(ByteBufCodecs.registry(Registries.BLOCK))
					.build()
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<CapacitorState>> CAPACITOR_CONFIG = REGISTER.register(
			"cap_config", () -> DataComponentType.<CapacitorState>builder()
					.persistent(CapacitorState.CODEC)
					.networkSynchronized(CapacitorState.STREAM_CODEC)
					.build()
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<EarmuffData>> EARMUFF_DATA = REGISTER.register(
			"earmuff_data", () -> DataComponentType.<EarmuffData>builder()
					.persistent(EarmuffData.CODEC)
					.networkSynchronized(EarmuffData.STREAM_CODEC)
					.build()
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<AttachedItem>> CONTAINED_EARMUFF = REGISTER.register(
			"contained_earmuff", () -> DataComponentType.<AttachedItem>builder()
					.persistent(AttachedItem.CODEC)
					.networkSynchronized(AttachedItem.STREAM_CODEC)
					.build()
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<AttachedItem>> CONTAINED_POWERPACK = REGISTER.register(
			"contained_powerpack", () -> DataComponentType.<AttachedItem>builder()
					.persistent(AttachedItem.CODEC)
					.networkSynchronized(AttachedItem.STREAM_CODEC)
					.build()
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<ChemthrowerData>> CHEMTHROWER_DATA = REGISTER.register(
			"chemthrower_data", () -> DataComponentType.<ChemthrowerData>builder()
					.persistent(ChemthrowerData.CODEC)
					.networkSynchronized(ChemthrowerData.STREAM_CODEC)
					.build()
	);

	// TODO probably just a massive hack? Does this need to be persistent?
	public static DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> JERRYCAN_DRAIN = REGISTER.register(
			"jerrycan_drain", () -> DataComponentType.<Integer>builder().build()
	);
}
