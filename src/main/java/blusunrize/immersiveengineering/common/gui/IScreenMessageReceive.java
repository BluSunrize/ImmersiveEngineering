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
	// TODO some nicer system based on stream codecs?
	default void receiveMessageFromScreen(CompoundTag nbt)
	{
	}
}
