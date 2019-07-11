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
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.IContainerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class GuiHandler
{
	private static final Map<ResourceLocation, BiFunction<PlayerInventory, TileEntity, Container>> TILE_CONTAINERS = new HashMap<>();
	private static final Map<ResourceLocation, ItemContainerConstructor<? extends Container>> ITEM_CONTAINERS = new HashMap<>();

	//TODO dedicated server?
	public static <T, C extends Container, S extends Screen & IHasContainer<C>>
	void register(ResourceLocation name,
				  IScreenFactory<C, S> gui,
				  BiFunction<PlayerInventory, T, C> container)
	{
		TILE_CONTAINERS.put(name, (inv, te) -> container.apply(inv, (T)te));
		ContainerType<C> type = new ContainerType<>((IContainerFactory<C>)(windowId, inv, data) -> {
			World world = Minecraft.getInstance().world;
			BlockPos pos = data.readBlockPos();
			TileEntity te = world.getTileEntity(pos);
			return container.apply(inv, (T)te);
		});
		ScreenManager.registerFactory(type, gui);
	}

	public static <T, C extends Container, S extends Screen & IHasContainer<C>>
	void register(ResourceLocation name, IScreenFactory<C, S> gui,
				  ItemContainerConstructor<C> container)
	{
		ITEM_CONTAINERS.put(name, container);
		ContainerType<C> type = new ContainerType<>((IContainerFactory<C>)(windowId, inv, data) -> {
			World world = Minecraft.getInstance().world;
			int slotOrdinal = data.readInt();
			EquipmentSlotType slot = EquipmentSlotType.values()[slotOrdinal];
			ItemStack stack = Minecraft.getInstance().player.getItemStackFromSlot(slot);
			return container.construct(inv, world, slot, stack);

		});
		ScreenManager.registerFactory(type, gui);
	}

	public static Container createContainer(ResourceLocation rl, PlayerInventory inv, TileEntity te)
	{
		return TILE_CONTAINERS.get(rl).apply(inv, te);
	}

	public static Container createContainer(ResourceLocation rl, PlayerInventory inv, World w, EquipmentSlotType slot, ItemStack stack)
	{
		return ITEM_CONTAINERS.get(rl).construct(inv, w, slot, stack);
	}

	public interface ItemContainerConstructor<T>
	{
		T construct(PlayerInventory inventoryPlayer, World world, EquipmentSlotType slot, ItemStack stack);
	}
}
