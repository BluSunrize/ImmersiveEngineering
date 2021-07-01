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
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GunTurretScreen extends TurretScreen<TurretGunTileEntity, TurretContainer.GunTurretContainer>
{
	public GunTurretScreen(TurretContainer.GunTurretContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title);
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		List<InfoArea> result = new ArrayList<>(super.makeInfoAreas());
		result.add(new TooltipArea(
				new Rectangle2d(guiLeft+134, guiTop+31, 16, 16),
				() -> new TranslationTextComponent(Lib.GUI_CONFIG+"turret.expel_casings_"+(tile.expelCasings?"on": "off"))
		));
		return result;
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull MatrixStack transform, float f, int mx, int my)
	{
		super.drawContainerBackgroundPre(transform, f, mx, my);
		GuiHelper.drawDarkSlot(transform, guiLeft+134, guiTop+13, 16, 16);
		GuiHelper.drawDarkSlot(transform, guiLeft+134, guiTop+49, 16, 16);
	}

	@Override
	protected void addCustomButtons()
	{
		this.addButton(new GuiButtonBoolean(guiLeft+134, guiTop+31, 16, 16, "", tile.expelCasings, TEXTURE, 176, 81, 0,
				btn -> {
					CompoundNBT tag = new CompoundNBT();
					int listOffset = -1;
					tile.expelCasings = btn.getNextState();
					tag.putBoolean("expelCasings", tile.expelCasings);
					handleButtonClick(tag, listOffset);
				}));
	}
}
