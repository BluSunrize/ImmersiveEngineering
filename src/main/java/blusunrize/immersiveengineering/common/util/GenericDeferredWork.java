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
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
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

	public static void registerPotablePlant(ResourceLocation flower, Block fullPot)
	{
		enqueue(() -> ((FlowerPotBlock)Blocks.FLOWER_POT).addPlant(flower, () -> fullPot));
	}

	public static void registerDispenseBehavior(IItemProvider item, IDispenseItemBehavior behavior)
	{
		enqueue(() -> DispenserBlock.registerDispenseBehavior(item, behavior));
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
