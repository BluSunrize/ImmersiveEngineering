/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;


public class Lib
{
	public static final String MODID = "immersiveengineering";

	public static final String[] METALS_IE = {"Copper","Aluminum","Lead","Silver","Nickel","Uranium","Constantan","Electrum","Steel"};
	public static final String[] METALS_ALL = {"Copper","Aluminum","Lead","Silver","Nickel","Uranium","Constantan","Electrum","Steel","Iron","Gold"};

	public static final String TOOL_HAMMER = "IE_HAMMER";
	public static final String TOOL_WIRECUTTER = "IE_WIRECUTTER";

	public static final String CHAT = "chat." + MODID + ".";
	public static final String CHAT_WARN = CHAT+"warning.";
	public static final String CHAT_INFO = CHAT+"info.";
	public static final String CHAT_COMMAND = CHAT+"command.";

	public static final String DESC = "desc." + MODID + ".";
	public static final String DESC_INFO = DESC+"info.";
	public static final String DESC_FLAVOUR = DESC+"flavour.";

	public static final String GUI = "gui." + MODID + ".";
	public static final String GUI_CONFIG = "gui." + MODID + ".config.";

	public static final int COLOUR_I_ImmersiveOrange = 0xfff78034;
	public static final float[] COLOUR_F_ImmersiveOrange = {247 / 255f, 128 / 255f, 52 / 255f};
	public static final int COLOUR_I_ImmersiveOrangeShadow = 0xff3e200d;

	public static final String MAGNET_PREVENT_NBT = "PreventRemoteMovement";

	/**Gui IDs*/
	//Tiles
	public static final int GUIID_Base_Tile = 0;
	public static final int GUIID_CokeOven = GUIID_Base_Tile +0;
	public static final int GUIID_AlloySmelter = GUIID_Base_Tile +1;
	public static final int GUIID_BlastFurnace = GUIID_Base_Tile +2;
	public static final int GUIID_WoodenCrate = GUIID_Base_Tile +3;
	public static final int GUIID_Workbench = GUIID_Base_Tile +4;
	public static final int GUIID_Assembler = GUIID_Base_Tile +5;
	public static final int GUIID_Sorter = GUIID_Base_Tile +6;
	public static final int GUIID_Squeezer = GUIID_Base_Tile +7;
	public static final int GUIID_Fermenter = GUIID_Base_Tile +8;
	public static final int GUIID_Refinery = GUIID_Base_Tile +9;
	public static final int GUIID_ArcFurnace = GUIID_Base_Tile +10;
	public static final int GUIID_AutoWorkbench = GUIID_Base_Tile +11;
	public static final int GUIID_Mixer = GUIID_Base_Tile +12;
	public static final int GUIID_Turret = GUIID_Base_Tile +13;
	public static final int GUIID_FluidSorter = GUIID_Base_Tile +14;
	public static final int GUIID_Belljar = GUIID_Base_Tile +15;
	public static final int GUIID_ToolboxBlock = GUIID_Base_Tile +16;
	//Items
	public static final int GUIID_Base_Item = 64;
	public static final int GUIID_Manual = GUIID_Base_Item +0;
	public static final int GUIID_Revolver = GUIID_Base_Item +1;
	public static final int GUIID_Toolbox = GUIID_Base_Item +2;

	public static final String NBT_Earmuffs = "IE:Earmuffs";
	public static final String NBT_EarmuffColour = "IE:EarmuffColour";
	public static final String NBT_Powerpack = "IE:Powerpack";

	public static final int colour_nixieTubeText = 0xff9900;
	
	public static String DMG_RevolverCasull="ieRevolver_casull";
	public static String DMG_RevolverAP="ieRevolver_armorPiercing";
	public static String DMG_RevolverBuck="ieRevolver_buckshot";
	public static String DMG_RevolverDragon="ieRevolver_dragonsbreath";
	public static String DMG_RevolverHoming="ieRevolver_homing";
	public static String DMG_RevolverWolfpack="ieRevolver_wolfpack";
	public static String DMG_RevolverSilver = "ieRevolver_silver";
	public static String DMG_RevolverPotion = "ieRevolver_potion";
	public static String DMG_Crusher="ieCrushed";
	public static String DMG_Tesla="ieTesla";
	public static String DMG_Acid="ieAcid";
	public static String DMG_Railgun = "ieRailgun";
	public static String DMG_Tesla_prim = "ieTeslaPrimary";
	public static String DMG_RazorWire = "ieRazorWire";
	public static String DMG_RazorShock = "ieRazorShock";
	public static String DMG_WireShock = "ieWireShock";

	public static boolean BAUBLES = false;
	public static boolean IC2 = false;
	public static boolean GREG = false;
}