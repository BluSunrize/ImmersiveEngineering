/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.info;

import blusunrize.immersiveengineering.client.utils.GuiHelper;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.function.Supplier;

public class MultitankArea extends InfoArea
{
	private final int capacity;
	private final Supplier<List<FluidStack>> getFluids;

	public MultitankArea(Rect2i area, int capacity, Supplier<List<FluidStack>> getFluids)
	{
		super(area);
		this.capacity = capacity;
		this.getFluids = getFluids;
	}

	@Override
	protected void fillTooltipOverArea(int mouseX, int mouseY, List<Component> tooltip)
	{
		if(getFluids().isEmpty())
			tooltip.add(Component.translatable("gui.immersiveengineering.empty"));
		else
		{
			int myRelative = area.getY()+area.getHeight()-mouseY;
			forEachFluid((fluid, lastY, newY) -> {
				if(myRelative >= lastY&&myRelative < newY)
					FluidInfoArea.fillTooltip(fluid, capacity, tooltip::add);
			});
		}
	}

	@Override
	public void draw(GuiGraphics graphics)
	{
		MultiBufferSource.BufferSource buffers = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
		forEachFluid((fluid, lastY, newY) -> GuiHelper.drawRepeatedFluidSpriteGui(
				buffers, graphics.pose(), fluid, area.getX(), area.getY()+area.getHeight()-newY, area.getWidth(), newY-lastY
		));
		buffers.endBatch();
	}

	private void forEachFluid(TankVisitor visitor)
	{
		int fluidUpToNow = 0;
		int lastY = 0;
		final List<FluidStack> fluids = getFluids();
		for(int i = fluids.size()-1; i >= 0; i--)
		{
			FluidStack fs = fluids.get(i);
			if(!fs.isEmpty())
			{
				fluidUpToNow += fs.getAmount();
				int newY = (int)(area.getHeight()*(fluidUpToNow/(float)capacity));
				visitor.visit(fs, lastY, newY);
				lastY = newY;
			}
		}
	}

	private List<FluidStack> getFluids()
	{
		return getFluids.get();
	}

	private interface TankVisitor
	{
		void visit(FluidStack fluid, int lastY, int newY);
	}
}
