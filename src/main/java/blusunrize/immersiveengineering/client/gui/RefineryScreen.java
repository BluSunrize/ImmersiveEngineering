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
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fml.client.gui.GuiUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RefineryScreen extends IEContainerScreen<RefineryContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("refinery");

	private final RefineryTileEntity tile;

	public RefineryScreen(RefineryContainer container, Inventory inventoryPlayer, Component component)
	{
		super(container, inventoryPlayer, component);
		this.tile = container.tile;
	}

	@Override
	public void render(PoseStack transform, int mx, int my, float partial)
	{
		super.render(transform, mx, my, partial);
		List<Component> tooltip = new ArrayList<>();
		handleTanks(transform, mx, my, tooltip);
		if(mx > leftPos+157&&mx < leftPos+164&&my > topPos+21&&my < topPos+67)
			tooltip.add(new TextComponent(tile.getEnergyStored(null)+"/"+tile.getMaxEnergyStored(null)+" IF"));

		if(!tooltip.isEmpty())
			GuiUtils.drawHoveringText(transform, tooltip, mx, my, width, height, -1, font);
	}

	@Override
	protected void renderBg(PoseStack transform, float f, int mx, int my)
	{
		ClientUtils.bindTexture(TEXTURE);
		this.blit(transform, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		int stored = (int)(46*(tile.getEnergyStored(null)/(float)tile.getMaxEnergyStored(null)));
		fillGradient(transform, leftPos+157, topPos+21+(46-stored), leftPos+164, topPos+67, 0xffb51500, 0xff600b00);

		handleTanks(transform, mx, my, null);
	}

	private void handleTanks(PoseStack transform, int mx, int my, @Nullable List<Component> tooltip)
	{
		GuiHelper.handleGuiTank(transform, tile.tanks[0], leftPos+13, topPos+20, 16, 47, 177, 31, 20, 51, mx, my, TEXTURE, tooltip);
		GuiHelper.handleGuiTank(transform, tile.tanks[1], leftPos+61, topPos+20, 16, 47, 177, 31, 20, 51, mx, my, TEXTURE, tooltip);
		GuiHelper.handleGuiTank(transform, tile.tanks[2], leftPos+109, topPos+20, 16, 47, 177, 31, 20, 51, mx, my, TEXTURE, tooltip);
	}
}