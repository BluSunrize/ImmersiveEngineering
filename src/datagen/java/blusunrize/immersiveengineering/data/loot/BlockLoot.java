/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.loot;

import blusunrize.immersiveengineering.common.blocks.generic.GenericTileBlock;
import blusunrize.immersiveengineering.common.blocks.metal.CapacitorTileEntity;
import blusunrize.immersiveengineering.common.blocks.plant.EnumHempGrowth;
import blusunrize.immersiveengineering.common.blocks.plant.HempBlock;
import blusunrize.immersiveengineering.common.blocks.wooden.SawdustBlock;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEBlocks.*;
import blusunrize.immersiveengineering.common.register.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.loot.*;
import com.google.common.collect.ImmutableList;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
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
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
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
		register(StoneDecoration.concreteSprayed, LootTable.lootTable());
		register(WoodenDevices.windmill, LootTable.lootTable().withPool(
				createPoolBuilder().add(
						LootItem.lootTableItem(WoodenDevices.windmill)
								.apply(new WindmillLootFunction.Builder())
				)));

		LootPoolEntryContainer.Builder<?> tileOrInv = AlternativesEntry.alternatives(
				TileDropLootEntry.builder().when(ExplosionCondition.survivesExplosion()),
				DropInventoryLootEntry.builder()
		);
		register(WoodenDevices.crate, LootPool.lootPool().add(tileOrInv));
		register(WoodenDevices.reinforcedCrate, tileDrop());
		register(StoneDecoration.coresample, tileDrop());
		register(MetalDevices.toolbox, tileDrop());
		register(Cloth.shaderBanner, tileDrop());
		register(Cloth.shaderBannerWall, tileDrop());
		register(Cloth.curtain, tileDrop());
		for(BlockEntry<? extends GenericTileBlock<? extends CapacitorTileEntity>> cap : ImmutableList.of(
				MetalDevices.capacitorLV, MetalDevices.capacitorMV, MetalDevices.capacitorHV, MetalDevices.capacitorCreative
		))
			register(cap, tileDrop());
		register(Connectors.feedthrough, tileDrop());
		register(MetalDevices.turretChem, tileDrop());
		register(MetalDevices.turretGun, tileDrop(), dropInv());
		register(WoodenDevices.woodenBarrel, tileDrop());
		register(WoodenDevices.logicUnit, tileDrop());
		register(MetalDevices.barrel, tileDrop());

		registerMultiblocks();

		registerSelfDropping(WoodenDevices.craftingTable, dropInv());
		registerSelfDropping(WoodenDevices.workbench, dropInv());
		registerSelfDropping(WoodenDevices.itemBatcher, dropInv());
		registerSelfDropping(MetalDevices.cloche, dropInv());
		registerSelfDropping(MetalDevices.chargingStation, dropInv());
		registerSlabs();
		registerSawdust();

		registerAllRemainingAsDefault();
	}

	private void registerMultiblocks()
	{
		registerMultiblock(Multiblocks.cokeOven);
		registerMultiblock(Multiblocks.blastFurnace);
		registerMultiblock(Multiblocks.alloySmelter);
		registerMultiblock(Multiblocks.blastFurnaceAdv);

		registerMultiblock(Multiblocks.metalPress);
		registerMultiblock(Multiblocks.crusher);
		registerMultiblock(Multiblocks.sawmill);
		registerMultiblock(Multiblocks.tank);
		registerMultiblock(Multiblocks.silo);
		registerMultiblock(Multiblocks.assembler);
		registerMultiblock(Multiblocks.autoWorkbench);
		registerMultiblock(Multiblocks.bottlingMachine);
		registerMultiblock(Multiblocks.squeezer);
		registerMultiblock(Multiblocks.fermenter);
		registerMultiblock(Multiblocks.refinery);
		registerMultiblock(Multiblocks.dieselGenerator);
		registerMultiblock(Multiblocks.excavator);
		registerMultiblock(Multiblocks.bucketWheel);
		registerMultiblock(Multiblocks.arcFurnace);
		registerMultiblock(Multiblocks.lightningrod);
		registerMultiblock(Multiblocks.mixer);
	}

	private void registerSlabs()
	{
		for(BlockEntry<SlabBlock> slab : IEBlocks.toSlab.values())
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
				.add(TileDropLootEntry.builder());
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
				.withPool(singleItem(Misc.hempSeeds));
		for(EnumHempGrowth g : EnumHempGrowth.values())
			if(g==HempBlock.getMaxGrowth(g))
			{
				ret.withPool(
						binBonusLootPool(Ingredients.hempFiber, Enchantments.BLOCK_FORTUNE, g.ordinal()/8f, 3)
								.when(propertyIs(IEBlocks.Misc.hempPlant, HempBlock.GROWTH, g))
				);
			}
		register(IEBlocks.Misc.hempPlant, ret);
	}

	private void registerSawdust()
	{
		LootTable.Builder ret = LootTable.lootTable()
				.withPool(singleItem(WoodenDecoration.sawdust))
				.apply(new PropertyCountLootFunction.Builder(SawdustBlock.LAYERS.getName()));
		register(WoodenDecoration.sawdust, ret);
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
