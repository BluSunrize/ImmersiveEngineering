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
import blusunrize.immersiveengineering.common.blocks.IEBlocks.*;
import blusunrize.immersiveengineering.common.blocks.plant.EnumHempGrowth;
import blusunrize.immersiveengineering.common.blocks.plant.HempBlock;
import blusunrize.immersiveengineering.common.data.loot.LootGenerator;
import blusunrize.immersiveengineering.common.items.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.loot.DropInventoryLootEntry;
import blusunrize.immersiveengineering.common.util.loot.MBOriginalBlockLootEntry;
import blusunrize.immersiveengineering.common.util.loot.TileDropLootEntry;
import blusunrize.immersiveengineering.common.util.loot.WindmillLootFunction;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.state.IProperty;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.LootTable.Builder;
import net.minecraft.world.storage.loot.conditions.BlockStateProperty;
import net.minecraft.world.storage.loot.conditions.ILootCondition;
import net.minecraft.world.storage.loot.conditions.SurvivesExplosion;
import net.minecraft.world.storage.loot.functions.ApplyBonus;
import net.minecraft.world.storage.loot.functions.SetCount;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class BlockLoot extends LootGenerator
{
	public BlockLoot(DataGenerator gen)
	{
		super(gen);
	}

	@Nonnull
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
		register(WoodenDevices.crate, tileDrop());
		register(WoodenDevices.reinforcedCrate, tileDrop());
		register(StoneDecoration.coresample, tileDrop());
		register(MetalDevices.toolbox, tileDrop());
		register(Cloth.shaderBanner, tileDrop());
		register(Cloth.shaderBannerWall, tileDrop());
		register(Cloth.curtain, tileDrop());
		for(Block cap : new Block[]{MetalDevices.capacitorLV, MetalDevices.capacitorMV, MetalDevices.capacitorHV, MetalDevices.capacitorCreative})
			register(cap, tileDrop());
		register(Connectors.feedthrough, tileDrop());
		register(MetalDevices.turretChem, tileDrop());
		register(MetalDevices.turretGun, tileDrop(), dropInv());
		register(WoodenDevices.woodenBarrel, tileDrop());
		register(MetalDevices.barrel, tileDrop());

		registerMultiblocks();

		registerSelfDropping(WoodenDevices.craftingTable, dropInv());
		registerSelfDropping(WoodenDevices.workbench, dropInv());
		registerSelfDropping(MetalDevices.cloche, dropInv());
		registerSelfDropping(MetalDevices.chargingStation, dropInv());
		registerSlabs();

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
		for(SlabBlock slab : IEBlocks.toSlab.values())
		{
			LootFunction.Builder<?> doubleSlabFunction = SetCount.builder(new ConstantRange(2))
					.acceptCondition(propertyIs(slab, SlabBlock.TYPE, SlabType.DOUBLE));
			LootTable.Builder lootBuilder = LootTable.builder().addLootPool(
					singleItem(slab).acceptFunction(doubleSlabFunction)
			);
			register(slab, lootBuilder);
		}
	}

	private void registerAllRemainingAsDefault()
	{
		for(Block b : IEContent.registeredIEBlocks)
			if(!tables.containsKey(toTableLoc(b.getRegistryName())))
				registerSelfDropping(b);
	}

	private void registerMultiblock(Block b)
	{
		register(b, dropInv(), dropOriginalBlock());
	}

	private LootPool.Builder dropInv()
	{
		return createPoolBuilder()
				.addEntry(DropInventoryLootEntry.builder());
	}

	private LootPool.Builder tileDrop()
	{
		return createPoolBuilder()
				.addEntry(TileDropLootEntry.builder());
	}

	private LootPool.Builder dropOriginalBlock()
	{
		return createPoolBuilder()
				.addEntry(MBOriginalBlockLootEntry.builder());
	}

	private void register(Block b, LootPool.Builder... pools)
	{
		LootTable.Builder builder = LootTable.builder();
		for(LootPool.Builder pool : pools)
			builder.addLootPool(pool);
		register(b, builder);
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

	private void registerSelfDropping(Block b, LootPool.Builder... pool)
	{
		LootPool.Builder[] withSelf = Arrays.copyOf(pool, pool.length+1);
		withSelf[withSelf.length-1] = singleItem(b);
		register(b, withSelf);
	}

	private Builder dropProvider(IItemProvider in)
	{
		return LootTable
				.builder()
				.addLootPool(singleItem(in)
				);
	}

	private LootPool.Builder singleItem(IItemProvider in)
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
				.addLootPool(singleItem(Misc.hempSeeds));
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

	private <T extends Comparable<T> & IStringSerializable> ILootCondition.IBuilder propertyIs(Block b, IProperty<T> prop, T value)
	{
		return BlockStateProperty.builder(b)
				.fromProperties(
						StatePropertiesPredicate.Builder.newBuilder().withProp(prop, value)
				);
	}
}
