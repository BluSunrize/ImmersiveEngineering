/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;


import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockSetType.PressurePlateSensitivity;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.SimpleTier;

import static blusunrize.immersiveengineering.api.IEApi.ieLoc;

public class Lib
{
	public static final String MODID = "immersiveengineering";

	public static final String[] METALS_IE = {"Copper", "Aluminum", "Lead", "Silver", "Nickel", "Uranium", "Constantan", "Electrum", "Steel"};
	public static final String[] METALS_ALL = {"Copper", "Aluminum", "Lead", "Silver", "Nickel", "Uranium", "Constantan", "Electrum", "Steel", "Iron", "Gold"};

	public static final String CHAT = "chat."+MODID+".";
	public static final String CHAT_WARN = CHAT+"warning.";
	public static final String CHAT_INFO = CHAT+"info.";
	public static final String CHAT_COMMAND = CHAT+"command.";

	public static final String DESC = "desc."+MODID+".";
	public static final String DESC_INFO = DESC+"info.";
	public static final String DESC_FLAVOUR = DESC+"flavour.";

	public static final String GUI = "gui."+MODID+".";
	public static final String GUI_CONFIG = "gui."+MODID+".config.";

	public static final int COLOUR_I_ImmersiveOrange = 0xfff78034;
	public static final float[] COLOUR_F_ImmersiveOrange = {247/255f, 128/255f, 52/255f};
	public static final int COLOUR_I_ImmersiveOrangeShadow = 0xff3e200d;

	public static MutableComponent getRedstoneColorComponent(DyeColor channel)
	{
		Style style = Style.EMPTY.withColor(channel.getTextureDiffuseColor());
		if(channel==DyeColor.BLACK)
			// special case for black to make it more readable
			style = style.withColor(0x2d2c38);
		return Component.empty()
				.append(Component.literal("█ ").withStyle(style))
				.append(Component.translatable("color.minecraft."+channel.getName()));
	}


	public static final String MAGNET_PREVENT_NBT = "PreventRemoteMovement";
	public static final String MAGNET_SOURCE_NBT = "immersiveengineering:magnet_source";
	public static final String MAGNET_TIME_NBT = "immersiveengineering:magnet_last_pulled";

	/**
	 * Gui IDs
	 */
	//Tiles
	public static final String GUIID_CokeOven = "cokeoven";
	public static final String GUIID_AlloySmelter = "alloysmelter";
	public static final String GUIID_BlastFurnace = "blastfurnace";
	public static final String GUIID_BlastFurnaceAdv = "blastfurnace_advanced";
	public static final String GUIID_CraftingTable = "craftingtable";
	public static final String GUIID_WoodenCrate = "woodencrate";
	public static final String GUIID_Workbench = "workbench";
	public static final String GUIID_CircuitTable = "circuittable";
	public static final String GUIID_Assembler = "assembler";
	public static final String GUIID_Sorter = "sorter";
	public static final String GUIID_ItemBatcher = "item_batcher";
	public static final String GUIID_LogicUnit = "logic_unit";
	public static final String GUIID_MachineInterface = "machineinterface";
	public static final String GUIID_Squeezer = "squeezer";
	public static final String GUIID_Fermenter = "fermenter";
	public static final String GUIID_Refinery = "refinery";
	public static final String GUIID_ArcFurnace = "arcfurnace";
	public static final String GUIID_AutoWorkbench = "autoworkbench";
	public static final String GUIID_Mixer = "mixer";
	public static final String GUIID_RadioTower = "radiotower";
	public static final String GUIID_ChunkLoader = "chunkloader";
	public static final String GUIID_Turret_Gun = "turret_gun";
	public static final String GUIID_Turret_Chem = "turret_chem";
	public static final String GUIID_FluidSorter = "fluidsorter";
	public static final String GUIID_Cloche = "cloche";
	public static final String GUIID_ToolboxBlock = "toolboxblock";
	public static final String GUIID_RedstoneConnector = "redstoneconnector";
	public static final String GUIID_RedstoneProbe = "redstoneprobe";
	public static final String GUIID_RedstoneStateCell = "redstonestatecell";
	public static final String GUIID_RedstoneTimer = "redstonetimer";
	public static final String GUIID_RedstoneSwitchboard = "redstoneswitchboard";
	public static final String GUIID_Siren = "siren";
	//Items
	public static final String GUIID_Revolver = "revolver";
	public static final String GUIID_Toolbox = "toolbox";
	public static final String GUIID_MaintenanceKit = "maintenancekit";
	//Entities
	public static final String GUIID_CartCrate = "cart_crate";

	public static class GuiLayers
	{
		public static final ResourceLocation ITEMS = IEApi.ieLoc("items");
		public static final ResourceLocation BLOCKS = IEApi.ieLoc("blocks");
	}

	public static class DamageTypes
	{
		public static final TurretDamageType REVOLVER_CASULL = new TurretDamageType("revolver_casull");
		public static final TurretDamageType REVOLVER_ARMORPIERCING = new TurretDamageType("revolver_armorpiercing");
		public static final TurretDamageType REVOLVER_BUCKSHOT = new TurretDamageType("revolver_buckshot");
		public static final TurretDamageType REVOLVER_DRAGONSBREATH = new TurretDamageType("revolver_dragonsbreath");
		public static final TurretDamageType REVOLVER_HOMING = new TurretDamageType("revolver_homing");
		public static final TurretDamageType REVOLVER_WOLFPACK = new TurretDamageType("revolver_wolfpack");
		public static final TurretDamageType REVOLVER_SILVER = new TurretDamageType("revolver_silver");
		public static final TurretDamageType REVOLVER_POTION = new TurretDamageType("revolver_potion");
		public static final ResourceKey<DamageType> CRUSHER = ieDamage("crushed");
		public static final ResourceKey<DamageType> SAWMILL = ieDamage("sawmill");
		public static final ResourceKey<DamageType> TESLA = ieDamage("tesla");
		public static final ResourceKey<DamageType> ACID = ieDamage("acid");
		public static final TurretDamageType RAILGUN = new TurretDamageType("railgun");
		public static final TurretDamageType SAWBLADE = new TurretDamageType("sawblade");
		public static final ResourceKey<DamageType> TESLA_PRIMARY = ieDamage("tesla_primary");
		public static final ResourceKey<DamageType> RAZOR_WIRE = ieDamage("razor_wire");
		public static final ResourceKey<DamageType> RAZOR_SHOCK = ieDamage("razor_shock");
		public static final ResourceKey<DamageType> WIRE_SHOCK = ieDamage("wire_shock");
	}

	public static class BlockSetTypes
	{
		public static final BlockSetType TREATED_WOOD = new BlockSetType("treated_wood");
		public static final BlockSetType STEEL = new BlockSetType(
				"steel",
				true,
				false,
				false,
				PressurePlateSensitivity.MOBS,
				SoundType.METAL,
				SoundEvents.IRON_DOOR_CLOSE,
				SoundEvents.IRON_DOOR_OPEN,
				SoundEvents.IRON_TRAPDOOR_CLOSE,
				SoundEvents.IRON_TRAPDOOR_OPEN,
				SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF,
				SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON,
				SoundEvents.STONE_BUTTON_CLICK_OFF,
				SoundEvents.STONE_BUTTON_CLICK_ON
		);
		public static final BlockSetType ALUMINUM = new BlockSetType(
				"aluminum",
				true,
				false,
				false,
				PressurePlateSensitivity.MOBS,
				SoundType.METAL,
				SoundEvents.IRON_DOOR_CLOSE,
				SoundEvents.IRON_DOOR_OPEN,
				SoundEvents.IRON_TRAPDOOR_CLOSE,
				SoundEvents.IRON_TRAPDOOR_OPEN,
				SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF,
				SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON,
				SoundEvents.STONE_BUTTON_CLICK_OFF,
				SoundEvents.STONE_BUTTON_CLICK_ON
		);
	}

	public static class WoodTypes
	{
		// Only one of these is a wood!
		public static final WoodType TREATED_WOOD = WoodType.register(new WoodType(ieLoc("treated_wood").toString(), BlockSetTypes.TREATED_WOOD));
		public static final WoodType STEEL = WoodType.register(new WoodType(ieLoc("steel").toString(), BlockSetTypes.STEEL));
		public static final WoodType ALUMINUM = WoodType.register(new WoodType(ieLoc("aluminum").toString(), BlockSetTypes.ALUMINUM));
	}

	private static ResourceKey<DamageType> ieDamage(String path)
	{
		return ResourceKey.create(Registries.DAMAGE_TYPE, ieLoc(path));
	}

	public record TurretDamageType(ResourceKey<DamageType> playerType, ResourceKey<DamageType> turretType)
	{
		private TurretDamageType(String path)
		{
			this(ieDamage(path), ieDamage(path+"_turret"));
		}
	}

	public static final Tier MATERIAL_Steel = new SimpleTier(
			IETags.incorrectDropsSteel, 641, 7, 3, 10, () -> Ingredient.of(IETags.getTagsFor(EnumMetals.STEEL).ingot)
	);
	public static final EnumProxy<Rarity> RARITY_MASTERWORK = new EnumProxy<>(
			// 0 is "index" parameter, should get replaced by Neo's enum extension handler
			Rarity.class, 0, MODID+":masterwork", ChatFormatting.GOLD
	);

	public static final ItemAbility WIRECUTTER_DIG = ItemAbility.get("wirecutter_dig");
}