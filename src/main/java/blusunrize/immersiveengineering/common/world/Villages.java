/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.world;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralWorldInfo;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.*;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.blocks.wooden.TreatedWoodStyles;
import blusunrize.immersiveengineering.common.items.BulletItem;
import blusunrize.immersiveengineering.common.items.IEItems;
import blusunrize.immersiveengineering.common.items.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.items.IEItems.Metals;
import blusunrize.immersiveengineering.common.items.IEItems.Tools;
import blusunrize.immersiveengineering.common.items.RevolverItem;
import blusunrize.immersiveengineering.common.items.ToolUpgradeItem.ToolUpgrade;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.merchant.villager.VillagerTrades.ITrade;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.jigsaw.JigsawManager;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern.PlacementBehaviour;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.SingleJigsawPiece;
import net.minecraft.world.gen.feature.structure.*;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration.Type;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;
import static blusunrize.immersiveengineering.common.data.IEDataGenerator.rl;
import static blusunrize.immersiveengineering.common.items.IEItems.Misc.toolUpgrades;
import static blusunrize.immersiveengineering.common.items.IEItems.Misc.wireCoils;

public class Villages
{
	public static final PointOfInterestType IE_POI = new PointOfInterestType(MODID+":engineer",
			ImmutableSet.copyOf(WoodenDevices.workbench.getStateContainer().getValidStates()), 1,
			SoundEvents.ENTITY_VILLAGER_WORK_TOOLSMITH, 1).setRegistryName(new ResourceLocation(MODID, "engineer"));
	public static final ResourceLocation ENGINEER = new ResourceLocation(MODID, "engineer");
	public static final ResourceLocation MACHINIST = new ResourceLocation(MODID, "machinist");
	public static final ResourceLocation ELECTRICIAN = new ResourceLocation(MODID, "electrician");
	public static final ResourceLocation OUTFITTER = new ResourceLocation(MODID, "outfitter");
	public static final ResourceLocation GUNSMITH = new ResourceLocation(MODID, "gunsmith");

	public static void init()
	{
		PlainsVillagePools.init();
		SnowyVillagePools.init();
		SavannaVillagePools.init();
		DesertVillagePools.init();
		TaigaVillagePools.init();
		for(String biome : new String[]{
				"plains", "snowy", "savanna", "desert", "taiga"
		})
			addToPool(new ResourceLocation("village/"+biome+"/houses"),
					rl("villages/engineers_house_"+biome), 5);
	}

	private static void addToPool(ResourceLocation pool, ResourceLocation toAdd, int weight)
	{
		JigsawPattern old = JigsawManager.REGISTRY.get(pool);
		List<JigsawPiece> shuffled = old.getShuffledPieces(Utils.RAND);
		List<Pair<JigsawPiece, Integer>> newPieces = new ArrayList<>();
		for(JigsawPiece p : shuffled)
		{
			newPieces.add(new Pair<>(p, 1));
		}
		newPieces.add(new Pair<>(new SingleJigsawPiece(toAdd.toString()), weight));
		ResourceLocation something = old.func_214948_a();
		JigsawManager.REGISTRY.register(new JigsawPattern(pool, something, newPieces, PlacementBehaviour.RIGID));
	}

	@Mod.EventBusSubscriber(modid = MODID, bus = Bus.MOD)
	public static class Registers
	{
		private static VillagerProfession create(ResourceLocation name)
		{
			return new VillagerProfession(
					name.toString(),
					//TODO
					IE_POI,
					ImmutableSet.of(),
					//TODO
					ImmutableSet.of()
					//ImmutableSet.of(WoodenDevices.crate)
			).setRegistryName(name);
		}

		@SubscribeEvent
		public static void registerPOI(RegistryEvent.Register<PointOfInterestType> ev)
		{
			ev.getRegistry().register(IE_POI);
		}

		@SubscribeEvent
		public static void registerProfessions(RegistryEvent.Register<VillagerProfession> ev)
		{
			ev.getRegistry().register(create(ENGINEER));
			ev.getRegistry().register(create(MACHINIST));
			ev.getRegistry().register(create(ELECTRICIAN));
			ev.getRegistry().register(create(OUTFITTER));
			ev.getRegistry().register(create(GUNSMITH));
		}
	}

	@Mod.EventBusSubscriber(modid = MODID, bus = Bus.FORGE)
	public static class Events
	{
		@SubscribeEvent
		public static void registerTrades(VillagerTradesEvent ev)
		{
			Int2ObjectMap<List<ITrade>> trades = ev.getTrades();
			if(ENGINEER.equals(ev.getType().getRegistryName()))
			{
				trades.get(1).add(new EmeraldForItems(Ingredients.stickTreated, new PriceInterval(8, 16), 16, 2));
				trades.get(1).add(new ItemsForEmerald(WoodenDecoration.treatedWood.get(TreatedWoodStyles.HORIZONTAL), new PriceInterval(-10, -6), 12, 1, 0.2f));
				trades.get(1).add(new ItemsForEmerald(Cloth.balloon, new PriceInterval(-3, -1), 12, 1, 0.2f));

				trades.get(2).add(new EmeraldForItems(Ingredients.stickIron, new PriceInterval(2, 6), 12, 10));
				trades.get(2).add(new ItemsForEmerald(MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD), new PriceInterval(-8, -4), 12, 5, 0.2f));
				trades.get(2).add(new ItemsForEmerald(MetalDecoration.aluScaffolding.get(MetalScaffoldingType.STANDARD), new PriceInterval(-8, -4), 12, 5, 0.2f));

				trades.get(3).add(new EmeraldForItems(Ingredients.stickSteel, new PriceInterval(2, 6), 12, 20));
				trades.get(3).add(new EmeraldForItems(Ingredients.slag, new PriceInterval(4, 8), 12, 20));
				trades.get(3).add(new ItemsForEmerald(StoneDecoration.concrete, new PriceInterval(-6, -2), 12, 10, 0.2f));

				trades.get(4).add(new OreveinMapForEmeralds());
			}
			else if(MACHINIST.equals(ev.getType().getRegistryName()))
			{
				/* Machinist
				 * Sells tools, metals, blueprints and drillheads
				 */
				trades.get(1).add(new EmeraldForItems(Ingredients.coalCoke, new PriceInterval(8, 16), 16, 2));
				trades.get(1).add(new ItemsForEmerald(Tools.hammer, new PriceInterval(4, 7), 12, 1, 0.2f));

				trades.get(2).add(new EmeraldForItems(Metals.ingots.get(EnumMetals.COPPER), new PriceInterval(4, 6), 12, 10));
				trades.get(2).add(new EmeraldForItems(Metals.ingots.get(EnumMetals.ALUMINUM), new PriceInterval(4, 6), 12, 10));
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
				trades.get(1).add(new EmeraldForItems(Ingredients.wireCopper, new PriceInterval(8, 16), 16, 2));
				trades.get(1).add(new ItemsForEmerald(Tools.wirecutter, new PriceInterval(4, 7), 12, 1, 0.2f));
				trades.get(1).add(new ItemsForEmerald(wireCoils.get(WireType.COPPER), new PriceInterval(-4, -2), 12, 1, 0.2f));

				trades.get(2).add(new EmeraldForItems(Ingredients.wireElectrum, new PriceInterval(6, 12), 12, 10));
				trades.get(2).add(new ItemsForEmerald(Tools.voltmeter, new PriceInterval(4, 7), 12, 5, 0.2f));
				trades.get(2).add(new ItemsForEmerald(wireCoils.get(WireType.ELECTRUM), new PriceInterval(-4, -1), 12, 5, 0.2f));
				trades.get(2).add(new ItemsForEmerald(IEItems.Misc.faradaySuit.get(EquipmentSlotType.FEET), new PriceInterval(5, 7), 12, 5, 0.2f));
				trades.get(2).add(new ItemsForEmerald(IEItems.Misc.faradaySuit.get(EquipmentSlotType.LEGS), new PriceInterval(9, 11), 12, 5, 0.2f));

				trades.get(3).add(new EmeraldForItems(Ingredients.wireAluminum, new PriceInterval(4, 8), 12, 20));
				trades.get(3).add(new ItemsForEmerald(wireCoils.get(WireType.STEEL), new PriceInterval(-2, -1), 12, 10, 0.2f));
				trades.get(3).add(new ItemsForEmerald(IEItems.Misc.faradaySuit.get(EquipmentSlotType.CHEST), new PriceInterval(11, 15), 12, 10, 0.2f));
				trades.get(3).add(new ItemsForEmerald(IEItems.Misc.faradaySuit.get(EquipmentSlotType.HEAD), new PriceInterval(5, 7), 12, 10, 0.2f));

				trades.get(4).add(new ItemsForEmerald(IEItems.Misc.fluorescentTube, new PriceInterval(8, 12), 3, 15, 0.2f));
				trades.get(4).add(new ItemsForEmerald(toolUpgrades.get(ToolUpgrade.REVOLVER_ELECTRO), new PriceInterval(8, 12), 3, 15, 0.2f));

				trades.get(5).add(new ItemsForEmerald(toolUpgrades.get(ToolUpgrade.RAILGUN_CAPACITORS), new PriceInterval(8, 12), 3, 30, 0.2f));
			}
			else if(OUTFITTER.equals(ev.getType().getRegistryName()))
			{
				/* Outfitter
				 * Sells Shaderbags
				 */
				Item bag_common = IEItems.Misc.shaderBag.get(Rarity.COMMON);
				Item bag_uncommon = IEItems.Misc.shaderBag.get(Rarity.UNCOMMON);
				Item bag_rare = IEItems.Misc.shaderBag.get(Rarity.RARE);

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

	private static class EmeraldForItems implements ITrade
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

		public EmeraldForItems(@Nonnull IItemProvider item, @Nonnull PriceInterval buyAmounts, int maxUses, int xp)
		{
			this(new ItemStack(item), buyAmounts, maxUses, xp);
		}

		@Nullable
		@Override
		public MerchantOffer getOffer(Entity trader, Random rand)
		{
			return new MerchantOffer(
					ApiUtils.copyStackWithAmount(this.buyingItem, this.buyAmounts.getPrice(rand)),
					new ItemStack(Items.EMERALD),
					//TODO adjust values for individual trades
					maxUses, xp, 0.05f);
		}
	}

	private static class ItemsForEmerald implements ITrade
	{
		public ItemStack sellingItem;
		public PriceInterval priceInfo;
		final int maxUses;
		final int xp;
		final float priceMult;

		public ItemsForEmerald(IItemProvider par1Item, PriceInterval priceInfo, int maxUses, int xp, float priceMult)
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
				selling = ApiUtils.copyStackWithAmount(sellingItem, -i);
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

	private static class OreveinMapForEmeralds implements ITrade
	{
		public PriceInterval value;

		public OreveinMapForEmeralds()
		{
		}

		@Override
		@Nullable
		public MerchantOffer getOffer(Entity trader, @Nonnull Random random)
		{
			World world = trader.getEntityWorld();
			BlockPos merchantPos = trader.getPosition();

			int cX = merchantPos.getX() >> 4;
			int cZ = merchantPos.getZ() >> 4;
			DimensionChunkCoords chunkCoords = null;
			for(int i = 0; i < 8; i++) //Let's just try this a maximum of 8 times before I give up
			{
				chunkCoords = new DimensionChunkCoords(world.getDimension().getType(), cX+(random.nextInt(32)-16)*2, cZ+(random.nextInt(32)-16)*2);
				if(!ExcavatorHandler.mineralCache.containsKey(chunkCoords))
					break;
				else
					chunkCoords = null;
			}

			if(chunkCoords!=null)
			{
				MineralWorldInfo mineralWorldInfo = ExcavatorHandler.getMineralWorldInfo(world, chunkCoords, true);
				if(mineralWorldInfo==null||mineralWorldInfo.mineral==null)
				{
					if(!world.isRemote)
						IELogger.logger.error("Null "+(mineralWorldInfo==null?"WorldInfo": "Mineral")+" on building Cartographer trade.");
					return null;
				}
				BlockPos blockPos = new BlockPos(chunkCoords.getXStart()+8, 64, chunkCoords.getZStart()+8);
				ItemStack selling = FilledMapItem.setupNewMap(world, blockPos.getX(), blockPos.getZ(), (byte)1, true, true);
				FilledMapItem.renderBiomePreviewMap(world, selling);
				MapData.addTargetDecoration(selling, blockPos, "ie:coresample_treasure", Type.TARGET_POINT);
				selling.setDisplayName(new TranslationTextComponent("item.immersiveengineering.map_orevein.name"));
				ItemNBTHelper.setLore(selling, mineralWorldInfo.mineral.name);

				return new MerchantOffer(new ItemStack(Items.EMERALD, 8+random.nextInt(8)),
						new ItemStack(Metals.ingots.get(EnumMetals.COPPER)), selling, 0, 16, 30, 0.5F);
			}
			return null;
		}
	}

	private static class RevolverPieceForEmeralds implements ITrade
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
			if(trader instanceof AbstractVillagerEntity&&((AbstractVillagerEntity)trader).hasCustomer())
			{
				luck = ((AbstractVillagerEntity)trader).getCustomer().getLuck();
			}
			CompoundNBT perksTag = RevolverItem.RevolverPerk.generatePerkSet(random, luck);
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
