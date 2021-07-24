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
import blusunrize.immersiveengineering.common.blocks.metal.TurretGunTileEntity;
import blusunrize.immersiveengineering.common.gui.TurretContainer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GunTurretScreen extends TurretScreen<TurretGunTileEntity, TurretContainer.GunTurretContainer>
{
	public GunTurretScreen(TurretContainer.GunTurretContainer container, Inventory inventoryPlayer, Component title)
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
				() -> new TranslatableComponent(Lib.GUI_CONFIG+"turret.expel_casings_"+(tile.expelCasings?"on": "off"))
		));
		return result;
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull PoseStack transform, float f, int mx, int my)
	{
		super.drawContainerBackgroundPre(transform, f, mx, my);
		GuiHelper.drawDarkSlot(transform, leftPos+134, topPos+13, 16, 16);
		GuiHelper.drawDarkSlot(transform, leftPos+134, topPos+49, 16, 16);
	}

	@Override
	protected void addCustomButtons()
	{
		this.addRenderableWidget(new GuiButtonBoolean(leftPos+134, topPos+31, 16, 16, "", tile.expelCasings, TEXTURE, 176, 81, 0,
				btn -> {
					CompoundTag tag = new CompoundTag();
					int listOffset = -1;
					tile.expelCasings = btn.getNextState();
					tag.putBoolean("expelCasings", tile.expelCasings);
					handleButtonClick(tag, listOffset);
				}));
	}
}
