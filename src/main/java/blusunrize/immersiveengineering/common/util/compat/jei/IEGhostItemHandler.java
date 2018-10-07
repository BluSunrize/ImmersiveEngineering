/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.gui.GuiIEContainerBase;
import blusunrize.immersiveengineering.common.gui.ContainerIEBase;
import blusunrize.immersiveengineering.common.gui.IESlot.Ghost;
import blusunrize.immersiveengineering.common.util.network.MessageSetGhostSlots;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.List;

public class IEGhostItemHandler implements IGhostIngredientHandler<GuiIEContainerBase>
{
	@Override
	public <I> List<Target<I>> getTargets(GuiIEContainerBase gui, I ingredient, boolean doStart)
	{
		if(ingredient instanceof ItemStack)
		{
			ImmutableList.Builder<Target<I>> builder = ImmutableList.builder();
			for(Slot s : gui.inventorySlots.inventorySlots)
				if(s instanceof Ghost)
					builder.add((Target<I>)new GhostSlotTarget((Ghost)s, gui));
			return builder.build();
		}
		return ImmutableList.of();
	}

	@Override
	public void onComplete()
	{

	}

	private static class GhostSlotTarget implements Target<ItemStack>
	{
		final Ghost slot;
		final GuiIEContainerBase gui;
		final ContainerIEBase<?> container;
		Rectangle area;
		int lastGuiLeft, lastGuiTop;

		public GhostSlotTarget(Ghost slot, GuiIEContainerBase gui)
		{
			this.slot = slot;
			this.container = (ContainerIEBase<?>)gui.inventorySlots;
			this.gui = gui;
			initRectangle();
		}

		private void initRectangle()
		{
			area = new Rectangle(gui.getGuiLeft()+slot.xPos, gui.getGuiTop()+slot.yPos, 16, 16);
			lastGuiLeft = gui.getGuiLeft();
			lastGuiTop = gui.getGuiTop();
		}

		@Override
		public Rectangle getArea()
		{
			if(lastGuiLeft!=gui.getGuiLeft()||lastGuiTop!=gui.getGuiTop())
				initRectangle();
			return area;
		}

		@Override
		public void accept(ItemStack ingredient)
		{
			Int2ObjectMap<ItemStack> change = new Int2ObjectOpenHashMap<>();
			change.put(slot.slotNumber, ingredient);
			ImmersiveEngineering.packetHandler.sendToServer(new MessageSetGhostSlots(change));
		}
	}
}