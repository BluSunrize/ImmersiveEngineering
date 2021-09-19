/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;


import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

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
	public static final ResourceLocation GUIID_CokeOven = new ResourceLocation(MODID, "cokeoven");
	public static final ResourceLocation GUIID_AlloySmelter = new ResourceLocation(MODID, "alloysmelter");
	public static final ResourceLocation GUIID_BlastFurnace = new ResourceLocation(MODID, "blastfurnace");
	public static final ResourceLocation GUIID_CraftingTable = new ResourceLocation(MODID, "craftingtable");
	public static final ResourceLocation GUIID_WoodenCrate = new ResourceLocation(MODID, "woodencrate");
	public static final ResourceLocation GUIID_Workbench = new ResourceLocation(MODID, "workbench");
	public static final ResourceLocation GUIID_CircuitTable = new ResourceLocation(MODID, "circuittable");
	public static final ResourceLocation GUIID_Assembler = new ResourceLocation(MODID, "assembler");
	public static final ResourceLocation GUIID_Sorter = new ResourceLocation(MODID, "sorter");
	public static final ResourceLocation GUIID_ItemBatcher = new ResourceLocation(MODID, "item_batcher");
	public static final ResourceLocation GUIID_LogicUnit = new ResourceLocation(MODID, "logic_unit");
	public static final ResourceLocation GUIID_Squeezer = new ResourceLocation(MODID, "squeezer");
	public static final ResourceLocation GUIID_Fermenter = new ResourceLocation(MODID, "fermenter");
	public static final ResourceLocation GUIID_Refinery = new ResourceLocation(MODID, "refinery");
	public static final ResourceLocation GUIID_ArcFurnace = new ResourceLocation(MODID, "arcfurnace");
	public static final ResourceLocation GUIID_AutoWorkbench = new ResourceLocation(MODID, "autoworkbench");
	public static final ResourceLocation GUIID_Mixer = new ResourceLocation(MODID, "mixer");
	public static final ResourceLocation GUIID_Turret_Gun = new ResourceLocation(MODID, "turret_gun");
	public static final ResourceLocation GUIID_Turret_Chem = new ResourceLocation(MODID, "turret_chem");
	public static final ResourceLocation GUIID_FluidSorter = new ResourceLocation(MODID, "fluidsorter");
	public static final ResourceLocation GUIID_Cloche = new ResourceLocation(MODID, "cloche");
	public static final ResourceLocation GUIID_ToolboxBlock = new ResourceLocation(MODID, "toolboxblock");
	public static final ResourceLocation GUIID_RedstoneConnector = new ResourceLocation(MODID, "redstoneconnector");
	public static final ResourceLocation GUIID_RedstoneProbe = new ResourceLocation(MODID, "redstoneprobe");
	//Items
	public static final ResourceLocation GUIID_Manual = new ResourceLocation(MODID, "manual");
	public static final ResourceLocation GUIID_Revolver = new ResourceLocation(MODID, "revolver");
	public static final ResourceLocation GUIID_Toolbox = new ResourceLocation(MODID, "toolbox");
	public static final ResourceLocation GUIID_MaintenanceKit = new ResourceLocation(MODID, "maintenancekit");
	//Entities
	public static final ResourceLocation GUIID_CartCrate = new ResourceLocation(MODID, "cart_crate");
	public static final ResourceLocation GUIID_CartReinforcedCrate = new ResourceLocation(MODID, "cart_reinforcedcrate");

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
}