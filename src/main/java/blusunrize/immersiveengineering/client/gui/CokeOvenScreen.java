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
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import java.util.List;

public class CokeOvenScreen extends IEContainerScreen<CokeOvenContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("coke_oven");

	private final CokeOvenTileEntity tile;

	public CokeOvenScreen(CokeOvenContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
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
				new FluidInfoArea(tile.tank, new Rectangle2d(guiLeft+129, guiTop+20, 16, 47), 176, 31, 20, 51, TEXTURE)
		);
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull MatrixStack transform, float f, int mx, int my)
	{
		if(tile.processMax > 0&&tile.process > 0)
		{
			int h = (int)(12*(tile.process/(float)tile.processMax));
			this.blit(transform, guiLeft+59, guiTop+37+12-h, 179, 1+12-h, 9, h);
		}
	}
}
