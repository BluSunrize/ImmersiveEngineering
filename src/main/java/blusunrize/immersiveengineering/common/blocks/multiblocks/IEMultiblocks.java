/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

public class IEMultiblocks
{
	//TODO replace with correct instances
	public static IETemplateMultiblock CRUSHER;
	public static IETemplateMultiblock ALLOY_SMELTER;
	public static IETemplateMultiblock ARC_FURNACE;
	public static IETemplateMultiblock ASSEMBLER;
	public static IETemplateMultiblock AUTO_WORKBENCH;
	public static IETemplateMultiblock BLAST_FURNACE;
	public static IETemplateMultiblock ADVANCED_BLAST_FURNACE;
	public static IETemplateMultiblock BOTTLING_MACHINE;
	public static IETemplateMultiblock BUCKET_WHEEL;
	public static IETemplateMultiblock COKE_OVEN;
	public static IETemplateMultiblock DIESEL_GENERATOR;
	public static IETemplateMultiblock EXCAVATOR;
	public static IETemplateMultiblock EXCAVATOR_DEMO;
	public static IETemplateMultiblock FEEDTHROUGH;
	public static IETemplateMultiblock FERMENTER;
	public static IETemplateMultiblock LIGHTNING_ROD;
	public static IETemplateMultiblock METAL_PRESS;
	public static IETemplateMultiblock MIXER;
	public static IETemplateMultiblock REFINERY;
	public static IETemplateMultiblock SHEETMETAL_TANK;
	public static IETemplateMultiblock SILO;
	public static IETemplateMultiblock SQUEEZER;

	public static void init()
	{
		CRUSHER = new CrusherMultiblock();
		ALLOY_SMELTER = new AlloySmelterMultiblock();
		ARC_FURNACE = new ArcFurnaceMultiblock();
		ASSEMBLER = new AssemblerMultiblock();
		AUTO_WORKBENCH = new AutoWorkbenchMultiblock();
		BLAST_FURNACE = new BlastFurnaceMultiblock();
		ADVANCED_BLAST_FURNACE = new ImprovedBlastfurnaceMultiblock();
		BOTTLING_MACHINE = CRUSHER;
		BUCKET_WHEEL = new BucketWheelMultiblock();
		COKE_OVEN = new CokeOvenMultiblock();
		DIESEL_GENERATOR = CRUSHER;
		EXCAVATOR = new ExcavatorMultiblock();
		EXCAVATOR_DEMO = CRUSHER;
		FEEDTHROUGH = CRUSHER;
		FERMENTER = CRUSHER;
		LIGHTNING_ROD = CRUSHER;
		METAL_PRESS = new MetalPressMultiblock();
		MIXER = CRUSHER;
		REFINERY = CRUSHER;
		SHEETMETAL_TANK = CRUSHER;
		SILO = CRUSHER;
		SQUEEZER = CRUSHER;
	}
}
