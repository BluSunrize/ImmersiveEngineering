/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.entities.*;
import blusunrize.immersiveengineering.common.items.ToolUpgradeItem.ToolUpgrade;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.EquipmentSlotType.Group;
import net.minecraft.item.*;
import net.minecraft.item.Item.Properties;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

public final class IEItems
{
	public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, Lib.MODID);

	private IEItems()
	{
	}

	public static final class Molds
	{
		public static ItemRegObject<IEBaseItem> moldPlate = simpleWithStackSize("mold_plate", 1);
		public static ItemRegObject<IEBaseItem> moldGear = simpleWithStackSize("mold_gear", 1);
		public static ItemRegObject<IEBaseItem> moldRod = simpleWithStackSize("mold_rod", 1);
		public static ItemRegObject<IEBaseItem> moldBulletCasing = simpleWithStackSize("mold_bullet_casing", 1);
		public static ItemRegObject<IEBaseItem> moldWire = simpleWithStackSize("mold_wire", 1);
		public static ItemRegObject<IEBaseItem> moldPacking4 = simpleWithStackSize("mold_packing_4", 1);
		public static ItemRegObject<IEBaseItem> moldPacking9 = simpleWithStackSize("mold_packing_9", 1);
		public static ItemRegObject<IEBaseItem> moldUnpacking = simpleWithStackSize("mold_unpacking", 1);

		private static void init()
		{
		}
	}

	public static final class Ingredients
	{
		public static ItemRegObject<IEBaseItem> stickTreated = simple("stick_treated");
		public static ItemRegObject<IEBaseItem> stickIron = simple("stick_iron");
		public static ItemRegObject<IEBaseItem> stickSteel = simple("stick_steel");
		public static ItemRegObject<IEBaseItem> stickAluminum = simple("stick_aluminum");
		public static ItemRegObject<IEBaseItem> hempFiber = simple("hemp_fiber");
		public static ItemRegObject<IEBaseItem> hempFabric = simple("hemp_fabric");
		public static ItemRegObject<IEBaseItem> coalCoke = simple("coal_coke", nothing(), i -> i.setBurnTime(3200));
		public static ItemRegObject<IEBaseItem> slag = simple("slag");
		public static ItemRegObject<IEBaseItem> componentIron = simple("component_iron");
		public static ItemRegObject<IEBaseItem> componentSteel = simple("component_steel");
		public static ItemRegObject<IEBaseItem> waterwheelSegment = simple("waterwheel_segment");
		public static ItemRegObject<IEBaseItem> windmillBlade = simple("windmill_blade");
		public static ItemRegObject<IEBaseItem> windmillSail = simple("windmill_sail");
		public static ItemRegObject<IEBaseItem> woodenGrip = simple("wooden_grip");
		public static ItemRegObject<IEBaseItem> gunpartBarrel = simple("gunpart_barrel");
		public static ItemRegObject<IEBaseItem> gunpartDrum = simple("gunpart_drum");
		public static ItemRegObject<IEBaseItem> gunpartHammer = simple("gunpart_hammer");
		public static ItemRegObject<IEBaseItem> dustCoke = simple("dust_coke");
		public static ItemRegObject<IEBaseItem> dustHopGraphite = simple("dust_hop_graphite");
		public static ItemRegObject<IEBaseItem> ingotHopGraphite = simple("ingot_hop_graphite");
		public static ItemRegObject<IEBaseItem> wireCopper = simple("wire_copper");
		public static ItemRegObject<IEBaseItem> wireElectrum = simple("wire_electrum");
		public static ItemRegObject<IEBaseItem> wireAluminum = simple("wire_aluminum");
		public static ItemRegObject<IEBaseItem> wireSteel = simple("wire_steel");
		public static ItemRegObject<IEBaseItem> wireLead = simple("wire_lead");
		public static ItemRegObject<IEBaseItem> dustSaltpeter = simple("dust_saltpeter");
		public static ItemRegObject<IEBaseItem> dustSulfur = simple("dust_sulfur");
		public static ItemRegObject<IEBaseItem> dustWood = simple("dust_wood", nothing(), i -> i.setBurnTime(100));
		public static ItemRegObject<IEBaseItem> electronTube = simple("electron_tube");
		public static ItemRegObject<IEBaseItem> circuitBoard = simple("circuit_board");
		public static ItemRegObject<IEBaseItem> emptyCasing = simple("empty_casing");
		public static ItemRegObject<IEBaseItem> emptyShell = simple("empty_shell");

		private static void init()
		{
		}
	}

	public static final class Metals
	{
		public static Map<EnumMetals, ItemRegObject<Item>> ingots = new EnumMap<>(EnumMetals.class);
		public static Map<EnumMetals, ItemRegObject<Item>> nuggets = new EnumMap<>(EnumMetals.class);
		public static Map<EnumMetals, ItemRegObject<IEBaseItem>> dusts = new EnumMap<>(EnumMetals.class);
		public static Map<EnumMetals, ItemRegObject<IEBaseItem>> plates = new EnumMap<>(EnumMetals.class);

		private static void init()
		{
			for(EnumMetals m : EnumMetals.values())
			{
				String name = m.tagName();
				ItemRegObject<Item> nugget;
				ItemRegObject<Item> ingot;
				if(!m.isVanillaMetal())
				{
					nugget = register("nugget_"+name, IEBaseItem::new);
					ingot = register("ingot_"+name, IEBaseItem::new);
				}
				else if(m==EnumMetals.IRON)
				{
					nugget = of(Items.IRON_NUGGET);
					ingot = of(Items.IRON_INGOT);
				}
				else if(m==EnumMetals.GOLD)
				{
					nugget = of(Items.GOLD_NUGGET);
					ingot = of(Items.GOLD_INGOT);
				}
				else
					throw new RuntimeException("Unkown vanilla metal: "+m.name());
				IEItems.Metals.nuggets.put(m, nugget);
				IEItems.Metals.ingots.put(m, ingot);
				IEItems.Metals.plates.put(m, simple("plate_"+name));
				IEItems.Metals.dusts.put(m, simple("dust_"+name));
			}
		}
	}

	public static final class Tools
	{
		public static ItemRegObject<HammerItem> hammer = register("hammer", HammerItem::new);
		public static ItemRegObject<WirecutterItem> wirecutter = register("wirecutter", WirecutterItem::new);
		public static ItemRegObject<ScrewdriverItem> screwdriver = register("screwdriver", ScrewdriverItem::new);
		public static ItemRegObject<ManualItem> manual = register("manual", ManualItem::new);
		public static ItemRegObject<VoltmeterItem> voltmeter = register("voltmeter", VoltmeterItem::new);

		public static ItemRegObject<PickaxeItem> steelPick = register(
				"pickaxe_steel", IETools.createPickaxe(Lib.MATERIAL_Steel)
		);
		public static ItemRegObject<ShovelItem> steelShovel = register(
				"shovel_steel", IETools.createShovel(Lib.MATERIAL_Steel)
		);
		public static ItemRegObject<AxeItem> steelAxe = register(
				"axe_steel", IETools.createAxe(Lib.MATERIAL_Steel)
		);
		public static ItemRegObject<HoeItem> steelHoe = register(
				"hoe_steel", IETools.createHoe(Lib.MATERIAL_Steel)
		);
		public static ItemRegObject<SwordItem> steelSword = register(
				"sword_steel", IETools.createSword(Lib.MATERIAL_Steel)
		);
		public static Map<EquipmentSlotType, ItemRegObject<SteelArmorItem>> steelArmor = new EnumMap<>(EquipmentSlotType.class);

		public static ItemRegObject<ToolboxItem> toolbox = register("toolbox", ToolboxItem::new);

		public static ItemRegObject<DrillItem> drill = register("drill", DrillItem::new);
		public static ItemRegObject<DrillheadItem> drillheadSteel = register(
				"drillhead_steel", () -> new DrillheadItem(DrillheadItem.STEEL)
		);
		public static ItemRegObject<DrillheadItem> drillheadIron = register(
				"drillhead_iron", () -> new DrillheadItem(DrillheadItem.IRON)
		);

		public static ItemRegObject<BuzzsawItem> buzzsaw = register("buzzsaw", BuzzsawItem::new);
		public static ItemRegObject<SawbladeItem> sawblade = register(
				"sawblade", () -> new SawbladeItem(10000, 8f, 9f)
		);
		public static ItemRegObject<RockcutterItem> rockcutter = register(
				"rockcutter", () -> new RockcutterItem(5000, 5f, 9f)
		);

		public static ItemRegObject<SurveyToolsItem> surveyTools = register("survey_tools", SurveyToolsItem::new);

		private static void init()
		{
			for(EquipmentSlotType slot : EquipmentSlotType.values())
				if(slot.getSlotType()==Group.ARMOR)
					steelArmor.put(slot, register(
							"armor_steel_"+slot.getName().toLowerCase(Locale.ENGLISH), () -> new SteelArmorItem(slot)
					));
		}
	}

	public static final class Weapons
	{
		public static ItemRegObject<RevolverItem> revolver = register("revolver", RevolverItem::new);
		public static ItemRegObject<SpeedloaderItem> speedloader = register("speedloader", SpeedloaderItem::new);
		public static Map<IBullet, ItemRegObject<BulletItem>> bullets = new IdentityHashMap<>();
		public static ItemRegObject<ChemthrowerItem> chemthrower = register("chemthrower", ChemthrowerItem::new);
		public static ItemRegObject<RailgunItem> railgun = register("railgun", RailgunItem::new);

		private static void init()
		{
			for(ResourceLocation bulletType : BulletHandler.getAllKeys())
			{
				IBullet bullet = BulletHandler.getBullet(bulletType);
				if(bullet.isProperCartridge())
					Weapons.bullets.put(bullet, register(nameFor(bullet), () -> new BulletItem(bullet)));
			}
		}

		private static String nameFor(IBullet bullet)
		{
			ResourceLocation name = BulletHandler.findRegistryName(bullet);
			if(name.getNamespace().equals(ImmersiveEngineering.MODID))
				return name.getPath();
			else
				return name.getNamespace()+"_"+name.getPath();
		}
	}

	public static final class BannerPatterns
	{
		public static ItemRegObject<BannerPatternItem> bannerPatternHammer = addBanner("hammer", "hmr");
		public static ItemRegObject<BannerPatternItem> bannerPatternBevels = addBanner("bevels", "bvl");
		public static ItemRegObject<BannerPatternItem> bannerPatternOrnate = addBanner("ornate", "orn");
		public static ItemRegObject<BannerPatternItem> bannerPatternTreatedWood = addBanner("treated_wood", "twd");
		public static ItemRegObject<BannerPatternItem> bannerPatternWindmill = addBanner("windmill", "wnd");
		public static ItemRegObject<BannerPatternItem> bannerPatternWolfR = addBanner("wolf_r", "wlfr");
		public static ItemRegObject<BannerPatternItem> bannerPatternWolfL = addBanner("wolf_l", "wlfl");
		public static ItemRegObject<BannerPatternItem> bannerPatternWolf = addBanner("wolf", "wlf");

		private static ItemRegObject<BannerPatternItem> addBanner(String name, String id)
		{
			return register(
					"bannerpattern_"+name,
					() -> {
						String enumName = MODID+"_"+name;
						String fullId = "ie_"+id;
						BannerPattern pattern = BannerPattern.create(enumName.toUpperCase(), enumName, fullId, true);
						return new BannerPatternItem(pattern, new Properties().group(ImmersiveEngineering.ITEM_GROUP));
					}
			);
		}

		private static void init()
		{
		}
	}

	public static final class Minecarts
	{
		public static ItemRegObject<IEMinecartItem> cartWoodenCrate = register("woodencrate", CrateMinecartEntity::new);
		public static ItemRegObject<IEMinecartItem> cartReinforcedCrate = register("reinforcedcrate", ReinforcedCrateMinecartEntity::new);
		public static ItemRegObject<IEMinecartItem> cartWoodenBarrel = register("woodenbarrel", BarrelMinecartEntity::new);
		public static ItemRegObject<IEMinecartItem> cartMetalBarrel = register("metalbarrel", MetalBarrelMinecartEntity::new);

		private static void init()
		{
		}

		private static ItemRegObject<IEMinecartItem> register(String name, IEMinecartEntity.MinecartConstructor constructor)
		{
			return IEItems.register("minecart_"+name, () -> new IEMinecartItem(constructor));
		}
	}

	//TODO move all of these somewhere else
	public static final class Misc
	{
		public static Map<WireType, ItemRegObject<WireCoilItem>> wireCoils = new LinkedHashMap<>();
		public static Map<ToolUpgrade, ItemRegObject<ToolUpgradeItem>> toolUpgrades = new EnumMap<>(ToolUpgrade.class);

		public static ItemRegObject<IESeedItem> hempSeeds = register(
				"seed", () -> new IESeedItem(IEBlocks.Misc.hempPlant.get())
		);
		public static ItemRegObject<JerrycanItem> jerrycan = register("jerrycan", JerrycanItem::new);
		public static ItemRegObject<EngineersBlueprintItem> blueprint = register("blueprint", EngineersBlueprintItem::new);
		public static ItemRegObject<SkyhookItem> skyhook = register("skyhook", SkyhookItem::new);
		public static ItemRegObject<ShaderItem> shader = register("shader", ShaderItem::new);
		public static Map<Rarity, ItemRegObject<ShaderBagItem>> shaderBag = new EnumMap<>(Rarity.class);
		public static ItemRegObject<EarmuffsItem> earmuffs = register("earmuffs", EarmuffsItem::new);
		public static ItemRegObject<CoresampleItem> coresample = register("coresample", CoresampleItem::new);
		public static ItemRegObject<GraphiteElectrodeItem> graphiteElectrode = register("graphite_electrode", GraphiteElectrodeItem::new);
		public static Map<EquipmentSlotType, ItemRegObject<FaradaySuitItem>> faradaySuit = new EnumMap<>(EquipmentSlotType.class);
		public static ItemRegObject<FluorescentTubeItem> fluorescentTube = register("fluorescent_tube", FluorescentTubeItem::new);
		public static ItemRegObject<PowerpackItem> powerpack = register("powerpack", PowerpackItem::new);
		public static ItemRegObject<IEShieldItem> shield = register("shield", IEShieldItem::new);
		public static ItemRegObject<MaintenanceKitItem> maintenanceKit = register("maintenance_kit", MaintenanceKitItem::new);
		public static ItemRegObject<LogicCircuitBoardItem> logicCircuitBoard = register("logic_circuit", LogicCircuitBoardItem::new);

		public static ItemRegObject<FakeIconItem> iconBirthday = icon("birthday");
		public static ItemRegObject<FakeIconItem> iconLucky = icon("lucky");
		public static ItemRegObject<FakeIconItem> iconDrillbreak = icon("drillbreak");
		public static ItemRegObject<FakeIconItem> iconRavenholm = icon("ravenholm");

		public static ItemRegObject<PotionBucketItem> potionBucket = IEItems.register("potion_bucket", PotionBucketItem::new);

		private static ItemRegObject<FakeIconItem> icon(String name)
		{
			return register("fake_icon_"+name, FakeIconItem::new);
		}

		private static void init()
		{
			for(WireType t : WireType.getIEWireTypes())
				IEItems.Misc.wireCoils.put(t, register(
						"wirecoil_"+t.getUniqueName().toLowerCase(Locale.US), () -> new WireCoilItem(t)
				));
			for(ToolUpgrade upgrade : ToolUpgrade.values())
				IEItems.Misc.toolUpgrades.put(upgrade, register(
						"toolupgrade_"+upgrade.name().toLowerCase(Locale.US), () -> new ToolUpgradeItem(upgrade)
				));
			for(EquipmentSlotType slot : EquipmentSlotType.values())
				if(slot.getSlotType()==Group.ARMOR)
					IEItems.Misc.faradaySuit.put(slot, register(
							"armor_faraday_"+slot.getName().toLowerCase(Locale.ENGLISH), () -> new FaradaySuitItem(slot)
					));
		}

		public static void registerShaderBags()
		{
			for(Rarity r : ShaderRegistry.rarityWeightMap.keySet())
				IEItems.Misc.shaderBag.put(r, register(
						"shader_bag_"+r.name().toLowerCase(Locale.US).replace(':', '_'), () -> new ShaderBagItem(r)
				));
		}
	}

	public static void init()
	{
		REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
		// Load all classes to make sure the static variables are initialized
		Molds.init();
		Ingredients.init();
		Metals.init();
		Tools.init();
		Weapons.init();
		BannerPatterns.init();
		Minecarts.init();
		Misc.init();
	}

	private static <T> Consumer<T> nothing()
	{
		return $ -> {
		};
	}

	private static ItemRegObject<IEBaseItem> simpleWithStackSize(String name, int maxSize)
	{
		return simple(name, p -> p.maxStackSize(maxSize), i -> {
		});
	}

	private static ItemRegObject<IEBaseItem> simple(String name)
	{
		return simple(name, $ -> {
		}, $ -> {
		});
	}

	private static ItemRegObject<IEBaseItem> simple(
			String name, Consumer<Properties> makeProps, Consumer<IEBaseItem> processItem
	)
	{
		return register(
				name, () -> Util.make(new IEBaseItem(Util.make(new Properties(), makeProps)), processItem)
		);
	}

	private static <T extends Item> ItemRegObject<T> register(String name, Supplier<? extends T> make)
	{
		return new ItemRegObject<>(REGISTER.register(name, make));
	}

	private static <T extends Item> ItemRegObject<T> of(T existing)
	{
		return new ItemRegObject<>(RegistryObject.of(existing.getRegistryName(), existing::getRegistryType));
	}

	public static class ItemRegObject<T extends Item> implements Supplier<T>, IItemProvider
	{
		private final RegistryObject<T> regObject;

		private ItemRegObject(RegistryObject<T> regObject)
		{
			this.regObject = regObject;
		}

		@Override
		@Nonnull
		public T get()
		{
			return regObject.get();
		}

		@Nonnull
		@Override
		public Item asItem()
		{
			return regObject.get();
		}

		public ResourceLocation getId()
		{
			return regObject.getId();
		}
	}
}
