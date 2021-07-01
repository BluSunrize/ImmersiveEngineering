/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.gui.info.EnergyInfoArea;
import blusunrize.immersiveengineering.client.gui.info.FluidInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import blusunrize.immersiveengineering.common.blocks.metal.RefineryTileEntity;
import blusunrize.immersiveengineering.common.gui.RefineryContainer;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import java.util.List;

public class RefineryScreen extends IEContainerScreen<RefineryContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("refinery");

	private final RefineryTileEntity tile;

	public RefineryScreen(RefineryContainer container, PlayerInventory inventoryPlayer, ITextComponent component)
	{
		super(container, inventoryPlayer, component, TEXTURE);
		this.tile = container.tile;
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		return ImmutableList.of(
				new FluidInfoArea(tile.tanks[0], new Rectangle2d(guiLeft+13, guiTop+20, 16, 47), 177, 31, 20, 51, TEXTURE),
				new FluidInfoArea(tile.tanks[1], new Rectangle2d(guiLeft+61, guiTop+20, 16, 47), 177, 31, 20, 51, TEXTURE),
				new FluidInfoArea(tile.tanks[2], new Rectangle2d(guiLeft+109, guiTop+20, 16, 47), 177, 31, 20, 51, TEXTURE),
				new EnergyInfoArea(guiLeft+157, guiTop+21, tile)
		);
	}
}