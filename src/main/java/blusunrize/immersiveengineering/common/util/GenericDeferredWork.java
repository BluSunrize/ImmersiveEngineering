/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.api.Lib;
import com.google.common.base.Preconditions;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Lib.MODID, bus = Bus.MOD)
public class GenericDeferredWork
{
	private static final List<Runnable> TO_RUN_ON_THREAD = new ArrayList<>();
	private static boolean done = false;

	/**
	 * Use this to enqueue work during the loading process when no event is available to call enqueueWork on, and the
	 * exact timing of the Runnable is not relevant
	 */
	public static void enqueue(Runnable toRun)
	{
		Preconditions.checkState(!done);
		TO_RUN_ON_THREAD.add(toRun);
	}

	public static void registerDispenseBehavior(ItemLike item, DispenseItemBehavior behavior)
	{
		enqueue(() -> DispenserBlock.registerBehavior(item, behavior));
	}

	@SubscribeEvent
	public static void loadComplete(FMLLoadCompleteEvent ev)
	{
		ev.enqueueWork(() -> {
			TO_RUN_ON_THREAD.forEach(Runnable::run);
			done = true;
		});
	}
}
