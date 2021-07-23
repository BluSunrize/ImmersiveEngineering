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
import blusunrize.immersiveengineering.common.blocks.IEBlocks.*;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.blocks.wooden.TreatedWoodStyles;
import blusunrize.immersiveengineering.common.items.BulletItem;
import blusunrize.immersiveengineering.common.items.IEItems;
import blusunrize.immersiveengineering.common.items.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.items.IEItems.Tools;
import blusunrize.immersiveengineering.common.items.RevolverItem;
import blusunrize.immersiveengineering.common.items.ToolUpgradeItem.ToolUpgrade;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.mixin.accessors.HeroGiftsTaskAccess;
import blusunrize.immersiveengineering.mixin.accessors.SingleJigsawAccess;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool.Projection;
import net.minecraft.world.level.saveddata.maps.MapDecoration.Type;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;
import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;
import static blusunrize.immersiveengineering.common.items.IEItems.Misc.toolUpgrades;
import static blusunrize.immersiveengineering.common.items.IEItems.Misc.wireCoils;

public class Villages
{
	public static final ResourceLocation ENGINEER = new ResourceLocation(MODID, "engineer");
	public static final ResourceLocation MACHINIST = new ResourceLocation(MODID, "machinist");
	public static final ResourceLocation ELECTRICIAN = new ResourceLocation(MODID, "electrician");
	public static final ResourceLocation OUTFITTER = new ResourceLocation(MODID, "outfitter");
	public static final ResourceLocation GUNSMITH = new ResourceLocation(MODID, "gunsmith");

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
				Either.left(rl(name)),
				() -> ProcessorLists.EMPTY,
				Projection.RIGID
		);
	}

	private static void addToPool(ResourceLocation pool, ResourceLocation toAdd, int weight)
	{
		StructureTemplatePool old = BuiltinRegistries.TEMPLATE_POOL.get(pool);

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
				Either.left(toAdd), () -> ProcessorLists.EMPTY, Projection.RIGID
		), weight);
		List<Pair<StructurePoolElement, Integer>> newPieceList = newPieces.object2IntEntrySet().stream()
				.map(e -> Pair.of(e.getKey(), e.getIntValue()))
				.collect(Collectors.toList());

		ResourceLocation name = old.getName();
		Registry.register(BuiltinRegistries.TEMPLATE_POOL, pool, new StructureTemplatePool(pool, name, newPieceList));
	}

	@Mod.EventBusSubscriber(modid = MODID, bus = Bus.MOD)
	public static class Registers
	{
		public static final DeferredRegister<PoiType> POINTS_OF_INTEREST = DeferredRegister.create(ForgeRegistries.POI_TYPES, ImmersiveEngineering.MODID);
		public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(ForgeRegistries.PROFESSIONS, ImmersiveEngineering.MODID);

		// TODO: Add more workstations. We need a different one for each profession
		public static final RegistryObject<PoiType> POI_CRAFTINGTABLE = POINTS_OF_INTEREST.register(
				"craftingtable", () -> createPOI("craftingtable", assembleStates(WoodenDevices.craftingTable.get()))
		);
		public static final RegistryObject<PoiType> POI_ANVIL = POINTS_OF_INTEREST.register(
				"anvil", () -> createPOI("anvil", assembleStates(Blocks.ANVIL))
		);
		public static final RegistryObject<PoiType> POI_ENERGYMETER = POINTS_OF_INTEREST.register(
				"energymeter", () -> createPOI("energymeter", assembleStates(Connectors.currentTransformer.get()))
		);
		public static final RegistryObject<PoiType> POI_BANNER = POINTS_OF_INTEREST.register(
				"shaderbanner", () -> createPOI("shaderbanner", assembleStates(Cloth.shaderBanner.get()))
		);
		public static final RegistryObject<PoiType> POI_WORKBENCH = POINTS_OF_INTEREST.register(
				"workbench", () -> createPOI("workbench", assembleStates(WoodenDevices.workbench.get()))
		);

		public static final RegistryObject<VillagerProfession> PROF_ENGINEER = PROFESSIONS.register(
				ENGINEER.getPath(), () -> createProf(ENGINEER, POI_CRAFTINGTABLE.get(), SoundEvents.VILLAGER_WORK_MASON)
		);
		public static final RegistryObject<VillagerProfession> PROF_MACHINIST = PROFESSIONS.register(
				MACHINIST.getPath(), () -> createProf(MACHINIST, POI_ANVIL.get(), SoundEvents.VILLAGER_WORK_TOOLSMITH)
		);
		public static final RegistryObject<VillagerProfession> PROF_ELECTRICIAN = PROFESSIONS.register(
				ELECTRICIAN.getPath(), () -> createProf(ELECTRICIAN, POI_ENERGYMETER.get(), IESounds.spark)
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
				trades.get(1).add(new EmeraldForItems(IETags.treatedStick.getName(), new PriceInterval(8, 16), 16, 2));
				trades.get(1).add(new ItemsForEmerald(WoodenDecoration.treatedWood.get(TreatedWoodStyles.HORIZONTAL), new PriceInterval(-10, -6), 12, 1, 0.2f));
				trades.get(1).add(new ItemsForEmerald(Cloth.balloon, new PriceInterval(-3, -1), 12, 1, 0.2f));

				trades.get(2).add(new EmeraldForItems(IETags.ironRod.getName(), new PriceInterval(2, 6), 12, 10));
				trades.get(2).add(new ItemsForEmerald(MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD), new PriceInterval(-8, -4), 12, 5, 0.2f));
				trades.get(2).add(new ItemsForEmerald(MetalDecoration.aluScaffolding.get(MetalScaffoldingType.STANDARD), new PriceInterval(-8, -4), 12, 5, 0.2f));

				trades.get(3).add(new EmeraldForItems(IETags.steelRod.getName(), new PriceInterval(2, 6), 12, 20));
				trades.get(3).add(new EmeraldForItems(IETags.slag.getName(), new PriceInterval(4, 8), 12, 20));
				trades.get(3).add(new ItemsForEmerald(StoneDecoration.concrete, new PriceInterval(-6, -2), 12, 10, 0.2f));

				trades.get(4).add(new OreveinMapForEmeralds());
			}
			else if(MACHINIST.equals(ev.getType().getRegistryName()))
			{
				/* Machinist
				 * Sells tools, metals, blueprints and drillheads
				 */
				trades.get(1).add(new EmeraldForItems(IETags.coalCoke.getName(), new PriceInterval(8, 16), 16, 2));
				trades.get(1).add(new ItemsForEmerald(Tools.hammer, new PriceInterval(4, 7), 12, 1, 0.2f));

				trades.get(2).add(new EmeraldForItems(IETags.getIngot(EnumMetals.COPPER.tagName()), new PriceInterval(4, 6), 12, 10));
				trades.get(2).add(new EmeraldForItems(IETags.getIngot(EnumMetals.ALUMINUM.tagName()), new PriceInterval(4, 6), 12, 10));
				trades.get(2).add(new ItemsForEmerald(Ingredients.componentSteel, new PriceInterval(1, 3), 12, 5, 0.2f));

				trades.get(3).add(new ItemsForEmerald(Tools.toolbox, new PriceInterval(6, 8), 12, 10, 0.2f));
				trades.get(3).add(new ItemsForEmerald(Ingredients.waterwheelSegment, new PriceInterval(1, 3), 12, 10, 0.2f));
				// Todo, replace with somethign more appropriate
				// trades.get(3).add(new ItemsForEmerald(BlueprintCraftingRecipe.getTypedBlueprint("specialBullet"), new PriceInterval(5, 9), 12, 10, 0.2f));

				trades.get(4).add(new ItemsForEmerald(Tools.drillheadIron, new PriceInterval(28, 40), 3, 15, 0.2f));
				trades.get(4).add(new ItemsForEmerald(IEItems.Misc.earmuffs, new PriceInterval(4, 9), 3, 15, 0.2f));

				trades.get(5).add(new ItemsForEmerald(Tools.drillheadSteel, new PriceInterval(32, 48), 3, 30, 0.2f));
				trades.get(5).add(new ItemsForEmerald(BlueprintCraftingRecipe.getTypedBlueprint("electrode"), new PriceInterval(12, 24), 3, 30, 0.2f));
			}
			else if(ELECTRICIAN.equals(ev.getType().getRegistryName()))
			{
				/* Electrician
				 * Sells wires, tools and the faraday suit
				 */
				trades.get(1).add(new EmeraldForItems(IETags.copperWire.getName(), new PriceInterval(8, 16), 16, 2));
				trades.get(1).add(new ItemsForEmerald(Tools.wirecutter, new PriceInterval(4, 7), 12, 1, 0.2f));
				trades.get(1).add(new ItemsForEmerald(wireCoils.get(WireType.COPPER), new PriceInterval(-4, -2), 12, 1, 0.2f));

				trades.get(2).add(new EmeraldForItems(IETags.electrumWire.getName(), new PriceInterval(6, 12), 12, 10));
				trades.get(2).add(new ItemsForEmerald(Tools.voltmeter, new PriceInterval(4, 7), 12, 5, 0.2f));
				trades.get(2).add(new ItemsForEmerald(wireCoils.get(WireType.ELECTRUM), new PriceInterval(-4, -1), 12, 5, 0.2f));
				trades.get(2).add(new ItemsForEmerald(IEItems.Misc.faradaySuit.get(EquipmentSlot.FEET), new PriceInterval(5, 7), 12, 5, 0.2f));
				trades.get(2).add(new ItemsForEmerald(IEItems.Misc.faradaySuit.get(EquipmentSlot.LEGS), new PriceInterval(9, 11), 12, 5, 0.2f));

				trades.get(3).add(new EmeraldForItems(IETags.aluminumWire.getName(), new PriceInterval(4, 8), 12, 20));
				trades.get(3).add(new ItemsForEmerald(wireCoils.get(WireType.STEEL), new PriceInterval(-2, -1), 12, 10, 0.2f));
				trades.get(3).add(new ItemsForEmerald(IEItems.Misc.faradaySuit.get(EquipmentSlot.CHEST), new PriceInterval(11, 15), 12, 10, 0.2f));
				trades.get(3).add(new ItemsForEmerald(IEItems.Misc.faradaySuit.get(EquipmentSlot.HEAD), new PriceInterval(5, 7), 12, 10, 0.2f));

				trades.get(4).add(new ItemsForEmerald(IEItems.Misc.fluorescentTube, new PriceInterval(8, 12), 3, 15, 0.2f));
				trades.get(4).add(new ItemsForEmerald(toolUpgrades.get(ToolUpgrade.REVOLVER_ELECTRO), new PriceInterval(8, 12), 3, 15, 0.2f));

				trades.get(5).add(new ItemsForEmerald(toolUpgrades.get(ToolUpgrade.RAILGUN_CAPACITORS), new PriceInterval(8, 12), 3, 30, 0.2f));
			}
			else if(OUTFITTER.equals(ev.getType().getRegistryName()))
			{
				/* Outfitter
				 * Sells Shaderbags
				 */
				ItemLike bag_common = IEItems.Misc.shaderBag.get(Rarity.COMMON);
				ItemLike bag_uncommon = IEItems.Misc.shaderBag.get(Rarity.UNCOMMON);
				ItemLike bag_rare = IEItems.Misc.shaderBag.get(Rarity.RARE);

				trades.get(1).add(new ItemsForEmerald(bag_common, new PriceInterval(8, 16), 24, 1, 0.2f));
				trades.get(2).add(new ItemsForEmerald(bag_uncommon, new PriceInterval(12, 20), 24, 5, 0.2f));
				trades.get(3).add(new ItemsForEmerald(bag_rare, new PriceInterval(16, 24), 24, 10, 0.2f));
			}
			else if(GUNSMITH.equals(ev.getType().getRegistryName()))
			{
				/* Gunsmith
				 * Sells ammunition, blueprints and revolver parts
				 */
				trades.get(1).add(new EmeraldForItems(Ingredients.emptyCasing, new PriceInterval(6, 12), 16, 2));
				trades.get(1).add(new EmeraldForItems(Ingredients.emptyShell, new PriceInterval(6, 12), 16, 2));
				trades.get(1).add(new ItemsForEmerald(Ingredients.woodenGrip, new PriceInterval(2, 4), 12, 1, 0.2f));

				trades.get(2).add(new ItemsForEmerald(BlueprintCraftingRecipe.getTypedBlueprint("bullet"), new PriceInterval(3, 6), 1, 25, 0.2f));
				trades.get(2).add(new ItemsForEmerald(BulletHandler.getBulletStack(BulletItem.CASULL), new PriceInterval(-4, -2), 12, 5, 0.2f));
				trades.get(2).add(new ItemsForEmerald(BulletHandler.getBulletStack(BulletItem.BUCKSHOT), new PriceInterval(-6, -2), 12, 5, 0.2f));
				trades.get(2).add(new RevolverPieceForEmeralds());

				trades.get(3).add(new RevolverPieceForEmeralds());
				trades.get(3).add(new ItemsForEmerald(BulletHandler.getBulletStack(BulletItem.FLARE), new PriceInterval(-2, -1), 12, 10, 0.2f));
				trades.get(3).add(new ItemsForEmerald(BlueprintCraftingRecipe.getTypedBlueprint("specialBullet"), new PriceInterval(5, 9), 1, 30, 0.2f));

				trades.get(4).add(new RevolverPieceForEmeralds());
				trades.get(4).add(new ItemsForEmerald(BulletHandler.getBulletStack(BulletItem.SILVER), new PriceInterval(-4, -1), 6, 15, 0.2f));
				trades.get(4).add(new ItemsForEmerald(BulletHandler.getBulletStack(BulletItem.HIGH_EXPLOSIVE), new PriceInterval(2, 4), 6, 15, 0.2f));

				trades.get(5).add(new RevolverPieceForEmeralds());
				trades.get(5).add(new RevolverPieceForEmeralds());
				trades.get(5).add(new RevolverPieceForEmeralds());
			}
		}
	}

	private static class EmeraldForItems implements ItemListing
	{
		public ItemStack buyingItem;
		public PriceInterval buyAmounts;
		final int maxUses;
		final int xp;

		public EmeraldForItems(@Nonnull ItemStack item, @Nonnull PriceInterval buyAmounts, int maxUses, int xp)
		{
			this.buyingItem = item;
			this.buyAmounts = buyAmounts;
			this.maxUses = maxUses;
			this.xp = xp;
		}

		public EmeraldForItems(@Nonnull ItemLike item, @Nonnull PriceInterval buyAmounts, int maxUses, int xp)
		{
			this(new ItemStack(item), buyAmounts, maxUses, xp);
		}

		public EmeraldForItems(@Nonnull ResourceLocation tag, @Nonnull PriceInterval buyAmounts, int maxUses, int xp)
		{
			this(IEApi.getPreferredTagStack(tag), buyAmounts, maxUses, xp);
		}


		@Nullable
		@Override
		public MerchantOffer getOffer(Entity trader, Random rand)
		{
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
				if(vein!=null&&vein.getMineral()!=null&&!veins.contains(vein))
					veins.add(vein);
			}
			if(veins.size() > 0)
			{
				// lowest weight first, to pick the rarest vein
				veins.sort(Comparator.comparingInt(o -> o.getMineral().weight));
				MineralVein vein = veins.get(0);
				BlockPos blockPos = new BlockPos(vein.getPos().x, 64, vein.getPos().z);
				ItemStack selling = MapItem.create(world, blockPos.getX(), blockPos.getZ(), (byte)1, true, true);
				MapItem.lockMap(world, selling);
				MapItemSavedData.addTargetDecoration(selling, blockPos, "ie:coresample_treasure", Type.RED_X);
				selling.setHoverName(new TranslatableComponent("item.immersiveengineering.map_orevein"));
				ItemNBTHelper.setLore(selling, new TranslatableComponent(vein.getMineral().getTranslationKey()));
				ItemStack steelIngot = IEApi.getPreferredTagStack(IETags.getIngot(EnumMetals.STEEL.tagName()));
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

			ItemStack stack = new ItemStack(part==0?Ingredients.gunpartBarrel: part==1?Ingredients.gunpartDrum: Ingredients.gunpartDrum);

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
