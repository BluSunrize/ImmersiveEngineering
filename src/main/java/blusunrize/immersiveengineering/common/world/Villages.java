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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
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
import net.minecraft.world.level.block.Blocks;
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
			addToPool(new ResourceLocation("village/"+biome+"/houses"),
					rl("village/houses/"+biome+"_engineer"), 4);

		// Register workstations
		Pools.register(new StructureTemplatePool(
				new ResourceLocation(MODID, "village/workstations"),
				new ResourceLocation("empty"),
				ImmutableList.of(
						new Pair<>(createWorkstation("village/workstations/electrician"), 1),
						new Pair<>(createWorkstation("village/workstations/engineer"), 1),
						new Pair<>(createWorkstation("village/workstations/gunsmith"), 1),
						new Pair<>(createWorkstation("village/workstations/machinist"), 1),
						new Pair<>(createWorkstation("village/workstations/outfitter"), 1)
				)
		));

		// Register gifts
		HeroGiftsTaskAccess.getGifts().put(Registers.PROF_ENGINEER.get(), rl("gameplay/hero_of_the_village/engineer"));
		HeroGiftsTaskAccess.getGifts().put(Registers.PROF_MACHINIST.get(), rl("gameplay/hero_of_the_village/machinist"));
		HeroGiftsTaskAccess.getGifts().put(Registers.PROF_ELECTRICIAN.get(), rl("gameplay/hero_of_the_village/electrician"));
		HeroGiftsTaskAccess.getGifts().put(Registers.PROF_OUTFITTER.get(), rl("gameplay/hero_of_the_village/outfitter"));
		HeroGiftsTaskAccess.getGifts().put(Registers.PROF_GUNSMITH.get(), rl("gameplay/hero_of_the_village/gunsmith"));
	}

	private static StructurePoolElement createWorkstation(String name)
	{
		return SingleJigsawAccess.construct(
				Either.left(rl(name)), ProcessorLists.EMPTY, Projection.RIGID
		);
	}

	private static void addToPool(ResourceLocation pool, ResourceLocation toAdd, int weight)
	{
		StructureTemplatePool old = BuiltinRegistries.TEMPLATE_POOL.get(pool);
		int id = BuiltinRegistries.TEMPLATE_POOL.getId(old);

		// Fixed seed to prevent inconsistencies between different worlds
		List<StructurePoolElement> shuffled;
		if(old!=null)
			shuffled = old.getShuffledTemplates(new Random(0));
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
		public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(ForgeRegistries.PROFESSIONS, ImmersiveEngineering.MODID);

		// TODO: Add more workstations. We need a different one for each profession
		public static final RegistryObject<PoiType> POI_CRAFTINGTABLE = POINTS_OF_INTEREST.register(
				"craftingtable", () -> createPOI("craftingtable", assembleStates(WoodenDevices.CRAFTING_TABLE.get()))
		);
		public static final RegistryObject<PoiType> POI_ANVIL = POINTS_OF_INTEREST.register(
				"anvil", () -> createPOI("anvil", assembleStates(Blocks.ANVIL))
		);
		// TODO, 1.19: Change the name on this
		public static final RegistryObject<PoiType> POI_CIRCUITTABLE = POINTS_OF_INTEREST.register(
				"energymeter", () -> createPOI("energymeter", assembleStates(WoodenDevices.CIRCUIT_TABLE.get()))
		);
		public static final RegistryObject<PoiType> POI_BANNER = POINTS_OF_INTEREST.register(
				"shaderbanner", () -> createPOI("shaderbanner", assembleStates(Cloth.SHADER_BANNER.get()))
		);
		public static final RegistryObject<PoiType> POI_WORKBENCH = POINTS_OF_INTEREST.register(
				"workbench", () -> createPOI("workbench", assembleStates(WoodenDevices.WORKBENCH.get()))
		);

		public static final RegistryObject<VillagerProfession> PROF_ENGINEER = PROFESSIONS.register(
				ENGINEER.getPath(), () -> createProf(ENGINEER, POI_CRAFTINGTABLE.get(), SoundEvents.VILLAGER_WORK_MASON)
		);
		public static final RegistryObject<VillagerProfession> PROF_MACHINIST = PROFESSIONS.register(
				MACHINIST.getPath(), () -> createProf(MACHINIST, POI_ANVIL.get(), SoundEvents.VILLAGER_WORK_TOOLSMITH)
		);
		public static final RegistryObject<VillagerProfession> PROF_ELECTRICIAN = PROFESSIONS.register(
				ELECTRICIAN.getPath(), () -> createProf(ELECTRICIAN, POI_CIRCUITTABLE.get(), IESounds.spark)
		);
		public static final RegistryObject<VillagerProfession> PROF_OUTFITTER = PROFESSIONS.register(
				OUTFITTER.getPath(), () -> createProf(OUTFITTER, POI_BANNER.get(), SoundEvents.VILLAGER_WORK_CARTOGRAPHER)
		);
		public static final RegistryObject<VillagerProfession> PROF_GUNSMITH = PROFESSIONS.register(
				GUNSMITH.getPath(), () -> createProf(GUNSMITH, POI_WORKBENCH.get(), IESounds.revolverReload)
		);

		private static PoiType createPOI(String name, Collection<BlockState> block)
		{
			return new PoiType(MODID+":"+name, ImmutableSet.copyOf(block), 1, 1);
		}

		private static VillagerProfession createProf(ResourceLocation name, PoiType poi, SoundEvent sound)
		{
			return new VillagerProfession(
					name.toString(),
					poi,
					ImmutableSet.<Item>builder().build(),
					ImmutableSet.<Block>builder().build(),
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
			if(ENGINEER.equals(ev.getType().getRegistryName()))
			{
				/* Structural Engineer
				 * Sells various construction materials
				 */
				trades.get(1).add(new EmeraldForItems(IETags.treatedStick, new PriceInterval(8, 16), 16, 1));
				trades.get(1).add(new ItemsForEmerald(WoodenDecoration.TREATED_WOOD.get(TreatedWoodStyles.HORIZONTAL), new PriceInterval(-10, -6), 12, 1));
				trades.get(1).add(new ItemsForEmerald(Cloth.BALLOON, new PriceInterval(-3, -1), 12, 2));

				trades.get(2).add(new EmeraldForItems(IETags.ironRod, new PriceInterval(2, 6), 12, 10));
				trades.get(2).add(new ItemsForEmerald(MetalDecoration.STEEL_SCAFFOLDING.get(MetalScaffoldingType.STANDARD), new PriceInterval(-4, -2), 12, 5));
				trades.get(2).add(new ItemsForEmerald(MetalDecoration.ALU_SCAFFOLDING.get(MetalScaffoldingType.STANDARD), new PriceInterval(-4, -2), 12, 5));

				trades.get(3).add(new EmeraldForItems(IETags.steelRod, new PriceInterval(2, 6), 12, 6));
				trades.get(3).add(new EmeraldForItems(StoneDecoration.CONCRETE, new PriceInterval(4, 8), 8, 12));
				trades.get(3).add(new ItemsForEmerald(WoodenDevices.TREATED_WALLMOUNT, new PriceInterval(-3, -2), 10, 8));

				trades.get(4).add(new ItemsForEmerald(WoodenDecoration.TREATED_POST, new PriceInterval(-3, -2), 4, 10));
				trades.get(4).add(new EmeraldForItems(MetalDecoration.STEEL_POST, new PriceInterval(1, 2), 4, 20));
				trades.get(4).add(new EmeraldForItems(MetalDecoration.ALU_POST, new PriceInterval(1, 3), 6, 15));

				trades.get(5).add(new OreveinMapForEmeralds());
				trades.get(5).add(new EmeraldForItems(StoneDecoration.CONCRETE_LEADED, new PriceInterval(2, 6), 10, 10));
			}
			else if(MACHINIST.equals(ev.getType().getRegistryName()))
			{
				/* Machinist
				 * Sells tools, metals, blueprints and drillheads
				 */
				trades.get(1).add(new EmeraldForItems(IETags.coalCoke, new PriceInterval(8, 16), 16, 2));
				trades.get(1).add(new ItemsForEmerald(Tools.HAMMER, new PriceInterval(4, 7), 12, 1, 0.2f));

				trades.get(2).add(new EmeraldForItems(IETags.getIngot(EnumMetals.COPPER.tagName()), new PriceInterval(4, 6), 6, 10));
				trades.get(2).add(new EmeraldForItems(IETags.getIngot(EnumMetals.ALUMINUM.tagName()), new PriceInterval(4, 6), 6, 10));
				trades.get(2).add(new ItemsForEmerald(Ingredients.COMPONENT_STEEL, new PriceInterval(1, 3), 12, 5));

				trades.get(3).add(new ItemsForEmerald(Tools.TOOLBOX, new PriceInterval(6, 8), 3, 20, 0.2f));
				trades.get(3).add(new ItemsForEmerald(Ingredients.WATERWHEEL_SEGMENT, new PriceInterval(1, 3), 8, 10));
				trades.get(2).add(new EmeraldForItems(IETags.getPlate(EnumMetals.STEEL.tagName()), new PriceInterval(4, 6), 12, 7));

				trades.get(4).add(new ItemsForEmerald(Tools.DRILLHEAD_IRON, new PriceInterval(28, 40), 3, 15, 0.2f));
				trades.get(4).add(new ItemsForEmerald(IEItems.Misc.EARMUFFS, new PriceInterval(4, 9), 3, 20, 0.2f));

				trades.get(5).add(new ItemsForEmerald(Tools.DRILLHEAD_STEEL, new PriceInterval(32, 48), 3, 30, 0.2f));
				trades.get(5).add(new ItemsForEmerald(BlueprintCraftingRecipe.getTypedBlueprint("electrode"), new PriceInterval(12, 24), 3, 30, 0.2f));
			}
			else if(ELECTRICIAN.equals(ev.getType().getRegistryName()))
			{
				/* Electrician
				 * Sells wires, tools and the faraday suit
				 */
				trades.get(1).add(new EmeraldForItems(IETags.copperWire, new PriceInterval(8, 16), 16, 2));
				trades.get(1).add(new ItemsForEmerald(Tools.WIRECUTTER, new PriceInterval(4, 7), 12, 1, 0.2f));
				trades.get(1).add(new ItemsForEmerald(WIRE_COILS.get(WireType.COPPER), new PriceInterval(-4, -2), 12, 1, 0.2f));

				trades.get(2).add(new EmeraldForItems(IETags.electrumWire, new PriceInterval(6, 12), 12, 4));
				trades.get(2).add(new ItemsForEmerald(Tools.VOLTMETER, new PriceInterval(4, 7), 3, 12, 0.2f));
				trades.get(2).add(new ItemsForEmerald(WIRE_COILS.get(WireType.ELECTRUM), new PriceInterval(-4, -1), 12, 5));
				trades.get(2).add(new ItemsForEmerald(IEItems.Misc.FARADAY_SUIT.get(EquipmentSlot.FEET), new PriceInterval(5, 7), 3, 15, 0.2f));
				trades.get(2).add(new ItemsForEmerald(IEItems.Misc.FARADAY_SUIT.get(EquipmentSlot.LEGS), new PriceInterval(9, 11), 3, 15, 0.2f));

				trades.get(3).add(new EmeraldForItems(IETags.aluminumWire, new PriceInterval(4, 8), 20, 10));
				trades.get(3).add(new ItemsForEmerald(WIRE_COILS.get(WireType.STEEL), new PriceInterval(-2, -1), 12, 8));
				trades.get(3).add(new ItemsForEmerald(IEItems.Misc.FARADAY_SUIT.get(EquipmentSlot.CHEST), new PriceInterval(11, 15), 3, 18, 0.2f));
				trades.get(3).add(new ItemsForEmerald(IEItems.Misc.FARADAY_SUIT.get(EquipmentSlot.HEAD), new PriceInterval(5, 7), 3, 18, 0.2f));

				trades.get(4).add(new ItemsForEmerald(IEItems.Misc.FLUORESCENT_TUBE, new PriceInterval(8, 12), 3, 25, 0.2f));
				trades.get(4).add(new ItemsForEmerald(TOOL_UPGRADES.get(ToolUpgrade.REVOLVER_ELECTRO), new PriceInterval(8, 12), 3, 15, 0.2f));

				trades.get(5).add(new ItemsForEmerald(TOOL_UPGRADES.get(ToolUpgrade.RAILGUN_CAPACITORS), new PriceInterval(8, 12), 3, 30, 0.2f));
			}
			else if(OUTFITTER.equals(ev.getType().getRegistryName()))
			{
				/* Outfitter
				 * Sells Shaderbags
				 */
				ItemLike bag_common = IEItems.Misc.SHADER_BAG.get(Rarity.COMMON);
				ItemLike bag_uncommon = IEItems.Misc.SHADER_BAG.get(Rarity.UNCOMMON);
				ItemLike bag_rare = IEItems.Misc.SHADER_BAG.get(Rarity.RARE);

				trades.get(1).add(new ItemsForEmerald(bag_common, new PriceInterval(8, 16), 24, 1, 0.2f));
				trades.get(2).add(new ItemsForEmerald(bag_uncommon, new PriceInterval(12, 20), 24, 5, 0.2f));
				trades.get(3).add(new ItemsForEmerald(bag_rare, new PriceInterval(16, 24), 24, 10, 0.2f));
			}
			else if(GUNSMITH.equals(ev.getType().getRegistryName()))
			{
				/* Gunsmith
				 * Sells ammunition, blueprints and revolver parts
				 */
				trades.get(1).add(new EmeraldForItems(Ingredients.EMPTY_CASING, new PriceInterval(6, 12), 16, 2));
				trades.get(1).add(new EmeraldForItems(Ingredients.EMPTY_SHELL, new PriceInterval(6, 12), 16, 2));
				trades.get(1).add(new ItemsForEmerald(Ingredients.WOODEN_GRIP, new PriceInterval(2, 4), 1, 12, 0.2f));

				trades.get(2).add(new ItemsForEmerald(BlueprintCraftingRecipe.getTypedBlueprint("bullet"), new PriceInterval(3, 6), 1, 25));
				trades.get(2).add(new ItemsForEmerald(BulletHandler.getBulletStack(BulletItem.CASULL), new PriceInterval(-4, -2), 12, 5));
				trades.get(2).add(new ItemsForEmerald(BulletHandler.getBulletStack(BulletItem.BUCKSHOT), new PriceInterval(-6, -2), 12, 5));
				trades.get(2).add(new RevolverPieceForEmeralds());

				trades.get(3).add(new RevolverPieceForEmeralds());
				trades.get(3).add(new ItemsForEmerald(BulletHandler.getBulletStack(BulletItem.FLARE), new PriceInterval(-2, -1), 12, 10));
				trades.get(3).add(new ItemsForEmerald(BlueprintCraftingRecipe.getTypedBlueprint("specialBullet"), new PriceInterval(5, 9), 1, 30, 0.2f));

				trades.get(4).add(new RevolverPieceForEmeralds());
				trades.get(4).add(new ItemsForEmerald(BulletHandler.getBulletStack(BulletItem.SILVER), new PriceInterval(-4, -1), 8, 15));
				trades.get(4).add(new ItemsForEmerald(BulletHandler.getBulletStack(BulletItem.HIGH_EXPLOSIVE), new PriceInterval(2, 4), 8, 15));

				trades.get(5).add(new RevolverPieceForEmeralds());
				trades.get(5).add(new RevolverPieceForEmeralds());
				trades.get(5).add(new RevolverPieceForEmeralds());
			}
		}
	}

	private static class EmeraldForItems implements ItemListing
	{
		private final Function<Level, ItemStack> getBuyingItem;
		@Nullable
		private ItemStack buyingItem;
		private final PriceInterval buyAmounts;
		private final int maxUses;
		private final int xp;

		public EmeraldForItems(@Nonnull Function<Level, ItemStack> item, @Nonnull PriceInterval buyAmounts, int maxUses, int xp)
		{
			this.getBuyingItem = item;
			this.buyAmounts = buyAmounts;
			this.maxUses = maxUses;
			this.xp = xp;
		}

		public EmeraldForItems(@Nonnull ItemLike item, @Nonnull PriceInterval buyAmounts, int maxUses, int xp)
		{
			this(l -> new ItemStack(item), buyAmounts, maxUses, xp);
		}

		public EmeraldForItems(@Nonnull TagKey<Item> tag, @Nonnull PriceInterval buyAmounts, int maxUses, int xp)
		{
			this(l -> IEApi.getPreferredTagStack(l.registryAccess(), tag), buyAmounts, maxUses, xp);
		}


		public EmeraldForItems(@Nonnull ResourceLocation tag, @Nonnull PriceInterval buyAmounts, int maxUses, int xp)
		{
			this(TagKey.create(Registry.ITEM_REGISTRY, tag), buyAmounts, maxUses, xp);
		}


		@Nullable
		@Override
		public MerchantOffer getOffer(Entity trader, Random rand)
		{
			if(buyingItem==null)
				this.buyingItem = Objects.requireNonNull(this.getBuyingItem.apply(trader.level));
			return new MerchantOffer(
					ItemHandlerHelper.copyStackWithSize(this.buyingItem, this.buyAmounts.getPrice(rand)),
					new ItemStack(Items.EMERALD),
					//TODO adjust values for individual trades
					maxUses, xp, 0.05f);
		}
	}

	private static class ItemsForEmerald implements ItemListing
	{
		public ItemStack sellingItem;
		public PriceInterval priceInfo;
		final int maxUses;
		final int xp;
		final float priceMult;

		public ItemsForEmerald(ItemLike par1Item, PriceInterval priceInfo, int maxUses, int xp)
		{
			this(new ItemStack(par1Item), priceInfo, maxUses, xp);
		}

		public ItemsForEmerald(ItemStack par1Item, PriceInterval priceInfo, int maxUses, int xp)
		{
			this(par1Item, priceInfo, maxUses, xp, 0.05f);
		}

		public ItemsForEmerald(ItemLike par1Item, PriceInterval priceInfo, int maxUses, int xp, float priceMult)
		{
			this(new ItemStack(par1Item), priceInfo, maxUses, xp, priceMult);
		}

		public ItemsForEmerald(ItemStack par1Item, PriceInterval priceInfo, int maxUses, int xp, float priceMult)
		{
			this.sellingItem = par1Item;
			this.priceInfo = priceInfo;
			this.maxUses = maxUses;
			this.xp = xp;
			this.priceMult = priceMult;
		}

		@Nullable
		@Override
		public MerchantOffer getOffer(Entity trader, Random rand)
		{
			int i = 1;
			if(this.priceInfo!=null)
				i = this.priceInfo.getPrice(rand);
			ItemStack buying;
			ItemStack selling;
			if(i < 0)
			{
				buying = new ItemStack(Items.EMERALD);
				selling = ItemHandlerHelper.copyStackWithSize(sellingItem, -i);
			}
			else
			{
				buying = new ItemStack(Items.EMERALD, i);
				selling = sellingItem;
			}
			//TODO customize values
			return new MerchantOffer(buying, selling, maxUses, xp, priceMult);
		}
	}

	private static class OreveinMapForEmeralds implements ItemListing
	{
		public PriceInterval value;
		private static final int SEARCH_RADIUS = 32*16;

		public OreveinMapForEmeralds()
		{
		}

		@Override
		@Nullable
		public MerchantOffer getOffer(Entity trader, @Nonnull Random random)
		{
			Level world = trader.getCommandSenderWorld();
			BlockPos merchantPos = trader.blockPosition();
			List<MineralVein> veins = new ArrayList<>();
			for(int i = 0; i < 8; i++) //Let's just try this a maximum of 8 times before I give up
			{
				int offX = random.nextInt(SEARCH_RADIUS*2)-SEARCH_RADIUS;
				int offZ = random.nextInt(SEARCH_RADIUS*2)-SEARCH_RADIUS;
				MineralVein vein = ExcavatorHandler.getRandomMineral(world, merchantPos.offset(offX, 0, offZ));
				if(vein!=null&&vein.getMineral(world)!=null&&!veins.contains(vein))
					veins.add(vein);
			}
			if(veins.size() > 0)
			{
				// lowest weight first, to pick the rarest vein
				veins.sort(Comparator.comparingInt(o -> o.getMineral(world).weight));
				MineralVein vein = veins.get(0);
				BlockPos blockPos = new BlockPos(vein.getPos().x, 64, vein.getPos().z);
				ItemStack selling = MapItem.create(world, blockPos.getX(), blockPos.getZ(), (byte)1, true, true);
				MapItem.lockMap(world, selling);
				MapItemSavedData.addTargetDecoration(selling, blockPos, "ie:coresample_treasure", Type.RED_X);
				selling.setHoverName(new TranslatableComponent("item.immersiveengineering.map_orevein"));
				ItemNBTHelper.setLore(selling, new TranslatableComponent(vein.getMineral(world).getTranslationKey()));
				ItemStack steelIngot = IEApi.getPreferredTagStack(trader.level.registryAccess(), IETags.getTagsFor(EnumMetals.STEEL).ingot);
				return new MerchantOffer(new ItemStack(Items.EMERALD, 8+random.nextInt(8)),
						ItemHandlerHelper.copyStackWithSize(steelIngot, 4+random.nextInt(8)), selling, 0, 1, 30, 0.5F);
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
		public MerchantOffer getOffer(Entity trader, @Nonnull Random random)
		{
			int part = random.nextInt(3);

			ItemStack stack = new ItemStack(part==0?Ingredients.GUNPART_BARREL: part==1?Ingredients.GUNPART_DRUM: Ingredients.GUNPART_DRUM);

			float luck = 1;
			if(trader instanceof AbstractVillager&&((AbstractVillager)trader).isTrading())
			{
				luck = ((AbstractVillager)trader).getTradingPlayer().getLuck();
			}
			CompoundTag perksTag = RevolverItem.RevolverPerk.generatePerkSet(random, luck);
			ItemNBTHelper.setTagCompound(stack, "perks", perksTag);
			int tier = Math.max(1, RevolverItem.RevolverPerk.calculateTier(perksTag));

			ItemNBTHelper.putBoolean(stack, "generatePerks", true);
			return new MerchantOffer(new ItemStack(Items.EMERALD, 5*tier+random.nextInt(5)), stack, 1, 30, 0.25F);
		}
	}

	private static class PriceInterval
	{
		private final int min;
		private final int max;

		private PriceInterval(int min, int max)
		{
			this.min = min;
			this.max = max;
		}

		int getPrice(Random rand)
		{
			return min >= max?min: min+rand.nextInt(max-min+1);
		}
	}
}
