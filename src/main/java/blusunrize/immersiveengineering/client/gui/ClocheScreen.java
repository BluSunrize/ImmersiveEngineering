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
import blusunrize.immersiveengineering.common.blocks.metal.ClocheTileEntity;
import blusunrize.immersiveengineering.common.gui.ClocheContainer;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import java.util.List;

public class ClocheScreen extends IEContainerScreen<ClocheContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("cloche");

	private final ClocheTileEntity tile;

	public ClocheScreen(ClocheContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
		this.tile = container.tile;
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		return ImmutableList.of(
				new FluidInfoArea(tile.tank, new Rectangle2d(guiLeft+8, guiTop+8, 16, 47), 176, 30, 20, 51, TEXTURE),
				new EnergyInfoArea(guiLeft+158, guiTop+22, tile),
				new FertilizerInfoArea(guiLeft+30, guiTop+22, tile)
		);
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull MatrixStack transform, float f, int mx, int my)
	{
	}
}