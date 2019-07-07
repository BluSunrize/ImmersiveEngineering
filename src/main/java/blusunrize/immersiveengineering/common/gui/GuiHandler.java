/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class GuiHandler
{
	private static final Map<ResourceLocation, BiFunction<PlayerInventory, TileEntity, Screen>> TILE_GUIS = new HashMap<>();
	private static final Map<ResourceLocation, BiFunction<PlayerInventory, TileEntity, Container>> TILE_CONTAINERS = new HashMap<>();
	private static final Map<ResourceLocation, ItemContainerConstructor<Screen>> ITEM_GUIS = new HashMap<>();
	private static final Map<ResourceLocation, ItemContainerConstructor<Container>> ITEM_CONTAINERS = new HashMap<>();

	//TODO dedicated server?
	public static <T> void register(ResourceLocation name, BiFunction<PlayerInventory, T,
			Screen> gui, BiFunction<PlayerInventory, T, Container> container)
	{
		TILE_GUIS.put(name, (inv, te) -> gui.apply(inv, (T)te));
		TILE_CONTAINERS.put(name, (inv, te) -> container.apply(inv, (T)te));
	}

	public static <T> void register(ResourceLocation name, ItemContainerConstructor<Screen> gui,
									ItemContainerConstructor<Container> container)
	{
		ITEM_GUIS.put(name, gui);
		ITEM_CONTAINERS.put(name, container);
	}

	public static Container createContainer(ResourceLocation rl, PlayerInventory inv, TileEntity te)
	{
		return TILE_CONTAINERS.get(rl).apply(inv, te);
	}

	public static Screen createGui(ResourceLocation rl, PlayerInventory inv, TileEntity te)
	{
		return TILE_GUIS.get(rl).apply(inv, te);
	}


	public static Container createContainer(ResourceLocation rl, PlayerInventory inv, World w, EquipmentSlotType slot, ItemStack stack)
	{
		return ITEM_CONTAINERS.get(rl).construct(inv, w, slot, stack);
	}

	public static Screen createGui(ResourceLocation rl, PlayerInventory inv, World w, EquipmentSlotType slot, ItemStack stack)
	{
		return ITEM_GUIS.get(rl).construct(inv, w, slot, stack);
	}

	public static Screen createGui(FMLPlayMessages.OpenContainer msg)
	{
		PlayerInventory inv = Minecraft.getInstance().player.inventory;
		World world = Minecraft.getInstance().world;
		ResourceLocation name = msg.getId();
		boolean isTE = msg.getAdditionalData().readBoolean();
		if(isTE)
		{
			BlockPos pos = msg.getAdditionalData().readBlockPos();
			TileEntity te = world.getTileEntity(pos);
			return createGui(name, inv, te);
		}
		else
		{
			int slotOrdinal = msg.getAdditionalData().readInt();
			EquipmentSlotType slot = EquipmentSlotType.values()[slotOrdinal];
			ItemStack stack = Minecraft.getInstance().player.getItemStackFromSlot(slot);
			return createGui(name, inv, world, slot, stack);
		}
	}

	public static <T extends TileEntity & IInteractionObject> void openGuiForTile(@Nonnull PlayerEntity player, @Nonnull T tile)
	{
		NetworkHooks.openGui((ServerPlayerEntity)player, tile, buffer -> {
			buffer.writeBoolean(true);
			buffer.writeBlockPos(tile.getPos());
		});
	}

	public static void openGuiForItem(@Nonnull PlayerEntity player, @Nonnull EquipmentSlotType slot)
	{
		ItemStack stack = player.getItemStackFromSlot(slot);
		if(stack.isEmpty()||!(stack.getItem() instanceof IInteractionObject))
			return;
		IInteractionObject gui = (IInteractionObject)stack.getItem();
		NetworkHooks.openGui((ServerPlayerEntity)player, gui, buffer -> {
			buffer.writeBoolean(false);
			buffer.writeInt(slot.ordinal());
		});
	}

	public interface ItemContainerConstructor<T>
	{
		T construct(PlayerInventory inventoryPlayer, World world, EquipmentSlotType slot, ItemStack stack);
	}
}
