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
import blusunrize.immersiveengineering.common.blocks.wooden.FluidSorterTileEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.ModWorkbenchTileEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.SorterTileEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.WoodenCrateTileEntity;
import blusunrize.immersiveengineering.common.items.MaintenanceKitItem;
import blusunrize.immersiveengineering.common.items.RevolverItem;
import blusunrize.immersiveengineering.common.items.SpeedloaderItem;
import blusunrize.immersiveengineering.common.items.ToolboxItem;
import com.google.common.base.Preconditions;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
	private static final Map<Class<? extends TileEntity>, TileContainer<?, ?>> TILE_CONTAINERS = new HashMap<>();
	private static final Map<Class<? extends Item>, ItemContainer<?>> ITEM_CONTAINERS = new HashMap<>();
	private static final Map<ResourceLocation, ContainerType<?>> ALL_TYPES = new HashMap<>();

	public static void commonInit()
	{
		register(CokeOvenTileEntity.class, Lib.GUIID_CokeOven, CokeOvenContainer::new);
		register(AlloySmelterTileEntity.class, Lib.GUIID_AlloySmelter, AlloySmelterContainer::new);
		register(BlastFurnaceTileEntity.class, Lib.GUIID_BlastFurnace, BlastFurnaceContainer::new);
		useSameContainerTile(BlastFurnaceTileEntity.class, BlastFurnaceAdvancedTileEntity.class);
		register(WoodenCrateTileEntity.class, Lib.GUIID_WoodenCrate, CrateContainer::new);
		register(ModWorkbenchTileEntity.class, Lib.GUIID_Workbench, ModWorkbenchContainer::new);
		register(AssemblerTileEntity.class, Lib.GUIID_Assembler, AssemblerContainer::new);
		register(SorterTileEntity.class, Lib.GUIID_Sorter, SorterContainer::new);
		register(SqueezerTileEntity.class, Lib.GUIID_Squeezer, SqueezerContainer::new);
		register(FermenterTileEntity.class, Lib.GUIID_Fermenter, FermenterContainer::new);
		register(RefineryTileEntity.class, Lib.GUIID_Refinery, RefineryContainer::new);
		register(ArcFurnaceTileEntity.class, Lib.GUIID_ArcFurnace, ArcFurnaceContainer::new);
		register(AutoWorkbenchTileEntity.class, Lib.GUIID_AutoWorkbench, AutoWorkbenchContainer::new);
		register(MixerTileEntity.class, Lib.GUIID_Mixer, MixerContainer::new);
		register(TurretTileEntity.class, Lib.GUIID_Turret, TurretContainer::new);
		register(FluidSorterTileEntity.class, Lib.GUIID_FluidSorter, FluidSorterContainer::new);
		register(BelljarTileEntity.class, Lib.GUIID_Belljar, BelljarContainer::new);
		register(ToolboxTileEntity.class, Lib.GUIID_ToolboxBlock, ToolboxBlockContainer::new);

		register(ToolboxItem.class, Lib.GUIID_Toolbox, ToolboxContainer::new);
		register(RevolverItem.class, Lib.GUIID_Revolver, RevolverContainer::new);
		register(MaintenanceKitItem.class, Lib.GUIID_MaintenanceKit, MaintenanceKitContainer::new);
		useSameContainerItem(RevolverItem.class, SpeedloaderItem.class);
	}

	public static <T extends TileEntity, C extends IEBaseContainer<T>>
	void register(Class<T> tileClass, ResourceLocation name,
				  TileContainerConstructor<T, C> container)
	{
		ContainerType<C> type = new ContainerType<>((IContainerFactory<C>)(windowId, inv, data) -> {
			World world = ImmersiveEngineering.proxy.getClientWorld();
			BlockPos pos = data.readBlockPos();
			TileEntity te = world.getTileEntity(pos);
			return container.construct(windowId, inv, (T)te);
		});
		type.setRegistryName(name);
		TILE_CONTAINERS.put(tileClass, new TileContainer<>(type, container));
		ALL_TYPES.put(name, type);
	}

	public static <T0 extends TileEntity, T extends T0> void useSameContainerTile(Class<T0> existing, Class<T> toAdd)
	{
		Preconditions.checkArgument(TILE_CONTAINERS.containsKey(existing));
		TILE_CONTAINERS.put(toAdd, TILE_CONTAINERS.get(existing));
	}

	public static <C extends Container>
	void register(Class<? extends Item> itemClass, ResourceLocation name,
				  ItemContainerConstructor<C> container)
	{
		ContainerType<C> type = new ContainerType<>((IContainerFactory<C>)(windowId, inv, data) -> {
			World world = ImmersiveEngineering.proxy.getClientWorld();
			int slotOrdinal = data.readInt();
			EquipmentSlotType slot = EquipmentSlotType.values()[slotOrdinal];
			ItemStack stack = ImmersiveEngineering.proxy.getClientPlayer().getItemStackFromSlot(slot);
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

	public static <T extends TileEntity> Container createContainer(PlayerInventory inv, T te, int id)
	{
		return ((TileContainer<T, ?>)TILE_CONTAINERS.get(te.getClass())).factory.construct(id, inv, te);
	}

	public static Container createContainer(PlayerInventory inv, World w, EquipmentSlotType slot, ItemStack stack, int id)
	{
		return ITEM_CONTAINERS.get(stack.getItem().getClass()).factory.construct(id, inv, w, slot, stack);
	}

	public static ContainerType<?> getContainerTypeFor(TileEntity te)
	{
		return TILE_CONTAINERS.get(te.getClass()).type;
	}

	public static ContainerType<?> getContainerTypeFor(ItemStack stack)
	{
		return ITEM_CONTAINERS.get(stack.getItem().getClass()).type;
	}

	public static ContainerType<?> getContainerType(ResourceLocation name)
	{
		return ALL_TYPES.get(name);
	}

	@SubscribeEvent
	public static void registerContainers(RegistryEvent.Register<ContainerType<?>> evt)
	{
		for(TileContainer<?, ?> tc : new HashSet<>(TILE_CONTAINERS.values()))
			evt.getRegistry().register(tc.type);
		for(ItemContainer<?> ic : new HashSet<>(ITEM_CONTAINERS.values()))
			evt.getRegistry().register(ic.type);
	}

	public interface ItemContainerConstructor<C extends Container>
	{
		C construct(int windowId, PlayerInventory inventoryPlayer, World world, EquipmentSlotType slot, ItemStack stack);
	}

	public interface TileContainerConstructor<T extends TileEntity, C extends IEBaseContainer<T>>
	{
		C construct(int windowId, PlayerInventory inventoryPlayer, T te);
	}

	private static class TileContainer<T extends TileEntity, C extends IEBaseContainer<T>>
	{
		final ContainerType<C> type;
		final TileContainerConstructor<T, C> factory;

		private TileContainer(ContainerType<C> type, TileContainerConstructor<T, C> factory)
		{
			this.type = type;
			this.factory = factory;
		}
	}

	private static class ItemContainer<C extends Container>
	{
		final ContainerType<C> type;
		final ItemContainerConstructor<C> factory;

		private ItemContainer(ContainerType<C> type, ItemContainerConstructor<C> factory)
		{
			this.type = type;
			this.factory = factory;
		}
	}
}
