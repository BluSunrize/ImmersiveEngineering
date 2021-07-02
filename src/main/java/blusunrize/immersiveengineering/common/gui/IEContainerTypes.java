/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.blocks.stone.AlloySmelterTileEntity;
import blusunrize.immersiveengineering.common.blocks.stone.BlastFurnaceTileEntity;
import blusunrize.immersiveengineering.common.blocks.stone.CokeOvenTileEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.*;
import blusunrize.immersiveengineering.common.entities.CrateMinecartEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

public class IEContainerTypes
{
	public static final DeferredRegister<ContainerType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.CONTAINERS, Lib.MODID);

	public static final TileContainer<CokeOvenTileEntity, CokeOvenContainer> COKE_OVEN = register(Lib.GUIID_CokeOven, CokeOvenContainer::new);
	public static final TileContainer<AlloySmelterTileEntity, AlloySmelterContainer> ALLOY_SMELTER = register(Lib.GUIID_AlloySmelter, AlloySmelterContainer::new);
	public static final TileContainer<BlastFurnaceTileEntity<?>, BlastFurnaceContainer> BLAST_FURNACE = register(Lib.GUIID_BlastFurnace, BlastFurnaceContainer::new);
	public static final TileContainer<CraftingTableTileEntity, CraftingTableContainer> CRAFTING_TABLE = register(Lib.GUIID_CraftingTable, CraftingTableContainer::new);
	public static final TileContainer<WoodenCrateTileEntity, CrateContainer> WOODEN_CRATE = register(Lib.GUIID_WoodenCrate, CrateContainer::new);
	public static final TileContainer<ModWorkbenchTileEntity, ModWorkbenchContainer> MOD_WORKBENCH = register(Lib.GUIID_Workbench, ModWorkbenchContainer::new);
	public static final TileContainer<CircuitTableTileEntity, CircuitTableContainer> CIRCUIT_TABLE = register(Lib.GUIID_CircuitTable, CircuitTableContainer::new);
	public static final TileContainer<AssemblerTileEntity, AssemblerContainer> ASSEMBLER = register(Lib.GUIID_Assembler, AssemblerContainer::new);
	public static final TileContainer<SorterTileEntity, SorterContainer> SORTER = register(Lib.GUIID_Sorter, SorterContainer::new);
	public static final TileContainer<ItemBatcherTileEntity, ItemBatcherContainer> ITEM_BATCHER = register(Lib.GUIID_ItemBatcher, ItemBatcherContainer::new);
	public static final TileContainer<LogicUnitTileEntity, LogicUnitContainer> LOGIC_UNIT = register(Lib.GUIID_LogicUnit, LogicUnitContainer::new);
	public static final TileContainer<SqueezerTileEntity, SqueezerContainer> SQUEEZER = register(Lib.GUIID_Squeezer, SqueezerContainer::new);
	public static final TileContainer<FermenterTileEntity, FermenterContainer> FERMENTER = register(Lib.GUIID_Fermenter, FermenterContainer::new);
	public static final TileContainer<RefineryTileEntity, RefineryContainer> REFINERY = register(Lib.GUIID_Refinery, RefineryContainer::new);
	public static final TileContainer<ArcFurnaceTileEntity, ArcFurnaceContainer> ARC_FURNACE = register(Lib.GUIID_ArcFurnace, ArcFurnaceContainer::new);
	public static final TileContainer<AutoWorkbenchTileEntity, AutoWorkbenchContainer> AUTO_WORKBENCH = register(Lib.GUIID_AutoWorkbench, AutoWorkbenchContainer::new);
	public static final TileContainer<MixerTileEntity, MixerContainer> MIXER = register(Lib.GUIID_Mixer, MixerContainer::new);
	public static final TileContainer<TurretGunTileEntity, TurretContainer.GunTurretContainer> GUN_TURRET = register(Lib.GUIID_Turret_Gun, TurretContainer.GunTurretContainer::new);
	public static final TileContainer<TurretChemTileEntity, TurretContainer.ChemTurretContainer> CHEM_TURRET = register(Lib.GUIID_Turret_Chem, TurretContainer.ChemTurretContainer::new);
	public static final TileContainer<FluidSorterTileEntity, FluidSorterContainer> FLUID_SORTER = register(Lib.GUIID_FluidSorter, FluidSorterContainer::new);
	public static final TileContainer<ClocheTileEntity, ClocheContainer> CLOCHE = register(Lib.GUIID_Cloche, ClocheContainer::new);
	public static final TileContainer<ToolboxTileEntity, ToolboxBlockContainer> TOOLBOX_BLOCK = register(Lib.GUIID_ToolboxBlock, ToolboxBlockContainer::new);

	public static final ItemContainerType<ToolboxContainer> TOOLBOX = register(Lib.GUIID_Toolbox, ToolboxContainer::new);
	public static final ItemContainerType<RevolverContainer> REVOLVER = register(Lib.GUIID_Revolver, RevolverContainer::new);
	public static final ItemContainerType<MaintenanceKitContainer> MAINTENANCE_KIT = register(Lib.GUIID_MaintenanceKit, MaintenanceKitContainer::new);

	public static final EntityContainerType<CrateMinecartEntity, CrateEntityContainer> CRATE_MINECART = register(Lib.GUIID_CartCrate, CrateEntityContainer::new);

	public static <T extends TileEntity, C extends IEBaseContainer<? super T>>
	TileContainer<T, C> register(String name, TileContainerConstructor<T, C> container)
	{
		RegistryObject<ContainerType<C>> typeRef = REGISTER.register(
				name, () -> {
					Mutable<ContainerType<C>> typeBox = new MutableObject<>();
					ContainerType<C> type = new ContainerType<>((IContainerFactory<C>)(windowId, inv, data) -> {
						World world = ImmersiveEngineering.proxy.getClientWorld();
						BlockPos pos = data.readBlockPos();
						TileEntity te = world.getTileEntity(pos);
						return container.construct(typeBox.getValue(), windowId, inv, (T)te);
					});
					typeBox.setValue(type);
					return type;
				}
		);
		return new TileContainer<>(typeRef, container);
	}

	public static <C extends Container>
	ItemContainerType<C> register(String name, ItemContainerConstructor<C> container)
	{
		RegistryObject<ContainerType<C>> typeRef = REGISTER.register(
				name, () -> {
					Mutable<ContainerType<C>> typeBox = new MutableObject<>();
					ContainerType<C> type = new ContainerType<>((IContainerFactory<C>)(windowId, inv, data) -> {
						World world = ImmersiveEngineering.proxy.getClientWorld();
						// Matches IEBaseItem#openGui
						int slotOrdinal = data.readInt();
						EquipmentSlotType slot = EquipmentSlotType.values()[slotOrdinal];
						ItemStack stack = ImmersiveEngineering.proxy.getClientPlayer().getItemStackFromSlot(slot);
						return container.construct(typeBox.getValue(), windowId, inv, world, slot, stack);
					});
					typeBox.setValue(type);
					return type;
				}
		);
		return new ItemContainerType<>(typeRef, container);
	}

	public static <E extends Entity, C extends Container>
	EntityContainerType<E, C> register(String name, EntityContainerConstructor<? super E, C> container)
	{
		RegistryObject<ContainerType<C>> typeRef = REGISTER.register(
				name, () -> {
					Mutable<ContainerType<C>> typeBox = new MutableObject<>();
					ContainerType<C> type = new ContainerType<>((IContainerFactory<C>)(windowId, inv, data) -> {
						int entityId = data.readInt();
						Entity entity = ImmersiveEngineering.proxy.getClientWorld().getEntityByID(entityId);
						return container.construct(typeBox.getValue(), windowId, inv, (E)entity);
					});
					typeBox.setValue(type);
					return type;
				}
		);
		return new EntityContainerType<>(typeRef, container);
	}

	public static class TileContainer<T extends TileEntity, C extends IEBaseContainer<? super T>>
	{
		private final RegistryObject<ContainerType<C>> type;
		private final TileContainerConstructor<T, C> factory;

		private TileContainer(RegistryObject<ContainerType<C>> type, TileContainerConstructor<T, C> factory)
		{
			this.type = type;
			this.factory = factory;
		}

		public C create(int windowId, PlayerInventory playerInv, T tile)
		{
			return factory.construct(getType(), windowId, playerInv, tile);
		}

		public ContainerType<C> getType()
		{
			return type.get();
		}
	}


	public static class ItemContainerType<C extends Container>
	{
		final RegistryObject<ContainerType<C>> type;
		final ItemContainerConstructor<C> factory;

		private ItemContainerType(RegistryObject<ContainerType<C>> type, ItemContainerConstructor<C> factory)
		{
			this.type = type;
			this.factory = factory;
		}

		public C create(int id, PlayerInventory inv, World w, EquipmentSlotType slot, ItemStack stack)
		{
			return factory.construct(getType(), id, inv, w, slot, stack);
		}

		public ContainerType<C> getType()
		{
			return type.get();
		}
	}

	public static class EntityContainerType<E extends Entity, C extends Container>
	{
		final RegistryObject<ContainerType<C>> type;
		final EntityContainerConstructor<? super E, C> factory;

		private EntityContainerType(RegistryObject<ContainerType<C>> type, EntityContainerConstructor<? super E, C> factory)
		{
			this.type = type;
			this.factory = factory;
		}

		public C construct(int id, PlayerInventory inv, E entity)
		{
			return factory.construct(getType(), id, inv, entity);
		}

		public ContainerType<C> getType()
		{
			return type.get();
		}
	}

	public interface TileContainerConstructor<T extends TileEntity, C extends IEBaseContainer<? super T>>
	{
		C construct(ContainerType<C> type, int windowId, PlayerInventory inventoryPlayer, T te);
	}

	public interface ItemContainerConstructor<C extends Container>
	{
		C construct(ContainerType<C> type, int windowId, PlayerInventory inventoryPlayer, World world, EquipmentSlotType slot, ItemStack stack);
	}

	public interface EntityContainerConstructor<E extends Entity, C extends Container>
	{
		C construct(ContainerType<?> type, int windowId, PlayerInventory inventoryPlayer, E entity);
	}
}
