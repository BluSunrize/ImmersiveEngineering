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
