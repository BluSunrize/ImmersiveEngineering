/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.entities.*;
import blusunrize.immersiveengineering.common.items.*;
import blusunrize.immersiveengineering.common.items.upgrades.ToolUpgrade;
import blusunrize.immersiveengineering.common.items.upgrades.ToolUpgradeItem;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.*;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class IEItems
{
	public static final int COKE_BURN_TIME = 3200;
	public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(BuiltInRegistries.ITEM, Lib.MODID);

	private IEItems()
	{
	}

	public static final class Molds
	{
		public static final ItemRegObject<IEBaseItem> MOLD_PLATE = simpleWithStackSize("mold_plate", 1);
		public static final ItemRegObject<IEBaseItem> MOLD_GEAR = simpleWithStackSize("mold_gear", 1);
		public static final ItemRegObject<IEBaseItem> MOLD_ROD = simpleWithStackSize("mold_rod", 1);
		public static final ItemRegObject<IEBaseItem> MOLD_BULLET_CASING = simpleWithStackSize("mold_bullet_casing", 1);
		public static final ItemRegObject<IEBaseItem> MOLD_WIRE = simpleWithStackSize("mold_wire", 1);
		public static final ItemRegObject<IEBaseItem> MOLD_PACKING_4 = simpleWithStackSize("mold_packing_4", 1);
		public static final ItemRegObject<IEBaseItem> MOLD_PACKING_9 = simpleWithStackSize("mold_packing_9", 1);
		public static final ItemRegObject<IEBaseItem> MOLD_UNPACKING = simpleWithStackSize("mold_unpacking", 1);

		private static void init()
		{
		}
	}

	public static final class Ingredients
	{
		public static final ItemRegObject<IEBaseItem> STICK_TREATED = simple("stick_treated");
		public static final ItemRegObject<IEBaseItem> STICK_IRON = simple("stick_iron");
		public static final ItemRegObject<IEBaseItem> STICK_STEEL = simple("stick_steel");
		public static final ItemRegObject<IEBaseItem> STICK_ALUMINUM = simple("stick_aluminum");
		public static final ItemRegObject<IEBaseItem> STICK_NETHERITE = simple("stick_netherite");
		public static final ItemRegObject<IEBaseItem> NUGGET_NETHERITE = simple("nugget_netherite");
		public static final ItemRegObject<IEBaseItem> HEMP_FIBER = simple("hemp_fiber");
		public static final ItemRegObject<IEBaseItem> HEMP_FABRIC = simple("hemp_fabric");
		public static final ItemRegObject<IEBaseItem> ERSATZ_LEATHER = simple("ersatz_leather");
		public static final ItemRegObject<IEBaseItem> COAL_COKE = simple("coal_coke", nothing(), i -> i.setBurnTime(COKE_BURN_TIME));
		public static final ItemRegObject<IEBaseItem> SLAG = simple("slag");
		public static final ItemRegObject<IEBaseItem> COMPONENT_IRON = simple("component_iron");
		public static final ItemRegObject<IEBaseItem> COMPONENT_STEEL = simple("component_steel");
		public static final ItemRegObject<IEBaseItem> COMPONENT_ELECTRONIC = simple("component_electronic");
		public static final ItemRegObject<IEBaseItem> COMPONENT_ELECTRONIC_ADV = simple("component_electronic_adv");
		public static final ItemRegObject<IEBaseItem> WATERWHEEL_SEGMENT = simple("waterwheel_segment");
		public static final ItemRegObject<IEBaseItem> WINDMILL_BLADE = simple("windmill_blade");
		public static final ItemRegObject<IEBaseItem> WINDMILL_SAIL = simple("windmill_sail");
		public static final ItemRegObject<IEBaseItem> WOODEN_GRIP = simple("wooden_grip");
		public static final ItemRegObject<IEBaseItem> GUNPART_BARREL = register("gunpart_barrel", RevolverpartItem::new);
		public static final ItemRegObject<IEBaseItem> GUNPART_DRUM = register("gunpart_drum", RevolverpartItem::new);
		public static final ItemRegObject<IEBaseItem> GUNPART_HAMMER = register("gunpart_hammer", RevolverpartItem::new);
		public static final ItemRegObject<IEBaseItem> DUST_COKE = simple("dust_coke");
		public static final ItemRegObject<IEBaseItem> DUST_HOP_GRAPHITE = simple("dust_hop_graphite");
		@Deprecated(since = "12.3.1")
		public static final ItemRegObject<IEBaseItem> INGOT_HOP_GRAPHITE = simple("ingot_hop_graphite");
		public static final ItemRegObject<IEBaseItem> PLATE_HOP_GRAPHITE = simple("plate_hop_graphite");
		public static final ItemRegObject<IEBaseItem> WIRE_COPPER = simple("wire_copper");
		public static final ItemRegObject<IEBaseItem> WIRE_ELECTRUM = simple("wire_electrum");
		public static final ItemRegObject<IEBaseItem> WIRE_ALUMINUM = simple("wire_aluminum");
		public static final ItemRegObject<IEBaseItem> WIRE_STEEL = simple("wire_steel");
		public static final ItemRegObject<IEBaseItem> WIRE_LEAD = simple("wire_lead");
		public static final ItemRegObject<IEBaseItem> DUST_SALTPETER = simple("dust_saltpeter");
		public static final ItemRegObject<IEBaseItem> DUST_SULFUR = simple("dust_sulfur");
		public static final ItemRegObject<IEBaseItem> DUST_WOOD = simple("dust_wood", nothing(), i -> i.setBurnTime(100));
		public static final ItemRegObject<IEBaseItem> LIGHT_BULB = simple("light_bulb");
		public static final ItemRegObject<IEBaseItem> ELECTRON_TUBE = simple("electron_tube");
		public static final ItemRegObject<IEBaseItem> CIRCUIT_BOARD = simple("circuit_board");
		public static final ItemRegObject<IEBaseItem> DUROPLAST_PLATE = simple("plate_duroplast");
		public static final ItemRegObject<IEBaseItem> EMPTY_CASING = simple("empty_casing");
		public static final ItemRegObject<IEBaseItem> EMPTY_SHELL = simple("empty_shell");

		private static void init()
		{
		}
	}

	public static final class Metals
	{
		public static final Map<EnumMetals, ItemRegObject<Item>> INGOTS = new EnumMap<>(EnumMetals.class);
		public static final Map<EnumMetals, ItemRegObject<Item>> NUGGETS = new EnumMap<>(EnumMetals.class);
		public static final Map<EnumMetals, ItemRegObject<Item>> RAW_ORES = new EnumMap<>(EnumMetals.class);
		public static final Map<EnumMetals, ItemRegObject<IEBaseItem>> DUSTS = new EnumMap<>(EnumMetals.class);
		public static final Map<EnumMetals, ItemRegObject<IEBaseItem>> PLATES = new EnumMap<>(EnumMetals.class);

		private static void init()
		{
			for(EnumMetals m : EnumMetals.values())
			{
				String name = m.tagName();
				ItemRegObject<Item> nugget;
				ItemRegObject<Item> ingot;
				ItemRegObject<Item> rawOre = null;
				if(!m.isVanillaMetal())
					ingot = register("ingot_"+name, IEBaseItem::new);
				else if(m==EnumMetals.IRON)
					ingot = of(Items.IRON_INGOT);
				else if(m==EnumMetals.GOLD)
					ingot = of(Items.GOLD_INGOT);
				else if(m==EnumMetals.COPPER)
					ingot = of(Items.COPPER_INGOT);
				else
					throw new RuntimeException("Unkown vanilla metal: "+m.name());
				if(m.shouldAddNugget())
					nugget = register("nugget_"+name, IEBaseItem::new);
				else if(m==EnumMetals.IRON)
					nugget = of(Items.IRON_NUGGET);
				else if(m==EnumMetals.GOLD)
					nugget = of(Items.GOLD_NUGGET);
				else
					throw new RuntimeException("Unkown vanilla metal: "+m.name());
				if(m.shouldAddOre())
					rawOre = register("raw_"+name, IEBaseItem::new);
				else if(m==EnumMetals.IRON)
					rawOre = of(Items.RAW_IRON);
				else if(m==EnumMetals.GOLD)
					rawOre = of(Items.RAW_GOLD);
				else if(m==EnumMetals.COPPER)
					rawOre = of(Items.RAW_COPPER);
				NUGGETS.put(m, nugget);
				INGOTS.put(m, ingot);
				if(rawOre!=null)
					RAW_ORES.put(m, rawOre);
				PLATES.put(m, simple("plate_"+name));
				DUSTS.put(m, simple("dust_"+name));
			}
		}
	}

	public static final class Tools
	{
		public static final ItemRegObject<HammerItem> HAMMER = register("hammer", HammerItem::new);
		public static final ItemRegObject<WirecutterItem> WIRECUTTER = register("wirecutter", WirecutterItem::new);
		public static final ItemRegObject<ScrewdriverItem> SCREWDRIVER = register("screwdriver", ScrewdriverItem::new);
		public static final ItemRegObject<ManualItem> MANUAL = register("manual", ManualItem::new);
		public static final ItemRegObject<VoltmeterItem> VOLTMETER = register("voltmeter", VoltmeterItem::new);

		public static final ItemRegObject<PickaxeItem> STEEL_PICK = register(
				"pickaxe_steel", IETools.createPickaxe(Lib.MATERIAL_Steel)
		);
		public static final ItemRegObject<ShovelItem> STEEL_SHOVEL = register(
				"shovel_steel", IETools.createShovel(Lib.MATERIAL_Steel)
		);
		public static final ItemRegObject<AxeItem> STEEL_AXE = register(
				"axe_steel", IETools.createAxe(Lib.MATERIAL_Steel)
		);
		public static final ItemRegObject<HoeItem> STEEL_HOE = register(
				"hoe_steel", IETools.createHoe(Lib.MATERIAL_Steel)
		);
		public static final ItemRegObject<SwordItem> STEEL_SWORD = register(
				"sword_steel", IETools.createSword(Lib.MATERIAL_Steel)
		);
		public static final Map<Type, ItemRegObject<ArmorItem>> STEEL_ARMOR = new EnumMap<>(Type.class);

		public static final ItemRegObject<ToolboxItem> TOOLBOX = register("toolbox", ToolboxItem::new);

		public static final ItemRegObject<DrillItem> DRILL = register("drill", DrillItem::new);
		public static final ItemRegObject<DrillheadItem> DRILLHEAD_STEEL = register(
				"drillhead_steel", () -> new DrillheadItem(DrillheadItem.STEEL)
		);
		public static final ItemRegObject<DrillheadItem> DRILLHEAD_IRON = register(
				"drillhead_iron", () -> new DrillheadItem(DrillheadItem.IRON)
		);

		public static final ItemRegObject<BuzzsawItem> BUZZSAW = register("buzzsaw", BuzzsawItem::new);
		public static final ItemRegObject<SawbladeItem> SAWBLADE = register(
				"sawblade", () -> new SawbladeItem(10000, 8f, 9f)
		);
		public static final ItemRegObject<RockcutterItem> ROCKCUTTER = register(
				"rockcutter", () -> new RockcutterItem(5000, 8f, 9f)
		);
		public static final ItemRegObject<GrindingDiskItem> GRINDINGDISK = register(
				"grindingdisk", () -> new GrindingDiskItem(4000, 20f, 9f)
		);

		public static final ItemRegObject<SurveyToolsItem> SURVEY_TOOLS = register("survey_tools", SurveyToolsItem::new);

		public static final ItemRegObject<GliderItem> GLIDER = register("glider", GliderItem::new);

		private static void init()
		{
			for(var slot : ArmorItem.Type.values())
				if(slot!=Type.BODY)
					STEEL_ARMOR.put(slot, register(
							"armor_steel_"+slot.getName().toLowerCase(Locale.ENGLISH),
							() -> new ArmorItem(IEArmorMaterials.STEEL, slot, IEArmorMaterials.getProperties(IEArmorMaterials.STEEL, slot))
					));
		}
	}

	public static final class Weapons
	{
		public static final ItemRegObject<RevolverItem> REVOLVER = register("revolver", RevolverItem::new);
		public static final ItemRegObject<SpeedloaderItem> SPEEDLOADER = register("speedloader", SpeedloaderItem::new);
		public static final Map<IBullet<?>, ItemRegObject<BulletItem<?>>> BULLETS = new IdentityHashMap<>();
		public static final ItemRegObject<ChemthrowerItem> CHEMTHROWER = register("chemthrower", ChemthrowerItem::new);
		public static final ItemRegObject<RailgunItem> RAILGUN = register("railgun", RailgunItem::new);

		private static void init()
		{
			for(ResourceLocation bulletType : BulletHandler.getAllKeys())
			{
				IBullet<?> bullet = BulletHandler.getBullet(bulletType);
				if(bullet.isProperCartridge())
					BULLETS.put(bullet, register(nameFor(bullet), () -> new BulletItem<>(bullet)));
			}
		}

		private static String nameFor(IBullet<?> bullet)
		{
			ResourceLocation name = BulletHandler.findRegistryName(bullet);
			if(name.getNamespace().equals(ImmersiveEngineering.MODID))
				return "bullet_"+name.getPath();
			else
				return "bullet_"+name.getNamespace()+"_"+name.getPath();
		}
	}

	public static final class Minecarts
	{
		public static final ItemRegObject<IEMinecartItem> CART_WOODEN_CRATE = register("woodencrate", CrateMinecartEntity::new, false);
		public static final ItemRegObject<IEMinecartItem> CART_REINFORCED_CRATE = register("reinforcedcrate", ReinforcedCrateMinecartEntity::new, false);
		public static final ItemRegObject<IEMinecartItem> CART_WOODEN_BARREL = register("woodenbarrel", BarrelMinecartEntity::new, true);
		public static final ItemRegObject<IEMinecartItem> CART_METAL_BARREL = register("metalbarrel", MetalBarrelMinecartEntity::new, true);

		private static void init()
		{
		}

		private static ItemRegObject<IEMinecartItem> register(
				String name, IEMinecartEntity.MinecartConstructor constructor, boolean fitsIntoContainer
		)
		{
			return IEItems.register("minecart_"+name, () -> new IEMinecartItem(constructor, fitsIntoContainer));
		}
	}

	//TODO move all of these somewhere else
	public static final class Misc
	{
		public static final Map<WireType, ItemRegObject<WireCoilItem>> WIRE_COILS = new LinkedHashMap<>();
		public static final Map<ToolUpgrade, ItemRegObject<ToolUpgradeItem>> TOOL_UPGRADES = new EnumMap<>(ToolUpgrade.class);

		public static final ItemRegObject<IESeedItem> HEMP_SEEDS = register(
				"seed", () -> new IESeedItem(IEBlocks.Misc.HEMP_PLANT.get())
		);
		public static final ItemRegObject<JerrycanItem> JERRYCAN = register("jerrycan", JerrycanItem::new);
		public static final ItemRegObject<EngineersBlueprintItem> BLUEPRINT = register("blueprint", EngineersBlueprintItem::new);
		public static final ItemRegObject<SkyhookItem> SKYHOOK = register("skyhook", SkyhookItem::new);
		public static final Map<ResourceLocation, ItemRegObject<ShaderItem>> SHADERS = new HashMap<>();
		// We can't use an EnumMap here, since Rarity is an "extensible enum" (Forge), so people may add to it later on.
		// And since this map is created during static class init, it may be initialized before another mod has any
		// chance of adding the rarity.
		// TODO probably no longer true due to data driven extension
		@SuppressWarnings("MapReplaceableByEnumMap")
		public static final Map<Rarity, ItemRegObject<ShaderBagItem>> SHADER_BAG = new HashMap<>();
		public static final ItemRegObject<EarmuffsItem> EARMUFFS = register("earmuffs", EarmuffsItem::new);
		public static final ItemRegObject<CoresampleItem> CORESAMPLE = register("coresample", CoresampleItem::new);
		public static final ItemRegObject<GraphiteElectrodeItem> GRAPHITE_ELECTRODE = register("graphite_electrode", GraphiteElectrodeItem::new);
		public static final Map<Type, ItemRegObject<FaradaySuitItem>> FARADAY_SUIT = new EnumMap<>(Type.class);
		public static final ItemRegObject<FluorescentTubeItem> FLUORESCENT_TUBE = register("fluorescent_tube", FluorescentTubeItem::new);
		public static final ItemRegObject<PowerpackItem> POWERPACK = register("powerpack", PowerpackItem::new);
		public static final ItemRegObject<IEShieldItem> SHIELD = register("shield", IEShieldItem::new);
		public static final ItemRegObject<MaintenanceKitItem> MAINTENANCE_KIT = register("maintenance_kit", MaintenanceKitItem::new);
		public static final ItemRegObject<LogicCircuitBoardItem> LOGIC_CIRCUIT_BOARD = register("logic_circuit", LogicCircuitBoardItem::new);
		public static final ItemRegObject<FertilizerItem> FERTILIZER = register("fertilizer", FertilizerItem::new);

		public static final ItemRegObject<FakeIconItem> ICON_BIRTHDAY = icon("birthday");
		public static final ItemRegObject<FakeIconItem> ICON_LUCKY = icon("lucky");
		public static final ItemRegObject<FakeIconItem> ICON_ACHTUNG = icon("achtung");
		public static final ItemRegObject<FakeIconItem> ICON_DRILLBREAK = icon("drillbreak");
		public static final ItemRegObject<FakeIconItem> ICON_RAVENHOLM = icon("ravenholm");
		public static final ItemRegObject<FakeIconItem> ICON_FRIED = icon("fried");
		public static final ItemRegObject<FakeIconItem> ICON_BTTF = icon("bttf");

		public static final ItemRegObject<PotionBucketItem> POTION_BUCKET = IEItems.register("potion_bucket", PotionBucketItem::new);

		private static ItemRegObject<FakeIconItem> icon(String name)
		{
			return register("fake_icon_"+name, FakeIconItem::new);
		}

		private static void init()
		{
			for(WireType t : WireType.getIEWireTypes())
				IEItems.Misc.WIRE_COILS.put(t, register(
						"wirecoil_"+t.getUniqueName().toLowerCase(Locale.US), () -> new WireCoilItem(t)
				));
			for(ToolUpgrade upgrade : ToolUpgrade.values())
				IEItems.Misc.TOOL_UPGRADES.put(upgrade, register(
						"toolupgrade_"+upgrade.name().toLowerCase(Locale.US), () -> new ToolUpgradeItem(upgrade)
				));
			for(Type slot : Type.values())
				if(slot!=Type.BODY)
					IEItems.Misc.FARADAY_SUIT.put(slot, register(
							"armor_faraday_"+slot.name().toLowerCase(Locale.ENGLISH), () -> new FaradaySuitItem(slot)
					));
			for(var shader : ShaderRegistry.shaderRegistry.keySet())
			{
				String path = shader.getNamespace().equals(Lib.MODID)?
						shader.getPath():
						(shader.getNamespace()+'_'+shader.getPath());
				SHADERS.put(shader, register("shader_"+path, () -> new ShaderItem(shader)));
			}
		}

		public static void registerShaderBags()
		{
			for(Rarity r : ShaderRegistry.rarityWeightMap.keySet())
				IEItems.Misc.SHADER_BAG.put(r, register(
						"shader_bag_"+r.name().toLowerCase(Locale.US).replace(':', '_'), () -> new ShaderBagItem(r)
				));
		}
	}

	public static final class SpawnEggs
	{
		public static final ItemRegObject<SpawnEggItem> EGG_FUSILIER = registerEgg(IEEntityTypes.FUSILIER, 0x959b9b, 0xaf6766);
		public static final ItemRegObject<SpawnEggItem> EGG_COMMANDO = registerEgg(IEEntityTypes.COMMANDO, 0x293a1e, 0x959b9b);
		public static final ItemRegObject<SpawnEggItem> EGG_BULWARK = registerEgg(IEEntityTypes.BULWARK, 0x959b9b, 0xc75538);
		public static final ItemRegObject<RobotWolfItem> ROBOT_WOLF = register("robot_wolf", RobotWolfItem::new);

		private static void init()
		{
		}

		private static ItemRegObject<SpawnEggItem> registerEgg(
				DeferredHolder<EntityType<?>, ? extends EntityType<? extends Mob>> type, int col1, int col2
		)
		{
			ResourceLocation id = type.unwrapKey().get().location();
			return register(id.getPath()+"_spawn_egg", () -> new DeferredSpawnEggItem(type::value, col1, col2, new Item.Properties()));
		}
	}

	public static void init(IEventBus modBus)
	{
		REGISTER.register(modBus);
		// Load all classes to make sure the static variables are initialized
		Molds.init();
		Ingredients.init();
		Metals.init();
		Tools.init();
		Weapons.init();
		Minecarts.init();
		Misc.init();
		SpawnEggs.init();
	}

	private static <T> Consumer<T> nothing()
	{
		return $ -> {
		};
	}

	private static ItemRegObject<IEBaseItem> simpleWithStackSize(String name, int maxSize)
	{
		return simple(name, p -> p.stacksTo(maxSize), i -> {
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

	static <T extends Item> ItemRegObject<T> register(String name, Supplier<? extends T> make)
	{
		return new ItemRegObject<>(REGISTER.register(name, make));
	}

	private static <T extends Item> ItemRegObject<T> of(T existing)
	{
		return new ItemRegObject<>(DeferredHolder.create(
				Registries.ITEM, BuiltInRegistries.ITEM.getKey(existing)
		));
	}

	// TODO replace by NFs DeferredItem?
	public record ItemRegObject<T extends Item>(DeferredHolder<Item, T> regObject) implements Supplier<T>, ItemLike
	{
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
