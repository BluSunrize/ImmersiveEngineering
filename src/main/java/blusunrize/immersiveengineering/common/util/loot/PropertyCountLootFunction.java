/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nonnull;

public class PropertyCountLootFunction extends LootItemConditionalFunction
{
	private final String propertyName;

	protected PropertyCountLootFunction(LootItemCondition[] conditionsIn, String propertyName)
	{
		super(conditionsIn);
		this.propertyName = propertyName;
	}

	@Nonnull
	@Override
	protected ItemStack run(@Nonnull ItemStack stack, @Nonnull LootContext context)
	{
		BlockState blockstate = context.getParamOrNull(LootContextParams.BLOCK_STATE);
		if(blockstate!=null)
			stack.setCount(getPropertyValue(blockstate));
		return stack;
	}

	private int getPropertyValue(BlockState blockState)
	{
		for(Property<?> prop : blockState.getProperties())
			if(prop instanceof IntegerProperty&&prop.getName().equals(this.propertyName))
				return blockState.getValue((IntegerProperty)prop);
		return 1;
	}

	@Override
	public LootItemFunctionType getType()
	{
		return IELootFunctions.PROPERTY_COUNT.get();
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<PropertyCountLootFunction>
	{
		private final static String JSON_KEY = "propery_name";

		@Override
		public void serialize(JsonObject object, PropertyCountLootFunction function, JsonSerializationContext context)
		{
			super.serialize(object, function, context);
			object.addProperty(JSON_KEY, function.propertyName);
		}

		@Nonnull
		@Override
		public PropertyCountLootFunction deserialize(@Nonnull JsonObject object,
													 @Nonnull JsonDeserializationContext deserializationContext,
													 @Nonnull LootItemCondition[] conditionsIn)
		{
			return new PropertyCountLootFunction(conditionsIn, GsonHelper.getAsString(object, JSON_KEY));
		}
	}

	public static class Builder extends LootItemConditionalFunction.Builder<blusunrize.immersiveengineering.common.util.loot.PropertyCountLootFunction.Builder>
	{
		private final String propertyName;

		public Builder(String propertyName)
		{
			this.propertyName = propertyName;
		}

		@Nonnull
		@Override
		protected blusunrize.immersiveengineering.common.util.loot.PropertyCountLootFunction.Builder getThis()
		{
			return this;
		}

		@Nonnull
		@Override
		public LootItemFunction build()
		{
			return new PropertyCountLootFunction(getConditions(), propertyName);
		}
	}
}
