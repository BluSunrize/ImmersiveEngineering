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
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class CrateScreen extends IEContainerScreen<CrateContainer>
{
	public CrateScreen(CrateContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title);
		this.ySize = 168;
	}

	@Override
	protected void func_230451_b_(MatrixStack transform, int mouseX, int mouseY)
	{
		WoodenCrateTileEntity te = container.tile;
		this.font.drawString(transform, te.getDisplayName().getUnformattedComponentText(),
				8, 6, 0x190b06);
	}

	@Override
	protected void func_230450_a_(MatrixStack transform, float f, int mx, int my)
	{
		RenderSystem.color3f(1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/crate.png");
		this.blit(transform, guiLeft, guiTop, 0, 0, xSize, ySize);
	}
}