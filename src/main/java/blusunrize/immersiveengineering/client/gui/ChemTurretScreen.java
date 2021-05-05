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
import blusunrize.immersiveengineering.common.blocks.metal.TurretChemTileEntity;
import blusunrize.immersiveengineering.common.gui.TurretContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class ChemTurretScreen extends TurretScreen
{
	public ChemTurretScreen(TurretContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title);
	}

	@Override
	protected void renderCustom(MatrixStack transform, List<ITextComponent> tooltipOut, int mx, int my)
	{
		ClientUtils.handleGuiTank(transform, ((TurretChemTileEntity)tile).tank, guiLeft+134, guiTop+16, 16, 47, 196, 0, 20, 51, mx, my,
				TEXTURE, tooltipOut);
		if(mx >= guiLeft+135&&mx < guiLeft+149&&my >= guiTop+68&&my < guiTop+82)
			tooltipOut.add(new TranslationTextComponent(Lib.GUI_CONFIG+"turret.ignite_fluid"));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack transform, float f, int mx, int my)
	{
		super.drawGuiContainerBackgroundLayer(transform, f, mx, my);
		this.blit(transform, guiLeft+132, guiTop+14, 176, 0, 20, 51);
		ClientUtils.handleGuiTank(transform, ((TurretChemTileEntity)tile).tank, guiLeft+134, guiTop+16, 16, 47, 196, 0, 20, 51, mx, my, TEXTURE, null);
	}

	@Override
	protected void addCustomButtons()
	{
		this.addButton(new GuiButtonBoolean(guiLeft+135, guiTop+68, 14, 14, "", ((TurretChemTileEntity)tile).ignite, TEXTURE, 176, 51, 0,
				btn -> {
					CompoundNBT tag = new CompoundNBT();
					int listOffset = -1;
					((TurretChemTileEntity)tile).ignite = !btn.getState();
					tag.putBoolean("ignite", ((TurretChemTileEntity)tile).ignite);
					handleButtonClick(tag, listOffset);
				}));
	}
}
