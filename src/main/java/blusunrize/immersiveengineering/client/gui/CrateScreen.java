/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.wooden.WoodenCrateTileEntity;
import blusunrize.immersiveengineering.common.gui.CrateContainer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CrateScreen extends IEContainerScreen<CrateContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("crate");

	public CrateScreen(CrateContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title);
		this.imageHeight = 168;
	}

	@Override
	protected void renderLabels(PoseStack transform, int mouseX, int mouseY)
	{
		WoodenCrateTileEntity te = menu.tile;
		this.font.draw(transform, te.getDisplayName().getContents(),
				8, 6, 0x190b06);
	}

	@Override
	protected void renderBg(PoseStack transform, float f, int mx, int my)
	{
		ClientUtils.bindTexture(TEXTURE);
		this.blit(transform, leftPos, topPos, 0, 0, imageWidth, imageHeight);
	}
}