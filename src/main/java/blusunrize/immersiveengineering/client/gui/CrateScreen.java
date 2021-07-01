/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.common.blocks.wooden.WoodenCrateTileEntity;
import blusunrize.immersiveengineering.common.gui.CrateContainer;
import blusunrize.immersiveengineering.common.gui.CrateEntityContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public abstract class CrateScreen<C extends CrateContainer> extends IEContainerScreen<C>
{
	public CrateScreen(C container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title, makeTextureLocation("crate"));
		this.ySize = 168;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack transform, int mouseX, int mouseY)
	{
		WoodenCrateTileEntity te = container.tile;
		this.font.drawString(transform, te.getDisplayName().getUnformattedComponentText(), 8, 6, 0x190b06);
	}

	// Unfortunately necessary to calm down the compiler wrt generics
	public static class StandardCrate extends CrateScreen<CrateContainer>
	{
		public StandardCrate(CrateContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
		{
			super(container, inventoryPlayer, title);
		}
	}

	public static class EntityCrate extends CrateScreen<CrateEntityContainer>
	{
		public EntityCrate(CrateEntityContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
		{
			super(container, inventoryPlayer, title);
		}
	}
}