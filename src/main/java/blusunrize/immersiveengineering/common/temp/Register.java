package blusunrize.immersiveengineering.common.temp;

import blusunrize.immersiveengineering.api.Lib;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = Lib.MODID, bus = Bus.MOD)
public class Register
{
	public static Block block;
	public static TileEntityType<?> type;

	@SubscribeEvent
	public static void blocks(RegistryEvent.Register<Block> ev)
	{
		block = new ConnectorBlock();
		block.setRegistryName(Lib.MODID, "test_connector");
		ev.getRegistry().register(block);
	}

	@SubscribeEvent
	public static void tiles(RegistryEvent.Register<TileEntityType<?>> ev)
	{
		type = new TileEntityType<>(() -> new ConnectorTile(type), ImmutableSet.of(block), null);
		type.setRegistryName(Lib.MODID, "test_connector");
		ev.getRegistry().register(type);
	}

	@SubscribeEvent
	public static void items(RegistryEvent.Register<Item> ev)
	{
		Item item = new BlockItem(block, new Item.Properties());
		item.setRegistryName(Lib.MODID, "test_connector");
		ev.getRegistry().register(item);
	}
}
