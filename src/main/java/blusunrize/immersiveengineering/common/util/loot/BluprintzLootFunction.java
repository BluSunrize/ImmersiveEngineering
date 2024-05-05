/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util.loot;

import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nonnull;
import java.util.List;

public class BluprintzLootFunction extends LootItemConditionalFunction
{
	public static final MapCodec<BluprintzLootFunction> CODEC = RecordCodecBuilder.mapCodec(
			inst -> commonFields(inst).apply(inst, BluprintzLootFunction::new)
	);

	protected BluprintzLootFunction(List<LootItemCondition> conditionsIn)
	{
		super(conditionsIn);
	}

	@Nonnull
	@Override
	public ItemStack run(ItemStack stack, @Nonnull LootContext context)
	{
		stack.set(DataComponents.CUSTOM_NAME, Component.literal("Super Special BluPrintz"));
		ItemNBTHelper.setLore(stack, Component.literal("Congratulations!"), Component.literal("You have found an easter egg!"));
		return stack;
	}

	@Nonnull
	@Override
	public LootItemFunctionType getType()
	{
		return IELootFunctions.BLUPRINTZ.value();
	}

	public static Builder<?> builder()
	{
		return LootItemConditionalFunction.simpleBuilder(BluprintzLootFunction::new);
	}
}
