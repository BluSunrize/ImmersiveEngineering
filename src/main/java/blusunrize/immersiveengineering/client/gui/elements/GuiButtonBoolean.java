/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import net.minecraft.util.text.ITextComponent;

public class GuiButtonBoolean extends GuiButtonState<Boolean>
{
	public GuiButtonBoolean(int x, int y, int w, int h, String name, boolean state, String texture, int u, int v,
							int offsetDir, IIEPressable<GuiButtonState<Boolean>> handler)
	{
		super(x, y, w, h, ITextComponent.func_241827_a_(name), new Boolean[]{false, true}, state?1: 0, texture, u, v, offsetDir, handler);
	}
}