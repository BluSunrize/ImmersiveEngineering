/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class ExternalHeaterHandler
{
	//These are set on IE loading
	public static int defaultFurnaceEnergyCost;
	public static int defaultFurnaceSpeedupCost;

	@CapabilityInject(IExternalHeatable.class)
	public static Capability<IExternalHeatable> CAPABILITY;

	/**
	 * Expose this interface on the null side to allow the external heater to work with your block entity
	 */
	public interface IExternalHeatable
	{
		/**
		 * Called each tick<br>
		 * Handle fueling as well as possible smelting speed increases here
		 *
		 * @param energyAvailable the amount of RF the furnace heater has stored and can supply
		 * @param redstone        whether a redstone signal is applied to the furnace heater. To keep the target warm, but not do speed increases
		 * @return the amount of RF consumed that tick. Should be lower or equal to "energyAvailable", obviously
		 */
		int doHeatTick(int energyAvailable, boolean redstone);
	}
}