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
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.common.blocks.metal.ClocheBlockEntity;
import blusunrize.immersiveengineering.common.blocks.metal.ToolboxBlockEntity;
import blusunrize.immersiveengineering.common.blocks.metal.TurretChemBlockEntity;
import blusunrize.immersiveengineering.common.blocks.metal.TurretGunBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.*;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.arcfurnace.ArcFurnaceLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.mixer.MixerLogic;
import blusunrize.immersiveengineering.common.blocks.wooden.*;
import blusunrize.immersiveengineering.common.gui.*;
import blusunrize.immersiveengineering.common.gui.IEContainerMenu.MultiblockMenuContext;
import blusunrize.immersiveengineering.common.gui.TurretMenu.ChemTurretMenu;
import blusunrize.immersiveengineering.common.gui.TurretMenu.GunTurretMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class IEMenuTypes
{
	public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.MENU, Lib.MODID);

	public static final MultiblockContainer<CokeOvenLogic.State, CokeOvenMenu> COKE_OVEN = registerMultiblock(
			Lib.GUIID_CokeOven, CokeOvenMenu::makeServer, CokeOvenMenu::makeClient
	);
	public static final MultiblockContainer<AlloySmelterLogic.State, AlloySmelterMenu> ALLOY_SMELTER = registerMultiblock(
			Lib.GUIID_AlloySmelter, AlloySmelterMenu::makeServer, AlloySmelterMenu::makeClient
	);
	public static final MultiblockContainer<BlastFurnaceLogic.State, BlastFurnaceMenu> BLAST_FURNACE = registerMultiblock(
			Lib.GUIID_BlastFurnace, BlastFurnaceMenu::makeServer, BlastFurnaceMenu::makeClient
	);
	public static final MultiblockContainer<AdvBlastFurnaceLogic.State, BlastFurnaceMenu> BLAST_FURNACE_ADV = registerMultiblock(
			Lib.GUIID_BlastFurnaceAdv, BlastFurnaceMenu::makeServerAdv, BlastFurnaceMenu::makeClient
	);
	public static final ArgContainer<CraftingTableBlockEntity, CraftingTableMenu> CRAFTING_TABLE = registerArg(
			Lib.GUIID_CraftingTable, CraftingTableMenu::makeServer, CraftingTableMenu::makeClient
	);
	public static final Supplier<MenuType<CrateMenu>> WOODEN_CRATE = registerSimple(Lib.GUIID_WoodenCrate, CrateMenu::new);
	public static final ArgContainer<ModWorkbenchBlockEntity, ModWorkbenchContainer> MOD_WORKBENCH = register(Lib.GUIID_Workbench, ModWorkbenchContainer::new);
	public static final ArgContainer<CircuitTableBlockEntity, CircuitTableMenu> CIRCUIT_TABLE = registerArg(
			Lib.GUIID_CircuitTable, CircuitTableMenu::makeServer, CircuitTableMenu::makeClient
	);
	public static final MultiblockContainer<AssemblerLogic.State, AssemblerMenu> ASSEMBLER = registerMultiblock(
			Lib.GUIID_Assembler, AssemblerMenu::makeServer, AssemblerMenu::makeClient
	);
	public static final ArgContainer<SorterBlockEntity, SorterMenu> SORTER = registerArg(
			Lib.GUIID_Sorter, SorterMenu::makeServer, SorterMenu::makeClient
	);
	public static final ArgContainer<ItemBatcherBlockEntity, ItemBatcherMenu> ITEM_BATCHER = registerArg(
			Lib.GUIID_ItemBatcher, ItemBatcherMenu::makeServer, ItemBatcherMenu::makeClient
	);
	public static final ArgContainer<LogicUnitBlockEntity, LogicUnitMenu> LOGIC_UNIT = registerArg(
			Lib.GUIID_LogicUnit, LogicUnitMenu::makeServer, LogicUnitMenu::makeClient
	);
	public static final MultiblockContainer<SqueezerLogic.State, SqueezerMenu> SQUEEZER = registerMultiblock(
			Lib.GUIID_Squeezer, SqueezerMenu::makeServer, SqueezerMenu::makeClient
	);
	public static final MultiblockContainer<FermenterLogic.State, FermenterMenu> FERMENTER = registerMultiblock(
			Lib.GUIID_Fermenter, FermenterMenu::makeServer, FermenterMenu::makeClient
	);
	public static final MultiblockContainer<RefineryLogic.State, RefineryMenu> REFINERY = registerMultiblock(
			Lib.GUIID_Refinery, RefineryMenu::makeServer, RefineryMenu::makeClient
	);
	public static final MultiblockContainer<ArcFurnaceLogic.State, ArcFurnaceMenu> ARC_FURNACE = registerMultiblock(
			Lib.GUIID_ArcFurnace, ArcFurnaceMenu::makeServer, ArcFurnaceMenu::makeClient
	);
	public static final MultiblockContainer<AutoWorkbenchLogic.State, AutoWorkbenchMenu> AUTO_WORKBENCH = registerMultiblock(
			Lib.GUIID_AutoWorkbench, AutoWorkbenchMenu::makeServer, AutoWorkbenchMenu::makeClient
	);
	public static final MultiblockContainer<MixerLogic.State, MixerMenu> MIXER = registerMultiblock(
			Lib.GUIID_Mixer, MixerMenu::makeServer, MixerMenu::makeClient
	);
	public static final MultiblockContainer<RadioTowerLogic.State, RadioTowerMenu> RADIO_TOWER = registerMultiblock(
			Lib.GUIID_RadioTower, RadioTowerMenu::makeServer, RadioTowerMenu::makeClient
	);
	public static final MultiblockContainer<ChunkLoaderLogic.State, ChunkLoaderMenu> CHUNK_LOADER = registerMultiblock(
			Lib.GUIID_ChunkLoader, ChunkLoaderMenu::makeServer, ChunkLoaderMenu::makeClient
	);
	public static final ArgContainer<TurretGunBlockEntity, GunTurretMenu> GUN_TURRET = registerArg(
			Lib.GUIID_Turret_Gun, GunTurretMenu::makeServer, GunTurretMenu::makeClient
	);
	public static final ArgContainer<TurretChemBlockEntity, ChemTurretMenu> CHEM_TURRET = registerArg(
			Lib.GUIID_Turret_Chem, ChemTurretMenu::makeServer, ChemTurretMenu::makeClient
	);
	public static final ArgContainer<FluidSorterBlockEntity, FluidSorterMenu> FLUID_SORTER = registerArg(
			Lib.GUIID_FluidSorter, FluidSorterMenu::makeServer, FluidSorterMenu::makeClient
	);
	public static final ArgContainer<ClocheBlockEntity, ClocheMenu> CLOCHE = registerArg(
			Lib.GUIID_Cloche, ClocheMenu::makeServer, ClocheMenu::makeClient
	);
	public static final ArgContainer<ToolboxBlockEntity, ToolboxMenu> TOOLBOX_BLOCK = registerArg(
			Lib.GUIID_ToolboxBlock, ToolboxMenu::makeFromBE, ToolboxMenu::makeClient
	);

	public static final ItemContainerTypeNew<ToolboxMenu> TOOLBOX = registerItem(
			Lib.GUIID_Toolbox, ToolboxMenu::makeFromItem, ToolboxMenu::makeClientItem
	);
	public static final ItemContainerType<RevolverContainer> REVOLVER = register(Lib.GUIID_Revolver, RevolverContainer::new);
	public static final ItemContainerType<MaintenanceKitContainer> MAINTENANCE_KIT = register(Lib.GUIID_MaintenanceKit, MaintenanceKitContainer::new);

	public static final Supplier<MenuType<CrateEntityContainer>> CRATE_MINECART = registerSimple(Lib.GUIID_CartCrate, CrateEntityContainer::new);

	public static <T, C extends IEContainerMenu>
	ArgContainer<T, C> registerArg(
			String name, ArgContainerConstructor<T, C> container, ClientContainerConstructor<C> client
	)
	{
		DeferredHolder<MenuType<?>, MenuType<C>> typeRef = registerType(name, client);
		return new ArgContainer<>(typeRef, container);
	}

	public static <S extends IMultiblockState, C extends IEContainerMenu>
	MultiblockContainer<S, C> registerMultiblock(
			String name,
			ArgContainerConstructor<MultiblockMenuContext<S>, C> container,
			ClientContainerConstructor<C> client
	)
	{
		DeferredHolder<MenuType<?>, MenuType<C>> typeRef = registerType(name, client);
		return new MultiblockContainer<>(typeRef, container);
	}

	public static <C extends IEContainerMenu>
	ItemContainerTypeNew<C> registerItem(
			String name, NewItemContainerConstructor<C> container, ClientContainerConstructor<C> client
	)
	{
		DeferredHolder<MenuType<?>, MenuType<C>> typeRef = registerType(name, client);
		return new ItemContainerTypeNew<>(typeRef, container);
	}

	private static <C extends IEContainerMenu>
	DeferredHolder<MenuType<?>, MenuType<C>> registerType(String name, ClientContainerConstructor<C> client)
	{
		return REGISTER.register(
				name, () -> {
					Mutable<MenuType<C>> typeBox = new MutableObject<>();
					MenuType<C> type = new MenuType<>((id, inv) -> client.construct(typeBox.getValue(), id, inv), FeatureFlagSet.of());
					typeBox.setValue(type);
					return type;
				}
		);
	}

	public static <T extends BlockEntity, C extends IEBaseContainerOld<? super T>>
	ArgContainer<T, C> register(String name, ArgContainerConstructor<T, C> container)
	{
		DeferredHolder<MenuType<?>, MenuType<C>> typeRef = REGISTER.register(
				name, () -> {
					Mutable<MenuType<C>> typeBox = new MutableObject<>();
					MenuType<C> type = new MenuType<>((IContainerFactory<C>)(windowId, inv, data) -> {
						Level world = ImmersiveEngineering.proxy.getClientWorld();
						BlockPos pos = data.readBlockPos();
						BlockEntity te = world.getBlockEntity(pos);
						return container.construct(typeBox.getValue(), windowId, inv, (T)te);
					}, FeatureFlagSet.of());
					typeBox.setValue(type);
					return type;
				}
		);
		return new ArgContainer<>(typeRef, container);
	}

	public static <C extends AbstractContainerMenu>
	ItemContainerType<C> register(String name, ItemContainerConstructor<C> container)
	{
		DeferredHolder<MenuType<?>, MenuType<C>> typeRef = REGISTER.register(
				name, () -> {
					Mutable<MenuType<C>> typeBox = new MutableObject<>();
					MenuType<C> type = new MenuType<>((IContainerFactory<C>)(windowId, inv, data) -> {
						Level world = ImmersiveEngineering.proxy.getClientWorld();
						// Matches IEBaseItem#openGui
						int slotOrdinal = data.readInt();
						EquipmentSlot slot = EquipmentSlot.values()[slotOrdinal];
						ItemStack stack = ImmersiveEngineering.proxy.getClientPlayer().getItemBySlot(slot);
						return container.construct(typeBox.getValue(), windowId, inv, world, slot, stack);
					}, FeatureFlagSet.of());
					typeBox.setValue(type);
					return type;
				}
		);
		return new ItemContainerType<>(typeRef, container);
	}

	public static <M extends AbstractContainerMenu>
	DeferredHolder<MenuType<?>, MenuType<M>> registerSimple(String name, SimpleContainerConstructor<M> factory)
	{
		return REGISTER.register(
				name, () -> {
					Mutable<MenuType<M>> typeBox = new MutableObject<>();
					MenuType<M> type = new MenuType<>((id, inv) -> factory.construct(typeBox.getValue(), id, inv), FeatureFlagSet.of());
					typeBox.setValue(type);
					return type;
				}
		);
	}

	public static class ArgContainer<T, C extends IEContainerMenu>
	{
		private final DeferredHolder<MenuType<?>, MenuType<C>> type;
		private final ArgContainerConstructor<T, C> factory;

		private ArgContainer(DeferredHolder<MenuType<?>, MenuType<C>> type, ArgContainerConstructor<T, C> factory)
		{
			this.type = type;
			this.factory = factory;
		}

		public C create(int windowId, Inventory playerInv, T tile)
		{
			return factory.construct(getType(), windowId, playerInv, tile);
		}

		public MenuProvider provide(T arg)
		{
			return new MenuProvider()
			{
				@Nonnull
				@Override
				public Component getDisplayName()
				{
					return Component.empty();
				}

				@Nullable
				@Override
				public AbstractContainerMenu createMenu(
						int containerId, @Nonnull Inventory inventory, @Nonnull Player player
				)
				{
					return create(containerId, inventory, arg);
				}
			};
		}

		public MenuType<C> getType()
		{
			return type.get();
		}
	}

	public static class MultiblockContainer<S extends IMultiblockState, C extends IEContainerMenu> extends
			ArgContainer<MultiblockMenuContext<S>, C>
	{
		private MultiblockContainer(
				DeferredHolder<MenuType<?>, MenuType<C>> type,
				ArgContainerConstructor<MultiblockMenuContext<S>, C> factory
		)
		{
			super(type, factory);
		}

		public MenuProvider provide(IMultiblockContext<S> ctx, BlockPos relativeClicked)
		{
			return provide(new MultiblockMenuContext<>(ctx, ctx.getLevel().toAbsolute(relativeClicked)));
		}
	}

	public record ItemContainerType<C extends AbstractContainerMenu>(
			DeferredHolder<MenuType<?>, MenuType<C>> type, ItemContainerConstructor<C> factory
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
			DeferredHolder<MenuType<?>, MenuType<C>> type, NewItemContainerConstructor<C> factory
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

	public interface ArgContainerConstructor<T, C extends IEContainerMenu>
	{
		C construct(MenuType<C> type, int windowId, Inventory inventoryPlayer, T te);
	}

	public interface ClientContainerConstructor<C extends IEContainerMenu>
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

	public interface SimpleContainerConstructor<C extends AbstractContainerMenu>
	{
		C construct(MenuType<?> type, int windowId, Inventory inventoryPlayer);
	}
}
