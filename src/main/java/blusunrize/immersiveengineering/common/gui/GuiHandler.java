/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

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
import net.minecraftforge.fml.network.IContainerFactory;

import java.util.HashMap;
import java.util.Map;

public class GuiHandler
{
	private static final Map<Class<? extends TileEntity>, TileContainer<?, ?>> TILE_CONTAINERS = new HashMap<>();
	private static final Map<Class<? extends Item>, ItemContainer<?>> ITEM_CONTAINERS = new HashMap<>();

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

	public static <C extends IEBaseContainer<?>, S extends Screen & IHasContainer<C>>
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
