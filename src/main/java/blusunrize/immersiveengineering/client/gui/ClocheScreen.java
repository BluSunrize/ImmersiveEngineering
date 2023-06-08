/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.gui.info.EnergyInfoArea;
import blusunrize.immersiveengineering.client.gui.info.FertilizerInfoArea;
import blusunrize.immersiveengineering.client.gui.info.FluidInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import blusunrize.immersiveengineering.common.gui.ClocheMenu;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.List;

public class ClocheScreen extends IEContainerScreen<ClocheMenu>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("cloche");

	public ClocheScreen(ClocheMenu container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		return ImmutableList.of(
				new FluidInfoArea(menu.tank, new Rect2i(leftPos+8, topPos+8, 16, 47), 176, 30, 20, 51, TEXTURE),
				new EnergyInfoArea(leftPos+158, topPos+22, menu.energyStorage),
				new FertilizerInfoArea(leftPos+30, topPos+22, menu.fertilizerAmount, menu.fertilizerMod)
		);
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull GuiGraphics graphics, float f, int mx, int my)
	{
		float process = menu.guiProgress.get();
		if(process > 0)
		{
			int w = (int)Math.max(1, process*12);
			graphics.blit(TEXTURE, leftPos+101, topPos+36, 181, 2, w, 12);
		}
	}
}