/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import blusunrize.immersiveengineering.common.items.IEBaseItem;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.register.IEItems.SpawnEggs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.Row;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.Holder;

@EventBusSubscriber(modid = Lib.MODID, bus = Bus.MOD)
public class IECreativeTabs
{
	public static final DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister.create(
			Registries.CREATIVE_MODE_TAB, Lib.MODID
	);

	private static Holder<CreativeModeTab> TAB = REGISTER.register(
			"main",
			// TODO what am I supposed to do with the row/col args
			() -> new CreativeModeTab.Builder(Row.TOP, 0)
					.icon(() -> IEItems.Misc.WIRE_COILS.get(WireType.COPPER).get().getDefaultInstance())
					.title(Component.literal(ImmersiveEngineering.MODNAME))
					.displayItems(IECreativeTabs::fillIETab)
					.build()
	);

	@SubscribeEvent
	public static void fillTab(BuildCreativeModeTabContentsEvent ev)
	{
		if(ev.getTabKey()==CreativeModeTabs.FOOD_AND_DRINKS)
			Misc.POTION_BUCKET.get().fillCreativeTab(ev);
		if(ev.getTabKey()==CreativeModeTabs.SPAWN_EGGS)
		{
			ev.accept(SpawnEggs.EGG_FUSILIER.get());
			ev.accept(SpawnEggs.EGG_COMMANDO.get());
			ev.accept(SpawnEggs.EGG_BULWARK.get());
		}
	}

	private static void fillIETab(CreativeModeTab.ItemDisplayParameters parms, CreativeModeTab.Output out)
	{
		for(final Holder<Item> itemRef : IEItems.REGISTER.getEntries())
		{
			final Item item = itemRef.value();
			if(item==Misc.POTION_BUCKET.get())
				continue;
			if(item instanceof IEBaseItem ieItem)
				ieItem.fillCreativeTab(out);
			else if(item instanceof BlockItem blockItem&&blockItem.getBlock() instanceof IEBaseBlock ieBlock)
				ieBlock.fillCreativeTab(out);
			else
				out.accept(itemRef.value());
		}
	}
}
