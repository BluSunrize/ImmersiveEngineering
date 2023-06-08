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

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TooltipArea extends InfoArea
{
	private final Consumer<List<Component>> tooltip;

	public TooltipArea(Rect2i area, Component first, Component... tooltip)
	{
		this(area, tt -> {
			tt.add(first);
			tt.addAll(Arrays.asList(tooltip));
		});
	}

	public TooltipArea(Rect2i area, Supplier<Component> text)
	{
		this(area, tt -> tt.add(text.get()));
	}

	public TooltipArea(Rect2i area, Consumer<List<Component>> tooltip)
	{
		super(area);
		this.tooltip = tooltip;
	}

	@Override
	protected void fillTooltipOverArea(int mouseX, int mouseY, List<Component> tooltip)
	{
		this.tooltip.accept(tooltip);
	}

	@Override
	public void draw(GuiGraphics graphics)
	{
	}
}
