/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.loot;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.register.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.loot.RevolverperkLootFunction;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Builder;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.BiConsumer;

import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;

public class EntityLoot implements LootTableSubProvider
{
	@Override
	public void generate(BiConsumer<ResourceLocation, LootTable.Builder> out)
	{
		LootTable.Builder builder = LootTable.lootTable().withPool(LootPool.lootPool()
				.setRolls(ConstantValue.exactly(1))
				.setBonusRolls(ConstantValue.exactly(1))
				.add(createEntry(Items.EMERALD)
						.apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 1)))
						.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0, 1)))
				).add(createEntry(Ingredients.STICK_STEEL)
						.apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4)))
				));
		out.accept(rl("entities/fusilier"), builder);

		builder = LootTable.lootTable().withPool(LootPool.lootPool()
				.setRolls(ConstantValue.exactly(1))
				.add(createEntry(Items.EMERALD)
						.apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 1)))
						.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0, 1)))
				).add(createEntry(Ingredients.GUNPART_BARREL)
						.apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 1)))
						.apply(RevolverperkLootFunction.builder())
				).add(createEntry(Ingredients.GUNPART_DRUM)
						.apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 1)))
						.apply(RevolverperkLootFunction.builder())
				).add(createEntry(Ingredients.GUNPART_HAMMER)
						.apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 1)))
						.apply(RevolverperkLootFunction.builder())
				));
		out.accept(rl("entities/commando"), builder);

		builder = LootTable.lootTable().withPool(LootPool.lootPool()
				.setRolls(ConstantValue.exactly(1))
				.setBonusRolls(ConstantValue.exactly(1))
				.add(createEntry(Items.EMERALD)
						.apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2)))
						.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0, 1)))
				).add(createEntry(Misc.SHADER_BAG.get(Lib.RARITY_MASTERWORK))
						.apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 1)))
				));
		out.accept(rl("entities/bulwark"), builder);
	}

	private Builder<?> createEntry(ItemLike item)
	{
		return LootItem.lootTableItem(item).when(LootItemKilledByPlayerCondition.killedByPlayer());
	}
}
