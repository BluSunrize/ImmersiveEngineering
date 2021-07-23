/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.gui.info.FluidInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import blusunrize.immersiveengineering.common.blocks.stone.CokeOvenTileEntity;
import blusunrize.immersiveengineering.common.gui.CokeOvenContainer;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.List;

public class CokeOvenScreen extends IEContainerScreen<CokeOvenContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("coke_oven");

	private final CokeOvenTileEntity tile;

	public CokeOvenScreen(CokeOvenContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
		this.tile = container.tile;
		clearIntArray(tile.guiData);
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		return ImmutableList.of(
				new FluidInfoArea(tile.tank, new Rect2i(leftPos+129, topPos+20, 16, 47), 176, 31, 20, 51, TEXTURE)
		);
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull PoseStack transform, float f, int mx, int my)
	{
		if(tile.processMax > 0&&tile.process > 0)
		{
			int h = (int)(12*(tile.process/(float)tile.processMax));
			this.blit(transform, leftPos+59, topPos+37+12-h, 179, 1+12-h, 9, h);
		}
	}
}
