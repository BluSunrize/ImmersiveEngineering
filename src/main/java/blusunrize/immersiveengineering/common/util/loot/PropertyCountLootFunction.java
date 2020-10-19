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
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.Property;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class PropertyCountLootFunction extends LootFunction
{
	public static final ResourceLocation ID = new ResourceLocation(ImmersiveEngineering.MODID, "property_count");

	private final String propertyName;

	protected PropertyCountLootFunction(ILootCondition[] conditionsIn, String propertyName)
	{
		super(conditionsIn);
		this.propertyName = propertyName;
	}

	@Nonnull
	@Override
	protected ItemStack doApply(@Nonnull ItemStack stack, @Nonnull LootContext context)
	{
		BlockState blockstate = context.get(LootParameters.BLOCK_STATE);
		if(blockstate!=null)
			stack.setCount(getPropertyValue(blockstate));
		return stack;
	}

	private int getPropertyValue(BlockState blockState)
	{
		for(Property<?> prop : blockState.func_235904_r_())
			if(prop instanceof IntegerProperty&&prop.getName().equals(this.propertyName))
				return blockState.get((IntegerProperty)prop);
		return 1;
	}

	@Override
	public LootFunctionType func_230425_b_()
	{
		return IELootFunctions.propertyCount;
	}

	public static class Serializer extends LootFunction.Serializer<PropertyCountLootFunction>
	{
		private final static String JSON_KEY = "propery_name";

		@Override
		public void func_230424_a_(JsonObject object, PropertyCountLootFunction function, JsonSerializationContext context)
		{
			super.func_230424_a_(object, function, context);
			object.addProperty(JSON_KEY, function.propertyName);
		}

		@Nonnull
		@Override
		public PropertyCountLootFunction deserialize(@Nonnull JsonObject object,
													 @Nonnull JsonDeserializationContext deserializationContext,
													 @Nonnull ILootCondition[] conditionsIn)
		{
			return new PropertyCountLootFunction(conditionsIn, JSONUtils.getString(object, JSON_KEY));
		}
	}

	public static class Builder extends LootFunction.Builder<Builder>
	{
		private final String propertyName;

		public Builder(String propertyName)
		{
			this.propertyName = propertyName;
		}

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
			return new PropertyCountLootFunction(getConditions(), propertyName);
		}
	}
}
