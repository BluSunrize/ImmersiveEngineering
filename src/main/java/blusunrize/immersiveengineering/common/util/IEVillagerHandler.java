/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralWorldInfo;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.world.VillageEngineersHouse;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration.Type;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * @author BluSunrize - 23.07.2016
 */
public class IEVillagerHandler
{
	private static final VillagerRegistry VILLAGER_REGISTRY = VillagerRegistry.instance();
	public static VillagerRegistry.VillagerProfession PROF_ENGINEER;

	@ObjectHolder("minecraft:librarian")
	public static final VillagerProfession LIBRARIAN = null;

	public static void initIEVillagerHouse()
	{
		if(!IEConfig.villagerHouse)
			return;

		VILLAGER_REGISTRY.registerVillageCreationHandler(new VillageEngineersHouse.VillageManager());
		MapGenStructureIO.registerStructureComponent(VillageEngineersHouse.class, ImmersiveEngineering.MODID+":EngineersHouse");
	}

	public static void initIEVillagerTrades()
	{
		if(!IEConfig.enableVillagers)
			return;

		PROF_ENGINEER = new VillagerRegistry.VillagerProfession(ImmersiveEngineering.MODID+":engineer", "immersiveengineering:textures/models/villager_engineer.png", "immersiveengineering:textures/models/villager_engineer_zombie.png");
		ForgeRegistries.VILLAGER_PROFESSIONS.register(PROF_ENGINEER);

		if(LIBRARIAN!=null)
			LIBRARIAN.getCareer(1).addTrade(4, new OreveinMapForEmeralds());

		/* Engineer
		 * Deals in treated wood, later metal rods, scaffold and concrete
		 */
		VillagerRegistry.VillagerCareer career_engineer = new VillagerRegistry.VillagerCareer(PROF_ENGINEER, ImmersiveEngineering.MODID+".engineer");
		career_engineer.addTrade(1,
				new EmeraldForItemstack(new ItemStack(IEContent.itemMaterial, 1, 0), new EntityVillager.PriceInfo(8, 16)),
				new ItemstackForEmerald(new ItemStack(IEContent.blockWoodenDecoration, 1, 1), new EntityVillager.PriceInfo(-10, -6)),
				new ItemstackForEmerald(new ItemStack(IEContent.blockClothDevice, 1, 1), new EntityVillager.PriceInfo(-3, -1))
		);
		career_engineer.addTrade(2,
				new EmeraldForItemstack(new ItemStack(IEContent.itemMaterial, 1, 1), new EntityVillager.PriceInfo(2, 6)),
				new ItemstackForEmerald(new ItemStack(IEContent.blockMetalDecoration1, 1, 1), new EntityVillager.PriceInfo(-8, -4)),
				new ItemstackForEmerald(new ItemStack(IEContent.blockMetalDecoration1, 1, 5), new EntityVillager.PriceInfo(-8, -4))
		);
		career_engineer.addTrade(3,
				new EmeraldForItemstack(new ItemStack(IEContent.itemMaterial, 1, 2), new EntityVillager.PriceInfo(2, 6)),
				new EmeraldForItemstack(new ItemStack(IEContent.itemMaterial, 1, 7), new EntityVillager.PriceInfo(4, 8)),
				new ItemstackForEmerald(new ItemStack(IEContent.blockStoneDecoration, 1, 5), new EntityVillager.PriceInfo(-6, -2))
		);
		career_engineer.addTrade(4,
				new OreveinMapForEmeralds()
		);

		/* Machinist
		 * Sells tools, metals, blueprints and drillheads
		 */
		VillagerRegistry.VillagerCareer career_machinist = new VillagerRegistry.VillagerCareer(PROF_ENGINEER, ImmersiveEngineering.MODID+".machinist");
		career_machinist.addTrade(1,
				new EmeraldForItemstack(new ItemStack(IEContent.itemMaterial, 1, 6), new EntityVillager.PriceInfo(8, 16)),
				new ItemstackForEmerald(new ItemStack(IEContent.itemTool, 1, 0), new EntityVillager.PriceInfo(4, 7))
		);
		career_machinist.addTrade(2,
				new EmeraldForItemstack(new ItemStack(IEContent.itemMetal, 1, 0), new EntityVillager.PriceInfo(4, 6)),
				new EmeraldForItemstack(new ItemStack(IEContent.itemMetal, 1, 1), new EntityVillager.PriceInfo(4, 6)),
				new ItemstackForEmerald(new ItemStack(IEContent.itemMaterial, 1, 9), new EntityVillager.PriceInfo(1, 3))
		);
		career_machinist.addTrade(3,
				new ItemstackForEmerald(new ItemStack(IEContent.itemToolbox, 1, 0), new EntityVillager.PriceInfo(6, 8)),
				new ItemstackForEmerald(new ItemStack(IEContent.itemMaterial, 1, 10), new EntityVillager.PriceInfo(1, 3))
		);
		career_machinist.addTrade(4,
				new ItemstackForEmerald(new ItemStack(IEContent.itemDrillhead, 1, 1), new EntityVillager.PriceInfo(28, 40)),
				new ItemstackForEmerald(IEContent.itemEarmuffs, new EntityVillager.PriceInfo(4, 9))
		);
		career_machinist.addTrade(5,
				new ItemstackForEmerald(new ItemStack(IEContent.itemDrillhead, 1, 0), new EntityVillager.PriceInfo(32, 48)),
				new ItemstackForEmerald(BlueprintCraftingRecipe.getTypedBlueprint("electrode"), new EntityVillager.PriceInfo(12, 24))
		);

		/* Electrician
		 * Sells wires, tools and the faraday suit
		 */
		VillagerRegistry.VillagerCareer career_electrician = new VillagerRegistry.VillagerCareer(PROF_ENGINEER, ImmersiveEngineering.MODID+".electrician");
		career_electrician.addTrade(1,
				new EmeraldForItemstack(new ItemStack(IEContent.itemMaterial, 1, 20), new EntityVillager.PriceInfo(8, 16)),
				new ItemstackForEmerald(new ItemStack(IEContent.itemTool, 1, 1), new EntityVillager.PriceInfo(4, 7)),
				new ItemstackForEmerald(new ItemStack(IEContent.itemWireCoil, 1, 0), new EntityVillager.PriceInfo(-4, -2))
		);
		career_electrician.addTrade(2,
				new EmeraldForItemstack(new ItemStack(IEContent.itemMaterial, 1, 21), new EntityVillager.PriceInfo(6, 12)),
				new ItemstackForEmerald(new ItemStack(IEContent.itemTool, 1, 2), new EntityVillager.PriceInfo(4, 7)),
				new ItemstackForEmerald(new ItemStack(IEContent.itemWireCoil, 1, 1), new EntityVillager.PriceInfo(-4, -1))
		);
		career_electrician.addTrade(3,
				new EmeraldForItemstack(new ItemStack(IEContent.itemMaterial, 1, 22), new EntityVillager.PriceInfo(4, 8)),
				new ItemstackForEmerald(new ItemStack(IEContent.itemWireCoil, 1, 2), new EntityVillager.PriceInfo(-2, -1)),
				new ItemstackForEmerald(new ItemStack(IEContent.itemToolUpgrades, 1, 6), new EntityVillager.PriceInfo(8, 12))
		);
		career_electrician.addTrade(4,
				new ItemstackForEmerald(new ItemStack(IEContent.itemToolUpgrades, 1, 9), new EntityVillager.PriceInfo(8, 12)),
				new ItemstackForEmerald(new ItemStack(IEContent.itemFluorescentTube), new EntityVillager.PriceInfo(8, 12)),
				new ItemstackForEmerald(new ItemStack(IEContent.itemsFaradaySuit[0]), new EntityVillager.PriceInfo(5, 7)),
				new ItemstackForEmerald(new ItemStack(IEContent.itemsFaradaySuit[1]), new EntityVillager.PriceInfo(9, 11)),
				new ItemstackForEmerald(new ItemStack(IEContent.itemsFaradaySuit[2]), new EntityVillager.PriceInfo(5, 7)),
				new ItemstackForEmerald(new ItemStack(IEContent.itemsFaradaySuit[3]), new EntityVillager.PriceInfo(11, 15))
		);

		/* Outfitter
		 * Sells Shaderbags
		 */
		VillagerRegistry.VillagerCareer career_outfitter = new VillagerRegistry.VillagerCareer(PROF_ENGINEER, ImmersiveEngineering.MODID+".outfitter");

		ItemStack bag_common = new ItemStack(IEContent.itemShaderBag);
		ItemNBTHelper.setString(bag_common, "rarity", EnumRarity.COMMON.toString());
		ItemStack bag_uncommon = new ItemStack(IEContent.itemShaderBag);
		ItemNBTHelper.setString(bag_uncommon, "rarity", EnumRarity.UNCOMMON.toString());
		ItemStack bag_rare = new ItemStack(IEContent.itemShaderBag);
		ItemNBTHelper.setString(bag_rare, "rarity", EnumRarity.RARE.toString());

		career_outfitter.addTrade(1,
				new ItemstackForEmerald(bag_common, new EntityVillager.PriceInfo(8, 16))
		);
		career_outfitter.addTrade(2,
				new ItemstackForEmerald(bag_uncommon, new EntityVillager.PriceInfo(12, 20))
		);
		career_outfitter.addTrade(3,
				new ItemstackForEmerald(bag_rare, new EntityVillager.PriceInfo(16, 24))
		);

		/* Gunsmith
		 * Sells ammunition, blueprints and revolver parts
		 */
		VillagerRegistry.VillagerCareer career_gunsmith = new VillagerRegistry.VillagerCareer(PROF_ENGINEER, ImmersiveEngineering.MODID+".gunsmith");

		career_gunsmith.addTrade(1,
				new EmeraldForItemstack(BulletHandler.emptyCasing, new EntityVillager.PriceInfo(6, 12)),
				new EmeraldForItemstack(BulletHandler.emptyShell, new EntityVillager.PriceInfo(6, 12)),
				new ItemstackForEmerald(new ItemStack(IEContent.itemMaterial, 1, 13), new EntityVillager.PriceInfo(2, 4))
		);
		career_gunsmith.addTrade(2,
				new ItemstackForEmerald(BlueprintCraftingRecipe.getTypedBlueprint("bullet"), new EntityVillager.PriceInfo(3, 6)),
				new ItemstackForEmerald(BulletHandler.getBulletStack("casull"), new EntityVillager.PriceInfo(-4, -2)),
				new RevolverPieceForEmeralds()
		);
		career_gunsmith.addTrade(3,
				new RevolverPieceForEmeralds(),
				new ItemstackForEmerald(BulletHandler.getBulletStack("buckshot"), new EntityVillager.PriceInfo(-6, -2)),
				new ItemstackForEmerald(BulletHandler.getBulletStack("flare"), new EntityVillager.PriceInfo(-2, -1)),
				new ItemstackForEmerald(BlueprintCraftingRecipe.getTypedBlueprint("specialBullet"), new EntityVillager.PriceInfo(5, 9))
		);
		career_gunsmith.addTrade(4,
				new RevolverPieceForEmeralds(),
				new ItemstackForEmerald(BulletHandler.getBulletStack("silver"), new EntityVillager.PriceInfo(-4, -1)),
				new ItemstackForEmerald(BulletHandler.getBulletStack("he"), new EntityVillager.PriceInfo(2, 4))
		);
		career_gunsmith.addTrade(5,
				new RevolverPieceForEmeralds(),
				new RevolverPieceForEmeralds(),
				new RevolverPieceForEmeralds()
		);
	}

	private static class EmeraldForItemstack implements EntityVillager.ITradeList
	{
		public ItemStack buyingItem;
		public EntityVillager.PriceInfo buyAmounts;

		public EmeraldForItemstack(@Nonnull ItemStack item, @Nonnull EntityVillager.PriceInfo buyAmounts)
		{
			this.buyingItem = item;
			this.buyAmounts = buyAmounts;
		}

		@Override
		public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random)
		{
			recipeList.add(new MerchantRecipe(Utils.copyStackWithAmount(this.buyingItem, this.buyAmounts.getPrice(random)), Items.EMERALD));
		}
	}

	private static class ItemstackForEmerald implements EntityVillager.ITradeList
	{
		public ItemStack sellingItem;
		public EntityVillager.PriceInfo priceInfo;

		public ItemstackForEmerald(Item par1Item, EntityVillager.PriceInfo priceInfo)
		{
			this.sellingItem = new ItemStack(par1Item);
			this.priceInfo = priceInfo;
		}

		public ItemstackForEmerald(ItemStack stack, EntityVillager.PriceInfo priceInfo)
		{
			this.sellingItem = stack;
			this.priceInfo = priceInfo;
		}

		@Override
		public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random)
		{
			int i = 1;
			if(this.priceInfo!=null)
				i = this.priceInfo.getPrice(random);
			ItemStack itemstack;
			ItemStack itemstack1;
			if(i < 0)
			{
				itemstack = new ItemStack(Items.EMERALD);
				itemstack1 = Utils.copyStackWithAmount(sellingItem, -i);
			}
			else
			{
				itemstack = new ItemStack(Items.EMERALD, i, 0);
				itemstack1 = Utils.copyStackWithAmount(sellingItem, 1);
			}
			recipeList.add(new MerchantRecipe(itemstack, itemstack1));
		}
	}

	private static class OreveinMapForEmeralds implements EntityVillager.ITradeList
	{
		public EntityVillager.PriceInfo value;

		public OreveinMapForEmeralds()
		{
		}

		@Override
		public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random)
		{
			World world = merchant.getWorld();
			BlockPos merchantPos = merchant.getPos();

			int cX = merchantPos.getX() >> 4;
			int cZ = merchantPos.getZ() >> 4;
			DimensionChunkCoords chunkCoords = null;
			for(int i = 0; i < 8; i++) //Let's just try this a maximum of 8 times before I give up
			{
				chunkCoords = new DimensionChunkCoords(merchant.getWorld().provider.getDimension(), cX+(random.nextInt(32)-16)*2, cZ+(random.nextInt(32)-16)*2);
				if(!ExcavatorHandler.mineralCache.containsKey(chunkCoords))
					break;
				else
					chunkCoords = null;
			}

			if(chunkCoords!=null)
			{
				MineralWorldInfo mineralWorldInfo = ExcavatorHandler.getMineralWorldInfo(merchant.getWorld(), chunkCoords, true);
				if(mineralWorldInfo==null||mineralWorldInfo.mineral==null)
				{
					if(!world.isRemote)
						IELogger.error("Null "+(mineralWorldInfo==null?"WorldInfo": "Mineral")+" on building Cartographer trade.");
					return;
				}
				BlockPos blockPos = new BlockPos(chunkCoords.getXStart()+8, 64, chunkCoords.getZStart()+8);
				ItemStack itemstack = ItemMap.setupNewMap(world, blockPos.getX(), blockPos.getZ(), (byte)1, true, true);
				ItemMap.renderBiomePreviewMap(world, itemstack);
				MapData.addTargetDecoration(itemstack, blockPos, "ie:coresample_treasure", Type.TARGET_POINT);
				itemstack.setTranslatableName("item.immersiveengineering.map_orevein.name");
				ItemNBTHelper.setLore(itemstack, mineralWorldInfo.mineral.name);

				recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 8+random.nextInt(8)), new ItemStack(IEContent.itemMetal), itemstack));
			}
		}
	}

	private static class RevolverPieceForEmeralds implements EntityVillager.ITradeList
	{
		public RevolverPieceForEmeralds()
		{
		}

		@Override
		public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random)
		{
			ItemStack stack = new ItemStack(IEContent.itemMaterial, 1, 14+random.nextInt(3));
			ItemNBTHelper.setBoolean(stack, "generatePerks", true);
			recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 1), ItemStack.EMPTY, stack, 0, 1));
		}
	}
}
