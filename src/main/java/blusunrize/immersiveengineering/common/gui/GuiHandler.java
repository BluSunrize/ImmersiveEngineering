/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
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
	private static final Map<ResourceLocation, BiFunction<InventoryPlayer, TileEntity, GuiScreen>> TILE_GUIS = new HashMap<>();
	private static final Map<ResourceLocation, BiFunction<InventoryPlayer, TileEntity, Container>> TILE_CONTAINERS = new HashMap<>();
	private static final Map<ResourceLocation, ItemContainerConstructor<GuiScreen>> ITEM_GUIS = new HashMap<>();
	private static final Map<ResourceLocation, ItemContainerConstructor<Container>> ITEM_CONTAINERS = new HashMap<>();

	//TODO dedicated server?
	public static <T> void register(ResourceLocation name, BiFunction<InventoryPlayer, T,
			GuiScreen> gui, BiFunction<InventoryPlayer, T, Container> container)
	{
		TILE_GUIS.put(name, (inv, te) -> gui.apply(inv, (T)te));
		TILE_CONTAINERS.put(name, (inv, te) -> container.apply(inv, (T)te));
	}

	public static <T> void register(ResourceLocation name, ItemContainerConstructor<GuiScreen> gui,
									ItemContainerConstructor<Container> container)
	{
		ITEM_GUIS.put(name, gui);
		ITEM_CONTAINERS.put(name, container);
	}

	public static Container createContainer(ResourceLocation rl, InventoryPlayer inv, TileEntity te)
	{
		return TILE_CONTAINERS.get(rl).apply(inv, te);
	}

	public static GuiScreen createGui(ResourceLocation rl, InventoryPlayer inv, TileEntity te)
	{
		return TILE_GUIS.get(rl).apply(inv, te);
	}


	public static Container createContainer(ResourceLocation rl, InventoryPlayer inv, World w, EntityEquipmentSlot slot, ItemStack stack)
	{
		return ITEM_CONTAINERS.get(rl).construct(inv, w, slot, stack);
	}

	public static GuiScreen createGui(ResourceLocation rl, InventoryPlayer inv, World w, EntityEquipmentSlot slot, ItemStack stack)
	{
		return ITEM_GUIS.get(rl).construct(inv, w, slot, stack);
	}

	public static GuiScreen createGui(FMLPlayMessages.OpenContainer msg)
	{
		InventoryPlayer inv = Minecraft.getInstance().player.inventory;
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
			EntityEquipmentSlot slot = EntityEquipmentSlot.values()[slotOrdinal];
			ItemStack stack = Minecraft.getInstance().player.getItemStackFromSlot(slot);
			return createGui(name, inv, world, slot, stack);
		}
	}

	public static <T extends TileEntity & IInteractionObject> void openGuiForTile(@Nonnull EntityPlayer player, @Nonnull T tile)
	{
		NetworkHooks.openGui((EntityPlayerMP)player, tile, buffer -> {
			buffer.writeBoolean(true);
			buffer.writeBlockPos(tile.getPos());
		});
	}

	public static void openGuiForItem(@Nonnull EntityPlayer player, @Nonnull EntityEquipmentSlot slot)
	{
		ItemStack stack = player.getItemStackFromSlot(slot);
		if(stack.isEmpty()||!(stack.getItem() instanceof IInteractionObject))
			return;
		IInteractionObject gui = (IInteractionObject)stack.getItem();
		NetworkHooks.openGui((EntityPlayerMP)player, gui, buffer -> {
			buffer.writeBoolean(false);
			buffer.writeInt(slot.ordinal());
		});
	}

	public interface ItemContainerConstructor<T>
	{
		T construct(InventoryPlayer inventoryPlayer, World world, EntityEquipmentSlot slot, ItemStack stack);
	}
}
