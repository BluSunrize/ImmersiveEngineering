/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.api.IEApiDataComponents;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.api.tool.LogicCircuitHandler.LogicCircuitInstruction;
import blusunrize.immersiveengineering.api.utils.Color4;
import blusunrize.immersiveengineering.api.utils.codec.DualCodec;
import blusunrize.immersiveengineering.api.utils.codec.DualCodecs;
import blusunrize.immersiveengineering.api.wires.utils.WireLink;
import blusunrize.immersiveengineering.common.blocks.metal.CapacitorBlockEntity.CapacitorState;
import blusunrize.immersiveengineering.common.blocks.metal.TurretBlockEntity.TurretConfig;
import blusunrize.immersiveengineering.common.items.ChemthrowerItem.ChemthrowerData;
import blusunrize.immersiveengineering.common.items.CoresampleItem;
import blusunrize.immersiveengineering.common.items.EarmuffsItem.EarmuffData;
import blusunrize.immersiveengineering.common.items.FluorescentTubeItem.LitState;
import blusunrize.immersiveengineering.common.items.HammerItem.MultiblockRestriction;
import blusunrize.immersiveengineering.common.items.RevolverItem.Perks;
import blusunrize.immersiveengineering.common.items.SurveyToolsItem.VeinEntry;
import blusunrize.immersiveengineering.common.items.components.AttachedItem;
import blusunrize.immersiveengineering.common.items.components.DirectNBT;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class IEDataComponents
{
	private static final DeferredRegister<DataComponentType<?>> REGISTER = DeferredRegister.create(
			Registries.DATA_COMPONENT_TYPE, Lib.MODID
	);

	public static DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> BALLOON_OFFSET = make(
			"balloon_offset", DualCodecs.INT
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<LogicCircuitInstruction>> CIRCUIT_INSTRUCTION = make(
			"logic_instruction", LogicCircuitInstruction.CODECS
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
	public static DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> GENERIC_ENERGY = make(
			"energy", DualCodecs.INT
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<CoresampleItem.ItemData>> CORESAMPLE = make(
			"coresample_data", CoresampleItem.ItemData.CODECS
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<Color4>> COLOR = make("color", Color4.CODECS);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<Block>> DEFAULT_COVER = make(
			"default_cover", DualCodecs.registry(BuiltInRegistries.BLOCK)
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<CapacitorState>> CAPACITOR_CONFIG = make(
			"cap_config", CapacitorState.CODECS
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<EarmuffData>> EARMUFF_DATA = make(
			"earmuff_data", EarmuffData.CODECS
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<AttachedItem>> CONTAINED_EARMUFF = make(
			"contained_earmuff", AttachedItem.CODECS
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<AttachedItem>> CONTAINED_POWERPACK = make(
			"contained_powerpack", AttachedItem.CODECS
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<ChemthrowerData>> CHEMTHROWER_DATA = make(
			"chemthrower_data", ChemthrowerData.CODECS
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<TurretConfig>> TURRET_DATA = make("turret_data", TurretConfig.CODECS);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<List<VeinEntry>>> SURVERYTOOL_DATA = make("surveytool_data", VeinEntry.CODECS.listOf());
	public static DeferredHolder<DataComponentType<?>, DataComponentType<DirectNBT>> FLUID_SORTER_DATA = directNBT("fluid_sorter_nbt");
	public static DeferredHolder<DataComponentType<?>, DataComponentType<DirectNBT>> LOGIC_UNIT_DATA = directNBT("logic_unit_nbt");
	public static DeferredHolder<DataComponentType<?>, DataComponentType<DirectNBT>> SORTER_DATA = directNBT("sorter_nbt");
	public static DeferredHolder<DataComponentType<?>, DataComponentType<DirectNBT>> UPGRADE_DATA = directNBT("upgrade_nbt");
	public static DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SKYHOOK_SPEED_LIMIT = make("speed_limit", DualCodecs.BOOL);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> DRILL_SINGLEBLOCK = make("drill_singleblock", DualCodecs.BOOL);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<MultiblockRestriction>> MULTIBLOCK_RESTRICTION = make(
			"multiblock_restriction", MultiblockRestriction.CODECS
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<Perks>> REVOLVER_PERKS = make("revolver_perks", Perks.CODECS);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<String>> REVOLVER_ELITE = make("revolver_elite", DualCodecs.STRING);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<String>> REVOLVER_FLAVOUR = make("revolver_flavour", DualCodecs.STRING);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<LitState>> FLUORESCENT_TUBE_LIT = make("fluorescent_tube", LitState.CODECS);
	private static final Map<IBullet<?>, Supplier<DataComponentType<?>>> BULLETS = new IdentityHashMap<>();

	// TODO probably just a massive hack? Does this need to be persistent?
	public static DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> JERRYCAN_DRAIN = REGISTER.register(
			"jerrycan_drain", () -> DataComponentType.<Integer>builder()
					.persistent(Codec.INT)
					.networkSynchronized(ByteBufCodecs.INT)
					.build()
	);

	public static void init(IEventBus bus)
	{
		REGISTER.register(bus);
		for(ResourceLocation name : BulletHandler.getAllKeys())
		{
			var bullet = BulletHandler.getBullet(name);
			var path = (name.getNamespace().equals(Lib.MODID)?"": name.getNamespace()+"_")+name.getPath();
			var codecs = bullet.getCodec();
			var entry = make(path, codecs.codecs());
			BULLETS.put(bullet, entry::get);
		}
		IEApiDataComponents.WIRE_LINK = make("wire_link", WireLink.CODECS);
		IEApiDataComponents.BLUEPRINT_TYPE = make("blueprint", DualCodecs.STRING);
		IEApiDataComponents.ATTACHED_SHADER = make("shader", DualCodecs.RESOURCE_LOCATION);
		IEApiDataComponents.FLUID_PRESSURIZED = make("fluid_pressurized", DualCodecs.unit(Unit.INSTANCE));
	}

	@SuppressWarnings("unchecked")
	public static <T> DataComponentType<T> getBulletData(IBullet<T> bullet)
	{
		return (DataComponentType<T>)BULLETS.get(bullet).get();
	}

	private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> make(String name, DualCodec<? super RegistryFriendlyByteBuf, T> codec)
	{
		return REGISTER.register(
				name, () -> DataComponentType.<T>builder()
						.persistent(codec.codec())
						.networkSynchronized(codec.streamCodec())
						.build()
		);
	}

	@Deprecated
	private static DeferredHolder<DataComponentType<?>, DataComponentType<DirectNBT>> directNBT(String name)
	{
		return make(name, DirectNBT.CODECS);
	}
}
