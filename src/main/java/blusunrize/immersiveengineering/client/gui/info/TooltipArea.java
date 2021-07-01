/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.info;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.text.ITextComponent;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TooltipArea extends InfoArea
{
	private final Consumer<List<ITextComponent>> tooltip;

	public TooltipArea(Rectangle2d area, ITextComponent first, ITextComponent... tooltip)
	{
		this(area, tt -> {
			tt.add(first);
			tt.addAll(Arrays.asList(tooltip));
		});
	}

	public TooltipArea(Rectangle2d area, Supplier<ITextComponent> text)
	{
		this(area, tt -> tt.add(text.get()));
	}

	public TooltipArea(Rectangle2d area, Consumer<List<ITextComponent>> tooltip)
	{
		super(area);
		this.tooltip = tooltip;
	}

	@Override
	protected void fillTooltipOverArea(int mouseX, int mouseY, List<ITextComponent> tooltip)
	{
		this.tooltip.accept(tooltip);
	}

	@Override
	public void draw(MatrixStack transform)
	{}
}
