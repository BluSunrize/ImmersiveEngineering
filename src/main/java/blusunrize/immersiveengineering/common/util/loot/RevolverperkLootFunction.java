/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util.loot;

import blusunrize.immersiveengineering.common.items.RevolverItem;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nonnull;
import java.util.List;

public class RevolverperkLootFunction extends LootItemConditionalFunction
{
	public static final Codec<RevolverperkLootFunction> CODEC = RecordCodecBuilder.create(
			inst -> commonFields(inst).apply(inst, RevolverperkLootFunction::new)
	);

	protected RevolverperkLootFunction(List<LootItemCondition> conditionsIn)
	{
		super(conditionsIn);
	}

	@Nonnull
	@Override
	public ItemStack run(ItemStack stack, @Nonnull LootContext context)
	{
		CompoundTag perksTag = RevolverItem.RevolverPerk.generatePerkSet(context.getRandom(), context.getLuck());
		ItemNBTHelper.setTagCompound(stack, "perks", perksTag);
		return stack;
	}

	@Nonnull
	@Override
	public LootItemFunctionType getType()
	{
		return IELootFunctions.REVOLVERPERK.value();
	}

	public static Builder<?> builder()
	{
		return LootItemConditionalFunction.simpleBuilder(RevolverperkLootFunction::new);
	}
}
