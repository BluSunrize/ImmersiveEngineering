/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import net.minecraft.network.chat.Component;

import java.util.List;

public interface ITooltipWidget
{
	void gatherTooltip(int mouseX, int mouseY, List<Component> tooltip);
}
