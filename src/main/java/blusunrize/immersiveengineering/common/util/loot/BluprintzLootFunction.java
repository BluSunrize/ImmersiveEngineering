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
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nonnull;

public class BluprintzLootFunction extends LootItemConditionalFunction
{
	public static final ResourceLocation ID = new ResourceLocation(ImmersiveEngineering.MODID, "secret_bluprintz");

	protected BluprintzLootFunction(LootItemCondition[] conditionsIn)
	{
		super(conditionsIn);
	}

	@Nonnull
	@Override
	public ItemStack run(ItemStack stack, @Nonnull LootContext context)
	{
		stack.setHoverName(new TextComponent("Super Special BluPrintz"));
		ItemNBTHelper.setLore(stack, new TextComponent("Congratulations!"), new TextComponent("You have found an easter egg!"));
		return stack;
	}

	@Nonnull
	@Override
	public LootItemFunctionType getType()
	{
		return IELootFunctions.bluprintz;
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<BluprintzLootFunction>
	{
		@Override
		@Nonnull
		public BluprintzLootFunction deserialize(@Nonnull JsonObject object, @Nonnull JsonDeserializationContext deserializationContext, @Nonnull LootItemCondition[] conditionsIn)
		{
			return new BluprintzLootFunction(conditionsIn);
		}
	}

	public static class Builder extends LootItemConditionalFunction.Builder<blusunrize.immersiveengineering.common.util.loot.BluprintzLootFunction.Builder>
	{
		@Nonnull
		@Override
		protected blusunrize.immersiveengineering.common.util.loot.BluprintzLootFunction.Builder getThis()
		{
			return this;
		}

		@Nonnull
		@Override
		public LootItemFunction build()
		{
			return new BluprintzLootFunction(getConditions());
		}
	}

	public static blusunrize.immersiveengineering.common.util.loot.BluprintzLootFunction.Builder builder()
	{
		return new blusunrize.immersiveengineering.common.util.loot.BluprintzLootFunction.Builder();
	}
}
