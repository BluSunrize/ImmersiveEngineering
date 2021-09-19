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
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.common.blocks.metal.TurretGunTileEntity;
import blusunrize.immersiveengineering.common.gui.TurretContainer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class GunTurretScreen extends TurretScreen
{
	public GunTurretScreen(TurretContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title);
	}

	@Override
	protected void renderCustom(PoseStack transform, List<Component> tooltipOut, int mx, int my)
	{
		if(mx >= leftPos+134&&mx < leftPos+150&&my >= topPos+31&&my < topPos+47)
			tooltipOut.add(new TranslatableComponent(Lib.GUI_CONFIG+"turret.expel_casings_"+(((TurretGunTileEntity)tile).expelCasings?"on": "off")));
	}

	@Override
	protected void renderBg(PoseStack transform, float f, int mx, int my)
	{
		super.renderBg(transform, f, mx, my);
		GuiHelper.drawDarkSlot(transform, leftPos+134, topPos+13, 16, 16);
		GuiHelper.drawDarkSlot(transform, leftPos+134, topPos+49, 16, 16);
	}

	@Override
	protected void addCustomButtons()
	{
		this.addButton(new GuiButtonBoolean(leftPos+134, topPos+31, 16, 16, "", ((TurretGunTileEntity)tile).expelCasings, TEXTURE, 176, 81, 0,
				btn -> {
					CompoundTag tag = new CompoundTag();
					int listOffset = -1;
					((TurretGunTileEntity)tile).expelCasings = btn.getNextState();
					tag.putBoolean("expelCasings", ((TurretGunTileEntity)tile).expelCasings);
					handleButtonClick(tag, listOffset);
				}));
	}
}
