/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.info;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Supplier;

public class FertilizerInfoArea extends InfoArea
{
	private final Supplier<Integer> fertilizerAmount;
	private final Supplier<Float> fertilizerMod;

	public FertilizerInfoArea(int xMin, int yMin, Supplier<Integer> fertilizerAmount, Supplier<Float> fertilizerMod)
	{
		super(new Rect2i(xMin, yMin, 7, 47));
		this.fertilizerAmount = fertilizerAmount;
		this.fertilizerMod = fertilizerMod;
	}

	@Override
	protected void fillTooltipOverArea(int mouseX, int mouseY, List<Component> tooltip)
	{
		tooltip.add(Component.translatable(Lib.DESC_INFO+"fertFill", Utils.formatDouble(fertilizerAmount.get()/(float)IEServerConfig.MACHINES.cloche_fertilizer.get(), "0.00")));
		tooltip.add(Component.translatable(Lib.DESC_INFO+"fertMod", Utils.formatDouble(fertilizerMod.get(), "0.00")));
	}

	@Override
	public void draw(GuiGraphics graphics)
	{
		final int height = area.getHeight();
		int stored = (int)(height*(fertilizerAmount.get()/(float)IEServerConfig.MACHINES.cloche_fertilizer.get()));
		graphics.fillGradient(
				area.getX(), area.getY()+(height-stored),
				area.getX()+area.getWidth(), area.getY()+area.getHeight(),
				0xff95ed00, 0xff8a5a00
		);
	}
}
