/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.common.items.BulletItem;
import blusunrize.immersiveengineering.common.items.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.items.IEItems.Metals;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.items.ToolUpgradeItem.ToolUpgrade;
import blusunrize.immersiveengineering.common.util.loot.BluprintzLootFunction;
import blusunrize.immersiveengineering.common.world.Villages;
import blusunrize.immersiveengineering.data.loot.LootGenerator;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.ConstantIntValue;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Builder;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;

public class GeneralLoot extends LootGenerator
{
	public GeneralLoot(DataGenerator gen)
	{
		super(gen);
	}

	@Override
	protected void registerTables()
	{
		LootPool.Builder mainPool = LootPool.lootPool();
		mainPool
				.setRolls(new ConstantIntValue(4))
				.add(createEntry(Ingredients.stickTreated, 20, 2, 7))
				.add(createEntry(Ingredients.stickIron, 10, 1, 4))
				.add(createEntry(Ingredients.stickSteel, 6, 1, 4))
				.add(createEntry(Ingredients.stickAluminum, 10, 1, 4))
				.add(createEntry(Ingredients.hempFabric, 10, 1, 3))
				.add(createEntry(Ingredients.coalCoke, 10, 1, 3))
				.add(createEntry(Ingredients.componentIron, 8, 1, 2))
				.add(createEntry(Ingredients.componentSteel, 5, 1, 1))
				.add(createEntry(Items.IRON_INGOT, 10, 1, 4))
				.add(createEntry(Metals.ingots.get(EnumMetals.COPPER), 10, 1, 4))
				.add(createEntry(Metals.ingots.get(EnumMetals.ALUMINUM), 10, 1, 4))
				.add(createEntry(Metals.nuggets.get(EnumMetals.LEAD), 9, 1, 4))
				.add(createEntry(Metals.nuggets.get(EnumMetals.SILVER), 7, 1, 2))
				.add(createEntry(Metals.nuggets.get(EnumMetals.NICKEL), 7, 1, 2))
				.add(createBlueprint("bullet", 4))
				.add(createBlueprint("specialBullet", 4))
				.add(createBlueprint("electrode", 4));
		LootTable.Builder builder = LootTable.lootTable();
		builder.withPool(mainPool);
		tables.put(rl("chests/engineers_house"), builder.build());

		/* Add Advancement Loot Tables */

		builder = LootTable.lootTable();
		builder.withPool(LootPool.lootPool().name("rare_shader").setRolls(new ConstantIntValue(1))
				.add(createEntry(Misc.shaderBag.get(Rarity.RARE), 1, 1, 1)));
		tables.put(rl("advancements/shader_rare"), builder.build());

		builder = LootTable.lootTable();
		builder.withPool(LootPool.lootPool().name("epic_shader").setRolls(new ConstantIntValue(1))
				.add(createEntry(Misc.shaderBag.get(Rarity.EPIC), 1, 1, 1)));
		tables.put(rl("advancements/shader_epic"), builder.build());

		builder = LootTable.lootTable();
		builder.withPool(LootPool.lootPool().name("masterwork_shader").setRolls(new ConstantIntValue(1))
				.add(createEntry(Misc.shaderBag.get(Lib.RARITY_MASTERWORK), 1, 1, 1)));
		tables.put(rl("advancements/shader_masterwork"), builder.build());

		/* Add Hero of the Village Loot Tables */

		builder = LootTable.lootTable();
		builder.withPool(LootPool.lootPool().setRolls(new ConstantIntValue(1))
				.add(createEntry(Ingredients.stickTreated))
				.add(createEntry(Ingredients.stickIron))
				.add(createEntry(Ingredients.stickSteel)));
		tables.put(rl("gameplay/hero_of_the_village/"+Villages.ENGINEER.getPath()), builder.build());

		builder = LootTable.lootTable();
		builder.withPool(LootPool.lootPool().setRolls(new ConstantIntValue(1))
				.add(createEntry(Ingredients.componentIron))
				.add(createEntry(Ingredients.componentSteel)));
		tables.put(rl("gameplay/hero_of_the_village/"+Villages.MACHINIST.getPath()), builder.build());

		builder = LootTable.lootTable();
		builder.withPool(LootPool.lootPool().setRolls(new ConstantIntValue(1))
				.add(createEntry(Misc.faradaySuit.get(EquipmentSlot.HEAD)))
				.add(createEntry(Misc.faradaySuit.get(EquipmentSlot.CHEST)))
				.add(createEntry(Misc.faradaySuit.get(EquipmentSlot.LEGS)))
				.add(createEntry(Misc.faradaySuit.get(EquipmentSlot.FEET))));
		tables.put(rl("gameplay/hero_of_the_village/"+Villages.ELECTRICIAN.getPath()), builder.build());

		builder = LootTable.lootTable();
		builder.withPool(LootPool.lootPool().setRolls(new ConstantIntValue(1))
				.add(createEntry(Items.WHITE_BANNER))
				.add(createEntry(Items.ORANGE_BANNER))
				.add(createEntry(Items.GREEN_BANNER))
				.add(createEntry(Misc.shaderBag.get(Rarity.RARE)))
				.add(createEntry(Misc.shaderBag.get(Rarity.EPIC))));
		tables.put(rl("gameplay/hero_of_the_village/"+Villages.OUTFITTER.getPath()), builder.build());

		builder = LootTable.lootTable();
		builder.withPool(LootPool.lootPool().setRolls(new ConstantIntValue(1))
				.add(createEntry(BulletHandler.getBulletItem(BulletItem.SILVER)))
				.add(createEntry(BulletHandler.getBulletItem(BulletItem.DRAGONS_BREATH)))
				.add(createEntry(BulletHandler.getBulletItem(BulletItem.HOMING)))
				.add(createEntry(Misc.toolUpgrades.get(ToolUpgrade.REVOLVER_MAGAZINE))));
		tables.put(rl("gameplay/hero_of_the_village/"+Villages.GUNSMITH.getPath()), builder.build());
	}

	private LootPoolEntryContainer.Builder<?> createEntry(ItemLike item)
	{
		return LootItem.lootTableItem(item);
	}

	private LootPoolEntryContainer.Builder<?> createEntry(ItemLike item, int weight, int min, int max)
	{
		return createEntry(new ItemStack(item), weight)
				.apply(SetItemCountFunction.setCount(new RandomValueBounds(min, max)));
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

	@Override
	public String getName()
	{
		return "General Loot";
	}
}
