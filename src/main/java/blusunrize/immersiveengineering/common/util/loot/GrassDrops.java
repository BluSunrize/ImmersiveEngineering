/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.loot;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import javax.annotation.Nonnull;
import java.util.List;

@EventBusSubscriber(modid = Lib.MODID, bus = Bus.MOD)
public class GrassDrops
{
	@SubscribeEvent
	public static void registerModifiers(RegistryEvent.Register<GlobalLootModifierSerializer<?>> ev)
	{
		ev.getRegistry().register(
				new GrassDropSerializer().setRegistryName(Lib.MODID, "hemp_seed_drops")
		);
	}

	public static class GrassDropSerializer extends GlobalLootModifierSerializer<GrassDropModifier>
	{

		@Override
		public GrassDropModifier read(ResourceLocation location, JsonObject object, LootItemCondition[] ailootcondition)
		{
			return new GrassDropModifier(ailootcondition);
		}

		@Override
		public JsonObject write(GrassDropModifier instance)
		{
			return new JsonObject();
		}
	}

	private static class GrassDropModifier extends LootModifier
	{
		protected GrassDropModifier(LootItemCondition[] conditionsIn)
		{
			super(conditionsIn);
		}

		@Nonnull
		@Override
		protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context)
		{
			generatedLoot.add(new ItemStack(Misc.hempSeeds));
			return generatedLoot;
		}
	}
}
