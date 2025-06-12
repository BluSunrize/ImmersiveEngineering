/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.loot;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.common.blocks.IEEntityBlock;
import blusunrize.immersiveengineering.common.blocks.generic.PostBlock;
import blusunrize.immersiveengineering.common.blocks.generic.PostBlock.HorizontalOffset;
import blusunrize.immersiveengineering.common.blocks.metal.CapacitorBlockEntity;
import blusunrize.immersiveengineering.common.blocks.metal.ConveyorBlock;
import blusunrize.immersiveengineering.common.blocks.plant.HempBlock;
import blusunrize.immersiveengineering.common.blocks.wooden.SawdustBlock;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEBlocks.*;
import blusunrize.immersiveengineering.common.register.IEItems;
import blusunrize.immersiveengineering.common.register.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.register.IEItems.ItemRegObject;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import blusunrize.immersiveengineering.common.util.loot.*;
import com.google.common.collect.ImmutableList;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
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
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.data.loot.GeneralLoot.key;

public class BlockLoot implements LootTableSubProvider
{
	private final Set<ResourceKey<LootTable>> generatedTables = new HashSet<>();
	private BiConsumer<ResourceKey<LootTable>, LootTable.Builder> out;
	private final Provider provider;

	public BlockLoot(Provider p)
	{
		this.provider = p;
	}

	private ResourceKey<LootTable> toTableLoc(ResourceLocation in)
	{
		return key("blocks/"+in.getPath());
	}

	@Override
	public void generate(BiConsumer<ResourceKey<LootTable>, Builder> out)
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
		register(WoodenDevices.SORTER, tileDrop());
		register(WoodenDevices.FLUID_SORTER, tileDrop());
		register(StoneDecoration.CORESAMPLE, tileDrop());
		register(MetalDevices.TOOLBOX, tileDrop());
		register(Cloth.SHADER_BANNER, singleItem(Items.WHITE_BANNER));
		register(Cloth.SHADER_BANNER_WALL, singleItem(Items.WHITE_BANNER));
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

		registerDoor(WoodenDecoration.DOOR);
		registerDoor(WoodenDecoration.DOOR_FRAMED);
		registerDoor(MetalDecoration.STEEL_DOOR);

		registerPost(WoodenDecoration.TREATED_POST);
		registerPost(MetalDecoration.STEEL_POST);
		registerPost(MetalDecoration.ALU_POST);

		registerMultiblocks();

		registerSelfDropping(WoodenDevices.CRAFTING_TABLE, dropInv());
		registerSelfDropping(WoodenDevices.WORKBENCH, dropInv());
		registerSelfDropping(WoodenDevices.BLUEPRINT_SHELF, dropInv());
		registerSelfDropping(WoodenDevices.CIRCUIT_TABLE, dropInv());
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
		registerMultiblock(IEMultiblockLogic.COKE_OVEN);
		registerMultiblock(IEMultiblockLogic.BLAST_FURNACE);
		registerMultiblock(IEMultiblockLogic.ALLOY_SMELTER);
		registerMultiblock(IEMultiblockLogic.ADV_BLAST_FURNACE);

		registerMultiblock(IEMultiblockLogic.METAL_PRESS);
		registerMultiblock(IEMultiblockLogic.CRUSHER);
		registerMultiblock(IEMultiblockLogic.SAWMILL);
		registerMultiblock(IEMultiblockLogic.TANK);
		registerMultiblock(IEMultiblockLogic.SILO);
		registerMultiblock(IEMultiblockLogic.ASSEMBLER);
		registerMultiblock(IEMultiblockLogic.AUTO_WORKBENCH);
		registerMultiblock(IEMultiblockLogic.BOTTLING_MACHINE);
		registerMultiblock(IEMultiblockLogic.SQUEEZER);
		registerMultiblock(IEMultiblockLogic.FERMENTER);
		registerMultiblock(IEMultiblockLogic.REFINERY);
		registerMultiblock(IEMultiblockLogic.DIESEL_GENERATOR);
		registerMultiblock(IEMultiblockLogic.EXCAVATOR);
		registerMultiblock(IEMultiblockLogic.BUCKET_WHEEL);
		registerMultiblock(IEMultiblockLogic.ARC_FURNACE);
		registerMultiblock(IEMultiblockLogic.LIGHTNING_ROD);
		registerMultiblock(IEMultiblockLogic.MIXER);
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

	private void registerMultiblock(MultiblockRegistration<?> registration)
	{
		registerMultiblock(registration.block());
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
				.add(LootUtils.getMultiblockDropBuilder());
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
		register(BuiltInRegistries.BLOCK.getKey(b.get()), table);
	}

	private void register(ResourceLocation name, LootTable.Builder table)
	{
		var loc = toTableLoc(name);
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
		ret.withPool(
				binBonusLootPool(Ingredients.HEMP_FIBER, Enchantments.FORTUNE, 4/8f, 3).when(
						LootItemBlockStatePropertyCondition.hasBlockStateProperties(IEBlocks.Misc.HEMP_PLANT.get())
								.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(HempBlock.AGE, 4))
				)
		);
		ret.withPool(
				binBonusLootPool(Ingredients.HEMP_FIBER, Enchantments.FORTUNE, 5/8f, 3).when(
						LootItemBlockStatePropertyCondition.hasBlockStateProperties(IEBlocks.Misc.HEMP_PLANT.get())
								.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(HempBlock.HALF, DoubleBlockHalf.UPPER))
				)
		);
		register(IEBlocks.Misc.HEMP_PLANT, ret);
	}


	private void registerOre(EnumMetals metal)
	{
		registerOre(metal, IEBlocks.Metals.ORES.get(metal));
		registerOre(metal, IEBlocks.Metals.DEEPSLATE_ORES.get(metal));
	}

	private void registerOre(EnumMetals metal, BlockEntry<?> oreBlock)
	{
		final var enchantments = this.provider.lookupOrThrow(Registries.ENCHANTMENT);
		ItemRegObject<Item> rawOre = IEItems.Metals.RAW_ORES.get(metal);
		LootTable.Builder ret = LootTable.lootTable().withPool(LootPool.lootPool()
				.setRolls(ConstantValue.exactly(1.0F))
				.add(LootItem.lootTableItem(oreBlock)
						// if silk touch
						.when(MatchTool.toolMatches(ItemPredicate.Builder.item().withSubPredicate(
								ItemSubPredicates.ENCHANTMENTS,
								ItemEnchantmentsPredicate.enchantments(
										List.of(new EnchantmentPredicate(enchantments.getOrThrow(Enchantments.SILK_TOUCH), MinMaxBounds.Ints.atLeast(1))))
						)))
						// else
						.otherwise(LootItem.lootTableItem(rawOre)
								.apply(ApplyBonusCount.addOreBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE)))
								.apply(ApplyExplosionDecay.explosionDecay())
						)
				)
		);
		register(oreBlock, ret);
	}

	private void registerDoor(Supplier<? extends Block> b)
	{
		LootPool.Builder ret = createPoolBuilder().setRolls(ConstantValue.exactly(1.0F))
				.add(LootItem.lootTableItem(b.get()).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(b.get()).setProperties(
						StatePropertiesPredicate.Builder.properties().hasProperty(DoorBlock.HALF, DoubleBlockHalf.LOWER))
				));
		register(b, ret);
	}

	private void registerPost(Supplier<? extends Block> b)
	{
		LootPool.Builder ret = createPoolBuilder().setRolls(ConstantValue.exactly(1.0F))
				.add(LootItem.lootTableItem(b.get()).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(b.get()).setProperties(
						StatePropertiesPredicate.Builder.properties().hasProperty(PostBlock.HORIZONTAL_OFFSET, HorizontalOffset.NONE))
				));
		register(b, ret);
	}

	private void registerSawdust()
	{
		LootTable.Builder ret = LootTable.lootTable()
				.withPool(singleItem(WoodenDecoration.SAWDUST))
				.apply(new PropertyCountLootFunction.Builder(SawdustBlock.LAYERS.getName()));
		register(WoodenDecoration.SAWDUST, ret);
	}

	private LootPool.Builder binBonusLootPool(ItemLike item, ResourceKey<Enchantment> ench, float prob, int extra)
	{
		final var enchantments = this.provider.lookupOrThrow(Registries.ENCHANTMENT);
		return createPoolBuilder()
				.add(LootItem.lootTableItem(item))
				.apply(ApplyBonusCount.addBonusBinomialDistributionCount(enchantments.getOrThrow(ench), prob, extra));
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
