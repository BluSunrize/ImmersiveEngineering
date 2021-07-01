/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.info;

import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxProvider;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

public class EnergyInfoArea extends InfoArea
{
	private final IFluxProvider energy;

	public EnergyInfoArea(int xMin, int yMin, IFluxProvider energy)
	{
		super(new Rectangle2d(xMin, yMin, 7, 46));
		this.energy = energy;
	}

	@Override
	protected void fillTooltipOverArea(int mouseX, int mouseY, List<ITextComponent> tooltip)
	{
		tooltip.add(new StringTextComponent(energy.getEnergyStored(null)+"/"+energy.getMaxEnergyStored(null)+" IF"));
	}

	@Override
	public void draw(MatrixStack transform)
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
