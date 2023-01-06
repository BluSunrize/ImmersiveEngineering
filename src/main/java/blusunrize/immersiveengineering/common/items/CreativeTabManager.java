/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import blusunrize.immersiveengineering.common.register.IEItems;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = Lib.MODID, bus = Bus.MOD)
public class CreativeTabManager
{
	private static CreativeModeTab TAB;

	@SubscribeEvent
	public static void registerTab(CreativeModeTabEvent.Register ev)
	{
		TAB = ev.registerCreativeModeTab(
				ImmersiveEngineering.rl("main"),
				b -> b.icon(() -> IEItems.Misc.WIRE_COILS.get(WireType.COPPER).get().getDefaultInstance())
		);
	}

	@SubscribeEvent
	public static void fillTab(CreativeModeTabEvent.BuildContents ev)
	{
		if(ev.getTab()==CreativeModeTabs.FOOD_AND_DRINKS)
			Misc.POTION_BUCKET.get().fillCreativeTab(ev);
		if(ev.getTab()!=TAB)
			return;
		for(final var itemRef : IEItems.REGISTER.getEntries())
		{
			final var item = itemRef.get();
			if(item==Misc.POTION_BUCKET.get())
				continue;
			if(item instanceof IEBaseItem ieItem)
				ieItem.fillCreativeTab(ev);
			else if(item instanceof BlockItem blockItem&&blockItem.getBlock() instanceof IEBaseBlock ieBlock)
				ieBlock.fillCreativeTab(ev);
			else
				ev.accept(itemRef);
		}
	}
}
