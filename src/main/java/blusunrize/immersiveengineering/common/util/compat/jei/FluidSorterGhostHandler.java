/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei;

import blusunrize.immersiveengineering.client.gui.GuiFluidSorter;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.collect.ImmutableList;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraftforge.fluids.FluidStack;

import java.awt.*;
import java.util.List;

public class FluidSorterGhostHandler implements IGhostIngredientHandler<GuiFluidSorter>
{

	@Override
	public <I> List<Target<I>> getTargets(GuiFluidSorter gui, I ingredient, boolean doStart)
	{
		if(ingredient instanceof FluidStack)
		{
			ImmutableList.Builder<Target<I>> builder = ImmutableList.builder();
			for(int side = 0; side < 6; side++)
				for(int slot = 0; slot < 8; slot++)
					builder.add((Target<I>)new GhostFluidTarget(side, slot, gui));
			return builder.build();
		}
		else
			IELogger.info(ingredient+" of type "+ingredient.getClass());
		return ImmutableList.of();
	}

	@Override
	public void onComplete()
	{

	}

	private static class GhostFluidTarget implements Target<FluidStack>
	{
		final int side;
		final int slot;
		final GuiFluidSorter gui;
		Rectangle area;
		int lastGuiLeft, lastGuiTop;

		public GhostFluidTarget(int side, int slot, GuiFluidSorter gui)
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
			area = new Rectangle(gui.getGuiLeft()+x, gui.getGuiTop()+y, 16, 16);
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
		public void accept(FluidStack ingredient)
		{
			gui.setFluidInSlot(side, slot, ingredient);
		}
	}
}
