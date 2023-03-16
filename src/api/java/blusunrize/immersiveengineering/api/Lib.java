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
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ToolAction;

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

	public static final String MAGNET_PREVENT_NBT = "PreventRemoteMovement";

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
	public static final String GUIID_Squeezer = "squeezer";
	public static final String GUIID_Fermenter = "fermenter";
	public static final String GUIID_Refinery = "refinery";
	public static final String GUIID_ArcFurnace = "arcfurnace";
	public static final String GUIID_AutoWorkbench = "autoworkbench";
	public static final String GUIID_Mixer = "mixer";
	public static final String GUIID_Turret_Gun = "turret_gun";
	public static final String GUIID_Turret_Chem = "turret_chem";
	public static final String GUIID_FluidSorter = "fluidsorter";
	public static final String GUIID_Cloche = "cloche";
	public static final String GUIID_ToolboxBlock = "toolboxblock";
	public static final String GUIID_RedstoneConnector = "redstoneconnector";
	public static final String GUIID_RedstoneProbe = "redstoneprobe";
	//Items
	public static final String GUIID_Revolver = "revolver";
	public static final String GUIID_Toolbox = "toolbox";
	public static final String GUIID_MaintenanceKit = "maintenancekit";
	//Entities
	public static final String GUIID_CartCrate = "cart_crate";
	public static final String GUIID_CartReinforcedCrate = "cart_reinforcedcrate";

	public static final String NBT_Earmuffs = "IE:Earmuffs";
	public static final String NBT_EarmuffColour = "IE:EarmuffColour";
	public static final String NBT_Powerpack = "IE:Powerpack";
	public static final String NBT_DAMAGE = "Damage";

	public static final int colour_nixieTubeText = 0xff9900;

	public static ResourceKey<DamageType> DMG_RevolverCasull = ieDamage("revolver_casull");
	public static ResourceKey<DamageType> DMG_RevolverAP = ieDamage("revolver_armorpiercing");
	public static ResourceKey<DamageType> DMG_RevolverBuck = ieDamage("revolver_buckshot");
	public static ResourceKey<DamageType> DMG_RevolverDragon = ieDamage("revolver_dragonsbreath");
	public static ResourceKey<DamageType> DMG_RevolverHoming = ieDamage("revolver_homing");
	public static ResourceKey<DamageType> DMG_RevolverWolfpack = ieDamage("revolver_wolfpack");
	public static ResourceKey<DamageType> DMG_RevolverSilver = ieDamage("revolver_silver");
	public static ResourceKey<DamageType> DMG_RevolverPotion = ieDamage("revolver_potion");
	public static ResourceKey<DamageType> DMG_Crusher = ieDamage("crushed");
	public static ResourceKey<DamageType> DMG_Sawmill = ieDamage("sawmill");
	public static ResourceKey<DamageType> DMG_Tesla = ieDamage("tesla");
	public static ResourceKey<DamageType> DMG_Acid = ieDamage("acid");
	public static ResourceKey<DamageType> DMG_Railgun = ieDamage("railgun");
	public static ResourceKey<DamageType> DMG_Sawblade = ieDamage("sawblade");
	public static ResourceKey<DamageType> DMG_Tesla_prim = ieDamage("tesla_primary");
	public static ResourceKey<DamageType> DMG_RazorWire = ieDamage("razor_wire");
	public static ResourceKey<DamageType> DMG_RazorShock = ieDamage("razor_shock");
	public static ResourceKey<DamageType> DMG_WireShock = ieDamage("wire_shock");

	private static ResourceKey<DamageType> ieDamage(String path)
	{
		return ResourceKey.create(Registries.DAMAGE_TYPE, IEApi.ieLoc(path));
	}

	public static final Tier MATERIAL_Steel = new Tier()
	{
		@Override
		public int getUses()
		{
			return 641;
		}

		@Override
		public float getSpeed()
		{
			return 7F;
		}

		@Override
		public float getAttackDamageBonus()
		{
			return 3F;
		}

		@Override
		public int getLevel()
		{
			return 2;
		}

		@Override
		public int getEnchantmentValue()
		{
			return 10;
		}

		@Override
		public Ingredient getRepairIngredient()
		{
			return Ingredient.of(IETags.getTagsFor(EnumMetals.STEEL).ingot);
		}
	};
	public static final Rarity RARITY_MASTERWORK = Rarity.create("IE_MASTERWORK", ChatFormatting.GOLD);

	public static final ToolAction WIRECUTTER_DIG = ToolAction.get("wirecutter_dig");
}