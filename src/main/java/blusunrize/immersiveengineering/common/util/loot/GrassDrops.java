/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.loot;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries.Keys;

import javax.annotation.Nonnull;

public class GrassDrops
{
	public static void init()
	{
		DeferredRegister<GlobalLootModifierSerializer<?>> register = DeferredRegister.create(
				Keys.LOOT_MODIFIER_SERIALIZERS, ImmersiveEngineering.MODID
		);
		register.register(FMLJavaModLoadingContext.get().getModEventBus());
		register.register("hemp_seed_drops", GrassDropSerializer::new);
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
		protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context)
		{
			generatedLoot.add(new ItemStack(Misc.HEMP_SEEDS));
			return generatedLoot;
		}
	}
}
