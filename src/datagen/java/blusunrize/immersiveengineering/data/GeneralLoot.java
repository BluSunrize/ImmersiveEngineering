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
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Rarity;
import net.minecraft.loot.*;
import net.minecraft.loot.StandaloneLootEntry.Builder;
import net.minecraft.loot.conditions.RandomChance;
import net.minecraft.loot.functions.SetCount;
import net.minecraft.loot.functions.SetNBT;
import net.minecraft.util.IItemProvider;

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
		tables.put(rl("chests/engineers_house"), builder.build());

		/* Add Advancement Loot Tables */

		builder = LootTable.builder();
		builder.addLootPool(LootPool.builder().name("rare_shader").rolls(new ConstantRange(1))
				.addEntry(createEntry(Misc.shaderBag.get(Rarity.RARE), 1, 1, 1)));
		tables.put(rl("advancements/shader_rare"), builder.build());

		builder = LootTable.builder();
		builder.addLootPool(LootPool.builder().name("epic_shader").rolls(new ConstantRange(1))
				.addEntry(createEntry(Misc.shaderBag.get(Rarity.EPIC), 1, 1, 1)));
		tables.put(rl("advancements/shader_epic"), builder.build());

		builder = LootTable.builder();
		builder.addLootPool(LootPool.builder().name("masterwork_shader").rolls(new ConstantRange(1))
				.addEntry(createEntry(Misc.shaderBag.get(Lib.RARITY_MASTERWORK), 1, 1, 1)));
		tables.put(rl("advancements/shader_masterwork"), builder.build());

		/* Add Hero of the Village Loot Tables */

		builder = LootTable.builder();
		builder.addLootPool(LootPool.builder().rolls(new ConstantRange(1))
				.addEntry(createEntry(Ingredients.stickTreated))
				.addEntry(createEntry(Ingredients.stickIron))
				.addEntry(createEntry(Ingredients.stickSteel)));
		tables.put(rl("gameplay/hero_of_the_village/"+Villages.ENGINEER.getPath()), builder.build());

		builder = LootTable.builder();
		builder.addLootPool(LootPool.builder().rolls(new ConstantRange(1))
				.addEntry(createEntry(Ingredients.componentIron))
				.addEntry(createEntry(Ingredients.componentSteel)));
		tables.put(rl("gameplay/hero_of_the_village/"+Villages.MACHINIST.getPath()), builder.build());

		builder = LootTable.builder();
		builder.addLootPool(LootPool.builder().rolls(new ConstantRange(1))
				.addEntry(createEntry(Misc.faradaySuit.get(EquipmentSlotType.HEAD)))
				.addEntry(createEntry(Misc.faradaySuit.get(EquipmentSlotType.CHEST)))
				.addEntry(createEntry(Misc.faradaySuit.get(EquipmentSlotType.LEGS)))
				.addEntry(createEntry(Misc.faradaySuit.get(EquipmentSlotType.FEET))));
		tables.put(rl("gameplay/hero_of_the_village/"+Villages.ELECTRICIAN.getPath()), builder.build());

		builder = LootTable.builder();
		builder.addLootPool(LootPool.builder().rolls(new ConstantRange(1))
				.addEntry(createEntry(Items.WHITE_BANNER))
				.addEntry(createEntry(Items.ORANGE_BANNER))
				.addEntry(createEntry(Items.GREEN_BANNER))
				.addEntry(createEntry(Misc.shaderBag.get(Rarity.RARE)))
				.addEntry(createEntry(Misc.shaderBag.get(Rarity.EPIC))));
		tables.put(rl("gameplay/hero_of_the_village/"+Villages.OUTFITTER.getPath()), builder.build());

		builder = LootTable.builder();
		builder.addLootPool(LootPool.builder().rolls(new ConstantRange(1))
				.addEntry(createEntry(BulletHandler.getBulletItem(BulletItem.SILVER)))
				.addEntry(createEntry(BulletHandler.getBulletItem(BulletItem.DRAGONS_BREATH)))
				.addEntry(createEntry(BulletHandler.getBulletItem(BulletItem.HOMING)))
				.addEntry(createEntry(Misc.toolUpgrades.get(ToolUpgrade.REVOLVER_MAGAZINE))));
		tables.put(rl("gameplay/hero_of_the_village/"+Villages.GUNSMITH.getPath()), builder.build());
	}

	private LootEntry.Builder<?> createEntry(IItemProvider item)
	{
		return ItemLootEntry.builder(item);
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
			ret.acceptFunction(SetNBT.builder(item.getOrCreateTag()));
		return ret;
	}

	@Override
	public String getName()
	{
		return "General Loot";
	}
}
