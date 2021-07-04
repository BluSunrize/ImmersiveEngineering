/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import net.minecraft.tileentity.TileEntity;

import java.util.HashMap;
import java.util.Map;

public class ExternalHeaterHandler
{
	//These are set on IE loading
	public static int defaultFurnaceEnergyCost;
	public static int defaultFurnaceSpeedupCost;

	//TODO cap?
	/**
	 * @author BluSunrize - 09.12.2015
	 * <p>
	 * An interface to be implemented by TileEntities that want to allow direct interaction with the external heater
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

	public static Map<Class<? extends TileEntity>, HeatableAdapter<?>> adapterMap = new HashMap<>();

	/**
	 * @author BluSunrize - 09.12.2015
	 * <p>
	 * An adapter to appyl to TileEntities that can't implement the IExternalHeatable interface
	 */
	public abstract static class HeatableAdapter<E extends TileEntity>
	{
		/**
		 * Called each tick<br>
		 * Handle fueling as well as possible smelting speed increases here
		 *
		 * @param energyAvailable the amount of RF the furnace heater has stored and can supply
		 * @param canHeat         whether a redstone signal is applied to the furnace heater. To keep the target warm, but not do speed increases
		 * @return the amount of RF consumed that tick. Should be lower or equal to "energyAvailable", obviously
		 */
		public abstract int doHeatTick(E tileEntity, int energyAvailable, boolean canHeat);
	}

	/**
	 * registers a HeatableAdapter to a TileEnttiy class. Should really only be used when implementing the interface is not an option
	 */
	public static <T extends TileEntity>
	void registerHeatableAdapter(Class<T> c, HeatableAdapter<? super T> adapter)
	{
		adapterMap.put(c, adapter);
	}

	/**
	 * @return a HeatableAdapter for the given TileEntity class
	 */
	public static <T extends TileEntity> HeatableAdapter<? super T> getHeatableAdapter(T tile)
	{
		Class<? extends TileEntity> c = tile.getClass();
		HeatableAdapter<?> adapter = null;
		while(adapter==null&&c!=TileEntity.class&&c.getSuperclass()!=TileEntity.class)
		{
			adapter = adapterMap.get(c);
			c = (Class<? extends TileEntity>)c.getSuperclass();
		}
		adapterMap.put(tile.getClass(), adapter);
		return (HeatableAdapter<? super T>)adapter;
	}

}