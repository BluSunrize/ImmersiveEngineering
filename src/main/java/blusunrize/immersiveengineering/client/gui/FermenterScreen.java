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
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.List;

public class FermenterScreen extends IEContainerScreen<FermenterContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("fermenter");

	private final FermenterTileEntity tile;

	public FermenterScreen(FermenterContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
		this.tile = container.tile;
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		return ImmutableList.of(
				new FluidInfoArea(tile.tanks[0], new Rect2i(leftPos+112, topPos+21, 16, 47), 177, 31, 20, 51, TEXTURE),
				new EnergyInfoArea(leftPos+158, topPos+22, tile)
		);
	}
}
