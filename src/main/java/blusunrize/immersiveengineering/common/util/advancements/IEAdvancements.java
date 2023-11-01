/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.advancements;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockAdvancementTrigger;
import net.minecraft.advancements.CriteriaTriggers;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

/**
 * @author BluSunrize - 04.07.2017
 */
public class IEAdvancements
{
	public static MultiblockAdvancementTrigger TRIGGER_MULTIBLOCK = new MultiblockAdvancementTrigger();

	public static void preInit()
	{
		TRIGGER_MULTIBLOCK = CriteriaTriggers.register(MODID+":"+"multiblock", TRIGGER_MULTIBLOCK);
	}
}
