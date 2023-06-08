/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.info;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.List;

public class EnergyInfoArea extends InfoArea
{
	private final IEnergyStorage energy;

	public EnergyInfoArea(int xMin, int yMin, IEnergyStorage energy)
	{
		super(new Rect2i(xMin, yMin, 7, 46));
		this.energy = energy;
	}

	@Override
	protected void fillTooltipOverArea(int mouseX, int mouseY, List<Component> tooltip)
	{
		tooltip.add(Component.literal(energy.getEnergyStored()+"/"+energy.getMaxEnergyStored()+" IF"));
	}

	@Override
	public void draw(GuiGraphics graphics)
	{
		final int height = area.getHeight();
		int stored = (int)(height*(energy.getEnergyStored()/(float)energy.getMaxEnergyStored()));
		graphics.fillGradient(
				area.getX(), area.getY()+(height-stored),
				area.getX()+area.getWidth(), area.getY()+area.getHeight(),
				0xffb51500, 0xff600b00
		);
	}
}
