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
import blusunrize.immersiveengineering.common.items.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.items.IEItems.Metals;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.items.ToolUpgradeItem.ToolUpgrade;
import blusunrize.immersiveengineering.common.util.loot.BluprintzLootFunction;
import blusunrize.immersiveengineering.common.world.Villages;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
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
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;

public class GeneralLoot implements Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>
{
	@Override
	public void accept(BiConsumer<ResourceLocation, LootTable.Builder> out)
	{
		LootPool.Builder mainPool = LootPool.lootPool();
		mainPool
				.setRolls(ConstantValue.exactly(4))
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
		out.accept(rl("chests/engineers_house"), builder);

		/* Add Advancement Loot Tables */

		builder = LootTable.lootTable();
		builder.withPool(LootPool.lootPool().name("rare_shader").setRolls(ConstantValue.exactly(1))
				.add(createEntry(Misc.shaderBag.get(Rarity.RARE), 1, 1, 1)));
		out.accept(rl("advancements/shader_rare"), builder);

		builder = LootTable.lootTable();
		builder.withPool(LootPool.lootPool().name("epic_shader").setRolls(ConstantValue.exactly(1))
				.add(createEntry(Misc.shaderBag.get(Rarity.EPIC), 1, 1, 1)));
		out.accept(rl("advancements/shader_epic"), builder);

		builder = LootTable.lootTable();
		builder.withPool(LootPool.lootPool().name("masterwork_shader").setRolls(ConstantValue.exactly(1))
				.add(createEntry(Misc.shaderBag.get(Lib.RARITY_MASTERWORK), 1, 1, 1)));
		out.accept(rl("advancements/shader_masterwork"), builder);

		/* Add Hero of the Village Loot Tables */

		builder = LootTable.lootTable();
		builder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
				.add(createEntry(Ingredients.stickTreated))
				.add(createEntry(Ingredients.stickIron))
				.add(createEntry(Ingredients.stickSteel)));
		out.accept(rl("gameplay/hero_of_the_village/"+Villages.ENGINEER.getPath()), builder);

		builder = LootTable.lootTable();
		builder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
				.add(createEntry(Ingredients.componentIron))
				.add(createEntry(Ingredients.componentSteel)));
		out.accept(rl("gameplay/hero_of_the_village/"+Villages.MACHINIST.getPath()), builder);

		builder = LootTable.lootTable();
		builder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
				.add(createEntry(Misc.faradaySuit.get(EquipmentSlot.HEAD)))
				.add(createEntry(Misc.faradaySuit.get(EquipmentSlot.CHEST)))
				.add(createEntry(Misc.faradaySuit.get(EquipmentSlot.LEGS)))
				.add(createEntry(Misc.faradaySuit.get(EquipmentSlot.FEET))));
		out.accept(rl("gameplay/hero_of_the_village/"+Villages.ELECTRICIAN.getPath()), builder);

		builder = LootTable.lootTable();
		builder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
				.add(createEntry(Items.WHITE_BANNER))
				.add(createEntry(Items.ORANGE_BANNER))
				.add(createEntry(Items.GREEN_BANNER))
				.add(createEntry(Misc.shaderBag.get(Rarity.RARE)))
				.add(createEntry(Misc.shaderBag.get(Rarity.EPIC))));
		out.accept(rl("gameplay/hero_of_the_village/"+Villages.OUTFITTER.getPath()), builder);

		builder = LootTable.lootTable();
		builder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
				.add(createEntry(BulletHandler.getBulletItem(BulletItem.SILVER)))
				.add(createEntry(BulletHandler.getBulletItem(BulletItem.DRAGONS_BREATH)))
				.add(createEntry(BulletHandler.getBulletItem(BulletItem.HOMING)))
				.add(createEntry(Misc.toolUpgrades.get(ToolUpgrade.REVOLVER_MAGAZINE))));
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
