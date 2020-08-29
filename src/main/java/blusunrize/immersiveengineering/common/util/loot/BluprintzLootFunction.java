/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util.loot;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootFunction;
import net.minecraft.world.storage.loot.conditions.ILootCondition;
import net.minecraft.world.storage.loot.functions.ILootFunction;

import javax.annotation.Nonnull;

public class BluprintzLootFunction extends LootFunction
{
	protected BluprintzLootFunction(ILootCondition[] conditionsIn)
	{
		super(conditionsIn);
	}

	@Nonnull
	@Override
	public ItemStack doApply(ItemStack stack, @Nonnull LootContext context)
	{
		stack.setDisplayName(new StringTextComponent("Super Special BluPrintz"));
		ItemNBTHelper.setLore(stack, new StringTextComponent("Congratulations!"), new StringTextComponent("You have found an easter egg!"));
		return stack;
	}

	public static class Serializer extends LootFunction.Serializer<BluprintzLootFunction>
	{
		protected Serializer()
		{
			super(new ResourceLocation(ImmersiveEngineering.MODID, "secret_bluprintz"), BluprintzLootFunction.class);
		}

		@Override
		public void serialize(@Nonnull JsonObject object, @Nonnull BluprintzLootFunction functionClazz, @Nonnull JsonSerializationContext serializationContext)
		{
			super.serialize(object, functionClazz, serializationContext);
		}

		@Override
		@Nonnull
		public BluprintzLootFunction deserialize(@Nonnull JsonObject object, @Nonnull JsonDeserializationContext deserializationContext, @Nonnull ILootCondition[] conditionsIn)
		{
			return new BluprintzLootFunction(conditionsIn);
		}
	}

	public static class Builder extends LootFunction.Builder<Builder>
	{
		@Nonnull
		@Override
		protected Builder doCast()
		{
			return this;
		}

		@Nonnull
		@Override
		public ILootFunction build()
		{
			return new BluprintzLootFunction(getConditions());
		}
	}

	public static Builder builder()
	{
		return new Builder();
	}
}
