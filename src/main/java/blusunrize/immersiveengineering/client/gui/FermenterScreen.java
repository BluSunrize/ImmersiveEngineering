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
import blusunrize.immersiveengineering.common.blocks.metal.FermenterTileEntity;
import blusunrize.immersiveengineering.common.gui.FermenterContainer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.ArrayList;
import java.util.List;

public class FermenterScreen extends IEContainerScreen<FermenterContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("fermenter");

	private final FermenterTileEntity tile;

	public FermenterScreen(FermenterContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title);
		this.tile = container.tile;
	}

	@Override
	public void render(PoseStack transform, int mx, int my, float partial)
	{
		super.render(transform, mx, my, partial);
		List<Component> tooltip = new ArrayList<>();
		GuiHelper.handleGuiTank(transform, tile.tanks[0], leftPos+112, topPos+21, 16, 47, 177, 31, 20, 51, mx, my, TEXTURE, tooltip);
		if(mx > leftPos+158&&mx < leftPos+165&&my > topPos+22&&my < topPos+68)
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
		fillGradient(transform, leftPos+158, topPos+22+(46-stored), leftPos+165, topPos+68, 0xffb51500, 0xff600b00);

		GuiHelper.handleGuiTank(transform, tile.tanks[0], leftPos+112, topPos+21, 16, 47, 177, 31, 20, 51, mx, my, TEXTURE, null);

	}
}
