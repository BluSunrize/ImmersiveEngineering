/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonBoolean;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonState;
import blusunrize.immersiveengineering.common.blocks.metal.TurretGunTileEntity;
import blusunrize.immersiveengineering.common.gui.TurretContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class GunTurretScreen extends TurretScreen
{
	public GunTurretScreen(TurretContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title);
	}

	@Override
	protected void renderCustom(List<ITextComponent> tooltipOut, int mx, int my)
	{
		if(mx >= guiLeft+134&&mx < guiLeft+150&&my >= guiTop+31&&my < guiTop+47)
			tooltipOut.add(new TranslationTextComponent(Lib.GUI_CONFIG+"turret.expel_casings_"+(((TurretGunTileEntity)tile).expelCasings?"on": "off")));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		super.drawGuiContainerBackgroundLayer(f, mx, my);
		ClientUtils.drawDarkSlot(guiLeft+134, guiTop+13, 16, 16);
		ClientUtils.drawDarkSlot(guiLeft+134, guiTop+49, 16, 16);
	}

	@Override
	protected void addCustomButtons()
	{
		this.addButton(new GuiButtonBoolean(guiLeft+134, guiTop+31, 16, 16, "", ((TurretGunTileEntity)tile).expelCasings, "immersiveengineering:textures/gui/turret.png", 176, 81, 0,
				btn -> {
					CompoundNBT tag = new CompoundNBT();
					int listOffset = -1;
					((TurretGunTileEntity)tile).expelCasings = btn.getNextState();
					tag.putBoolean("expelCasings", ((TurretGunTileEntity)tile).expelCasings);
					handleButtonClick(tag, listOffset);
				}));
	}
}
