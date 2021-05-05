/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.SqueezerTileEntity;
import blusunrize.immersiveengineering.common.gui.SqueezerContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.ArrayList;
import java.util.List;

public class SqueezerScreen extends IEContainerScreen<SqueezerContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("squeezer");

	private final SqueezerTileEntity tile;

	public SqueezerScreen(SqueezerContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title);
		this.tile = container.tile;
	}

	@Override
	public void render(MatrixStack transform, int mx, int my, float partial)
	{
		super.render(transform, mx, my, partial);
		List<ITextComponent> tooltip = new ArrayList<>();
		ClientUtils.handleGuiTank(transform, tile.tanks[0], guiLeft+112, guiTop+21, 16, 47, 177, 31, 20, 51, mx, my, TEXTURE, tooltip);
		if(mx > guiLeft+158&&mx < guiLeft+165&&my > guiTop+22&&my < guiTop+68)
			tooltip.add(new StringTextComponent(tile.getEnergyStored(null)+"/"+tile.getMaxEnergyStored(null)+" IF"));
		if(!tooltip.isEmpty())
			GuiUtils.drawHoveringText(transform, tooltip, mx, my, width, height, -1, font);
	}


	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack transform, float f, int mx, int my)
	{
		ClientUtils.bindTexture(TEXTURE);
		this.blit(transform, guiLeft, guiTop, 0, 0, xSize, ySize);

		int stored = (int)(46*(tile.getEnergyStored(null)/(float)tile.getMaxEnergyStored(null)));
		fillGradient(transform, guiLeft+158, guiTop+22+(46-stored), guiLeft+165, guiTop+68, 0xffb51500, 0xff600b00);
		ClientUtils.handleGuiTank(transform, tile.tanks[0], guiLeft+112, guiTop+21, 16, 47, 177, 31, 20, 51, mx, my, TEXTURE, null);
	}
}
