/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import net.minecraft.util.text.ITextComponent;

import java.util.List;

public interface ITooltipWidget
{
	void gatherTooltip(int mouseX, int mouseY, List<ITextComponent> tooltip);
}
