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
import blusunrize.immersiveengineering.common.blocks.metal.TurretChemTileEntity;
import blusunrize.immersiveengineering.common.gui.TurretContainer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class ChemTurretScreen extends TurretScreen
{
	public ChemTurretScreen(TurretContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title);
	}

	@Override
	protected void renderCustom(PoseStack transform, List<Component> tooltipOut, int mx, int my)
	{
		GuiHelper.handleGuiTank(transform, ((TurretChemTileEntity)tile).tank, leftPos+134, topPos+16, 16, 47, 196, 0, 20, 51, mx, my,
				TEXTURE, tooltipOut);
		if(mx >= leftPos+135&&mx < leftPos+149&&my >= topPos+68&&my < topPos+82)
			tooltipOut.add(new TranslatableComponent(Lib.GUI_CONFIG+"turret.ignite_fluid"));
	}

	@Override
	protected void renderBg(PoseStack transform, float f, int mx, int my)
	{
		super.renderBg(transform, f, mx, my);
		this.blit(transform, leftPos+132, topPos+14, 176, 0, 20, 51);
		GuiHelper.handleGuiTank(transform, ((TurretChemTileEntity)tile).tank, leftPos+134, topPos+16, 16, 47, 196, 0, 20, 51, mx, my, TEXTURE, null);
	}

	@Override
	protected void addCustomButtons()
	{
		this.addButton(new GuiButtonBoolean(leftPos+135, topPos+68, 14, 14, "", ((TurretChemTileEntity)tile).ignite, TEXTURE, 176, 51, 0,
				btn -> {
					CompoundTag tag = new CompoundTag();
					int listOffset = -1;
					((TurretChemTileEntity)tile).ignite = !btn.getState();
					tag.putBoolean("ignite", ((TurretChemTileEntity)tile).ignite);
					handleButtonClick(tag, listOffset);
				}));
	}
}
