/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.gui.IEContainerScreen;
import blusunrize.immersiveengineering.common.gui.IEBaseContainer;
import blusunrize.immersiveengineering.common.gui.IESlot.ItemHandlerGhost;
import blusunrize.immersiveengineering.common.network.MessageSetGhostSlots;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import java.util.List;

public class IEGhostItemHandler implements IGhostIngredientHandler<IEContainerScreen>
{
	@Override
	public <I> List<Target<I>> getTargets(IEContainerScreen gui, I ingredient, boolean doStart)
	{
		if(ingredient instanceof ItemStack)
		{
			ImmutableList.Builder<Target<I>> builder = ImmutableList.builder();
			for(Slot s : gui.getContainer().inventorySlots)
				if(s instanceof ItemHandlerGhost)
					builder.add((Target<I>)new GhostSlotTarget((ItemHandlerGhost)s, gui));
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
		final ItemHandlerGhost slot;
		final IEContainerScreen<?> gui;
		final IEBaseContainer<?> container;
		Rectangle2d area;
		int lastGuiLeft, lastGuiTop;

		public GhostSlotTarget(ItemHandlerGhost slot, IEContainerScreen<?> gui)
		{
			this.slot = slot;
			this.container = (IEBaseContainer<?>)gui.getContainer();
			this.gui = gui;
			initRectangle();
		}

		private void initRectangle()
		{
			area = new Rectangle2d(gui.getGuiLeft()+slot.xPos, gui.getGuiTop()+slot.yPos, 16, 16);
			lastGuiLeft = gui.getGuiLeft();
			lastGuiTop = gui.getGuiTop();
		}

		@Override
		public Rectangle2d getArea()
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