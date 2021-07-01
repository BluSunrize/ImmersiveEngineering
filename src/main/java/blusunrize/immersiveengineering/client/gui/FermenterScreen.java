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
import blusunrize.immersiveengineering.common.blocks.metal.FermenterTileEntity;
import blusunrize.immersiveengineering.common.gui.FermenterContainer;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import java.util.List;

public class FermenterScreen extends IEContainerScreen<FermenterContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("fermenter");

	private final FermenterTileEntity tile;

	public FermenterScreen(FermenterContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
		this.tile = container.tile;
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		return ImmutableList.of(
				new FluidInfoArea(tile.tanks[0], new Rectangle2d(guiLeft+112, guiTop+21, 16, 47), 177, 31, 20, 51, TEXTURE),
				new EnergyInfoArea(guiLeft+158, guiTop+22, tile)
		);
	}
}
