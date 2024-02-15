/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.gui.MachineInterfaceScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.IntSupplier;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class GuiButtonSelectBox<E> extends GuiButtonState<E>
{
	private final Function<E, Component> messageGetter;
	private boolean opened = false;
	private final int openedHeight;

	private int selectedState = -1;

	public GuiButtonSelectBox(
			int x, int y, String name, E[] options, IntSupplier selectedOption,
			Function<E, Component> messageGetter, IIEPressable<GuiButtonSelectBox<E>> handler
	)
	{
		super(x, y, 64, 16, Component.nullToEmpty(name), options, selectedOption,
				MachineInterfaceScreen.TEXTURE, 88, 186, -1, btn -> handler.onIEPress((GuiButtonSelectBox<E>)btn));
		this.messageGetter = messageGetter;
		// set width based on widest text
		this.width = 16+Arrays.stream(options).mapToInt(value -> mc().font.width(messageGetter.apply(value))).max().orElse(this.width);
		this.openedHeight = mc().font.lineHeight*options.length;
	}

	public int getClickedState()
	{
		return selectedState!=-1?selectedState: this.getStateAsInt();
	}


	@Override
	public Component getMessage()
	{
		return this.messageGetter.apply(this.getState());
	}

	@Override
	protected int getTextColor()
	{
		if(this.isHovered)
			return Lib.COLOUR_I_ImmersiveOrange;
		return 0x2C2C2C;
	}

	@Override
	public void onClick(double mouseX, double mouseY)
	{
		if(!opened)
		{
			this.opened = true;
			this.height = 16+openedHeight;
		}
		else
		{
			this.selectedState = getHighlightedIndex((int)mouseY);
			this.onPress.onPress(this);
			this.opened = false;
			this.height = 16;
		}
	}

	private int getHighlightedIndex(int mouseY)
	{
		int calc = (mouseY-getY()-8)/mc().font.lineHeight;
		return calc >= 0&&calc < states.length?calc: -1;
	}


	@Override
	public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();
		if(this.visible)
		{
			Font fontrenderer = mc.font;
			this.isHovered = mouseX >= this.getX()&&mouseY >= this.getY()&&mouseX < this.getX()+this.width&&mouseY < this.getY()+this.height;
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(770, 771, 1, 0);
			RenderSystem.blendFunc(770, 771);

			if(!this.opened)
			{
				// background
				graphics.blit(texture, getX(), getY(), texU, texV, 8, height);
				for(int i = 0; i < width-16; i += 2)
					graphics.blit(texture, getX()+8+i, getY(), texU+9, texV, 2, height);
				graphics.blit(texture, getX()+width-8, getY(), texU+12, texV, 8, height);
				// text
				Component text = getMessage();
				int textX = getX()+width/2-fontrenderer.width(text)/2;
				int textY = getY()+height/2-fontrenderer.lineHeight/2;
				graphics.drawString(fontrenderer, text, textX, textY, getTextColor(), false);
			}
			else
			{
				graphics.pose().pushPose();
				graphics.pose().translate(0, 0, 2);
				// background
				graphics.blit(texture, getX(), getY(), texU, texV, 8, 8);
				for(int i = 0; i < width-16; i += 2)
					graphics.blit(texture, getX()+8+i, getY(), texU+9, texV, 2, 8);
				graphics.blit(texture, getX()+width-8, getY(), texU+12, texV, 8, 8);

				for(int j = 0; j < openedHeight; j += 2)
				{
					graphics.blit(texture, getX(), getY()+8+j, texU, texV+17, 8, 2);
					for(int i = 0; i < width-16; i += 2)
						graphics.blit(texture, getX()+8+i, getY()+8+j, texU+9, texV+17, 2, 2);
					graphics.blit(texture, getX()+width-8, getY()+8+j, texU+12, texV+17, 8, 2);
				}

				graphics.blit(texture, getX(), getY()+8+openedHeight, texU, texV+8, 8, 8);
				for(int i = 0; i < width-16; i += 2)
					graphics.blit(texture, getX()+8+i, getY()+8+openedHeight, texU+9, texV+8, 2, 8);
				graphics.blit(texture, getX()+width-8, getY()+8+openedHeight, texU+12, texV+8, 8, 8);

				// text
				for(int j = 0; j < states.length; j++)
				{
					Component text = messageGetter.apply(states[j]);
					int textX = getX()+width/2-fontrenderer.width(text)/2;
					int textY = getY()+8+j*fontrenderer.lineHeight;
					boolean highlighted = isHovered&&getHighlightedIndex(mouseY)==j;
					graphics.drawString(fontrenderer, text, textX, textY, highlighted?Lib.COLOUR_I_ImmersiveOrange: 0x2C2C2C, false);
				}
				graphics.pose().popPose();
			}

		}
	}
}
