/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonCheckbox;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE;
import blusunrize.immersiveengineering.client.gui.elements.GuiReactiveList;
import blusunrize.immersiveengineering.client.gui.info.EnergyInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import blusunrize.immersiveengineering.common.blocks.metal.TurretBlockEntity;
import blusunrize.immersiveengineering.common.gui.TurretContainer;
import blusunrize.immersiveengineering.common.network.MessageBlockEntitySync;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.util.List;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public abstract class TurretScreen<T extends TurretBlockEntity<T>, C extends TurretContainer<T>> extends IEContainerScreen<C>
{
	protected static final ResourceLocation TEXTURE = makeTextureLocation("turret");

	public T tile;
	private EditBox nameField;

	public TurretScreen(C container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
		this.tile = container.tile;
		this.imageHeight = 190;
		this.inventoryLabelY = 99;
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		return ImmutableList.of(new EnergyInfoArea(leftPos+158, topPos+16, tile.energyStorage));
	}

	@Override
	public void init()
	{
		super.init();
		mc().keyboardHandler.setSendRepeatsToGui(true);
		this.nameField = new EditBox(this.font, leftPos+11, topPos+88, 58, 12, Component.empty());
		this.nameField.setTextColor(-1);
		this.nameField.setTextColorUneditable(-1);
		this.nameField.setBordered(false);
		this.nameField.setMaxLength(30);

		this.clearWidgets();
		this.addRenderableWidget(new GuiReactiveList(this, leftPos+10, topPos+10, 60, 72,
				btn -> {
					GuiReactiveList list = (GuiReactiveList)btn;
					CompoundTag tag = new CompoundTag();
					int listOffset = -1;
					int rem = list.selectedOption;
					if(rem >= 0&&tile.targetList.size() > 0)
					{
						tile.targetList.remove(rem);
						tag.putInt("remove", rem);
						listOffset = list.getOffset()-1;
						handleButtonClick(tag, listOffset);
					}
				}, tile.targetList.toArray(new String[0]))
				.setPadding(0, 0, 2, 2));
		this.addRenderableWidget(new GuiButtonIE(leftPos+74, topPos+84, 24, 16, Component.translatable(Lib.GUI_CONFIG+"turret.add"), TEXTURE, 176, 65,
				btn -> {
					CompoundTag tag = new CompoundTag();
					int listOffset = -1;
					String name = nameField.getValue();
					if(!tile.targetList.contains(name))
					{
						listOffset = ((GuiReactiveList)children().get(0)).getMaxOffset();
						tag.putString("add", name);
						tile.targetList.add(name);
					}
					nameField.setValue("");
					handleButtonClick(tag, listOffset);
				}));
		this.addRenderableWidget(new GuiButtonCheckbox(leftPos+74, topPos+10, I18n.get(Lib.GUI_CONFIG+"turret.blacklist"), () -> !tile.whitelist,
				btn -> {
					CompoundTag tag = new CompoundTag();
					int listOffset = -1;
					tile.whitelist = btn.getState();
					tag.putBoolean("whitelist", tile.whitelist);
					handleButtonClick(tag, listOffset);
				}));
		this.addRenderableWidget(new GuiButtonCheckbox(leftPos+74, topPos+26, I18n.get(Lib.GUI_CONFIG+"turret.animals"), () -> tile.attackAnimals,
				btn -> {
					CompoundTag tag = new CompoundTag();
					int listOffset = -1;
					tile.attackAnimals = !btn.getState();
					tag.putBoolean("attackAnimals", tile.attackAnimals);
					handleButtonClick(tag, listOffset);
				}));
		this.addRenderableWidget(new GuiButtonCheckbox(leftPos+74, topPos+42, I18n.get(Lib.GUI_CONFIG+"turret.players"), () -> tile.attackPlayers,
				btn -> {
					CompoundTag tag = new CompoundTag();
					int listOffset = -1;
					tile.attackPlayers = !btn.getState();
					tag.putBoolean("attackPlayers", tile.attackPlayers);
					handleButtonClick(tag, listOffset);
				}));
		this.addRenderableWidget(new GuiButtonCheckbox(leftPos+74, topPos+58, I18n.get(Lib.GUI_CONFIG+"turret.neutrals"), () -> tile.attackNeutrals,
				btn -> {
					CompoundTag tag = new CompoundTag();
					int listOffset = -1;
					tile.attackNeutrals = !btn.getState();
					tag.putBoolean("attackNeutrals", tile.attackNeutrals);
					handleButtonClick(tag, listOffset);
				}));

		addCustomButtons();
	}

	protected abstract void addCustomButtons();

	protected void handleButtonClick(CompoundTag nbt, int listOffset)
	{
		if(!nbt.isEmpty())
		{
			ImmersiveEngineering.packetHandler.sendToServer(new MessageBlockEntitySync(tile, nbt));
			this.init();
			if(listOffset >= 0)
				((GuiReactiveList)this.children().get(0)).setOffset(listOffset);
		}
	}

	@Override
	public void render(@Nonnull PoseStack transform, int mx, int my, float partial)
	{
		super.render(transform, mx, my, partial);
		this.nameField.render(transform, mx, my, partial);
	}

	@Override
	public void removed()
	{
		super.removed();
		mc().keyboardHandler.setSendRepeatsToGui(false);
	}

	@Override
	public boolean keyPressed(int key, int scancode, int p_keyPressed_3_)
	{
		if(this.nameField.isFocused())
		{
			if(key==GLFW.GLFW_KEY_ENTER)
			{
				String name = this.nameField.getValue();
				if(!tile.targetList.contains(name))
				{
					CompoundTag tag = new CompoundTag();
					tag.putString("add", name);
					tile.targetList.add(name);
					ImmersiveEngineering.packetHandler.sendToServer(new MessageBlockEntitySync(tile, tag));

					this.init();
					((GuiReactiveList)this.children().get(0)).setOffset(((GuiReactiveList)this.children().get(0)).getMaxOffset());
				}
			}
			else
				this.nameField.keyPressed(key, scancode, p_keyPressed_3_);
			return true;
		}
		else
			return super.keyPressed(key, scancode, p_keyPressed_3_);
	}

	@Override
	public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_)
	{
		return this.nameField.charTyped(p_charTyped_1_, p_charTyped_2_);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);
		return this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
	}
}
