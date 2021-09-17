/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class SimpleSerializer<T extends LootItemConditionalFunction> extends LootItemConditionalFunction.Serializer<T>
{
	private final Function<LootItemCondition[], T> make;

	public SimpleSerializer(Function<LootItemCondition[], T> make)
	{
		this.make = make;
	}

	@Nonnull
	@Override
	public T deserialize(@Nonnull JsonObject pObject, @Nonnull JsonDeserializationContext pDeserializationContext, @Nonnull LootItemCondition[] pConditions)
	{
		return make.apply(pConditions);
	}
}
