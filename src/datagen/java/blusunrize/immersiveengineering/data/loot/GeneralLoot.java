/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.loot;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.common.items.BulletItem;
import blusunrize.immersiveengineering.common.items.ToolUpgradeItem.ToolUpgrade;
import blusunrize.immersiveengineering.common.register.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.register.IEItems.Metals;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.loot.BluprintzLootFunction;
import blusunrize.immersiveengineering.common.world.Villages;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Builder;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.BiConsumer;

import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;

public class GeneralLoot implements LootTableSubProvider
{
	@Override
	public void generate(BiConsumer<ResourceLocation, LootTable.Builder> out)
	{
		LootPool.Builder mainPool = LootPool.lootPool();
		mainPool
				.setRolls(ConstantValue.exactly(4))
				.add(createEntry(Ingredients.STICK_TREATED, 20, 2, 7))
				.add(createEntry(Ingredients.STICK_IRON, 10, 1, 4))
				.add(createEntry(Ingredients.STICK_STEEL, 6, 1, 4))
				.add(createEntry(Ingredients.STICK_ALUMINUM, 10, 1, 4))
				.add(createEntry(Ingredients.HEMP_FABRIC, 10, 1, 3))
				.add(createEntry(Ingredients.COAL_COKE, 10, 1, 3))
				.add(createEntry(Ingredients.COMPONENT_IRON, 8, 1, 2))
				.add(createEntry(Ingredients.COMPONENT_STEEL, 5, 1, 1))
				.add(createEntry(Items.IRON_INGOT, 10, 1, 4))
				.add(createEntry(Metals.INGOTS.get(EnumMetals.COPPER), 10, 1, 4))
				.add(createEntry(Metals.INGOTS.get(EnumMetals.ALUMINUM), 10, 1, 4))
				.add(createEntry(Metals.NUGGETS.get(EnumMetals.LEAD), 9, 1, 4))
				.add(createEntry(Metals.NUGGETS.get(EnumMetals.SILVER), 7, 1, 2))
				.add(createEntry(Metals.NUGGETS.get(EnumMetals.NICKEL), 7, 1, 2))
				.add(createBlueprint("bullet", 4))
				.add(createBlueprint("specialBullet", 4))
				.add(createBlueprint("electrode", 4));
		LootTable.Builder builder = LootTable.lootTable();
		builder.withPool(mainPool);
		out.accept(rl("chests/engineers_house"), builder);

		/* Add Advancement Loot Tables */

		builder = LootTable.lootTable();
		builder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
				.add(createEntry(Misc.SHADER_BAG.get(Rarity.RARE), 1, 1, 1)));
		out.accept(rl("advancements/shader_rare"), builder);

		builder = LootTable.lootTable();
		builder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
				.add(createEntry(Misc.SHADER_BAG.get(Rarity.EPIC), 1, 1, 1)));
		out.accept(rl("advancements/shader_epic"), builder);

		builder = LootTable.lootTable();
		builder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
				.add(createEntry(Misc.SHADER_BAG.get(Lib.RARITY_MASTERWORK), 1, 1, 1)));
		out.accept(rl("advancements/shader_masterwork"), builder);

		/* Add Hero of the Village Loot Tables */

		builder = LootTable.lootTable();
		builder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
				.add(createEntry(Ingredients.STICK_TREATED))
				.add(createEntry(Ingredients.STICK_IRON))
				.add(createEntry(Ingredients.STICK_STEEL)));
		out.accept(rl("gameplay/hero_of_the_village/"+Villages.ENGINEER.getPath()), builder);

		builder = LootTable.lootTable();
		builder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
				.add(createEntry(Ingredients.COMPONENT_IRON))
				.add(createEntry(Ingredients.COMPONENT_STEEL)));
		out.accept(rl("gameplay/hero_of_the_village/"+Villages.MACHINIST.getPath()), builder);

		builder = LootTable.lootTable();
		builder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
				.add(createEntry(Misc.FARADAY_SUIT.get(Type.HELMET)))
				.add(createEntry(Misc.FARADAY_SUIT.get(Type.CHESTPLATE)))
				.add(createEntry(Misc.FARADAY_SUIT.get(Type.LEGGINGS)))
				.add(createEntry(Misc.FARADAY_SUIT.get(Type.BOOTS))));
		out.accept(rl("gameplay/hero_of_the_village/"+Villages.ELECTRICIAN.getPath()), builder);

		builder = LootTable.lootTable();
		builder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
				.add(createEntry(Items.WHITE_BANNER))
				.add(createEntry(Items.ORANGE_BANNER))
				.add(createEntry(Items.GREEN_BANNER))
				.add(createEntry(Misc.SHADER_BAG.get(Rarity.RARE)))
				.add(createEntry(Misc.SHADER_BAG.get(Rarity.EPIC))));
		out.accept(rl("gameplay/hero_of_the_village/"+Villages.OUTFITTER.getPath()), builder);

		builder = LootTable.lootTable();
		builder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
				.add(createEntry(BulletHandler.getBulletItem(BulletItem.SILVER)))
				.add(createEntry(BulletHandler.getBulletItem(BulletItem.DRAGONS_BREATH)))
				.add(createEntry(BulletHandler.getBulletItem(BulletItem.HOMING)))
				.add(createEntry(Misc.TOOL_UPGRADES.get(ToolUpgrade.REVOLVER_MAGAZINE))));
		out.accept(rl("gameplay/hero_of_the_village/"+Villages.GUNSMITH.getPath()), builder);
	}

	private LootPoolEntryContainer.Builder<?> createEntry(ItemLike item)
	{
		return LootItem.lootTableItem(item);
	}

	private LootPoolEntryContainer.Builder<?> createEntry(ItemLike item, int weight, int min, int max)
	{
		return createEntry(new ItemStack(item), weight)
				.apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));
	}

	private LootPoolEntryContainer.Builder<?> createBlueprint(String type, int weight)
	{
		return createEntry(BlueprintCraftingRecipe.getTypedBlueprint(type), weight)
				.apply(
						BluprintzLootFunction.builder()
								.when(LootItemRandomChanceCondition.randomChance(0.125F))
				);
	}

	private Builder<?> createEntry(ItemStack item, int weight)
	{
		Builder<?> ret = LootItem.lootTableItem(item.getItem())
				.setWeight(weight);
		if(item.hasTag())
			ret.apply(SetNbtFunction.setTag(item.getOrCreateTag()));
		return ret;
	}
}
