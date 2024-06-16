/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.gui.CrateEntityContainer;
import blusunrize.immersiveengineering.common.gui.CrateMenu;
import blusunrize.immersiveengineering.common.gui.IScreenMessageReceive;
import blusunrize.immersiveengineering.common.network.MessageContainerUpdate;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.util.Objects;

public abstract class CrateScreen<C extends CrateMenu> extends IEContainerScreen<C>
{
	private EditBox nameField;

	public CrateScreen(C container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, makeTextureLocation("crate"));
		this.imageHeight = 168;
	}

	@Override
	protected void init()
	{
		super.init();
		this.nameField = new EditBox(this.font, leftPos+titleLabelX, topPos+titleLabelY, 162, 12, Component.translatable("container.repair"));
		this.nameField.setTextColor(Lib.COLOUR_I_ImmersiveOrange);
		this.nameField.setBordered(false);
		this.nameField.setMaxLength(30);
		this.nameField.setResponder(s -> {
			if(!Objects.equals(s, this.title.getString()))
			{
				CompoundTag message = new CompoundTag();
				message.putString("name", s);
				PacketDistributor.SERVER.noArg().send(new MessageContainerUpdate(getMenu().containerId, message));
			}
		});
		this.nameField.setValue(this.title.getString());
		this.addWidget(this.nameField);
	}

	@Override
	public void render(@Nonnull GuiGraphics graphics, int mx, int my, float partial)
	{
		super.render(graphics, mx, my, partial);
		this.nameField.render(graphics, mx, my, partial);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
	{
		graphics.drawString(this.font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, Lib.COLOUR_I_ImmersiveOrange, true);
	}

	@Override
	public boolean keyPressed(int key, int scancode, int p_keyPressed_3_)
	{
		if(this.nameField.isFocused() && key!=GLFW.GLFW_KEY_ESCAPE)
			if(this.nameField.keyPressed(key, scancode, p_keyPressed_3_))
				return true;
		return super.keyPressed(key, scancode, p_keyPressed_3_);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
	{
		boolean ret = super.mouseClicked(mouseX, mouseY, mouseButton);
		if(this.nameField.mouseClicked(mouseX, mouseY, mouseButton))
		{
			this.nameField.setFocused(true);
			ret = true;
		} else if (this.nameField.isFocused())
		{
			this.nameField.setFocused(false);
			ret = true;
		}
		return ret;
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