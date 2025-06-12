/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE.ButtonTexture;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE.IIEPressable;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class GuiButtonBoolean extends GuiButtonState<Boolean>
{
	public GuiButtonBoolean(
			int x, int y, int w, int h,
			Component name,
			Supplier<Boolean> state, ButtonTexture falseTex, ButtonTexture trueTex,
			IIEPressable<GuiButtonState<Boolean>> handler
	)
	{
		super(
				x, y, w, h, name,
				new Boolean[]{false, true}, () -> state.get()?1: 0, Map.of(false, falseTex, true, trueTex),
				handler
		);
	}

	public GuiButtonBoolean(
			int x, int y, int w, int h,
			Component name,
			Supplier<Boolean> state, ButtonTexture falseTex, ButtonTexture trueTex,
			IIEPressable<GuiButtonState<Boolean>> handler,
			BiConsumer<List<Component>, Boolean> tooltip
	)
	{
		super(
				x, y, w, h, name,
				new Boolean[]{false, true}, () -> state.get()?1: 0, Map.of(false, falseTex, true, trueTex),
				handler, tooltip
		);
	}
}