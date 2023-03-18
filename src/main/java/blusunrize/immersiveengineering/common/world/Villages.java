/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.world;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.excavator.ExcavatorHandler;
import blusunrize.immersiveengineering.api.excavator.MineralVein;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.blocks.wooden.TreatedWoodStyles;
import blusunrize.immersiveengineering.common.items.BulletItem;
import blusunrize.immersiveengineering.common.items.RevolverItem;
import blusunrize.immersiveengineering.common.items.ToolUpgradeItem.ToolUpgrade;
import blusunrize.immersiveengineering.common.register.IEBlocks.*;
import blusunrize.immersiveengineering.common.register.IEItems;
import blusunrize.immersiveengineering.common.register.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.mixin.accessors.HeroGiftsTaskAccess;
import blusunrize.immersiveengineering.mixin.accessors.SingleJigsawAccess;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.*;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool.Projection;
import net.minecraft.world.level.saveddata.maps.MapDecoration.Type;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;
import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;
import static blusunrize.immersiveengineering.common.register.IEItems.Misc.TOOL_UPGRADES;
import static blusunrize.immersiveengineering.common.register.IEItems.Misc.WIRE_COILS;

public class Villages
{
	public static final ResourceLocation ENGINEER = rl("engineer");
	public static final ResourceLocation MACHINIST = rl("machinist");
	public static final ResourceLocation ELECTRICIAN = rl("electrician");
	public static final ResourceLocation OUTFITTER = rl("outfitter");
	public static final ResourceLocation GUNSMITH = rl("gunsmith");

	public static void init()
	{
		PlainVillagePools.bootstrap();
		SnowyVillagePools.bootstrap();
		SavannaVillagePools.bootstrap();
		DesertVillagePools.bootstrap();
		TaigaVillagePools.bootstrap();

		// Register engineer's houses for each biome
		for(String biome : new String[]{"plains", "snowy", "savanna", "desert", "taiga"})
			for(String type : new String[]{"engineer", "machinist", "electrician", "gunsmith", "outfitter"})
				addToPool(new ResourceLocation("village/"+biome+"/houses"), rl("village/houses/"+biome+"_"+type), 1);

		// Register gifts
		HeroGiftsTaskAccess.getGifts().put(Registers.PROF_ENGINEER.get(), rl("gameplay/hero_of_the_village/engineer"));
		HeroGiftsTaskAccess.getGifts().put(Registers.PROF_MACHINIST.get(), rl("gameplay/hero_of_the_village/machinist"));
		HeroGiftsTaskAccess.getGifts().put(Registers.PROF_ELECTRICIAN.get(), rl("gameplay/hero_of_the_village/electrician"));
		HeroGiftsTaskAccess.getGifts().put(Registers.PROF_OUTFITTER.get(), rl("gameplay/hero_of_the_village/outfitter"));
		HeroGiftsTaskAccess.getGifts().put(Registers.PROF_GUNSMITH.get(), rl("gameplay/hero_of_the_village/gunsmith"));
	}

	private static void addToPool(ResourceLocation pool, ResourceLocation toAdd, int weight)
	{
		StructureTemplatePool old = BuiltinRegistries.TEMPLATE_POOL.get(pool);
		int id = BuiltinRegistries.TEMPLATE_POOL.getId(old);

		// Fixed seed to prevent inconsistencies between different worlds
		List<StructurePoolElement> shuffled;
		if(old!=null)
			shuffled = old.getShuffledTemplates(RandomSource.create(0));
		else
			shuffled = ImmutableList.of();
		Object2IntMap<StructurePoolElement> newPieces = new Object2IntLinkedOpenHashMap<>();
		for(StructurePoolElement p : shuffled)
			newPieces.computeInt(p, (StructurePoolElement pTemp, Integer i) -> (i==null?0: i)+1);
		newPieces.put(SingleJigsawAccess.construct(
				Either.left(toAdd), ProcessorLists.EMPTY, Projection.RIGID
		), weight);
		List<Pair<StructurePoolElement, Integer>> newPieceList = newPieces.object2IntEntrySet().stream()
				.map(e -> Pair.of(e.getKey(), e.getIntValue()))
				.collect(Collectors.toList());

		ResourceLocation name = old.getName();
		((WritableRegistry<StructureTemplatePool>)BuiltinRegistries.TEMPLATE_POOL).registerOrOverride(
				OptionalInt.of(id),
				ResourceKey.create(BuiltinRegistries.TEMPLATE_POOL.key(), name),
				new StructureTemplatePool(pool, name, newPieceList),
				Lifecycle.stable()
		);
	}

	@Mod.EventBusSubscriber(modid = MODID, bus = Bus.MOD)
	public static class Registers
	{
		public static final DeferredRegister<PoiType> POINTS_OF_INTEREST = DeferredRegister.create(ForgeRegistries.POI_TYPES, ImmersiveEngineering.MODID);
		public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(
				ForgeRegistries.VILLAGER_PROFESSIONS, ImmersiveEngineering.MODID
		);

		// TODO: Add more workstations. We need a different one for each profession
		public static final RegistryObject<PoiType> POI_CRAFTINGTABLE = POINTS_OF_INTEREST.register(
				"craftingtable", () -> createPOI(assembleStates(WoodenDevices.CRAFTING_TABLE.get()))
		);
		public static final RegistryObject<PoiType> POI_TURNTABLE = POINTS_OF_INTEREST.register(
				"turntable", () -> createPOI(assembleStates(WoodenDevices.TURNTABLE.get()))
		);
		public static final RegistryObject<PoiType> POI_CIRCUITTABLE = POINTS_OF_INTEREST.register(
				"circuit_table", () -> createPOI(assembleStates(WoodenDevices.CIRCUIT_TABLE.get()))
		);
		public static final RegistryObject<PoiType> POI_BANNER = POINTS_OF_INTEREST.register(
				"shaderbanner", () -> createPOI(assembleStates(Cloth.SHADER_BANNER.get()))
		);
		public static final RegistryObject<PoiType> POI_WORKBENCH = POINTS_OF_INTEREST.register(
				"workbench", () -> createPOI(assembleStates(WoodenDevices.WORKBENCH.get()))
		);

		public static final RegistryObject<VillagerProfession> PROF_ENGINEER = PROFESSIONS.register(
				ENGINEER.getPath(), () -> createProf(ENGINEER, POI_TURNTABLE, SoundEvents.VILLAGER_WORK_MASON)
		);
		public static final RegistryObject<VillagerProfession> PROF_MACHINIST = PROFESSIONS.register(
				MACHINIST.getPath(), () -> createProf(MACHINIST, POI_CRAFTINGTABLE, SoundEvents.VILLAGER_WORK_TOOLSMITH)
		);
		public static final RegistryObject<VillagerProfession> PROF_ELECTRICIAN = PROFESSIONS.register(
				ELECTRICIAN.getPath(), () -> createProf(ELECTRICIAN, POI_CIRCUITTABLE, IESounds.spark.get())
		);
		public static final RegistryObject<VillagerProfession> PROF_OUTFITTER = PROFESSIONS.register(
				OUTFITTER.getPath(), () -> createProf(OUTFITTER, POI_BANNER, SoundEvents.VILLAGER_WORK_CARTOGRAPHER)
		);
		public static final RegistryObject<VillagerProfession> PROF_GUNSMITH = PROFESSIONS.register(
				GUNSMITH.getPath(), () -> createProf(GUNSMITH, POI_WORKBENCH, IESounds.revolverReload.get())
		);

		private static PoiType createPOI(Collection<BlockState> block)
		{
			return new PoiType(ImmutableSet.copyOf(block), 1, 1);
		}

		private static VillagerProfession createProf(
				ResourceLocation name, RegistryObject<PoiType> poi, SoundEvent sound
		)
		{
			ResourceKey<PoiType> poiName = Objects.requireNonNull(poi.getKey());
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

	@Mod.EventBusSubscriber(modid = MODID, bus = Bus.FORGE)
	public static class Events
	{
		@SubscribeEvent
		public static void registerTrades(VillagerTradesEvent ev)
		{
			Int2ObjectMap<List<ItemListing>> trades = ev.getTrades();
			final ResourceLocation typeName = new ResourceLocation(ev.getType().name());
			if(ENGINEER.equals(typeName))
			{
				/* Structural Engineer
				 * Sells various construction materials
				 */
				trades.get(1).add(new TradeListing(EMERALD_FOR_ITEM, IETags.treatedStick, new PriceInterval(8, 16), 16, 1));
				trades.get(1).add(new TradeListing(ITEMS_FOR_ONE_EMERALD, WoodenDecoration.TREATED_WOOD.get(TreatedWoodStyles.HORIZONTAL), new PriceInterval(6, 10), 12, 1));
				trades.get(1).add(new TradeListing(ITEMS_FOR_ONE_EMERALD, Cloth.BALLOON, new PriceInterval(1, 3), 12, 2));

				trades.get(2).add(new TradeListing(EMERALD_FOR_ITEM, IETags.ironRod, new PriceInterval(2, 6), 12, 10));
				trades.get(2).add(new TradeListing(ITEMS_FOR_ONE_EMERALD, MetalDecoration.STEEL_SCAFFOLDING.get(MetalScaffoldingType.STANDARD), new PriceInterval(2, 4), 12, 5));
				trades.get(2).add(new TradeListing(ITEMS_FOR_ONE_EMERALD, MetalDecoration.ALU_SCAFFOLDING.get(MetalScaffoldingType.STANDARD), new PriceInterval(2, 4), 12, 5));

				trades.get(3).add(new TradeListing(EMERALD_FOR_ITEM, IETags.steelRod, new PriceInterval(2, 6), 12, 6));
				trades.get(3).add(new TradeListing(EMERALD_FOR_ITEM, StoneDecoration.CONCRETE, new PriceInterval(4, 8), 8, 12));
				trades.get(3).add(new TradeListing(ITEMS_FOR_ONE_EMERALD, WoodenDevices.TREATED_WALLMOUNT, new PriceInterval(2, 3), 10, 8));

				trades.get(4).add(new TradeListing(ITEMS_FOR_ONE_EMERALD, WoodenDecoration.TREATED_POST, new PriceInterval(2, 3), 4, 10));
				trades.get(4).add(new TradeListing(EMERALD_FOR_ITEM, MetalDecoration.STEEL_POST, new PriceInterval(1, 2), 4, 20));
				trades.get(4).add(new TradeListing(EMERALD_FOR_ITEM, MetalDecoration.ALU_POST, new PriceInterval(1, 3), 6, 15));
				trades.get(4).add(new TradeListing(EMERALD_FOR_ITEM, StoneDecoration.CONCRETE_LEADED, new PriceInterval(2, 4), 10, 10));
				trades.get(4).add(new OreveinMapForEmeralds());

				trades.get(5).add(new OreveinMapForEmeralds());
				trades.get(5).add(new OreveinMapForEmeralds());
			}
			else if(MACHINIST.equals(typeName))
			{
				/* Machinist
				 * Sells tools, metals, blueprints and drillheads
				 */
				trades.get(1).add(new TradeListing(EMERALD_FOR_ITEM, IETags.coalCoke, new PriceInterval(8, 16), 16, 2));
				trades.get(1).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, Tools.HAMMER, new PriceInterval(4, 7), 12, 1).setMultiplier(0.2f));

				trades.get(2).add(new TradeListing(EMERALD_FOR_ITEM, IETags.getTagsFor(EnumMetals.COPPER).ingot, new PriceInterval(4, 6), 6, 10));
				trades.get(2).add(new TradeListing(EMERALD_FOR_ITEM, IETags.getTagsFor(EnumMetals.ALUMINUM).ingot, new PriceInterval(4, 6), 6, 10));
				trades.get(2).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, Ingredients.COMPONENT_STEEL, new PriceInterval(1, 3), 12, 5));

				trades.get(3).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, Tools.TOOLBOX, new PriceInterval(6, 8), 3, 20).setMultiplier(0.2f));
				trades.get(3).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, Ingredients.WATERWHEEL_SEGMENT, new PriceInterval(1, 3), 8, 10));
				trades.get(2).add(new TradeListing(EMERALD_FOR_ITEM, IETags.getTagsFor(EnumMetals.STEEL).plate, new PriceInterval(4, 6), 12, 7));

				trades.get(4).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, Tools.DRILLHEAD_IRON, new PriceInterval(28, 40), 3, 15).setMultiplier(0.2f));
				trades.get(4).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, IEItems.Misc.EARMUFFS, new PriceInterval(4, 9), 3, 20).setMultiplier(0.2f));

				trades.get(5).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, Tools.DRILLHEAD_STEEL, new PriceInterval(32, 48), 3, 30).setMultiplier(0.2f));
				trades.get(5).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, BlueprintCraftingRecipe.getTypedBlueprint("electrode"), new PriceInterval(12, 24), 3, 30).setMultiplier(0.2f));
			}
			else if(ELECTRICIAN.equals(typeName))
			{
				/* Electrician
				 * Sells wires, tools and the faraday suit
				 */
				trades.get(1).add(new TradeListing(EMERALD_FOR_ITEM, IETags.copperWire, new PriceInterval(8, 16), 16, 2));
				trades.get(1).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, Tools.WIRECUTTER, new PriceInterval(4, 7), 12, 1).setMultiplier(0.2f));
				trades.get(1).add(new TradeListing(ITEMS_FOR_ONE_EMERALD, WIRE_COILS.get(WireType.COPPER), new PriceInterval(2, 4), 12, 1).setMultiplier(0.2f));

				trades.get(2).add(new TradeListing(EMERALD_FOR_ITEM, IETags.electrumWire, new PriceInterval(6, 12), 12, 4));
				trades.get(2).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, Tools.VOLTMETER, new PriceInterval(4, 7), 3, 12).setMultiplier(0.2f));
				trades.get(2).add(new TradeListing(ITEMS_FOR_ONE_EMERALD, WIRE_COILS.get(WireType.ELECTRUM), new PriceInterval(1, 4), 12, 5));
				trades.get(2).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, IEItems.Misc.FARADAY_SUIT.get(EquipmentSlot.FEET), new PriceInterval(5, 7), 3, 15).setMultiplier(0.2f));
				trades.get(2).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, IEItems.Misc.FARADAY_SUIT.get(EquipmentSlot.LEGS), new PriceInterval(9, 11), 3, 15).setMultiplier(0.2f));

				trades.get(3).add(new TradeListing(EMERALD_FOR_ITEM, IETags.aluminumWire, new PriceInterval(4, 8), 20, 10));
				trades.get(3).add(new TradeListing(ITEMS_FOR_ONE_EMERALD, WIRE_COILS.get(WireType.STEEL), new PriceInterval(1, 2), 12, 8));
				trades.get(3).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, IEItems.Misc.FARADAY_SUIT.get(EquipmentSlot.CHEST), new PriceInterval(11, 15), 3, 18).setMultiplier(0.2f));
				trades.get(3).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, IEItems.Misc.FARADAY_SUIT.get(EquipmentSlot.HEAD), new PriceInterval(5, 7), 3, 18).setMultiplier(0.2f));

				trades.get(4).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, IEItems.Misc.FLUORESCENT_TUBE, new PriceInterval(8, 12), 3, 25).setMultiplier(0.2f));
				trades.get(4).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, TOOL_UPGRADES.get(ToolUpgrade.REVOLVER_ELECTRO), new PriceInterval(8, 12), 3, 15).setMultiplier(0.2f));

				trades.get(5).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, TOOL_UPGRADES.get(ToolUpgrade.RAILGUN_CAPACITORS), new PriceInterval(8, 12), 3, 30).setMultiplier(0.2f));
			}
			else if(OUTFITTER.equals(typeName))
			{
				/* Outfitter
				 * Sells Shaderbags
				 */
				ItemLike bag_common = IEItems.Misc.SHADER_BAG.get(Rarity.COMMON);
				ItemLike bag_uncommon = IEItems.Misc.SHADER_BAG.get(Rarity.UNCOMMON);
				ItemLike bag_rare = IEItems.Misc.SHADER_BAG.get(Rarity.RARE);

				trades.get(1).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, bag_common, new PriceInterval(2, 8), 24, 1).setMultiplier(0.2f));
				trades.get(2).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, bag_uncommon, new PriceInterval(8, 14), 24, 5).setMultiplier(0.2f));
				trades.get(3).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, bag_rare, new PriceInterval(14, 20), 24, 10).setMultiplier(0.2f));
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
				trades.get(2).add(new TradeListing(ITEMS_FOR_ONE_EMERALD, BulletHandler.getBulletStack(BulletItem.CASULL), new PriceInterval(2, 4), 12, 5));
				trades.get(2).add(new TradeListing(ITEMS_FOR_ONE_EMERALD, BulletHandler.getBulletStack(BulletItem.BUCKSHOT), new PriceInterval(2, 6), 12, 5));
				trades.get(2).add(new RevolverPieceForEmeralds());

				trades.get(3).add(new RevolverPieceForEmeralds());
				trades.get(3).add(new TradeListing(ITEMS_FOR_ONE_EMERALD, BulletHandler.getBulletStack(BulletItem.FLARE), new PriceInterval(1, 2), 12, 10));
				trades.get(3).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, BlueprintCraftingRecipe.getTypedBlueprint("specialBullet"), new PriceInterval(5, 9), 1, 30).setMultiplier(0.2f));

				trades.get(4).add(new RevolverPieceForEmeralds());
				trades.get(4).add(new TradeListing(ITEMS_FOR_ONE_EMERALD, BulletHandler.getBulletStack(BulletItem.SILVER), new PriceInterval(1, 4), 8, 15));
				trades.get(4).add(new TradeListing(ONE_ITEM_FOR_EMERALDS, BulletHandler.getBulletStack(BulletItem.HIGH_EXPLOSIVE), new PriceInterval(2, 4), 8, 15));

				trades.get(5).add(new RevolverPieceForEmeralds());
				trades.get(5).add(new RevolverPieceForEmeralds());
				trades.get(5).add(new RevolverPieceForEmeralds());
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

		public TradeListing(@Nonnull TradeOutline outline, @Nonnull TagKey<Item> tag, @Nonnull PriceInterval buyAmounts, int maxUses, int xp)
		{
			this(outline, l -> l!=null?IEApi.getPreferredTagStack(l.registryAccess(), tag): ItemStack.EMPTY, buyAmounts, maxUses, xp);
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
			ItemStack buying = this.lazyItem.apply(trader!=null?trader.level: null);
			return this.outline.generateOffer(buying, priceInfo, rand, maxUses, xp, priceMultiplier);
		}
	}

	private static class OreveinMapForEmeralds implements ItemListing
	{
		public PriceInterval value;
		private static final int SEARCH_RADIUS = 16*16;
		private static final String TRADER_SOLD_KEY = "immersiveengineering:mapped_veins";

		public OreveinMapForEmeralds()
		{
		}

		@Override
		@Nullable
		public MerchantOffer getOffer(@Nullable Entity trader, @Nonnull RandomSource random)
		{
			if(trader==null)
				return null;
			Level world = trader.getCommandSenderWorld();
			BlockPos merchantPos = trader.blockPosition();
			// extract list of already sold veins from the rader
			CompoundTag traderData = trader.getPersistentData();
			List<Long> soldMaps = new ArrayList<>();
			if(traderData.contains(TRADER_SOLD_KEY))
				for(long l : traderData.getLongArray(TRADER_SOLD_KEY))
					soldMaps.add(l);
			// get veins in 16 chunk radius, ordered by their rarity (lowest weight first)
			List<MineralVein> veins = ExcavatorHandler.findVeinsForVillager(world, merchantPos, SEARCH_RADIUS, soldMaps);
			if(veins.size() > 0)
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
				MapItemSavedData.addTargetDecoration(selling, blockPos, "ie:coresample_treasure", Type.RED_X);
				selling.setHoverName(Component.translatable("item.immersiveengineering.map_orevein"));
				ItemNBTHelper.setLore(selling, Component.translatable(vein.getMineral(world).getTranslationKey()));
				// return offer
				return new MerchantOffer(
						new ItemStack(Items.EMERALD, 8+random.nextInt(8)), new ItemStack(Items.COMPASS),
						selling, 0, 1, 30, 0.5F
				);
			}
			return null;
		}
	}

	private static class RevolverPieceForEmeralds implements ItemListing
	{
		public RevolverPieceForEmeralds()
		{
		}

		@Override
		public MerchantOffer getOffer(Entity trader, @Nonnull RandomSource random)
		{
			int part = random.nextInt(3);

			ItemStack stack = new ItemStack(part==0?Ingredients.GUNPART_BARREL: part==1?Ingredients.GUNPART_DRUM: Ingredients.GUNPART_HAMMER);

			float luck = 1;
			if(trader instanceof AbstractVillager villager&&villager.isTrading())
			{
				luck = villager.getTradingPlayer().getLuck();
			}
			CompoundTag perksTag = RevolverItem.RevolverPerk.generatePerkSet(random, luck);
			ItemNBTHelper.setTagCompound(stack, "perks", perksTag);
			int tier = Math.max(1, RevolverItem.RevolverPerk.calculateTier(perksTag));

			ItemNBTHelper.putBoolean(stack, "generatePerks", true);
			return new MerchantOffer(new ItemStack(Items.EMERALD, 5*tier+random.nextInt(5)), stack, 1, 45, 0.25F);
		}
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
			ItemHandlerHelper.copyStackWithSize(buying, priceInfo.getPrice(random)),
			new ItemStack(Items.EMERALD),
			maxUses, xp, priceMultiplier
	);
	private static final TradeOutline ONE_ITEM_FOR_EMERALDS = (selling, priceInfo, random, maxUses, xp, priceMultiplier) -> new MerchantOffer(
			new ItemStack(Items.EMERALD, priceInfo.getPrice(random)),
			selling,
			maxUses, xp, priceMultiplier
	);
	private static final TradeOutline ITEMS_FOR_ONE_EMERALD = (selling, priceInfo, random, maxUses, xp, priceMultiplier) -> new MerchantOffer(
			new ItemStack(Items.EMERALD),
			ItemHandlerHelper.copyStackWithSize(selling, priceInfo.getPrice(random)),
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
