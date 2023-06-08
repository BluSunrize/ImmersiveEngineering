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
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import blusunrize.immersiveengineering.client.gui.info.TooltipArea;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.common.gui.TurretMenu.GunTurretMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GunTurretScreen extends TurretScreen<GunTurretMenu>
{
	public GunTurretScreen(GunTurretMenu container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title);
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		List<InfoArea> result = new ArrayList<>(super.makeInfoAreas());
		result.add(new TooltipArea(
				new Rect2i(leftPos+134, topPos+31, 16, 16),
				() -> Component.translatable(Lib.GUI_CONFIG+"turret.expel_casings_"+(menu.expelCasings.get()?"on": "off"))
		));
		return result;
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull GuiGraphics graphics, float f, int mx, int my)
	{
		super.drawContainerBackgroundPre(graphics, f, mx, my);
		GuiHelper.drawDarkSlot(graphics, leftPos+134, topPos+13, 16, 16);
		GuiHelper.drawDarkSlot(graphics, leftPos+134, topPos+49, 16, 16);
	}

	@Override
	protected void addCustomButtons()
	{
		this.addRenderableWidget(new GuiButtonBoolean(leftPos+134, topPos+31, 16, 16, "", menu.expelCasings, TEXTURE, 176, 81, 0,
				btn -> {
					CompoundTag tag = new CompoundTag();
					int listOffset = -1;
					tag.putBoolean("expelCasings", btn.getNextState());
					handleButtonClick(tag, listOffset);
				}));
	}
}
