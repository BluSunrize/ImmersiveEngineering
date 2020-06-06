/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.data.loot.LootGenerator;
import blusunrize.immersiveengineering.common.items.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.items.IEItems.Metals;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.loot.BluprintzLootFunction;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Rarity;
import net.minecraft.util.IItemProvider;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.StandaloneLootEntry.Builder;
import net.minecraft.world.storage.loot.conditions.RandomChance;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraft.world.storage.loot.functions.SetNBT;

public class GeneralLoot extends LootGenerator
{
	public GeneralLoot(DataGenerator gen)
	{
		super(gen);
	}

	@Override
	protected void registerTables()
	{
		LootPool.Builder mainPool = LootPool.builder();
		mainPool
				.rolls(new ConstantRange(4))
				.addEntry(createEntry(Ingredients.stickTreated, 20, 2, 7))
				.addEntry(createEntry(Ingredients.stickIron, 10, 1, 4))
				.addEntry(createEntry(Ingredients.stickSteel, 6, 1, 4))
				.addEntry(createEntry(Ingredients.stickAluminum, 10, 1, 4))
				.addEntry(createEntry(Ingredients.hempFabric, 10, 1, 3))
				.addEntry(createEntry(Ingredients.coalCoke, 10, 1, 3))
				.addEntry(createEntry(Ingredients.componentIron, 8, 1, 2))
				.addEntry(createEntry(Ingredients.componentSteel, 5, 1, 1))
				.addEntry(createEntry(Items.IRON_INGOT, 10, 1, 4))
				.addEntry(createEntry(Metals.ingots.get(EnumMetals.COPPER), 10, 1, 4))
				.addEntry(createEntry(Metals.ingots.get(EnumMetals.ALUMINUM), 10, 1, 4))
				.addEntry(createEntry(Metals.nuggets.get(EnumMetals.LEAD), 9, 1, 4))
				.addEntry(createEntry(Metals.nuggets.get(EnumMetals.SILVER), 7, 1, 2))
				.addEntry(createEntry(Metals.nuggets.get(EnumMetals.NICKEL), 7, 1, 2))
				.addEntry(createBlueprint("bullet", 4))
				.addEntry(createBlueprint("specialBullet", 4))
				.addEntry(createBlueprint("electrode", 4));
		LootTable.Builder builder = LootTable.builder();
		builder.addLootPool(mainPool);
		tables.put(IEDataGenerator.rl("chests/engineers_house"), builder.build());

		/* Add Advancement Loot Tables */

		builder = LootTable.builder();
		builder.addLootPool(LootPool.builder().name("rare_shader").rolls(new ConstantRange(1))
				.addEntry(createEntry(Misc.shaderBag.get(Rarity.RARE), 1, 1, 1)));
		tables.put(IEDataGenerator.rl("advancements/shader_rare"), builder.build());

		builder = LootTable.builder();
		builder.addLootPool(LootPool.builder().name("epic_shader").rolls(new ConstantRange(1))
				.addEntry(createEntry(Misc.shaderBag.get(Rarity.EPIC), 1, 1, 1)));
		tables.put(IEDataGenerator.rl("advancements/shader_epic"), builder.build());

		builder = LootTable.builder();
		builder.addLootPool(LootPool.builder().name("masterwork_shader").rolls(new ConstantRange(1))
				.addEntry(createEntry(Misc.shaderBag.get(Lib.RARITY_MASTERWORK), 1, 1, 1)));
		tables.put(IEDataGenerator.rl("advancements/shader_masterwork"), builder.build());
	}

	private LootEntry.Builder<?> createEntry(IItemProvider item, int weight, int min, int max)
	{
		return createEntry(new ItemStack(item), weight)
				.acceptFunction(SetCount.builder(new RandomValueRange(min, max)));
	}

	private LootEntry.Builder<?> createBlueprint(String type, int weight)
	{
		return createEntry(BlueprintCraftingRecipe.getTypedBlueprint(type), weight)
				.acceptFunction(
						BluprintzLootFunction.builder()
								.acceptCondition(RandomChance.builder(0.125F))
				);
	}

	private Builder<?> createEntry(ItemStack item, int weight)
	{
		Builder<?> ret = ItemLootEntry.builder(item.getItem())
				.weight(weight);
		if(item.hasTag())
			ret.acceptFunction(SetNBT.func_215952_a(item.getOrCreateTag()));
		return ret;
	}

	@Override
	public String getName()
	{
		return "General Loot";
	}
}
