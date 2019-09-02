/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.items.ItemRevolver;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * @author BluSunrize - 16.08.2018
 */
public class IELootFunctions
{
	public static void preInit()
	{
		LootFunctionManager.registerFunction(new Bluprintz.Serializer());
		LootFunctionManager.registerFunction(new Revolver.Serializer());
	}

	public static class Bluprintz extends LootFunction
	{
		protected Bluprintz(LootCondition[] conditionsIn)
		{
			super(conditionsIn);
		}

		@Override
		public ItemStack apply(ItemStack stack, Random rand, LootContext context)
		{
			stack.setStackDisplayName("Super Special BluPrintz");
			ItemNBTHelper.setLore(stack, "Congratulations!", "You have found an easter egg!");
			return stack;
		}

		public static class Serializer extends LootFunction.Serializer<Bluprintz>
		{
			protected Serializer()
			{
				super(new ResourceLocation(ImmersiveEngineering.MODID, "secret_bluprintz"), Bluprintz.class);
			}

			@Override
			public void serialize(@Nonnull JsonObject object, @Nonnull Bluprintz functionClazz, @Nonnull JsonSerializationContext serializationContext)
			{
			}

			@Override
			@Nonnull
			public Bluprintz deserialize(@Nonnull JsonObject object, @Nonnull JsonDeserializationContext deserializationContext, @Nonnull LootCondition[] conditionsIn)
			{
				return new Bluprintz(conditionsIn);
			}

		}
	}

	public static class Revolver extends LootFunction
	{
		protected Revolver(LootCondition[] conditionsIn)
		{
			super(conditionsIn);
		}

		@Override
		public ItemStack apply(ItemStack stack, Random rand, LootContext context)
		{
			ItemNBTHelper.setTagCompound(stack, "perks", ItemRevolver.RevolverPerk.generatePerkSet(rand, context.getLuck()));
			return stack;
		}

		public static class Serializer extends LootFunction.Serializer<Revolver>
		{
			protected Serializer()
			{
				super(new ResourceLocation(ImmersiveEngineering.MODID, "revolver_perks"), Revolver.class);
			}

			@Override
			public void serialize(@Nonnull JsonObject object, @Nonnull Revolver functionClazz, @Nonnull JsonSerializationContext serializationContext)
			{
			}

			@Override
			@Nonnull
			public Revolver deserialize(@Nonnull JsonObject object, @Nonnull JsonDeserializationContext deserializationContext, @Nonnull LootCondition[] conditionsIn)
			{
				return new Revolver(conditionsIn);
			}

		}
	}
}
