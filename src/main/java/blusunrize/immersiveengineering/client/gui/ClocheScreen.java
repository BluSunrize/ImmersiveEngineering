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
import blusunrize.immersiveengineering.common.blocks.metal.ClocheBlockEntity;
import blusunrize.immersiveengineering.common.gui.ClocheContainer;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.List;

public class ClocheScreen extends IEContainerScreen<ClocheContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("cloche");

	private final ClocheBlockEntity tile;

	public ClocheScreen(ClocheContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
		this.tile = container.tile;
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		return ImmutableList.of(
				new FluidInfoArea(tile.tank, new Rect2i(leftPos+8, topPos+8, 16, 47), 176, 30, 20, 51, TEXTURE),
				new EnergyInfoArea(leftPos+158, topPos+22, tile),
				new FertilizerInfoArea(leftPos+30, topPos+22, tile)
		);
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull PoseStack transform, float f, int mx, int my)
	{
	}
}