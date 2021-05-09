/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import blusunrize.immersiveengineering.api.tool.LogicCircuitHandler.LogicCircuitRegister;
import blusunrize.immersiveengineering.client.gui.IEContainerScreen;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;
import java.util.Locale;

public class GuiButtonLogicCircuitRegister extends GuiButtonState<LogicCircuitRegister>
{
	private static final ResourceLocation TEXTURE = IEContainerScreen.makeTextureLocation("circuit_table");

	public GuiButtonLogicCircuitRegister(int x, int y, ITextComponent name, IIEPressable<GuiButtonState<LogicCircuitRegister>> handler)
	{
		super(x, y, 18, 18, name, LogicCircuitRegister.values(), 0, TEXTURE, 234, 0, -1, handler);
		this.textOffset = new int[]{3, 5};
	}

	@Override
	public ITextComponent getMessage()
	{
		LogicCircuitRegister state = getState();
		if(state.ordinal() >= 16)
			return new StringTextComponent(state.name());
		return StringTextComponent.EMPTY;
	}

	@Override
	public void render(MatrixStack transform, int mouseX, int mouseY, float partialTicks)
	{
		super.render(transform, mouseX, mouseY, partialTicks);
		if(this.visible)
		{
			LogicCircuitRegister state = getState();
			if(state.ordinal() < 16)
			{
				DyeColor dye = DyeColor.byId(state.ordinal());
				int col = 0xff000000|dye.getColorValue();
				this.fillGradient(transform, x+3, y+3, x+15, y+15, col, col);
			}
		}
	}

	private static final ListMultimap<Character, Integer> SPLIT_BY_INITIAL = ArrayListMultimap.create(11, 2);

	static
	{
		for(DyeColor dye : DyeColor.values())
		{
			String transl = I18n.format("color.minecraft."+dye.getTranslationKey()).toLowerCase(Locale.ROOT);
			SPLIT_BY_INITIAL.get(transl.charAt(0)).add(dye.getId());
		}
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers)
	{
		if(Character.isDigit(codePoint))
		{
			int number = Character.digit(codePoint, 10);
			if(number>=0 && number<8)
			{
				this.setStateByInt(number+16); // plus 16 colors
				this.onPress.onPress(this);
				return true;
			}
		}
		else if(Character.isAlphabetic(codePoint))
		{
			List<Integer> options = SPLIT_BY_INITIAL.get(codePoint);
			if(!options.isEmpty())
			{
				int next = (options.indexOf(this.getStateAsInt())+1)%options.size();
				this.setStateByInt(options.get(next));
				this.onPress.onPress(this);
				return true;
			}
		}
		return false;
	}
}
