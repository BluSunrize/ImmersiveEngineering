/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util.loot;

import blusunrize.immersiveengineering.common.blocks.wooden.WindmillBlockEntity;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDevices;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nonnull;
import java.util.List;

public class WindmillLootFunction extends LootItemConditionalFunction
{
	public static final MapCodec<WindmillLootFunction> CODEC = RecordCodecBuilder.mapCodec(
			inst -> commonFields(inst).apply(inst, WindmillLootFunction::new)
	);

	protected WindmillLootFunction(List<LootItemCondition> conditionsIn)
	{
		super(conditionsIn);
	}

	@Nonnull
	@Override
	protected ItemStack run(@Nonnull ItemStack stack, @Nonnull LootContext context)
	{
		if(stack.getItem()==WoodenDevices.WINDMILL.asItem()&&context.hasParam(LootContextParams.BLOCK_ENTITY))
		{
			BlockEntity bEntity = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
			if(bEntity instanceof WindmillBlockEntity windmill&&windmill.sails > 0)
				stack.set(IEDataComponents.WINDMILL_BLADES, windmill.sails);
		}
		return stack;
	}

	@Override
	public LootItemFunctionType getType()
	{
		return IELootFunctions.WINDMILL.value();
	}

	public static LootItemFunction.Builder builder()
	{
		return simpleBuilder(WindmillLootFunction::new);
	}
}
