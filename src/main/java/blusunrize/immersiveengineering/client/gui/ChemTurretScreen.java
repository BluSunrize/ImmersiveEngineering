/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonBoolean;
import blusunrize.immersiveengineering.client.gui.info.FluidInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import blusunrize.immersiveengineering.client.gui.info.TooltipArea;
import blusunrize.immersiveengineering.common.gui.TurretMenu.ChemTurretMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ChemTurretScreen extends TurretScreen<ChemTurretMenu>
{
	public ChemTurretScreen(ChemTurretMenu container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title);
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		List<InfoArea> areas = new ArrayList<>(super.makeInfoAreas());
		areas.add(
				new FluidInfoArea(menu.tank, new Rect2i(leftPos+134, topPos+16, 16, 47), 196, 0, 20, 51, TEXTURE)
		);
		areas.add(new TooltipArea(
				new Rect2i(leftPos+135, topPos+68, 14, 14),
				Component.translatable(Lib.GUI_CONFIG+"turret.ignite_fluid")
		));
		return areas;
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull GuiGraphics graphics, float f, int mx, int my)
	{
		super.drawContainerBackgroundPre(graphics, f, mx, my);
		graphics.blit(TEXTURE, leftPos+132, topPos+14, 176, 0, 20, 51);
	}

	@Override
	protected void addCustomButtons()
	{
		this.addRenderableWidget(new GuiButtonBoolean(leftPos+135, topPos+68, 14, 14, "", menu.ignite, TEXTURE, 176, 51, 0,
				btn -> {
					CompoundTag tag = new CompoundTag();
					int listOffset = -1;
					tag.putBoolean("ignite", btn.getNextState());
					handleButtonClick(tag, listOffset);
				}));
	}
}
