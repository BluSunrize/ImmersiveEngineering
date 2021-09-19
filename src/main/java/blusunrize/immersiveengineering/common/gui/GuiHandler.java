/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.blocks.stone.AlloySmelterTileEntity;
import blusunrize.immersiveengineering.common.blocks.stone.BlastFurnaceAdvancedTileEntity;
import blusunrize.immersiveengineering.common.blocks.stone.BlastFurnaceTileEntity;
import blusunrize.immersiveengineering.common.blocks.stone.CokeOvenTileEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.*;
import blusunrize.immersiveengineering.common.entities.CrateMinecartEntity;
import blusunrize.immersiveengineering.common.entities.ReinforcedCrateMinecartEntity;
import blusunrize.immersiveengineering.common.items.MaintenanceKitItem;
import blusunrize.immersiveengineering.common.items.RevolverItem;
import blusunrize.immersiveengineering.common.items.SpeedloaderItem;
import blusunrize.immersiveengineering.common.items.ToolboxItem;
import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.network.IContainerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

@EventBusSubscriber(modid = MODID, bus = Bus.MOD)
public class GuiHandler
{
	private static final Map<Class<? extends BlockEntity>, TileContainer<?, ?>> TILE_CONTAINERS = new HashMap<>();
	private static final Map<Class<? extends Item>, ItemContainer<?>> ITEM_CONTAINERS = new HashMap<>();
	private static final Map<Class<? extends Entity>, EntityContainer<?, ?>> ENTITY_CONTAINERS = new HashMap<>();
	private static final Map<ResourceLocation, MenuType<?>> ALL_TYPES = new HashMap<>();

	public static void commonInit()
	{
		register(CokeOvenTileEntity.class, Lib.GUIID_CokeOven, CokeOvenContainer::new);
		register(AlloySmelterTileEntity.class, Lib.GUIID_AlloySmelter, AlloySmelterContainer::new);
		register(BlastFurnaceTileEntity.class, Lib.GUIID_BlastFurnace, BlastFurnaceContainer::new);
		useSameContainerTile(BlastFurnaceTileEntity.class, BlastFurnaceAdvancedTileEntity.class);
		register(CraftingTableTileEntity.class, Lib.GUIID_CraftingTable, CraftingTableContainer::new);
		register(WoodenCrateTileEntity.class, Lib.GUIID_WoodenCrate, CrateContainer::new);
		register(ModWorkbenchTileEntity.class, Lib.GUIID_Workbench, ModWorkbenchContainer::new);
		register(CircuitTableTileEntity.class, Lib.GUIID_CircuitTable, CircuitTableContainer::new);
		register(AssemblerTileEntity.class, Lib.GUIID_Assembler, AssemblerContainer::new);
		register(SorterTileEntity.class, Lib.GUIID_Sorter, SorterContainer::new);
		register(ItemBatcherTileEntity.class, Lib.GUIID_ItemBatcher, ItemBatcherContainer::new);
		register(LogicUnitTileEntity.class, Lib.GUIID_LogicUnit, LogicUnitContainer::new);
		register(SqueezerTileEntity.class, Lib.GUIID_Squeezer, SqueezerContainer::new);
		register(FermenterTileEntity.class, Lib.GUIID_Fermenter, FermenterContainer::new);
		register(RefineryTileEntity.class, Lib.GUIID_Refinery, RefineryContainer::new);
		register(ArcFurnaceTileEntity.class, Lib.GUIID_ArcFurnace, ArcFurnaceContainer::new);
		register(AutoWorkbenchTileEntity.class, Lib.GUIID_AutoWorkbench, AutoWorkbenchContainer::new);
		register(MixerTileEntity.class, Lib.GUIID_Mixer, MixerContainer::new);
		register(TurretGunTileEntity.class, Lib.GUIID_Turret_Gun, TurretContainer::new);
		register(TurretChemTileEntity.class, Lib.GUIID_Turret_Chem, TurretContainer::new);
		register(FluidSorterTileEntity.class, Lib.GUIID_FluidSorter, FluidSorterContainer::new);
		register(ClocheTileEntity.class, Lib.GUIID_Cloche, ClocheContainer::new);
		register(ToolboxTileEntity.class, Lib.GUIID_ToolboxBlock, ToolboxBlockContainer::new);

		register(ToolboxItem.class, Lib.GUIID_Toolbox, ToolboxContainer::new);
		register(RevolverItem.class, Lib.GUIID_Revolver, RevolverContainer::new);
		register(MaintenanceKitItem.class, Lib.GUIID_MaintenanceKit, MaintenanceKitContainer::new);
		useSameContainerItem(RevolverItem.class, SpeedloaderItem.class);


		register(CrateMinecartEntity.class, Lib.GUIID_CartCrate,
				(EntityContainerConstructor<CrateMinecartEntity, AbstractContainerMenu>)(windowId, inventoryPlayer, entity)
						-> new CrateEntityContainer(windowId, inventoryPlayer, entity.getContainedTileEntity(), entity));
		register(ReinforcedCrateMinecartEntity.class, Lib.GUIID_CartReinforcedCrate,
				(EntityContainerConstructor<CrateMinecartEntity, AbstractContainerMenu>)(windowId, inventoryPlayer, entity)
						-> new CrateEntityContainer(windowId, inventoryPlayer, entity.getContainedTileEntity(), entity));
	}

	public static <T extends BlockEntity, C extends IEBaseContainer<? super T>>
	void register(Class<T> tileClass, ResourceLocation name, TileContainerConstructor<T, C> container)
	{
		MenuType<C> type = new MenuType<>((IContainerFactory<C>)(windowId, inv, data) -> {
			Level world = ImmersiveEngineering.proxy.getClientWorld();
			BlockPos pos = data.readBlockPos();
			BlockEntity te = world.getBlockEntity(pos);
			return container.construct(windowId, inv, (T)te);
		});
		type.setRegistryName(name);
		TILE_CONTAINERS.put(tileClass, new TileContainer<>(type, container));
		ALL_TYPES.put(name, type);
	}

	public static void useSameContainerTile(Class<? extends BlockEntity> existing, Class<? extends BlockEntity> toAdd)
	{
		Preconditions.checkArgument(TILE_CONTAINERS.containsKey(existing));
		TILE_CONTAINERS.put(toAdd, TILE_CONTAINERS.get(existing));
	}

	public static <C extends AbstractContainerMenu>
	void register(Class<? extends Item> itemClass, ResourceLocation name,
				  ItemContainerConstructor<C> container)
	{
		MenuType<C> type = new MenuType<>((IContainerFactory<C>)(windowId, inv, data) -> {
			Level world = ImmersiveEngineering.proxy.getClientWorld();
			int slotOrdinal = data.readInt();
			EquipmentSlot slot = EquipmentSlot.values()[slotOrdinal];
			ItemStack stack = ImmersiveEngineering.proxy.getClientPlayer().getItemBySlot(slot);
			return container.construct(windowId, inv, world, slot, stack);
		});
		type.setRegistryName(name);
		ITEM_CONTAINERS.put(itemClass, new ItemContainer<>(type, container));
		ALL_TYPES.put(name, type);
	}

	public static <T0 extends Item, T extends Item> void useSameContainerItem(Class<T0> existing, Class<T> toAdd)
	{
		Preconditions.checkArgument(ITEM_CONTAINERS.containsKey(existing));
		ITEM_CONTAINERS.put(toAdd, ITEM_CONTAINERS.get(existing));
	}

	public static <E extends Entity, C extends AbstractContainerMenu>
	void register(Class<? extends Entity> entityClass, ResourceLocation name,
				  EntityContainerConstructor<E, C> container)
	{
		MenuType<C> type = new MenuType<>((IContainerFactory<C>)(windowId, inv, data) -> {
			Level world = ImmersiveEngineering.proxy.getClientWorld();
			int entityId = data.readInt();
			Entity entity = ImmersiveEngineering.proxy.getClientWorld().getEntity(entityId);
			return container.construct(windowId, inv, (E)entity);
		});
		type.setRegistryName(name);
		ENTITY_CONTAINERS.put(entityClass, new EntityContainer<>(type, container));
		ALL_TYPES.put(name, type);
	}

	public static <T extends BlockEntity> AbstractContainerMenu createContainer(Inventory inv, T te, int id)
	{
		return ((TileContainer<T, ?>)TILE_CONTAINERS.get(te.getClass())).factory.construct(id, inv, te);
	}

	public static AbstractContainerMenu createContainer(Inventory inv, Level w, EquipmentSlot slot, ItemStack stack, int id)
	{
		return ITEM_CONTAINERS.get(stack.getItem().getClass()).factory.construct(id, inv, w, slot, stack);
	}

	public static <E extends Entity> AbstractContainerMenu createContainer(Inventory inv, E entity, int id)
	{
		return ((EntityContainer<E, ?>)ENTITY_CONTAINERS.get(entity.getClass())).factory.construct(id, inv, entity);
	}

	public static MenuType<?> getContainerTypeFor(BlockEntity te)
	{
		return TILE_CONTAINERS.get(te.getClass()).type;
	}

	public static MenuType<?> getContainerTypeFor(ItemStack stack)
	{
		return ITEM_CONTAINERS.get(stack.getItem().getClass()).type;
	}

	public static MenuType<?> getContainerTypeFor(Entity entity)
	{
		return ENTITY_CONTAINERS.get(entity.getClass()).type;
	}

	public static MenuType<?> getContainerType(ResourceLocation name)
	{
		return ALL_TYPES.get(name);
	}

	@SubscribeEvent
	public static void registerContainers(RegistryEvent.Register<MenuType<?>> evt)
	{
		for(TileContainer<?, ?> tc : new HashSet<>(TILE_CONTAINERS.values()))
			evt.getRegistry().register(tc.type);
		for(ItemContainer<?> ic : new HashSet<>(ITEM_CONTAINERS.values()))
			evt.getRegistry().register(ic.type);
		for(EntityContainer<?, ?> ec : new HashSet<>(ENTITY_CONTAINERS.values()))
			evt.getRegistry().register(ec.type);
	}

	public interface ItemContainerConstructor<C extends AbstractContainerMenu>
	{
		C construct(int windowId, Inventory inventoryPlayer, Level world, EquipmentSlot slot, ItemStack stack);
	}

	public interface TileContainerConstructor<T extends BlockEntity, C extends IEBaseContainer<? super T>>
	{
		C construct(int windowId, Inventory inventoryPlayer, T te);
	}

	public interface EntityContainerConstructor<E extends Entity, C extends AbstractContainerMenu>
	{
		C construct(int windowId, Inventory inventoryPlayer, E entity);
	}

	private static class TileContainer<T extends BlockEntity, C extends IEBaseContainer<? super T>>
	{
		final MenuType<C> type;
		final TileContainerConstructor<T, C> factory;

		private TileContainer(MenuType<C> type, TileContainerConstructor<T, C> factory)
		{
			this.type = type;
			this.factory = factory;
		}
	}

	private static class ItemContainer<C extends AbstractContainerMenu>
	{
		final MenuType<C> type;
		final ItemContainerConstructor<C> factory;

		private ItemContainer(MenuType<C> type, ItemContainerConstructor<C> factory)
		{
			this.type = type;
			this.factory = factory;
		}
	}

	private static class EntityContainer<E extends Entity, C extends AbstractContainerMenu>
	{
		final MenuType<C> type;
		final EntityContainerConstructor<E, C> factory;

		private EntityContainer(MenuType<C> type, EntityContainerConstructor<E, C> factory)
		{
			this.type = type;
			this.factory = factory;
		}
	}
}
