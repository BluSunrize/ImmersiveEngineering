/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.world;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.*;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.excavator.ExcavatorHandler;
import blusunrize.immersiveengineering.api.excavator.MineralVein;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.blocks.wooden.TreatedWoodStyles;
import blusunrize.immersiveengineering.common.items.RevolverItem;
import blusunrize.immersiveengineering.common.items.bullets.IEBullets;
import blusunrize.immersiveengineering.common.items.upgrades.ToolUpgrade;
import blusunrize.immersiveengineering.common.register.IEBannerPatterns;
import blusunrize.immersiveengineering.common.register.IEBlocks.*;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.register.IEItems;
import blusunrize.immersiveengineering.common.register.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.world.Villages.RerollingItemListing.GenerateOffer;
import blusunrize.immersiveengineering.mixin.accessors.HeroGiftsTaskAccess;
import blusunrize.immersiveengineering.mixin.accessors.TemplatePoolAccess;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool.Projection;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent.UpdateCause;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;
import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;
import static blusunrize.immersiveengineering.common.register.IEItems.Misc.TOOL_UPGRADES;
import static blusunrize.immersiveengineering.common.register.IEItems.Misc.WIRE_COILS;

@EventBusSubscriber(modid = Lib.MODID, bus = Bus.GAME)
public class Villages
{
	public static final ResourceLocation ENGINEER = rl("engineer");
	public static final ResourceLocation MACHINIST = rl("machinist");
	public static final ResourceLocation ELECTRICIAN = rl("electrician");
	public static final ResourceLocation OUTFITTER = rl("outfitter");
	public static final ResourceLocation GUNSMITH = rl("gunsmith");

	@SubscribeEvent
	public static void onTagsUpdated(TagsUpdatedEvent ev)
	{
		if(ev.getUpdateCause()!=UpdateCause.SERVER_DATA_LOAD)
			return;
		// Register engineer's houses for each biome
		for(String biome : new String[]{"plains", "snowy", "savanna", "desert", "taiga"})
			for(String type : new String[]{"engineer", "machinist", "electrician", "gunsmith", "outfitter"})
				addToPool(
						ResourceLocation.withDefaultNamespace("village/"+biome+"/houses"),
						rl("village/houses/"+biome+"_"+type),
						ev.getRegistryAccess()
				);
	}

	public static void init()
	{
		// Register gifts
		HeroGiftsTaskAccess.getGifts().put(Registers.PROF_ENGINEER.value(), rl("gameplay/hero_of_the_village/engineer"));
		HeroGiftsTaskAccess.getGifts().put(Registers.PROF_MACHINIST.value(), rl("gameplay/hero_of_the_village/machinist"));
		HeroGiftsTaskAccess.getGifts().put(Registers.PROF_ELECTRICIAN.value(), rl("gameplay/hero_of_the_village/electrician"));
		HeroGiftsTaskAccess.getGifts().put(Registers.PROF_OUTFITTER.value(), rl("gameplay/hero_of_the_village/outfitter"));
		HeroGiftsTaskAccess.getGifts().put(Registers.PROF_GUNSMITH.value(), rl("gameplay/hero_of_the_village/gunsmith"));
	}

	private static void addToPool(ResourceLocation poolId, ResourceLocation toAdd, RegistryAccess regAccess)
	{
		Registry<StructureTemplatePool> registry = regAccess.registryOrThrow(Registries.TEMPLATE_POOL);
		StructureTemplatePool pool = Objects.requireNonNull(registry.get(poolId), poolId.getPath());
		TemplatePoolAccess poolAccess = (TemplatePoolAccess)pool;
		if(!(poolAccess.getRawTemplates() instanceof ArrayList))
			poolAccess.setRawTemplates(new ArrayList<>(poolAccess.getRawTemplates()));

		SinglePoolElement addedElement = SinglePoolElement.single(toAdd.toString()).apply(Projection.RIGID);
		poolAccess.getRawTemplates().add(Pair.of(addedElement, 1));
		poolAccess.getTemplates().add(addedElement);
	}

	public static class Registers
	{
		public static final DeferredRegister<PoiType> POINTS_OF_INTEREST = DeferredRegister.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE, ImmersiveEngineering.MODID);
		public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(
				BuiltInRegistries.VILLAGER_PROFESSION, ImmersiveEngineering.MODID
		);

		// TODO: Add more workstations. We need a different one for each profession
		public static final Holder<PoiType> POI_CRAFTINGTABLE = POINTS_OF_INTEREST.register(
				"craftingtable", () -> createPOI(assembleStates(WoodenDevices.CRAFTING_TABLE.get()))
		);
		public static final Holder<PoiType> POI_TURNTABLE = POINTS_OF_INTEREST.register(
				"turntable", () -> createPOI(assembleStates(WoodenDevices.TURNTABLE.get()))
		);
		public static final Holder<PoiType> POI_CIRCUITTABLE = POINTS_OF_INTEREST.register(
				"circuit_table", () -> createPOI(assembleStates(WoodenDevices.CIRCUIT_TABLE.get()))
		);
		public static final Holder<PoiType> POI_BANNER = POINTS_OF_INTEREST.register(
				"shaderbanner", () -> createPOI(assembleStates(Cloth.SHADER_BANNER.get()))
		);
		public static final Holder<PoiType> POI_WORKBENCH = POINTS_OF_INTEREST.register(
				"workbench", () -> createPOI(assembleStates(WoodenDevices.WORKBENCH.get()))
		);

		public static final Holder<VillagerProfession> PROF_ENGINEER = PROFESSIONS.register(
				ENGINEER.getPath(), () -> createProf(ENGINEER, POI_TURNTABLE, SoundEvents.VILLAGER_WORK_MASON)
		);
		public static final Holder<VillagerProfession> PROF_MACHINIST = PROFESSIONS.register(
				MACHINIST.getPath(), () -> createProf(MACHINIST, POI_CRAFTINGTABLE, SoundEvents.VILLAGER_WORK_TOOLSMITH)
		);
		public static final Holder<VillagerProfession> PROF_ELECTRICIAN = PROFESSIONS.register(
				ELECTRICIAN.getPath(), () -> createProf(ELECTRICIAN, POI_CIRCUITTABLE, IESounds.spark.value())
		);
		public static final Holder<VillagerProfession> PROF_OUTFITTER = PROFESSIONS.register(
				OUTFITTER.getPath(), () -> createProf(OUTFITTER, POI_BANNER, SoundEvents.VILLAGER_WORK_CARTOGRAPHER)
		);
		public static final Holder<VillagerProfession> PROF_GUNSMITH = PROFESSIONS.register(
				GUNSMITH.getPath(), () -> createProf(GUNSMITH, POI_WORKBENCH, IESounds.revolverReload.value())
		);

		private static PoiType createPOI(Collection<BlockState> block)
		{
			return new PoiType(ImmutableSet.copyOf(block), 1, 1);
		}

		private static VillagerProfession createProf(
				ResourceLocation name, Holder<PoiType> poi, SoundEvent sound
		)
		{
			ResourceKey<PoiType> poiName = poi.unwrapKey().orElseThrow();
			return new VillagerProfession(
					name.toString(),
					holder -> holder.is(poiName),
					holder -> holder.is(poiName),
					ImmutableSet.of(),
					ImmutableSet.of(),
					sound
			);
		}

		private static Collection<BlockState> assembleStates(Block block)
		{
			return block.getStateDefinition().getPossibleStates().stream().filter(blockState -> {
				if(blockState.hasProperty(IEProperties.MULTIBLOCKSLAVE))
					return !blockState.getValue(IEProperties.MULTIBLOCKSLAVE);
				return true;
			}).collect(Collectors.toList());
		}
	}

	@EventBusSubscriber(modid = MODID, bus = Bus.GAME)
	public static class Events
	{
		@SubscribeEvent
		public static void registerTrades(VillagerTradesEvent ev)
		{
			Int2ObjectMap<List<ItemListing>> trades = ev.getTrades();
			final ResourceLocation typeName = ResourceLocation.parse(ev.getType().name());
			if(ENGINEER.equals(typeName))
			{
				/* Structural Engineer
				 * Sells various construction materials
				 */
				trades.get(1).add(new TradeListing(EMERALD_FOR_ITEM, IETags.getItemTag(IETags.treatedWood), WoodenDecoration.TREATED_WOOD.get(TreatedWoodStyles.HORIZONTAL), new PriceInterval(4, 6), 16, 2));
				trades.get(1).add(new TradeListing(ITEMS_FOR_ONE_EMERALD, WoodenDecoration.TREATED_SCAFFOLDING, new PriceInterval(5, 8), 16, 1));
				trades.get(1).add(new TradeListing(ITEMS_FOR_ONE_EMERALD, Cloth.BALLOON, new PriceInterval(1, 3), 12, 2));

				trades.get(2).add(new TradeListing(EMERALD_FOR_ITEM, IETags.ironRod, Ingredients.STICK_IRON, new PriceInterval(4, 8), 16, 10));
				trades.get(2).add(new TradeListing(ITEMS_FOR_ONE_EMERALD, MetalDecoration.STEEL_SCAFFOLDING.get(MetalScaffoldingType.STANDARD), new PriceInterval(2, 4), 12, 5));
				trades.get(2).add(new TradeListing(ITEMS_FOR_ONE_EMERALD, MetalDecoration.ALU_SCAFFOLDING.get(MetalScaffoldingType.STANDARD), new PriceInterval(2, 4), 12, 5));

				trades.get(3).add(new TradeListing(ITEMS_FOR_ONE_EMERALD, WIRE_COILS.get(WireType.STRUCTURE_ROPE), new PriceInterval(4, 6), 16, 10));
				trades.get(3).add(new TradeListing(EMERALD_FOR_ITEM, Connectors.CONNECTOR_STRUCTURAL, new PriceInterval(4, 8), 16, 10));
				trades.get(3).add(new TradeListing(EMERALD_FOR_ITEM, StoneDecoration.CONCRETE, new PriceInterval(4, 8), 8, 15));
				trades.get(3).add(new TradeListing(ITEMS_FOR_ONE_EMERALD, StoneDecoration.CONCRETE_LEADED, new PriceInterval(4, 6), 16, 10));

				trades.get(4).add(new TradeListing(ITEMS_FOR_ONE_EMERALD, WoodenDecoration.TREATED_POST, new PriceInterval(2, 3), 8, 20));
				trades.get(4).add(new TradeListing(EMERALD_FOR_ITEM, StoneDecoration.INSULATING_GLASS, new PriceInterval(2, 6), 16, 10));
				trades.get(4).add(new TradeListing(EMERALD_FOR_ITEM, StoneDecoration.DUROPLAST, new PriceInterval(2, 6), 16, 10));
				trades.get(4).add(OreveinMapForEmeralds.INSTANCE);

				trades.get(5).add(OreveinMapForEmeralds.INSTANCE);
			}
			else if(MACHINIST.equals(typeName))
			{
				/* Machinist
				 * Sells tools, metals, blueprints and drillheads
				 */
				trades.get(1).add(new TradeListing(EMERALD_FOR_ITEM, IETags.coalCoke, Ingredients.COAL_COKE, new PriceInterval(8, 16), 16, 2));
				trades.get(1).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, Tools.HAMMER, new PriceInterval(1, 3), 12, 1).setMultiplier(0.2f));
				trades.get(1).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, BlueprintCraftingRecipe.getTypedBlueprint("components"), new PriceInterval(6, 16), 3, 5).setMultiplier(0.2f));

				trades.get(2).add(new TradeListing(EMERALD_FOR_ITEM, IETags.getTagsFor(EnumMetals.STEEL).ingot, IEItems.Metals.INGOTS.get(EnumMetals.STEEL), new PriceInterval(3, 6), 12, 10));
				trades.get(2).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, Ingredients.COMPONENT_STEEL, new PriceInterval(1, 3), 12, 5));
				trades.get(2).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, Ingredients.WATERWHEEL_SEGMENT, new PriceInterval(1, 3), 8, 10));

				trades.get(3).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, Tools.TOOLBOX, new PriceInterval(6, 8), 3, 20).setMultiplier(0.2f));
				trades.get(3).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, Tools.DRILLHEAD_IRON, new PriceInterval(20, 40), 3, 15).setMultiplier(0.2f));
				trades.get(3).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, Tools.SAWBLADE, new PriceInterval(20, 40), 3, 15).setMultiplier(0.2f));
				trades.get(3).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, IEItems.Misc.EARMUFFS, new PriceInterval(4, 9), 3, 20).setMultiplier(0.2f));

				trades.get(4).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, Tools.DRILLHEAD_STEEL, new PriceInterval(32, 48), 3, 30).setMultiplier(0.2f));
				trades.get(4).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, TOOL_UPGRADES.get(ToolUpgrade.DRILL_LUBE), new PriceInterval(5, 10), 8, 10));
				trades.get(4).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, TOOL_UPGRADES.get(ToolUpgrade.DRILL_CAPACITY), new PriceInterval(5, 10), 8, 10));
				trades.get(4).add(new TradeListing(EMERALD_FOR_ITEM, MetalDecoration.ENGINEERING_LIGHT, new PriceInterval(1, 2), 16, 10));

				trades.get(5).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, TOOL_UPGRADES.get(ToolUpgrade.DRILL_FORTUNE), new PriceInterval(5, 10), 8, 10));
			}
			else if(ELECTRICIAN.equals(typeName))
			{
				/* Electrician
				 * Sells wires, tools and the faraday suit
				 */
				trades.get(1).add(new TradeListing(EMERALD_FOR_ITEM, IETags.copperWire, Ingredients.WIRE_COPPER, new PriceInterval(8, 16), 16, 2));
				trades.get(1).add(new TradeListing(ITEMS_FOR_ONE_EMERALD, WIRE_COILS.get(WireType.COPPER), new PriceInterval(2, 4), 16, 1));
				trades.get(1).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, Tools.WIRECUTTER, new PriceInterval(1, 3), 12, 1).setMultiplier(0.2f));
				trades.get(1).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, Tools.SCREWDRIVER, new PriceInterval(1, 3), 12, 1).setMultiplier(0.2f));

				trades.get(2).add(new TradeListing(EMERALD_FOR_ITEM, IETags.electrumWire, Ingredients.WIRE_ELECTRUM, new PriceInterval(6, 12), 16, 5));
				trades.get(2).add(new TradeListing(ITEMS_FOR_ONE_EMERALD, WIRE_COILS.get(WireType.ELECTRUM), new PriceInterval(1, 4), 16, 5));
				trades.get(2).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, Tools.VOLTMETER, new PriceInterval(1, 3), 12, 5).setMultiplier(0.2f));
				trades.get(2).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, IEItems.Misc.FARADAY_SUIT.get(ArmorItem.Type.BOOTS), new PriceInterval(5, 7), 3, 15).setMultiplier(0.2f));
				trades.get(2).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, IEItems.Misc.FARADAY_SUIT.get(ArmorItem.Type.LEGGINGS), new PriceInterval(9, 11), 3, 15).setMultiplier(0.2f));

				trades.get(3).add(new TradeListing(EMERALD_FOR_ITEM, IETags.aluminumWire, Ingredients.WIRE_ALUMINUM, new PriceInterval(4, 8), 16, 10));
				trades.get(3).add(new TradeListing(ITEMS_FOR_ONE_EMERALD, WIRE_COILS.get(WireType.STEEL), new PriceInterval(1, 2), 16, 10));
				trades.get(3).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, IEItems.Misc.FARADAY_SUIT.get(ArmorItem.Type.CHESTPLATE), new PriceInterval(11, 15), 3, 20).setMultiplier(0.2f));
				trades.get(3).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, IEItems.Misc.FARADAY_SUIT.get(ArmorItem.Type.HELMET), new PriceInterval(5, 7), 3, 20).setMultiplier(0.2f));

				trades.get(4).add(new TradeListing(EMERALD_FOR_ITEM, Ingredients.ELECTRON_TUBE, new PriceInterval(2, 6), 16, 10));
				trades.get(4).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, Ingredients.COMPONENT_ELECTRONIC, new PriceInterval(1, 3), 16, 15));
				trades.get(4).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, IEItems.Misc.FLUORESCENT_TUBE, new PriceInterval(3, 6), 3, 20).setMultiplier(0.2f));

				trades.get(5).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, MetalDevices.TESLA_COIL, new PriceInterval(20, 32), 12, 30));
				trades.get(5).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, Ingredients.CIRCUIT_BOARD, new PriceInterval(1, 2), 16, 5));
			}
			else if(OUTFITTER.equals(typeName))
			{
				/* Outfitter
				 * Sells Shaderbags
				 */
				ItemLike bag_common = IEItems.Misc.SHADER_BAG.get(Rarity.COMMON);
				ItemLike bag_uncommon = IEItems.Misc.SHADER_BAG.get(Rarity.UNCOMMON);
				ItemLike bag_rare = IEItems.Misc.SHADER_BAG.get(Rarity.RARE);
				ItemLike bag_epic = IEItems.Misc.SHADER_BAG.get(Rarity.EPIC);

				trades.get(1).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, bag_common, new PriceInterval(1, 8), 16, 2));
				trades.get(1).add(new TradeListing(EMERALD_FOR_ITEM, Ingredients.HEMP_FABRIC, new PriceInterval(8, 12), 16, 1));

				trades.get(2).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, bag_uncommon, new PriceInterval(4, 12), 16, 5));
				trades.get(2).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, IEBannerPatterns.HAMMER.item(), new PriceInterval(10, 20), 3, 20));
				trades.get(2).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, IEBannerPatterns.WINDMILL.item(), new PriceInterval(4, 20), 3, 20));
				trades.get(2).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, IEBannerPatterns.ORNATE.item(), new PriceInterval(4, 20), 3, 20));

				trades.get(3).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, bag_rare, new PriceInterval(8, 16), 16, 15));
				trades.get(3).add(new TradeListing(EMERALD_FOR_ITEM, IETags.getTagsFor(EnumMetals.SILVER).dust, IEItems.Metals.DUSTS.get(EnumMetals.SILVER), new PriceInterval(4, 12), 16, 10));
				trades.get(3).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, bag_uncommon, new PriceInterval(4, 12), 8, 5));

				trades.get(4).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, bag_epic, new PriceInterval(12, 20), 3, 20));
				trades.get(4).add(new TradeListing(EMERALD_FOR_ITEM, IETags.getTagsFor(EnumMetals.GOLD).dust, IEItems.Metals.DUSTS.get(EnumMetals.SILVER), new PriceInterval(4, 12), 16, 15));
				trades.get(4).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, bag_rare, new PriceInterval(8, 16), 8, 15));

				trades.get(5).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, bag_epic, new PriceInterval(16, 28), 3, 30).setMultiplier(0.2f));
			}
			else if(GUNSMITH.equals(typeName))
			{
				/* Gunsmith
				 * Sells ammunition, blueprints and revolver parts
				 */
				trades.get(1).add(new TradeListing(EMERALD_FOR_ITEM, Ingredients.EMPTY_CASING, new PriceInterval(6, 12), 16, 2));
				trades.get(1).add(new TradeListing(EMERALD_FOR_ITEM, Ingredients.EMPTY_SHELL, new PriceInterval(6, 12), 16, 2));
				trades.get(1).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, Ingredients.WOODEN_GRIP, new PriceInterval(2, 4), 1, 12).setMultiplier(0.2f));

				trades.get(2).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, BlueprintCraftingRecipe.getTypedBlueprint("bullet"), new PriceInterval(3, 6), 1, 25));
				trades.get(2).add(new TradeListing(ITEMS_FOR_ONE_EMERALD, BulletHandler.getBulletStack(IEBullets.CASULL), new PriceInterval(2, 4), 12, 5));
				trades.get(2).add(new TradeListing(ITEMS_FOR_ONE_EMERALD, BulletHandler.getBulletStack(IEBullets.BUCKSHOT), new PriceInterval(2, 6), 12, 5));
				trades.get(2).add(RevolverPieceForEmeralds.INSTANCE);

				trades.get(3).add(RevolverPieceForEmeralds.INSTANCE);
				trades.get(3).add(new TradeListing(ITEMS_FOR_ONE_EMERALD, BulletHandler.getBulletStack(IEBullets.FLARE), new PriceInterval(1, 2), 12, 10));
				trades.get(3).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, BlueprintCraftingRecipe.getTypedBlueprint("specialBullet"), new PriceInterval(5, 9), 1, 30).setMultiplier(0.2f));

				trades.get(4).add(RevolverPieceForEmeralds.INSTANCE);
				trades.get(4).add(new TradeListing(ITEMS_FOR_ONE_EMERALD, BulletHandler.getBulletStack(IEBullets.SILVER), new PriceInterval(1, 4), 8, 15));
				trades.get(4).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, BulletHandler.getBulletStack(IEBullets.HIGH_EXPLOSIVE), new PriceInterval(2, 4), 8, 15));

				trades.get(5).add(RevolverPieceForEmeralds.INSTANCE);
				trades.get(5).add(RevolverPieceForEmeralds.INSTANCE);
				trades.get(5).add(RevolverPieceForEmeralds.INSTANCE);
			}
		}


		@SubscribeEvent
		public static void onTrade(TradeWithVillagerEvent ev)
		{
			AbstractVillager villager = ev.getAbstractVillager();
			CompoundTag randomizedOffers = villager.getPersistentData().getCompound(RerollingItemListing.RANDOMIZED_OFFERS_KEY);
			int offerIndex = villager.getOffers().indexOf(ev.getMerchantOffer());
			String offerIndexS = String.valueOf(offerIndex);
			if(randomizedOffers.contains(offerIndexS))
			{
				GenerateOffer offerFunction = RerollingItemListing.OFFER_FUNCTIONS.get(randomizedOffers.getString(offerIndexS));
				MerchantOffer offer = offerFunction.generateOffer(villager, ev.getEntity(), villager.getRandom());
				offer.increaseUses();
				villager.getOffers().set(offerIndex, offer);
			}
		}
	}

	private static class TradeListing implements ItemListing
	{
		private final TradeOutline outline;
		private final LazyItemStack lazyItem;
		private final PriceInterval priceInfo;
		private final int maxUses;
		private final int xp;
		private float priceMultiplier = 0.05f;

		public TradeListing(@Nonnull TradeOutline outline, @Nonnull Function<Level, ItemStack> item, @Nonnull PriceInterval priceInfo, int maxUses, int xp)
		{
			this.outline = outline;
			this.lazyItem = new LazyItemStack(item);
			this.priceInfo = priceInfo;
			this.maxUses = maxUses;
			this.xp = xp;
		}

		public TradeListing(@Nonnull TradeOutline outline, @Nonnull ItemStack itemStack, @Nonnull PriceInterval buyAmounts, int maxUses, int xp)
		{
			this(outline, l -> itemStack, buyAmounts, maxUses, xp);
		}

		public TradeListing(@Nonnull TradeOutline outline, @Nonnull ItemLike item, @Nonnull PriceInterval buyAmounts, int maxUses, int xp)
		{
			this(outline, new ItemStack(item), buyAmounts, maxUses, xp);
		}

		public TradeListing(@Nonnull TradeOutline outline, @Nonnull TagKey<Item> tag, @Nonnull ItemLike backup, @Nonnull PriceInterval buyAmounts, int maxUses, int xp)
		{
			this(outline, l -> {
				ItemStack fromTag = l!=null?IEApi.getPreferredTagStack(l.registryAccess(), tag): ItemStack.EMPTY;
				return fromTag.isEmpty()?new ItemStack(backup): fromTag;
			}, buyAmounts, maxUses, xp);
		}

		public TradeListing setMultiplier(float priceMultiplier)
		{
			this.priceMultiplier = priceMultiplier;
			return this;
		}

		@Nullable
		@Override
		public MerchantOffer getOffer(@Nullable Entity trader, @Nonnull RandomSource rand)
		{
			ItemStack buying = this.lazyItem.apply(trader!=null?trader.level(): null);
			return this.outline.generateOffer(buying, priceInfo, rand, maxUses, xp, priceMultiplier);
		}
	}

	public record RerollingItemListing(String functionKey) implements ItemListing
	{
		private static final String RANDOMIZED_OFFERS_KEY = "immersiveengineering:randomized_offers";
		private static final Map<String, GenerateOffer> OFFER_FUNCTIONS = new HashMap<>();

		public static RerollingItemListing register(String key, GenerateOffer function)
		{
			OFFER_FUNCTIONS.put(key, function);
			return new RerollingItemListing(key);
		}

		@Override
		public MerchantOffer getOffer(Entity trader, @Nonnull RandomSource random)
		{
			Player player = null;
			if(trader instanceof AbstractVillager villager&&!villager.level().isClientSide())
			{
				player = villager.getTradingPlayer();

				// make note that this is a randomized trade
				CompoundTag traderData = trader.getPersistentData();
				CompoundTag randomizedOffers = traderData.getCompound(RANDOMIZED_OFFERS_KEY);
				int offerIndex = villager.getOffers().size();
				randomizedOffers.putString(String.valueOf(offerIndex), this.functionKey);
				traderData.put(RANDOMIZED_OFFERS_KEY, randomizedOffers);
			}
			return OFFER_FUNCTIONS.get(functionKey).generateOffer(trader, player, random);
		}

		@FunctionalInterface
		public interface GenerateOffer
		{
			MerchantOffer generateOffer(Entity trader, @Nullable Player player, @Nonnull RandomSource random);
		}

	}

	private static class OreveinMapForEmeralds
	{
		private static final int SEARCH_RADIUS = 16*16;
		private static final String TRADER_SOLD_KEY = "immersiveengineering:mapped_veins";

		public static RerollingItemListing INSTANCE = RerollingItemListing.register("orevein_map", (trader, player, random) -> {
			if(trader==null)
				return null;
			Level world = trader.getCommandSenderWorld();
			BlockPos merchantPos = trader.blockPosition();
			// extract list of already sold veins from the trader
			CompoundTag traderData = trader.getPersistentData();
			List<Long> soldMaps = new ArrayList<>();
			if(traderData.contains(TRADER_SOLD_KEY))
				for(long l : traderData.getLongArray(TRADER_SOLD_KEY))
					soldMaps.add(l);
			// get veins in 16 chunk radius, ordered by their rarity (lowest weight first)
			List<MineralVein> veins = ExcavatorHandler.findVeinsForVillager(world, merchantPos, SEARCH_RADIUS, soldMaps);
			if(!veins.isEmpty())
			{
				//select random vein from top 10
				int select = random.nextInt(Math.min(10, veins.size()));
				MineralVein vein = veins.get(select);
				ColumnPos veinPos = vein.getPos();
				// store sold map in trader data
				soldMaps.add(veinPos.toLong());
				traderData.putLongArray(TRADER_SOLD_KEY, soldMaps);
				// build map
				BlockPos blockPos = new BlockPos(veinPos.x(), 64, veinPos.z());
				ItemStack selling = MapItem.create(world, blockPos.getX(), blockPos.getZ(), (byte)1, true, true);
				MapItem.lockMap(world, selling);
				MapItemSavedData.addTargetDecoration(selling, blockPos, "ie:coresample_treasure", MapDecorationTypes.RED_X);
				selling.set(
						DataComponents.ITEM_NAME,
						Component.translatable("item.immersiveengineering.map_orevein")
				);
				selling.set(
						DataComponents.LORE,
						new ItemLore(List.of(Component.translatable(vein.getMineral(world).getTranslationKey(vein.getMineralName()))))
				);
				// return offer
				return new MerchantOffer(
						new ItemCost(Items.EMERALD, 8+random.nextInt(8)),
						Optional.of(new ItemCost(Items.COMPASS)),
						selling, 0, 1, 30, 0.5F
				);
			}
			return null;
		});
	}

	private static class RevolverPieceForEmeralds
	{
		public static RerollingItemListing INSTANCE = RerollingItemListing.register("revolver_piece", (trader, player, random) -> {
			int part = random.nextInt(3);

			ItemStack stack = new ItemStack(part==0?Ingredients.GUNPART_BARREL: part==1?Ingredients.GUNPART_DRUM: Ingredients.GUNPART_HAMMER);

			float luck = player==null?0: player.getLuck();
			var perksTag = RevolverItem.RevolverPerk.generatePerkSet(random, luck);
			stack.set(IEDataComponents.REVOLVER_PERKS, perksTag);
			int tier = Math.max(1, RevolverItem.RevolverPerk.calculateTier(perksTag));

			return new MerchantOffer(new ItemCost(Items.EMERALD, 5*tier+random.nextInt(5)), stack, 1, 45, 0.25F);
		});
	}

	/**
	 * Lazy-loaded ItemStack to support tag-based trades
	 */
	private static class LazyItemStack implements Function<Level, ItemStack>
	{
		private final Function<Level, ItemStack> function;
		private ItemStack instance;

		private LazyItemStack(Function<Level, ItemStack> function)
		{
			this.function = function;
		}

		@Override
		public ItemStack apply(Level level)
		{
			if(instance==null)
				instance = function.apply(level);
			return instance;
		}
	}

	/**
	 * Functional interface to create constant implementations from
	 */
	@FunctionalInterface
	private interface TradeOutline
	{
		MerchantOffer generateOffer(ItemStack item, PriceInterval priceInfo, RandomSource random, int maxUses, int xp, float priceMultiplier);
	}

	private static final TradeOutline EMERALD_FOR_ITEM = (buying, priceInfo, random, maxUses, xp, priceMultiplier) -> new MerchantOffer(
			new ItemCost(buying.getItem(), priceInfo.getPrice(random)),
			new ItemStack(Items.EMERALD),
			maxUses, xp, priceMultiplier
	);
	private static final TradeOutline ONE_ITEM_FOR_EMERALDS = (selling, priceInfo, random, maxUses, xp, priceMultiplier) -> new MerchantOffer(
			new ItemCost(Items.EMERALD, priceInfo.getPrice(random)),
			selling,
			maxUses, xp, priceMultiplier
	);
	private static final TradeOutline ITEMS_FOR_ONE_EMERALD = (selling, priceInfo, random, maxUses, xp, priceMultiplier) -> new MerchantOffer(
			new ItemCost(Items.EMERALD),
			selling.copyWithCount(priceInfo.getPrice(random)),
			maxUses, xp, priceMultiplier
	);

	private record PriceInterval(int min, int max)
	{
		int getPrice(RandomSource rand)
		{
			return min >= max?min: min+rand.nextInt(max-min+1);
		}
	}
}
