/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.common.blocks.wooden.WoodenCrateBlockEntity;
import blusunrize.immersiveengineering.common.gui.CrateContainer;
import blusunrize.immersiveengineering.common.gui.CrateEntityContainer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public abstract class CrateScreen<C extends CrateContainer> extends IEContainerScreen<C>
{
	public CrateScreen(C container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, makeTextureLocation("crate"));
		this.imageHeight = 168;
	}

	@Override
	protected void renderLabels(PoseStack transform, int mouseX, int mouseY)
	{
		WoodenCrateBlockEntity te = menu.tile;
		this.font.draw(transform, te.getDisplayName().getContents(), 8, 6, 0x190b06);
	}

	// Unfortunately necessary to calm down the compiler wrt generics
	public static class StandardCrate extends CrateScreen<CrateContainer>
	{
		public StandardCrate(CrateContainer container, Inventory inventoryPlayer, Component title)
		{
			super(container, inventoryPlayer, title);
		}
	}

	public static class EntityCrate extends CrateScreen<CrateEntityContainer>
	{
		public EntityCrate(CrateEntityContainer container, Inventory inventoryPlayer, Component title)
		{
			super(container, inventoryPlayer, title);
		}
	}
}