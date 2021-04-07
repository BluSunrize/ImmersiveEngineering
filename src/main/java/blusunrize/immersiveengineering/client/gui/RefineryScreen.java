/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.common.blocks.metal.RefineryTileEntity;
import blusunrize.immersiveengineering.common.gui.RefineryContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RefineryScreen extends IEContainerScreen<RefineryContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("refinery");

	private final RefineryTileEntity tile;

	public RefineryScreen(RefineryContainer container, PlayerInventory inventoryPlayer, ITextComponent component)
	{
		super(container, inventoryPlayer, component);
		this.tile = container.tile;
	}

	@Override
	public void render(MatrixStack transform, int mx, int my, float partial)
	{
		super.render(transform, mx, my, partial);
		List<ITextComponent> tooltip = new ArrayList<>();
		handleTanks(transform, mx, my, tooltip);
		if(mx > guiLeft+157&&mx < guiLeft+164&&my > guiTop+21&&my < guiTop+67)
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
		fillGradient(transform, guiLeft+157, guiTop+21+(46-stored), guiLeft+164, guiTop+67, 0xffb51500, 0xff600b00);

		handleTanks(transform, mx, my, null);
	}

	private void handleTanks(MatrixStack transform, int mx, int my, @Nullable List<ITextComponent> tooltip)
	{
		GuiHelper.handleGuiTank(transform, tile.tanks[0], guiLeft+13, guiTop+20, 16, 47, 177, 31, 20, 51, mx, my, TEXTURE, tooltip);
		GuiHelper.handleGuiTank(transform, tile.tanks[1], guiLeft+61, guiTop+20, 16, 47, 177, 31, 20, 51, mx, my, TEXTURE, tooltip);
		GuiHelper.handleGuiTank(transform, tile.tanks[2], guiLeft+109, guiTop+20, 16, 47, 177, 31, 20, 51, mx, my, TEXTURE, tooltip);
	}
}