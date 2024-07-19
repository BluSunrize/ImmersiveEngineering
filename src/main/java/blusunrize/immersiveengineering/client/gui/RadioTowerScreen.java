/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE;
import blusunrize.immersiveengineering.client.gui.elements.ITooltipWidget;
import blusunrize.immersiveengineering.client.gui.info.EnergyInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import blusunrize.immersiveengineering.client.gui.info.TooltipArea;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.RadioTowerLogic;
import blusunrize.immersiveengineering.common.gui.RadioTowerMenu;
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.util.List;
import java.util.function.Consumer;

public class RadioTowerScreen extends IEContainerScreen<RadioTowerMenu>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("radio_tower");

	public RadioTowerScreen(RadioTowerMenu container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
		this.imageWidth = 240;
		this.imageHeight = 150;
		this.inventoryLabelY = 116;
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		return ImmutableList.of(
				new EnergyInfoArea(leftPos+221, topPos+10, menu.energy),
				new TooltipArea(
						new Rect2i(leftPos+78, topPos+40, 64, 16),
						() -> Component.translatable(Lib.GUI_CONFIG+"radio_tower.frequency")
				),
				new TooltipArea(
						new Rect2i(leftPos+175, topPos+126, 55, 16),
						() -> Component.translatable(Lib.GUI_CONFIG+"radio_tower.range")
				)
		);
	}

	@Override
	protected void gatherAdditionalTooltips(int mouseX, int mouseY, Consumer<Component> addLine, Consumer<Component> addGray)
	{
		super.gatherAdditionalTooltips(mouseX, mouseY, addLine, addGray);
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull GuiGraphics graphics, float f, int mx, int my)
	{
		Font font = getMinecraft().font;

		// display range string
		graphics.drawCenteredString(
				font, I18n.get(Lib.GUI_CONFIG+"radio_tower.range_m", Utils.formatDouble(menu.range.get()*16, "##,###")),
				getGuiLeft()+202, getGuiTop()+130, 0xffffff
		);

		// display nearby towers
		final int centerX = getGuiLeft()+202;
		final int centerY = getGuiTop()+96;
		final int radius = 25;
		for(Vec3 pos : this.menu.otherComponents.get().positions())
		{
			int xx = centerX+(int)Math.ceil(pos.x*radius);
			int yy = centerY+(int)Math.ceil(pos.y*radius);
			graphics.fill(xx, yy, xx+1, yy+1, 0xffffffff);
		}
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
	{
		graphics.drawString(font, I18n.get(Lib.GUI_CONFIG+"radio_tower.saved_frequencies"), 14, 61, 0x2d1a00, false);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double distX, double distY)
	{
		if(this.getFocused()!=null&&this.isDragging()&&button==0)
			return this.getFocused().mouseDragged(mouseX, mouseY, button, distX, distY);
		return super.mouseDragged(mouseX, mouseY, button, distX, distY);
	}

	@Override
	public void init()
	{
		super.init();

		this.clearWidgets();

		this.addRenderableWidget(new FrequencySlider(
				getGuiLeft()+8, getGuiTop()+8,
				RadioTowerLogic.FREQUENCY_MIN, RadioTowerLogic.FREQUENCY_MAX, this.menu.frequency,
				frq -> {
					CompoundTag message = new CompoundTag();
					message.putInt("frequency", frq);
					sendUpdateToServer(message);
				}
		));

		for(final DyeColor color : DyeColor.values())
		{
			final int ordinal = color.ordinal();
			this.addRenderableWidget(new SaveButton(
					getGuiLeft()+12+(ordinal%8)*20,
					getGuiTop()+78+(ordinal/8)*22,
					color,
					button -> {

					}
			));
		}
	}


	private static class FrequencySlider extends AbstractWidget
	{
		private final int minValue;
		private final int maxValue;
		private final GetterAndSetter<Integer> value;
		private final Consumer<Integer> sendToServer;

		public FrequencySlider(int x, int y, int min, int max, GetterAndSetter<Integer> value, Consumer<Integer> sendToServer)
		{
			super(x, y, 204, 48, Component.empty());
			this.minValue = min;
			this.maxValue = max;
			this.value = value;
			this.sendToServer = sendToServer;
		}

		private int getInnerX()
		{
			return getX()+12;
		}

		private int getInnerWidth()
		{
			return getWidth()-24;
		}


		private int mouseToValue(double mouseX)
		{
			double relativeValue = (mouseX-getInnerX())/getInnerWidth();
			return (int)Math.round(minValue+relativeValue*(maxValue-minValue));
		}

		private int valueToOffset(int value)
		{
			return (int)Math.round((value-minValue)/(double)(maxValue-minValue)*getInnerWidth());
		}

		private void setValue(int newValue)
		{
			newValue = Mth.clamp(newValue, minValue, maxValue);
			this.value.set(newValue);
			this.sendToServer.accept(newValue);
		}

		private static final DecimalFormat FRQ_FORMAT = new DecimalFormat("###");

		@Override
		protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
		{
			Minecraft minecraft = Minecraft.getInstance();
			graphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.enableDepthTest();

			// render background
			graphics.blit(RadioTowerScreen.TEXTURE, getX(), getY(), 0, 150, width, height);
			if(this.isHoveredOrFocused())
				graphics.blit(RadioTowerScreen.TEXTURE, getX(), getY(), 0, 150+height, width, 32);

			// render cursor
			int cursorU = isHoveredOrFocused()?243: 240;
			graphics.blit(RadioTowerScreen.TEXTURE, getInnerX()+valueToOffset(value.get())-1, getY()+19, cursorU, 0, 3, 6);
			graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
			for(int step = minValue; step <= maxValue; step += 64)
			{
				int offset = valueToOffset(step);
				graphics.drawCenteredString(
						minecraft.font, FRQ_FORMAT.format(step),
						getInnerX()+offset, getY()+4, 0xffffff
				);
				graphics.fill(getInnerX()+offset, getY()+minecraft.font.lineHeight+4, getInnerX()+offset+1, getY()+minecraft.font.lineHeight+8, 0xffffffff);
			}
			graphics.drawCenteredString(
					minecraft.font, I18n.get(Lib.GUI_CONFIG+"radio_tower.khz", value.get()),
					getX()+100, getY()+34, 0xffffff
			);
		}

		@Override
		protected void onDrag(double mouseX, double mouseY, double p_93593_, double p_93594_)
		{
			setValue(mouseToValue(mouseX));
		}

		@Override
		public void onClick(double mouseX, double mouseY, int button)
		{
			setValue(mouseToValue(mouseX));
		}

		@Override
		public boolean mouseScrolled(double mouseX, double mouseY, double scroll1, double scroll2)
		{
			if(this.isHovered())
			{
				setValue(this.value.get()+(scroll2 > 0?1: -1));
				return true;
			}
			return false;
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput graphics)
		{
		}
	}

	private static class SaveButton extends GuiButtonIE implements ITooltipWidget
	{
		private final DyeColor color;

		public SaveButton(int x, int y, DyeColor color, IIEPressable handler)
		{
			super(x, y, 17, 17, Component.empty(), TEXTURE, 110, 239, handler);
			this.color = color;
			this.setHoverOffset(width, 0);
		}

		@Override
		public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
		{
			super.renderWidget(graphics, mouseX, mouseY, partialTicks);
			float[] rgb = this.color.getTextureDiffuseColors();
			graphics.setColor(rgb[0], rgb[1], rgb[2], 1);
			graphics.blit(texture, getX(), getY(), texU+width*2, texV, width, height);
			graphics.setColor(1, 1, 1, 1);
		}

		@Override
		public void gatherTooltip(int mouseX, int mouseY, List<Component> tooltip)
		{
			tooltip.add(Component.translatable("color.minecraft."+this.color.getName()));
			tooltip.add(Component.translatable(Lib.GUI_CONFIG+"radio_tower.khz").withStyle(ChatFormatting.GRAY));
			tooltip.add(Component.translatable(Lib.GUI_CONFIG+"radio_tower.save").withStyle(ChatFormatting.DARK_GRAY));
			tooltip.add(Component.translatable(Lib.GUI_CONFIG+"radio_tower.load").withStyle(ChatFormatting.DARK_GRAY));
		}
	}

}
