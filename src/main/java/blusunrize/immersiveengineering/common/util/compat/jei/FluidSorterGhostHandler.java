/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei;

import blusunrize.immersiveengineering.client.gui.FluidSorterScreen;
import com.google.common.collect.ImmutableList;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.Optional;

public class FluidSorterGhostHandler implements IGhostIngredientHandler<FluidSorterScreen>
{

	@Override
	public <I> List<Target<I>> getTargetsTyped(FluidSorterScreen gui, ITypedIngredient<I> ingredient, boolean doStart)
	{
		Optional<FluidStack> ingr = ingredient.getIngredient(ForgeTypes.FLUID_STACK);
		if(ingr.isEmpty())
			return ImmutableList.of();
		ImmutableList.Builder<Target<I>> builder = ImmutableList.builder();
		for(int side = 0; side < 6; side++)
			for(int slot = 0; slot < 8; slot++)
				builder.add((Target<I>)new GhostFluidTarget(side, slot, gui));
		return builder.build();
	}

	@Override
	public void onComplete()
	{

	}

	private static class GhostFluidTarget implements Target<FluidStack>
	{
		final int side;
		final int slot;
		final FluidSorterScreen gui;
		Rect2i area;
		int lastGuiLeft, lastGuiTop;

		public GhostFluidTarget(int side, int slot, FluidSorterScreen gui)
		{
			this.side = side;
			this.slot = slot;
			this.gui = gui;
			initRectangle();
		}

		private void initRectangle()
		{
			int x = 4+(side/2)*58+(slot < 3?slot*18: slot > 4?(slot-5)*18: slot==3?0: 36);
			int y = 22+(side%2)*76+(slot < 3?0: slot > 4?36: 18);
			area = new Rect2i(gui.getGuiLeft()+x, gui.getGuiTop()+y, 16, 16);
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
		public void accept(FluidStack ingredient)
		{
			gui.setFluidInSlot(side, slot, ingredient);
		}
	}
}
