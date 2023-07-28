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
import blusunrize.immersiveengineering.common.gui.IESlot.ItemHandlerGhost;
import blusunrize.immersiveengineering.common.network.MessageSetGhostSlots;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class IEGhostItemHandler implements IGhostIngredientHandler<IEContainerScreen>
{
	@Override
	public <I> List<Target<I>> getTargetsTyped(IEContainerScreen gui, ITypedIngredient<I> ingredient, boolean doStart)
	{
		Optional<ItemStack> ingr = ingredient.getIngredient(VanillaTypes.ITEM_STACK);
		if(ingr.isEmpty())
			return ImmutableList.of();
		ImmutableList.Builder<Target<I>> builder = ImmutableList.builder();
		for(Slot s : gui.getMenu().slots)
			if(s instanceof ItemHandlerGhost)
				builder.add((Target<I>)new GhostSlotTarget((ItemHandlerGhost)s, gui));
		return builder.build();
	}

	@Override
	public void onComplete()
	{

	}

	private static class GhostSlotTarget implements Target<ItemStack>
	{
		final ItemHandlerGhost slot;
		final IEContainerScreen<?> gui;
		Rect2i area;
		int lastGuiLeft, lastGuiTop;

		public GhostSlotTarget(ItemHandlerGhost slot, IEContainerScreen<?> gui)
		{
			this.slot = slot;
			this.gui = gui;
			initRectangle();
		}

		private void initRectangle()
		{
			area = new Rect2i(gui.getGuiLeft()+slot.x, gui.getGuiTop()+slot.y, 16, 16);
			lastGuiLeft = gui.getGuiLeft();
			lastGuiTop = gui.getGuiTop();
		}

		@Override
		public Rect2i getArea()
		{
			if(lastGuiLeft!=gui.getGuiLeft()||lastGuiTop!=gui.getGuiTop())
				initRectangle();
			return area;
		}

		@Override
		public void accept(ItemStack ingredient)
		{
			Int2ObjectMap<ItemStack> change = new Int2ObjectOpenHashMap<>();
			change.put(((Slot)slot).index, ingredient);
			ImmersiveEngineering.packetHandler.sendToServer(new MessageSetGhostSlots(change));
		}
	}
}