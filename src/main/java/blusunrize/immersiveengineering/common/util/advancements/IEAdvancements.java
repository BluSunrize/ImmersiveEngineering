/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.advancements;

import net.minecraft.advancements.CriteriaTriggers;

/**
 * @author BluSunrize - 04.07.2017
 */
public class IEAdvancements
{
	public static MultiblockTrigger TRIGGER_MULTIBLOCK = new MultiblockTrigger();

	public static void preInit()
	{
		TRIGGER_MULTIBLOCK = CriteriaTriggers.register(TRIGGER_MULTIBLOCK);
	}
}
