/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.blocks.stone.AlloySmelterBlockEntity;
import blusunrize.immersiveengineering.common.blocks.stone.BlastFurnaceBlockEntity;
import blusunrize.immersiveengineering.common.blocks.stone.CokeOvenBlockEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.*;
import blusunrize.immersiveengineering.common.gui.*;
import blusunrize.immersiveengineering.common.gui.TurretContainer.ChemTurretContainer;
import blusunrize.immersiveengineering.common.gui.TurretContainer.GunTurretContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

public class IEContainerTypes
{
	public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.CONTAINERS, Lib.MODID);

	public static final BEContainer<CokeOvenBlockEntity, CokeOvenContainer> COKE_OVEN = register(Lib.GUIID_CokeOven, CokeOvenContainer::new);
	public static final BEContainer<AlloySmelterBlockEntity, AlloySmelterContainer> ALLOY_SMELTER = register(Lib.GUIID_AlloySmelter, AlloySmelterContainer::new);
	public static final BEContainer<BlastFurnaceBlockEntity<?>, BlastFurnaceContainer> BLAST_FURNACE = register(Lib.GUIID_BlastFurnace, BlastFurnaceContainer::new);
	public static final BEContainer<CraftingTableBlockEntity, CraftingTableContainer> CRAFTING_TABLE = register(Lib.GUIID_CraftingTable, CraftingTableContainer::new);
	public static final RegistryObject<MenuType<CrateContainer>> WOODEN_CRATE = registerSimple(Lib.GUIID_WoodenCrate, CrateContainer::new);
	public static final BEContainer<ModWorkbenchBlockEntity, ModWorkbenchContainer> MOD_WORKBENCH = register(Lib.GUIID_Workbench, ModWorkbenchContainer::new);
	public static final BEContainer<CircuitTableBlockEntity, CircuitTableContainer> CIRCUIT_TABLE = register(Lib.GUIID_CircuitTable, CircuitTableContainer::new);
	public static final BEContainer<AssemblerBlockEntity, AssemblerContainer> ASSEMBLER = register(Lib.GUIID_Assembler, AssemblerContainer::new);
	public static final BEContainer<SorterBlockEntity, SorterContainer> SORTER = register(Lib.GUIID_Sorter, SorterContainer::new);
	public static final BEContainer<ItemBatcherBlockEntity, ItemBatcherContainer> ITEM_BATCHER = register(Lib.GUIID_ItemBatcher, ItemBatcherContainer::new);
	public static final BEContainer<LogicUnitBlockEntity, LogicUnitContainer> LOGIC_UNIT = register(Lib.GUIID_LogicUnit, LogicUnitContainer::new);
	public static final BEContainer<SqueezerBlockEntity, SqueezerContainer> SQUEEZER = register(Lib.GUIID_Squeezer, SqueezerContainer::new);
	public static final BEContainer<FermenterBlockEntity, FermenterContainer> FERMENTER = register(Lib.GUIID_Fermenter, FermenterContainer::new);
	public static final BEContainer<RefineryBlockEntity, RefineryContainer> REFINERY = register(Lib.GUIID_Refinery, RefineryContainer::new);
	public static final BEContainer<ArcFurnaceBlockEntity, ArcFurnaceContainer> ARC_FURNACE = register(Lib.GUIID_ArcFurnace, ArcFurnaceContainer::new);
	public static final BEContainer<AutoWorkbenchBlockEntity, AutoWorkbenchContainer> AUTO_WORKBENCH = register(Lib.GUIID_AutoWorkbench, AutoWorkbenchContainer::new);
	public static final BEContainer<MixerBlockEntity, MixerContainer> MIXER = register(Lib.GUIID_Mixer, MixerContainer::new);
	public static final BEContainer<TurretGunBlockEntity, GunTurretContainer> GUN_TURRET = register(Lib.GUIID_Turret_Gun, TurretContainer.GunTurretContainer::new);
	public static final BEContainer<TurretChemBlockEntity, ChemTurretContainer> CHEM_TURRET = register(Lib.GUIID_Turret_Chem, TurretContainer.ChemTurretContainer::new);
	public static final BEContainer<FluidSorterBlockEntity, FluidSorterContainer> FLUID_SORTER = register(Lib.GUIID_FluidSorter, FluidSorterContainer::new);
	public static final BEContainer<ClocheBlockEntity, ClocheContainer> CLOCHE = register(Lib.GUIID_Cloche, ClocheContainer::new);
	public static final BEContainer<ToolboxBlockEntity, ToolboxContainer> TOOLBOX_BLOCK = registerBENew(
			Lib.GUIID_ToolboxBlock, ToolboxContainer::makeFromBE, ToolboxContainer::makeClient
	);

	public static final ItemContainerTypeNew<ToolboxContainer> TOOLBOX = registerItemNew(
			Lib.GUIID_Toolbox, ToolboxContainer::makeFromItem, ToolboxContainer::makeClient
	);
	public static final ItemContainerType<RevolverContainer> REVOLVER = register(Lib.GUIID_Revolver, RevolverContainer::new);
	public static final ItemContainerType<MaintenanceKitContainer> MAINTENANCE_KIT = register(Lib.GUIID_MaintenanceKit, MaintenanceKitContainer::new);

	public static final RegistryObject<MenuType<CrateEntityContainer>> CRATE_MINECART = registerSimple(Lib.GUIID_CartCrate, CrateEntityContainer::new);

	public static <T extends BlockEntity, C extends IEBaseContainer>
	BEContainer<T, C> registerBENew(
			String name, BEContainerConstructor<T, C> container, ClientContainerConstructor<C> client
	)
	{
		RegistryObject<MenuType<C>> typeRef = registerType(name, client);
		return new BEContainer<>(typeRef, container);
	}

	public static <C extends IEBaseContainer>
	ItemContainerTypeNew<C> registerItemNew(
			String name, NewItemContainerConstructor<C> container, ClientContainerConstructor<C> client
	)
	{
		RegistryObject<MenuType<C>> typeRef = registerType(name, client);
		return new ItemContainerTypeNew<>(typeRef, container);
	}

	private static <C extends IEBaseContainer>
	RegistryObject<MenuType<C>> registerType(String name, ClientContainerConstructor<C> client)
	{
		return REGISTER.register(
				name, () -> {
					Mutable<MenuType<C>> typeBox = new MutableObject<>();
					MenuType<C> type = new MenuType<>((id, inv) -> client.construct(typeBox.getValue(), id, inv));
					typeBox.setValue(type);
					return type;
				}
		);
	}

	public static <T extends BlockEntity, C extends IEBaseContainerOld<? super T>>
	BEContainer<T, C> register(String name, BEContainerConstructor<T, C> container)
	{
		RegistryObject<MenuType<C>> typeRef = REGISTER.register(
				name, () -> {
					Mutable<MenuType<C>> typeBox = new MutableObject<>();
					MenuType<C> type = new MenuType<>((IContainerFactory<C>)(windowId, inv, data) -> {
						Level world = ImmersiveEngineering.proxy.getClientWorld();
						BlockPos pos = data.readBlockPos();
						BlockEntity te = world.getBlockEntity(pos);
						return container.construct(typeBox.getValue(), windowId, inv, (T)te);
					});
					typeBox.setValue(type);
					return type;
				}
		);
		return new BEContainer<>(typeRef, container);
	}

	public static <C extends AbstractContainerMenu>
	ItemContainerType<C> register(String name, ItemContainerConstructor<C> container)
	{
		RegistryObject<MenuType<C>> typeRef = REGISTER.register(
				name, () -> {
					Mutable<MenuType<C>> typeBox = new MutableObject<>();
					MenuType<C> type = new MenuType<>((IContainerFactory<C>)(windowId, inv, data) -> {
						Level world = ImmersiveEngineering.proxy.getClientWorld();
						// Matches IEBaseItem#openGui
						int slotOrdinal = data.readInt();
						EquipmentSlot slot = EquipmentSlot.values()[slotOrdinal];
						ItemStack stack = ImmersiveEngineering.proxy.getClientPlayer().getItemBySlot(slot);
						return container.construct(typeBox.getValue(), windowId, inv, world, slot, stack);
					});
					typeBox.setValue(type);
					return type;
				}
		);
		return new ItemContainerType<>(typeRef, container);
	}

	public static <M extends AbstractContainerMenu>
	RegistryObject<MenuType<M>> registerSimple(String name, SimpleContainerConstructor<M> factory)
	{
		return REGISTER.register(
				name, () -> {
					Mutable<MenuType<M>> typeBox = new MutableObject<>();
					MenuType<M> type = new MenuType<>((id, inv) -> factory.construct(typeBox.getValue(), id, inv));
					typeBox.setValue(type);
					return type;
				}
		);
	}

	public static class BEContainer<T extends BlockEntity, C extends IEBaseContainer>
	{
		private final RegistryObject<MenuType<C>> type;
		private final BEContainerConstructor<T, C> factory;

		private BEContainer(RegistryObject<MenuType<C>> type, BEContainerConstructor<T, C> factory)
		{
			this.type = type;
			this.factory = factory;
		}

		public C create(int windowId, Inventory playerInv, T tile)
		{
			return factory.construct(getType(), windowId, playerInv, tile);
		}

		public MenuType<C> getType()
		{
			return type.get();
		}
	}


	public record ItemContainerType<C extends AbstractContainerMenu>(
			RegistryObject<MenuType<C>> type, ItemContainerConstructor<C> factory
	)
	{
		public C create(int id, Inventory inv, Level w, EquipmentSlot slot, ItemStack stack)
		{
			return factory.construct(getType(), id, inv, w, slot, stack);
		}

		public MenuType<C> getType()
		{
			return type.get();
		}
	}

	public record ItemContainerTypeNew<C extends AbstractContainerMenu>(
			RegistryObject<MenuType<C>> type, NewItemContainerConstructor<C> factory
	)
	{
		public C create(int id, Inventory inv, EquipmentSlot slot, ItemStack stack)
		{
			return factory.construct(getType(), id, inv, slot, stack);
		}

		public MenuType<C> getType()
		{
			return type.get();
		}
	}

	public interface BEContainerConstructor<T extends BlockEntity, C extends IEBaseContainer>
	{
		C construct(MenuType<C> type, int windowId, Inventory inventoryPlayer, T te);
	}

	public interface ClientContainerConstructor<C extends IEBaseContainer>
	{
		C construct(MenuType<C> type, int windowId, Inventory inventoryPlayer);
	}

	public interface ItemContainerConstructor<C extends AbstractContainerMenu>
	{
		C construct(MenuType<C> type, int windowId, Inventory inventoryPlayer, Level world, EquipmentSlot slot, ItemStack stack);
	}

	public interface NewItemContainerConstructor<C extends AbstractContainerMenu>
	{
		C construct(MenuType<C> type, int windowId, Inventory inventoryPlayer, EquipmentSlot slot, ItemStack stack);
	}

	public interface EntityContainerConstructor<E extends Entity, C extends AbstractContainerMenu>
	{
		C construct(MenuType<?> type, int windowId, Inventory inventoryPlayer, E entity);
	}

	public interface SimpleContainerConstructor<C extends AbstractContainerMenu>
	{
		C construct(MenuType<?> type, int windowId, Inventory inventoryPlayer);
	}
}
