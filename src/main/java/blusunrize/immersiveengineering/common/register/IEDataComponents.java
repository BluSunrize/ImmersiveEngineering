/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.api.tool.LogicCircuitHandler.LogicCircuitInstruction;
import blusunrize.immersiveengineering.api.utils.Color4;
import blusunrize.immersiveengineering.common.blocks.metal.CapacitorBlockEntity.CapacitorState;
import blusunrize.immersiveengineering.common.blocks.metal.TurretBlockEntity.TurretConfig;
import blusunrize.immersiveengineering.common.items.ChemthrowerItem.ChemthrowerData;
import blusunrize.immersiveengineering.common.items.CoresampleItem;
import blusunrize.immersiveengineering.common.items.EarmuffsItem.EarmuffData;
import blusunrize.immersiveengineering.common.items.HammerItem.MultiblockRestriction;
import blusunrize.immersiveengineering.common.items.RevolverItem.Perks;
import blusunrize.immersiveengineering.common.items.SurveyToolsItem.VeinEntry;
import blusunrize.immersiveengineering.common.items.components.AttachedItem;
import blusunrize.immersiveengineering.common.items.components.DirectNBT;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
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

public class IEDataComponents
{
	private static final DeferredRegister<DataComponentType<?>> REGISTER = DeferredRegister.create(
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
	public static DeferredHolder<DataComponentType<?>, DataComponentType<TurretConfig>> TURRET_DATA = REGISTER.register(
			"turret_data", () -> DataComponentType.<TurretConfig>builder()
					.persistent(TurretConfig.CODEC)
					.networkSynchronized(TurretConfig.STREAM_CODEC)
					.build()
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<List<VeinEntry>>> SURVERYTOOL_DATA = REGISTER.register(
			"surveytool_data", () -> DataComponentType.<List<VeinEntry>>builder()
					.persistent(VeinEntry.CODEC.listOf())
					.networkSynchronized(VeinEntry.STREAM_CODEC.apply(ByteBufCodecs.list()))
					.build()
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<DirectNBT>> FLUID_SORTER_DATA = directNBT("fluid_sorter_nbt");
	public static DeferredHolder<DataComponentType<?>, DataComponentType<DirectNBT>> LOGIC_UNIT_DATA = directNBT("logic_unit_nbt");
	public static DeferredHolder<DataComponentType<?>, DataComponentType<DirectNBT>> SORTER_DATA = directNBT("sorter_nbt");
	public static DeferredHolder<DataComponentType<?>, DataComponentType<DirectNBT>> UPGRADE_DATA = directNBT("upgrade_nbt");
	public static DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SKYHOOK_SPEED_LIMIT = REGISTER.register(
			"speed_limit", () -> DataComponentType.<Boolean>builder()
					.persistent(Codec.BOOL)
					.networkSynchronized(ByteBufCodecs.BOOL)
					.build()
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> DRILL_SINGLEBLOCK = REGISTER.register(
			"drill_singleblock", () -> DataComponentType.<Boolean>builder()
					.persistent(Codec.BOOL)
					.networkSynchronized(ByteBufCodecs.BOOL)
					.build()
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<MultiblockRestriction>> MULTIBLOCK_RESTRICTION = REGISTER.register(
			"multiblock_restriction", () -> DataComponentType.<MultiblockRestriction>builder()
					.persistent(MultiblockRestriction.CODEC)
					.networkSynchronized(MultiblockRestriction.STREAM_CODEC)
					.build()
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<Perks>> REVOLVER_PERKS = REGISTER.register(
			"revolver_perks", () -> DataComponentType.<Perks>builder()
					.persistent(Perks.CODEC)
					.networkSynchronized(Perks.STREAM_CODEC)
					.build()
	);
	private static final Map<IBullet<?>, DeferredHolder<DataComponentType<?>, DataComponentType<?>>> BULLETS = new IdentityHashMap<>();

	// TODO probably just a massive hack? Does this need to be persistent?
	public static DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> JERRYCAN_DRAIN = REGISTER.register(
			"jerrycan_drain", () -> DataComponentType.<Integer>builder().build()
	);

	public static void init(IEventBus bus)
	{
		REGISTER.register(bus);
		for(ResourceLocation name : BulletHandler.getAllKeys())
		{
			IBullet<?> bullet = BulletHandler.getBullet(name);
			var path = (name.getNamespace().equals(Lib.MODID)?"": name.getNamespace()+"_")+name.getPath();
			var codecs = bullet.getCodec();
			BULLETS.put(bullet, REGISTER.register(path, codecs::makeDataComponentType));
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> DataComponentType<T> getBulletData(IBullet<T> bullet)
	{
		return (DataComponentType<T>)BULLETS.get(bullet).get();
	}

	@Deprecated
	private static DeferredHolder<DataComponentType<?>, DataComponentType<DirectNBT>> directNBT(String name)
	{
		return REGISTER.register(
				name, () -> DataComponentType.<DirectNBT>builder()
						.persistent(DirectNBT.CODEC)
						.networkSynchronized(DirectNBT.STREAM_CODEC)
						.build()
		);
	}
}
