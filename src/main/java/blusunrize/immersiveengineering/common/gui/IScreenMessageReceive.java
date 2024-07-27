/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import net.minecraft.nbt.CompoundTag;

public interface IScreenMessageReceive
{
	default void receiveMessageFromScreen(CompoundTag nbt)
	{
	}
}
