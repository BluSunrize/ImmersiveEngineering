/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import java.util.List;

public class PropertyCountLootFunction extends LootItemConditionalFunction
{
	public static final Codec<PropertyCountLootFunction> CODEC = RecordCodecBuilder.create(
			inst -> commonFields(inst)
					.and(Codec.STRING.fieldOf("countProperty").forGetter(f -> f.propertyName))
					.apply(inst, PropertyCountLootFunction::new)
	);

	private final String propertyName;

	protected PropertyCountLootFunction(List<LootItemCondition> conditionsIn, String propertyName)
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
		return IELootFunctions.PROPERTY_COUNT.value();
	}

	public static class Builder extends LootItemConditionalFunction.Builder<PropertyCountLootFunction.Builder>
	{
		private final String propertyName;

		public Builder(String propertyName)
		{
			this.propertyName = propertyName;
		}

		@Nonnull
		@Override
		protected PropertyCountLootFunction.Builder getThis()
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
