/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.common.gui.CrateEntityContainer;
import blusunrize.immersiveengineering.common.gui.CrateMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public abstract class CrateScreen<C extends CrateMenu> extends IEContainerScreen<C>
{
	public CrateScreen(C container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, makeTextureLocation("crate"));
		this.imageHeight = 168;
	}

	// Unfortunately necessary to calm down the compiler wrt generics
	public static class StandardCrate extends CrateScreen<CrateMenu>
	{
		public StandardCrate(CrateMenu container, Inventory inventoryPlayer, Component title)
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