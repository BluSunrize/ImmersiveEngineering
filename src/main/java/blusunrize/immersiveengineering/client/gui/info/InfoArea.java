/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.info;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public abstract class InfoArea extends AbstractGui
{
	protected final Rectangle2d area;

	protected InfoArea(Rectangle2d area)
	{
		this.area = area;
	}

	public final void fillTooltip(int mouseX, int mouseY, List<ITextComponent> tooltip) {
		if (area.contains(mouseX, mouseY))
			fillTooltipOverArea(mouseX, mouseY, tooltip);
	}

	protected abstract void fillTooltipOverArea(int mouseX, int mouseY, List<ITextComponent> tooltip);

	public abstract void draw(MatrixStack transform);
}
