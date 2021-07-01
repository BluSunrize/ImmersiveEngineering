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
import blusunrize.immersiveengineering.common.blocks.metal.TurretChemTileEntity;
import blusunrize.immersiveengineering.common.gui.TurretContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ChemTurretScreen extends TurretScreen<TurretChemTileEntity, TurretContainer.ChemTurretContainer>
{
	public ChemTurretScreen(TurretContainer.ChemTurretContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title);
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		List<InfoArea> areas = new ArrayList<>(super.makeInfoAreas());
		areas.add(
				new FluidInfoArea(tile.tank, new Rectangle2d(guiLeft+134, guiTop+16, 16, 47), 196, 0, 20, 51, TEXTURE)
		);
		areas.add(new TooltipArea(
				new Rectangle2d(guiLeft+135, guiTop+68, 14, 14),
				new TranslationTextComponent(Lib.GUI_CONFIG+"turret.ignite_fluid")
		));
		return areas;
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull MatrixStack transform, float f, int mx, int my)
	{
		super.drawContainerBackgroundPre(transform, f, mx, my);
		this.blit(transform, guiLeft+132, guiTop+14, 176, 0, 20, 51);
	}

	@Override
	protected void addCustomButtons()
	{
		this.addButton(new GuiButtonBoolean(guiLeft+135, guiTop+68, 14, 14, "", tile.ignite, TEXTURE, 176, 51, 0,
				btn -> {
					CompoundNBT tag = new CompoundNBT();
					int listOffset = -1;
					tile.ignite = !btn.getState();
					tag.putBoolean("ignite", tile.ignite);
					handleButtonClick(tag, listOffset);
				}));
	}
}
