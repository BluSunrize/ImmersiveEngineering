/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import blusunrize.immersiveengineering.api.tool.LogicCircuitHandler.LogicCircuitRegister;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.item.DyeColor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

public class GuiButtonLogicCircuitRegister extends GuiButtonState<LogicCircuitRegister>
{
	public GuiButtonLogicCircuitRegister(int x, int y, ITextComponent name, IIEPressable<GuiButtonState<LogicCircuitRegister>> handler)
	{
		super(x, y, 18, 18, name, LogicCircuitRegister.values(), 0, "immersiveengineering:textures/gui/circuit_table.png", 234, 0, -1, handler);
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
			SPLIT_BY_INITIAL.get(dye.name().charAt(0)).add(dye.getId());
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if(keyCode >= 48&&keyCode <= 55) // digits 0-8
		{
			this.setStateByInt(keyCode-48+16); // keycode to zero, plus 16 colors
			this.onPress.onPress(this);
			return true;
		}
		else if(keyCode >= 65&&keyCode <= 90) // A-Z
		{
			List<Integer> options = SPLIT_BY_INITIAL.get((char)keyCode);
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
