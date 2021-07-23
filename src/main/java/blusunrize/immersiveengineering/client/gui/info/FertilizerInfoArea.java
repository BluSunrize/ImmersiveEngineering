/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.info;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.metal.ClocheTileEntity;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.Utils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;

public class FertilizerInfoArea extends InfoArea
{
	private final ClocheTileEntity tile;

	public FertilizerInfoArea(int xMin, int yMin, ClocheTileEntity tile)
	{
		super(new Rect2i(xMin, yMin, 7, 47));
		this.tile = tile;
	}

	@Override
	protected void fillTooltipOverArea(int mouseX, int mouseY, List<Component> tooltip)
	{
		tooltip.add(new TranslatableComponent(Lib.DESC_INFO+"fertFill", Utils.formatDouble(tile.fertilizerAmount/(float)IEServerConfig.MACHINES.cloche_fertilizer.get(), "0.00")));
		tooltip.add(new TranslatableComponent(Lib.DESC_INFO+"fertMod", Utils.formatDouble(tile.fertilizerMod, "0.00")));
	}

	@Override
	public void draw(PoseStack transform)
	{
		final int height = area.getHeight();
		int stored = (int)(height*(tile.fertilizerAmount/(float)IEServerConfig.MACHINES.cloche_fertilizer.get()));
		fillGradient(
				transform,
				area.getX(), area.getY()+(height-stored),
				area.getX() + area.getWidth(), area.getY()+area.getHeight(),
				0xff95ed00, 0xff8a5a00
		);
	}
}
