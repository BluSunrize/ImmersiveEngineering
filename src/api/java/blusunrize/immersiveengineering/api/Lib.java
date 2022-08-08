/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;


import net.minecraft.ChatFormatting;
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

	public static String DMG_RevolverCasull = "ieRevolver_casull";
	public static String DMG_RevolverAP = "ieRevolver_armorPiercing";
	public static String DMG_RevolverBuck = "ieRevolver_buckshot";
	public static String DMG_RevolverDragon = "ieRevolver_dragonsbreath";
	public static String DMG_RevolverHoming = "ieRevolver_homing";
	public static String DMG_RevolverWolfpack = "ieRevolver_wolfpack";
	public static String DMG_RevolverSilver = "ieRevolver_silver";
	public static String DMG_RevolverPotion = "ieRevolver_potion";
	public static String DMG_Crusher = "ieCrushed";
	public static String DMG_Sawmill = "ieSawmill";
	public static String DMG_Tesla = "ieTesla";
	public static String DMG_Acid = "ieAcid";
	public static String DMG_Railgun = "ieRailgun";
	public static String DMG_Sawblade = "ieSawblade";
	public static String DMG_Tesla_prim = "ieTeslaPrimary";
	public static String DMG_RazorWire = "ieRazorWire";
	public static String DMG_RazorShock = "ieRazorShock";
	public static String DMG_WireShock = "ieWireShock";

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