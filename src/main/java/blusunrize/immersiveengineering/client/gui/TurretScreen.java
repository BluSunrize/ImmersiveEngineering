/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonCheckbox;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE.ButtonTexture;
import blusunrize.immersiveengineering.client.gui.elements.GuiReactiveList;
import blusunrize.immersiveengineering.client.gui.info.EnergyInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import blusunrize.immersiveengineering.common.gui.TurretMenu;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.util.List;

import static blusunrize.immersiveengineering.api.IEApi.ieLoc;

public abstract class TurretScreen<C extends TurretMenu> extends IEContainerScreen<C>
{
	protected static final ResourceLocation TEXTURE = makeTextureLocation("turret");
	private static final ButtonTexture BUTTON_ADD = new ButtonTexture(ieLoc("turret/button_add"));

	private EditBox nameField;

	public TurretScreen(C container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
		this.imageHeight = 190;
		this.inventoryLabelY = 99;
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		return ImmutableList.of(new EnergyInfoArea(leftPos+158, topPos+16, menu.data.energy()));
	}

	@Override
	public void init()
	{
		super.init();
		this.nameField = new EditBox(this.font, leftPos+11, topPos+88, 58, 12, Component.empty());
		this.nameField.setTextColor(-1);
		this.nameField.setTextColorUneditable(-1);
		this.nameField.setBordered(false);
		this.nameField.setMaxLength(30);
		this.addWidget(this.nameField);

		this.clearWidgets();
		this.addRenderableWidget(GuiReactiveList.build(leftPos+10, topPos+10, 60, 72,
				list -> {
					CompoundTag tag = new CompoundTag();
					int listOffset = -1;
					int rem = list.selectedOption;
					if(rem >= 0&&!menu.data.targetList().get().isEmpty())
					{
						tag.putInt("remove", rem);
						listOffset = list.getOffset()-1;
						handleButtonClick(tag, listOffset);
					}
				}, menu.data.targetList())
				.setPadding(0, 0, 2, 2));
		this.addRenderableWidget(new GuiButtonIE(
				leftPos+74, topPos+84, 24, 16, Component.translatable(Lib.GUI_CONFIG+"turret.add"), BUTTON_ADD,
				btn -> addName()
		));
		this.addRenderableWidget(new GuiButtonCheckbox(leftPos+74, topPos+10, Component.translatable(Lib.GUI_CONFIG+"turret.blacklist"), () -> !menu.data.whitelist().get(),
				btn -> {
					CompoundTag tag = new CompoundTag();
					int listOffset = -1;
					tag.putBoolean("whitelist", btn.getState());
					handleButtonClick(tag, listOffset);
				}));
		this.addRenderableWidget(new GuiButtonCheckbox(leftPos+74, topPos+26, Component.translatable(Lib.GUI_CONFIG+"turret.animals"), menu.data.attackAnimals(),
				btn -> {
					CompoundTag tag = new CompoundTag();
					int listOffset = -1;
					tag.putBoolean("attackAnimals", btn.getNextState());
					handleButtonClick(tag, listOffset);
				}));
		this.addRenderableWidget(new GuiButtonCheckbox(leftPos+74, topPos+42, Component.translatable(Lib.GUI_CONFIG+"turret.players"), menu.data.attackPlayers(),
				btn -> {
					CompoundTag tag = new CompoundTag();
					int listOffset = -1;
					tag.putBoolean("attackPlayers", btn.getNextState());
					handleButtonClick(tag, listOffset);
				}));
		this.addRenderableWidget(new GuiButtonCheckbox(leftPos+74, topPos+58, Component.translatable(Lib.GUI_CONFIG+"turret.neutrals"), menu.data.attackNeutrals(),
				btn -> {
					CompoundTag tag = new CompoundTag();
					int listOffset = -1;
					tag.putBoolean("attackNeutrals", btn.getNextState());
					handleButtonClick(tag, listOffset);
				}));

		addCustomButtons();
	}

	protected abstract void addCustomButtons();

	protected void handleButtonClick(CompoundTag nbt, int listOffset)
	{
		if(!nbt.isEmpty())
		{
			sendUpdateToServer(nbt);
			if(listOffset >= 0)
				((GuiReactiveList)this.children().get(0)).setOffset(listOffset);
		}
	}

	@Override
	public void render(@Nonnull GuiGraphics graphics, int mx, int my, float partial)
	{
		super.render(graphics, mx, my, partial);
		this.nameField.render(graphics, mx, my, partial);
	}

	@Override
	public boolean keyPressed(int key, int scancode, int p_keyPressed_3_)
	{
		if(this.nameField.isFocused())
		{
			if(key==GLFW.GLFW_KEY_ENTER)
			{
				addName();
				return true;
			}
			else if(this.nameField.keyPressed(key, scancode, p_keyPressed_3_))
				return true;
			InputConstants.Key keyData = InputConstants.getKey(key, scancode);
			// Hack: Stop AbstractContainerScreen.keyPressed from closing the screen when the inventory key ('E') is
			// pressed. The name field handles its input in the (later) charTyped.
			if(minecraft!=null&&minecraft.options.keyInventory.isActiveAndMatches(keyData))
				return true;
		}

		return super.keyPressed(key, scancode, p_keyPressed_3_);
	}

	private void addName()
	{
		CompoundTag tag = new CompoundTag();
		int listOffset = -1;
		String name = nameField.getValue();
		if(!menu.data.targetList().get().contains(name))
		{
			listOffset = ((GuiReactiveList)children().get(0)).getMaxOffset();
			tag.putString("add", name);
		}
		nameField.setValue("");
		handleButtonClick(tag, listOffset);
	}

	@Override
	public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_)
	{
		return this.nameField.charTyped(p_charTyped_1_, p_charTyped_2_);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
	{
		boolean ret = super.mouseClicked(mouseX, mouseY, mouseButton);
		if (this.nameField.mouseClicked(mouseX, mouseY, mouseButton)) {
			this.nameField.setFocused(true);
			ret = true;
		}
		return ret;
	}
}
