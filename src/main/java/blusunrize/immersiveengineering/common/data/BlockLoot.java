/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.StoneDecoration;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.WoodenDecoration;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.WoodenDevices;
import blusunrize.immersiveengineering.common.blocks.plant.EnumHempGrowth;
import blusunrize.immersiveengineering.common.blocks.plant.HempBlock;
import blusunrize.immersiveengineering.common.data.loot.LootGenerator;
import blusunrize.immersiveengineering.common.items.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.loot.WindmillLootFunction;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.state.IProperty;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.LootTable.Builder;
import net.minecraft.world.storage.loot.conditions.BlockStateProperty;
import net.minecraft.world.storage.loot.conditions.ILootCondition;
import net.minecraft.world.storage.loot.conditions.SurvivesExplosion;
import net.minecraft.world.storage.loot.functions.ApplyBonus;

public class BlockLoot extends LootGenerator
{
	public BlockLoot(DataGenerator gen)
	{
		super(gen);
	}

	public String getName()
	{
		return "LootTablesBlock";
	}

	private ResourceLocation toTableLoc(ResourceLocation in)
	{
		return new ResourceLocation(in.getNamespace(), "blocks/"+in.getPath());
	}

	@Override
	protected void registerTables()
	{
		registerHemp();
		register(StoneDecoration.concreteSprayed, LootTable.builder());
		register(WoodenDevices.windmill, LootTable.builder().addLootPool(
				createPoolBuilder().addEntry(
						ItemLootEntry.builder(WoodenDevices.windmill)
								.acceptFunction(new WindmillLootFunction.Builder())
				)));
		registerAllRemainingAsDefault();
	}

	private void registerAllRemainingAsDefault()
	{
		for(Block b : IEContent.registeredIEBlocks)
			if(!tables.containsKey(toTableLoc(b.getRegistryName())))
				registerSelfDropping(b);
	}

	private void register(Block b, LootTable.Builder table)
	{
		register(b.getRegistryName(), table);
	}

	private void register(ResourceLocation name, LootTable.Builder table)
	{
		if(tables.put(toTableLoc(name), table.setParameterSet(LootParameterSets.BLOCK).build())!=null)
			throw new IllegalStateException("Duplicate loot table "+name);
	}

	private void registerSelfDropping(Block b)
	{
		register(b.getRegistryName(), dropProvider(b));
	}

	private Builder dropProvider(IItemProvider in)
	{
		return LootTable
				.builder()
				.addLootPool(singleItemPool(in)
				);
	}

	private LootPool.Builder singleItemPool(IItemProvider in)
	{
		return createPoolBuilder()
				.rolls(ConstantRange.of(1))
				.addEntry(ItemLootEntry.builder(in));
	}

	private LootPool.Builder createPoolBuilder()
	{
		return LootPool.builder().acceptCondition(SurvivesExplosion.builder());
	}

	private void registerHemp()
	{
		LootTable.Builder ret = LootTable.builder()
				.addLootPool(singleItemPool(Misc.hempSeeds));
		for(EnumHempGrowth g : EnumHempGrowth.values())
			if(g==HempBlock.getMaxGrowth(g))
			{
				ret.addLootPool(
						binBonusLootPool(Ingredients.hempFiber, Enchantments.FORTUNE, g.ordinal()/8f, 3)
								.acceptCondition(propertyIs(IEBlocks.Misc.hempPlant, HempBlock.GROWTH, g))
				);
			}
		register(IEBlocks.Misc.hempPlant, ret);
	}

	private LootPool.Builder binBonusLootPool(IItemProvider item, Enchantment ench, float prob, int extra)
	{
		return createPoolBuilder()
				.addEntry(ItemLootEntry.builder(item))
				.acceptFunction(ApplyBonus.binomialWithBonusCount(ench, prob, extra));
	}

	private <T extends Comparable<T>> ILootCondition.IBuilder propertyIs(Block b, IProperty<T> prop, T value)
	{
		return BlockStateProperty.builder(b)
				.with(prop, value);
	}
}
