/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.info;

import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.List;

public class EnergyInfoArea extends InfoArea
{
	private final IFluxProvider energy;

	public EnergyInfoArea(int xMin, int yMin, IFluxProvider energy)
	{
		super(new Rect2i(xMin, yMin, 7, 46));
		this.energy = energy;
	}

	@Override
	protected void fillTooltipOverArea(int mouseX, int mouseY, List<Component> tooltip)
	{
		tooltip.add(new TextComponent(energy.getEnergyStored(null)+"/"+energy.getMaxEnergyStored(null)+" IF"));
	}

	@Override
	public void draw(PoseStack transform)
	{
		final int height = area.getHeight();
		int stored = (int)(height*(energy.getEnergyStored(null)/(float)energy.getMaxEnergyStored(null)));
		fillGradient(
				transform,
				area.getX(), area.getY()+(height-stored),
				area.getX() + area.getWidth(), area.getY() +area.getHeight(),
				0xffb51500, 0xff600b00
		);
	}
}
