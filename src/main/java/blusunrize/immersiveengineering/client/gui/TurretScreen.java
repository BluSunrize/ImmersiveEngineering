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
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonCheckbox;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE;
import blusunrize.immersiveengineering.client.gui.elements.GuiReactiveList;
import blusunrize.immersiveengineering.common.blocks.metal.TurretTileEntity;
import blusunrize.immersiveengineering.common.gui.TurretContainer;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public abstract class TurretScreen extends IEContainerScreen<TurretContainer>
{
	protected static final ResourceLocation TEXTURE = makeTextureLocation("turret");

	public TurretTileEntity tile;
	private TextFieldWidget nameField;

	public TurretScreen(TurretContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title);
		this.tile = container.tile;
		this.ySize = 190;
	}

	@Override
	public void init()
	{
		super.init();
		mc().keyboardListener.enableRepeatEvents(true);
		this.nameField = new TextFieldWidget(this.font, guiLeft+11, guiTop+88, 58, 12, StringTextComponent.EMPTY);
		this.nameField.setTextColor(-1);
		this.nameField.setDisabledTextColour(-1);
		this.nameField.setEnableBackgroundDrawing(false);
		this.nameField.setMaxStringLength(30);

		this.buttons.clear();
		this.addButton(new GuiReactiveList(this, guiLeft+10, guiTop+10, 60, 72,
				btn -> {
					GuiReactiveList list = (GuiReactiveList)btn;
					CompoundNBT tag = new CompoundNBT();
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
		this.addButton(new GuiButtonIE(guiLeft+74, guiTop+84, 24, 16, new TranslationTextComponent(Lib.GUI_CONFIG+"turret.add"), TEXTURE, 176, 65,
				btn -> {
					CompoundNBT tag = new CompoundNBT();
					int listOffset = -1;
					String name = nameField.getText();
					if(!tile.targetList.contains(name))
					{
						listOffset = ((GuiReactiveList)buttons.get(0)).getMaxOffset();
						tag.putString("add", name);
						tile.targetList.add(name);
					}
					nameField.setText("");
					handleButtonClick(tag, listOffset);
				}));
		this.addButton(new GuiButtonCheckbox(guiLeft+74, guiTop+10, I18n.format(Lib.GUI_CONFIG+"turret.blacklist"), !tile.whitelist,
				btn -> {
					CompoundNBT tag = new CompoundNBT();
					int listOffset = -1;
					tile.whitelist = btn.getState();
					tag.putBoolean("whitelist", tile.whitelist);
					handleButtonClick(tag, listOffset);
				}));
		this.addButton(new GuiButtonCheckbox(guiLeft+74, guiTop+26, I18n.format(Lib.GUI_CONFIG+"turret.animals"), tile.attackAnimals,
				btn -> {
					CompoundNBT tag = new CompoundNBT();
					int listOffset = -1;
					tile.attackAnimals = !btn.getState();
					tag.putBoolean("attackAnimals", tile.attackAnimals);
					handleButtonClick(tag, listOffset);
				}));
		this.addButton(new GuiButtonCheckbox(guiLeft+74, guiTop+42, I18n.format(Lib.GUI_CONFIG+"turret.players"), tile.attackPlayers,
				btn -> {
					CompoundNBT tag = new CompoundNBT();
					int listOffset = -1;
					tile.attackPlayers = !btn.getState();
					tag.putBoolean("attackPlayers", tile.attackPlayers);
					handleButtonClick(tag, listOffset);
				}));
		this.addButton(new GuiButtonCheckbox(guiLeft+74, guiTop+58, I18n.format(Lib.GUI_CONFIG+"turret.neutrals"), tile.attackNeutrals,
				btn -> {
					CompoundNBT tag = new CompoundNBT();
					int listOffset = -1;
					tile.attackNeutrals = !btn.getState();
					tag.putBoolean("attackNeutrals", tile.attackNeutrals);
					handleButtonClick(tag, listOffset);
				}));

		addCustomButtons();
	}

	protected abstract void addCustomButtons();

	protected void handleButtonClick(CompoundNBT nbt, int listOffset)
	{
		if(!nbt.isEmpty())
		{
			ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile, nbt));
			this.init();
			if(listOffset >= 0)
				((GuiReactiveList)this.buttons.get(0)).setOffset(listOffset);
		}
	}

	protected abstract void renderCustom(MatrixStack transform, List<ITextComponent> tooltipOut, int mx, int my);

	@Override
	public void render(MatrixStack transform, int mx, int my, float partial)
	{
		super.render(transform, mx, my, partial);
		this.nameField.render(transform, mx, my, partial);

		ArrayList<ITextComponent> tooltip = new ArrayList<>();
		if(mx >= guiLeft+158&&mx < guiLeft+165&&my >= guiTop+16&&my < guiTop+62)
			tooltip.add(new StringTextComponent(tile.getEnergyStored(null)+"/"+tile.getMaxEnergyStored(null)+" IF"));

		renderCustom(transform, tooltip, mx, my);
		if(!tooltip.isEmpty())
			GuiUtils.drawHoveringText(transform, tooltip, mx, my, width, height, -1, font);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack transform, float f, int mx, int my)
	{
		ClientUtils.bindTexture(TEXTURE);
		this.blit(transform, guiLeft, guiTop, 0, 0, xSize, ySize);

		int stored = (int)(46*(tile.getEnergyStored(null)/(float)tile.getMaxEnergyStored(null)));
		fillGradient(transform, guiLeft+158, guiTop+16+(46-stored), guiLeft+165, guiTop+62, 0xffb51500, 0xff600b00);
	}

	@Override
	public void onClose()
	{
		super.onClose();
		mc().keyboardListener.enableRepeatEvents(false);
	}

	@Override
	public boolean keyPressed(int key, int scancode, int p_keyPressed_3_)
	{
		if(this.nameField.isFocused())
		{
			if(key==GLFW.GLFW_KEY_ENTER)
			{
				String name = this.nameField.getText();
				if(!tile.targetList.contains(name))
				{
					CompoundNBT tag = new CompoundNBT();
					tag.putString("add", name);
					tile.targetList.add(name);
					ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile, tag));

					this.init();
					((GuiReactiveList)this.buttons.get(0)).setOffset(((GuiReactiveList)this.buttons.get(0)).getMaxOffset());
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
