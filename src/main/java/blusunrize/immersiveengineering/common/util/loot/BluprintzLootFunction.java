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
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nonnull;

public class BluprintzLootFunction extends LootItemConditionalFunction
{
	protected BluprintzLootFunction(LootItemCondition[] conditionsIn)
	{
		super(conditionsIn);
	}

	@Nonnull
	@Override
	public ItemStack run(ItemStack stack, @Nonnull LootContext context)
	{
		stack.setHoverName(Component.literal("Super Special BluPrintz"));
		ItemNBTHelper.setLore(stack, Component.literal("Congratulations!"), Component.literal("You have found an easter egg!"));
		return stack;
	}

	@Nonnull
	@Override
	public LootItemFunctionType getType()
	{
		return IELootFunctions.BLUPRINTZ.get();
	}

	public static Builder<?> builder()
	{
		return LootItemConditionalFunction.simpleBuilder(BluprintzLootFunction::new);
	}
}
