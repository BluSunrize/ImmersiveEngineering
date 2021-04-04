/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import blusunrize.immersiveengineering.api.tool.LogicCircuitHandler.LogicCircuitRegister;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.item.DyeColor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

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
}
