/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.gui.*;
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
import blusunrize.immersiveengineering.common.items.ToolboxItem;
import com.google.common.base.Preconditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.ScreenManager.IScreenFactory;
import net.minecraft.client.gui.screen.Screen;
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
import java.util.Map;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

@EventBusSubscriber(modid = MODID, bus = Bus.MOD)
public class GuiHandler
{
	private static final Map<Class<? extends TileEntity>, TileContainer<?, ?>> TILE_CONTAINERS = new HashMap<>();
	private static final Map<Class<? extends Item>, ItemContainer<?>> ITEM_CONTAINERS = new HashMap<>();

	public static void init()
	{
		register(CokeOvenTileEntity.class, Lib.GUIID_CokeOven, CokeOvenScreen::new, CokeOvenContainer::new);
		register(AlloySmelterTileEntity.class, Lib.GUIID_AlloySmelter, AlloySmelterScreen::new, AlloySmelterContainer::new);
		register(BlastFurnaceTileEntity.class, Lib.GUIID_BlastFurnace, BlastFurnaceScreen::new, BlastFurnaceContainer::new);
		useSameContainer(BlastFurnaceTileEntity.class, BlastFurnaceAdvancedTileEntity.class);
		register(WoodenCrateTileEntity.class, Lib.GUIID_WoodenCrate, CrateScreen::new, CrateContainer::new);
		register(ModWorkbenchTileEntity.class, Lib.GUIID_Workbench, ModWorkbenchScreen::new, ModWorkbenchContainer::new);
		register(AssemblerTileEntity.class, Lib.GUIID_Assembler, AssemblerScreen::new, AssemblerContainer::new);
		register(SorterTileEntity.class, Lib.GUIID_Sorter, SorterScreen::new, SorterContainer::new);
		register(SqueezerTileEntity.class, Lib.GUIID_Squeezer, SqueezerScreen::new, SqueezerContainer::new);
		register(FermenterTileEntity.class, Lib.GUIID_Fermenter, FermenterScreen::new, FermenterContainer::new);
		register(RefineryTileEntity.class, Lib.GUIID_Refinery, RefineryScreen::new, RefineryContainer::new);
		register(ArcFurnaceTileEntity.class, Lib.GUIID_ArcFurnace, ArcFurnaceScreen::new, ArcFurnaceContainer::new);
		register(AutoWorkbenchTileEntity.class, Lib.GUIID_AutoWorkbench, AutoWorkbenchScreen::new, AutoWorkbenchContainer::new);
		register(MixerTileEntity.class, Lib.GUIID_Mixer, MixerScreen::new, MixerContainer::new);
		register(TurretTileEntity.class, Lib.GUIID_Turret, TurretScreen::new, TurretContainer::new);
		register(FluidSorterTileEntity.class, Lib.GUIID_FluidSorter, FluidSorterScreen::new, FluidSorterContainer::new);
		register(BelljarTileEntity.class, Lib.GUIID_Belljar, BelljarScreen::new, BelljarContainer::new);
		register(ToolboxTileEntity.class, Lib.GUIID_ToolboxBlock, ToolboxBlockScreen::new, ToolboxBlockContainer::new);

		register(ToolboxItem.class, Lib.GUIID_Toolbox, ToolboxScreen::new, ToolboxContainer::new);
		register(RevolverItem.class, Lib.GUIID_Revolver, RevolverScreen::new, RevolverContainer::new);
		//TODO Lib.GUIID_Manual
		register(MaintenanceKitItem.class, Lib.GUIID_MaintenanceKit, MaintenanceKitScreen::new, MaintenanceKitContainer::new);
	}

	//TODO dedicated server?
	public static <T extends TileEntity, C extends IEBaseContainer<T>, S extends Screen & IHasContainer<C>>
	void register(Class<T> tileClass, ResourceLocation name,
				  IScreenFactory<C, S> gui,
				  TileContainerConstructor<T, C> container)
	{
		ContainerType<C> type = new ContainerType<>((IContainerFactory<C>)(windowId, inv, data) -> {
			World world = Minecraft.getInstance().world;
			BlockPos pos = data.readBlockPos();
			TileEntity te = world.getTileEntity(pos);
			return container.construct(windowId, inv, (T)te);
		});
		type.setRegistryName(name);
		TILE_CONTAINERS.put(tileClass, new TileContainer<>(type, container));
		ScreenManager.registerFactory(type, gui);
	}

	public static <T0 extends TileEntity, T extends T0> void useSameContainer(Class<T0> existing, Class<T> toAdd)
	{
		Preconditions.checkArgument(TILE_CONTAINERS.containsKey(existing));
		TILE_CONTAINERS.put(toAdd, TILE_CONTAINERS.get(existing));
	}

	public static <C extends Container, S extends Screen & IHasContainer<C>>
	void register(Class<? extends Item> itemClass, ResourceLocation name, IScreenFactory<C, S> gui,
				  ItemContainerConstructor<C> container)
	{
		ContainerType<C> type = new ContainerType<>((IContainerFactory<C>)(windowId, inv, data) -> {
			World world = Minecraft.getInstance().world;
			int slotOrdinal = data.readInt();
			EquipmentSlotType slot = EquipmentSlotType.values()[slotOrdinal];
			ItemStack stack = Minecraft.getInstance().player.getItemStackFromSlot(slot);
			return container.construct(windowId, inv, world, slot, stack);
		});
		type.setRegistryName(name);
		ITEM_CONTAINERS.put(itemClass, new ItemContainer<>(type, container));
		ScreenManager.registerFactory(type, gui);
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

	@SubscribeEvent
	public static void registerContainers(RegistryEvent.Register<ContainerType<?>> evt)
	{
		for(TileContainer<?, ?> tc : TILE_CONTAINERS.values())
			evt.getRegistry().register(tc.type);
		for(ItemContainer<?> ic : ITEM_CONTAINERS.values())
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

		private ItemContainer(ContainerType<C> type, ItemContainerConstructor factory)
		{
			this.type = type;
			this.factory = factory;
		}
	}
}
