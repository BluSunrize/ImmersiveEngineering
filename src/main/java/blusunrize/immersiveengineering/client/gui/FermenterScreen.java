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
import blusunrize.immersiveengineering.common.gui.FermenterMenu;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.List;

import static blusunrize.immersiveengineering.api.IEApi.ieLoc;

public class FermenterScreen extends IEContainerScreen<FermenterMenu>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("fermenter");
	private static final ResourceLocation TANK = ieLoc("fermenter/tank_overlay");

	public FermenterScreen(FermenterMenu container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		return ImmutableList.of(
				new FluidInfoArea(menu.tank, new Rect2i(leftPos+112, topPos+21, 16, 47), 20, 51, TANK),
				new EnergyInfoArea(leftPos+158, topPos+22, menu.energyStorage)
		);
	}
}
