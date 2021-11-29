/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.loot;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.common.blocks.IEEntityBlock;
import blusunrize.immersiveengineering.common.blocks.metal.CapacitorBlockEntity;
import blusunrize.immersiveengineering.common.blocks.metal.ConveyorBlock;
import blusunrize.immersiveengineering.common.blocks.plant.EnumHempGrowth;
import blusunrize.immersiveengineering.common.blocks.plant.HempBlock;
import blusunrize.immersiveengineering.common.blocks.wooden.SawdustBlock;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEBlocks.*;
import blusunrize.immersiveengineering.common.register.IEItems;
import blusunrize.immersiveengineering.common.register.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.register.IEItems.ItemRegObject;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.loot.*;
import com.google.common.collect.ImmutableList;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BlockLoot implements Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>
{
	private final Set<ResourceLocation> generatedTables = new HashSet<>();
	private BiConsumer<ResourceLocation, LootTable.Builder> out;

	private ResourceLocation toTableLoc(ResourceLocation in)
	{
		return new ResourceLocation(in.getNamespace(), "blocks/"+in.getPath());
	}

	@Override
	public void accept(BiConsumer<ResourceLocation, Builder> out)
	{
		this.out = out;
		registerHemp();
		register(StoneDecoration.CONCRETE_SPRAYED, LootTable.lootTable());
		register(WoodenDevices.WINDMILL, LootTable.lootTable().withPool(
				createPoolBuilder().add(
						LootItem.lootTableItem(WoodenDevices.WINDMILL)
								.apply(WindmillLootFunction.builder())
				)));

		LootPoolEntryContainer.Builder<?> tileOrInv = AlternativesEntry.alternatives(
				BEDropLootEntry.builder().when(ExplosionCondition.survivesExplosion()),
				DropInventoryLootEntry.builder()
		);
		register(WoodenDevices.CRATE, LootPool.lootPool().add(tileOrInv));
		register(WoodenDevices.REINFORCED_CRATE, tileDrop());
		register(StoneDecoration.CORESAMPLE, tileDrop());
		register(MetalDevices.TOOLBOX, tileDrop());
		register(Cloth.SHADER_BANNER, tileDrop());
		register(Cloth.SHADER_BANNER_WALL, tileDrop());
		register(Cloth.STRIP_CURTAIN, tileDrop());
		for(BlockEntry<? extends IEEntityBlock<? extends CapacitorBlockEntity>> cap : ImmutableList.of(
				MetalDevices.CAPACITOR_LV, MetalDevices.CAPACITOR_MV, MetalDevices.CAPACITOR_HV, MetalDevices.CAPACITOR_CREATIVE
		))
			register(cap, tileDrop());
		register(Connectors.FEEDTHROUGH, tileDrop());
		register(MetalDevices.TURRET_CHEM, tileDrop());
		register(MetalDevices.TURRET_GUN, tileDrop(), dropInv());
		register(WoodenDevices.WOODEN_BARREL, tileDrop());
		register(WoodenDevices.LOGIC_UNIT, tileDrop());
		register(MetalDevices.BARREL, tileDrop());

		registerMultiblocks();

		registerSelfDropping(WoodenDevices.CRAFTING_TABLE, dropInv());
		registerSelfDropping(WoodenDevices.WORKBENCH, dropInv());
		registerSelfDropping(WoodenDevices.ITEM_BATCHER, dropInv());
		registerSelfDropping(MetalDevices.CLOCHE, dropInv());
		registerSelfDropping(MetalDevices.CHARGING_STATION, dropInv());
		registerSlabs();
		registerSawdust();
		for(BlockEntry<ConveyorBlock> entry : MetalDevices.CONVEYORS.values())
		{
			ConveyorBlock block = entry.get();
			register(entry, singleItem(block).apply(ConveyorCoverLootFunction.builder()));
		}
		for(EnumMetals metal : EnumMetals.values())
			if(metal.shouldAddOre())
				registerOre(metal);

		registerAllRemainingAsDefault();
	}

	private void registerMultiblocks()
	{
		registerMultiblock(Multiblocks.COKE_OVEN);
		registerMultiblock(Multiblocks.BLAST_FURNACE);
		registerMultiblock(Multiblocks.ALLOY_SMELTER);
		registerMultiblock(Multiblocks.ADVANCED_BLAST_FURNACE);

		registerMultiblock(Multiblocks.METAL_PRESS);
		registerMultiblock(Multiblocks.CRUSHER);
		registerMultiblock(Multiblocks.SAWMILL);
		registerMultiblock(Multiblocks.TANK);
		registerMultiblock(Multiblocks.SILO);
		registerMultiblock(Multiblocks.ASSEMBLER);
		registerMultiblock(Multiblocks.AUTO_WORKBENCH);
		registerMultiblock(Multiblocks.BOTTLING_MACHINE);
		registerMultiblock(Multiblocks.SQUEEZER);
		registerMultiblock(Multiblocks.FERMENTER);
		registerMultiblock(Multiblocks.REFINERY);
		registerMultiblock(Multiblocks.DIESEL_GENERATOR);
		registerMultiblock(Multiblocks.EXCAVATOR);
		registerMultiblock(Multiblocks.BUCKET_WHEEL);
		registerMultiblock(Multiblocks.ARC_FURNACE);
		registerMultiblock(Multiblocks.LIGHTNING_ROD);
		registerMultiblock(Multiblocks.MIXER);
	}

	private void registerSlabs()
	{
		for(BlockEntry<SlabBlock> slab : IEBlocks.TO_SLAB.values())
		{
			LootItemConditionalFunction.Builder<?> doubleSlabFunction = SetItemCountFunction.setCount(ConstantValue.exactly(2))
					.when(propertyIs(slab, SlabBlock.TYPE, SlabType.DOUBLE));
			LootTable.Builder lootBuilder = LootTable.lootTable().withPool(
					singleItem(slab).apply(doubleSlabFunction)
			);
			register(slab, lootBuilder);
		}
	}

	private void registerAllRemainingAsDefault()
	{
		for(BlockEntry<?> b : BlockEntry.ALL_ENTRIES)
			if(!generatedTables.contains(toTableLoc(b.getId())))
				registerSelfDropping(b);
	}

	private void registerMultiblock(Supplier<? extends Block> b)
	{
		register(b, dropInv(), dropOriginalBlock());
	}

	private LootPool.Builder dropInv()
	{
		return createPoolBuilder()
				.add(DropInventoryLootEntry.builder());
	}

	private LootPool.Builder tileDrop()
	{
		return createPoolBuilder()
				.add(BEDropLootEntry.builder());
	}

	private LootPool.Builder dropOriginalBlock()
	{
		return createPoolBuilder()
				.add(MBOriginalBlockLootEntry.builder());
	}

	private void register(Supplier<? extends Block> b, LootPool.Builder... pools)
	{
		LootTable.Builder builder = LootTable.lootTable();
		for(LootPool.Builder pool : pools)
			builder.withPool(pool);
		register(b, builder);
	}

	private void register(Supplier<? extends Block> b, LootTable.Builder table)
	{
		register(b.get().getRegistryName(), table);
	}

	private void register(ResourceLocation name, LootTable.Builder table)
	{
		ResourceLocation loc = toTableLoc(name);
		if(!generatedTables.add(loc))
			throw new IllegalStateException("Duplicate loot table "+name);
		out.accept(loc, table.setParamSet(LootContextParamSets.BLOCK));
	}

	private void registerSelfDropping(Supplier<? extends Block> b, LootPool.Builder... pool)
	{
		LootPool.Builder[] withSelf = Arrays.copyOf(pool, pool.length+1);
		withSelf[withSelf.length-1] = singleItem(b.get());
		register(b, withSelf);
	}

	private Builder dropProvider(ItemLike in)
	{
		return LootTable.lootTable().withPool(singleItem(in));
	}

	private LootPool.Builder singleItem(ItemLike in)
	{
		return createPoolBuilder()
				.setRolls(ConstantValue.exactly(1))
				.add(LootItem.lootTableItem(in));
	}

	private LootPool.Builder createPoolBuilder()
	{
		return LootPool.lootPool().when(ExplosionCondition.survivesExplosion());
	}

	private void registerHemp()
	{
		LootTable.Builder ret = LootTable.lootTable()
				.withPool(singleItem(Misc.HEMP_SEEDS));
		for(EnumHempGrowth g : EnumHempGrowth.values())
			if(g==g.getMax())
			{
				ret.withPool(
						binBonusLootPool(Ingredients.HEMP_FIBER, Enchantments.BLOCK_FORTUNE, g.ordinal()/8f, 3)
								.when(propertyIs(IEBlocks.Misc.HEMP_PLANT, HempBlock.GROWTH, g))
				);
			}
		register(IEBlocks.Misc.HEMP_PLANT, ret);
	}


	private void registerOre(EnumMetals metal)
	{
		registerOre(metal, IEBlocks.Metals.ORES.get(metal));
		registerOre(metal, IEBlocks.Metals.DEEPSLATE_ORES.get(metal));
	}

	private void registerOre(EnumMetals metal, BlockEntry<?> oreBlock)
	{
		ItemRegObject<Item> rawOre = IEItems.Metals.RAW_ORES.get(metal);
		LootTable.Builder ret = LootTable.lootTable().withPool(LootPool.lootPool()
				.setRolls(ConstantValue.exactly(1.0F))
				.add(LootItem.lootTableItem(oreBlock)
						// if silk touch
						.when(MatchTool.toolMatches(ItemPredicate.Builder.item().hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1)))))
						// else
						.otherwise(LootItem.lootTableItem(rawOre)
								.apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
								.apply(ApplyExplosionDecay.explosionDecay())
						)
				)
		);
		register(oreBlock, ret);
	}

	private void registerSawdust()
	{
		LootTable.Builder ret = LootTable.lootTable()
				.withPool(singleItem(WoodenDecoration.SAWDUST))
				.apply(new PropertyCountLootFunction.Builder(SawdustBlock.LAYERS.getName()));
		register(WoodenDecoration.SAWDUST, ret);
	}

	private LootPool.Builder binBonusLootPool(ItemLike item, Enchantment ench, float prob, int extra)
	{
		return createPoolBuilder()
				.add(LootItem.lootTableItem(item))
				.apply(ApplyBonusCount.addBonusBinomialDistributionCount(ench, prob, extra));
	}

	private <T extends Comparable<T> & StringRepresentable> LootItemCondition.Builder propertyIs(
			Supplier<? extends Block> b, Property<T> prop, T value
	)
	{
		return LootItemBlockStatePropertyCondition.hasBlockStateProperties(b.get())
				.setProperties(
						StatePropertiesPredicate.Builder.properties().hasProperty(prop, value)
				);
	}
}
