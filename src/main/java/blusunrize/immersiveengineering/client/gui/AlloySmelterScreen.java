/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.stone.AlloySmelterTileEntity;
import blusunrize.immersiveengineering.common.gui.AlloySmelterContainer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AlloySmelterScreen extends IEContainerScreen<AlloySmelterContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("alloy_smelter");

	private final AlloySmelterTileEntity tile;

	public AlloySmelterScreen(AlloySmelterContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title);
		this.tile = container.tile;
		clearIntArray(tile.guiState);
	}

	@Override
	protected void renderBg(PoseStack transform, float f, int mx, int my)
	{
		ClientUtils.bindTexture(TEXTURE);
		this.blit(transform, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		if(tile.lastBurnTime > 0)
		{
			int h = (int)(12*(tile.burnTime/(float)tile.lastBurnTime));
			this.blit(transform, leftPos+56, topPos+37+12-h, 179, 1+12-h, 9, h);
		}
		if(tile.processMax > 0)
		{
			int w = (int)(22*((tile.processMax-tile.process)/(float)tile.processMax));
			this.blit(transform, leftPos+84, topPos+35, 177, 14, w, 16);
		}
	}
}
