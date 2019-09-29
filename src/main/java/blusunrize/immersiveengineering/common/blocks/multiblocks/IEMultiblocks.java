/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;

public class IEMultiblocks
{
	public static final IMultiblock CRUSHER = new CrusherMultiblock();
	//TODO replace with correct instances
	public static final IMultiblock ALLOY_SMELTER = CRUSHER;
	public static final IMultiblock ARC_FURNACE = new ArcFurnaceMultiblock();
	public static final IMultiblock ASSEMBLER = new AssemblerMultiblock();
	public static final IMultiblock AUTO_WORKBENCH = new AutoWorkbenchMultiblock();
	public static final IMultiblock BLAST_FURNACE = CRUSHER;
	public static final IMultiblock ADVANCED_BLAST_FURNACE = new ImprovedBlastfurnaceMultiblock();
	public static final IMultiblock BOTTLING_MACHINE = CRUSHER;
	public static final IMultiblock BUCKET_WHEEL = new BucketWheelMultiblock();
	public static final IMultiblock COKE_OVEN = CRUSHER;
	public static final IMultiblock DIESEL_GENERATOR = CRUSHER;
	public static final IMultiblock EXCAVATOR = new ExcavatorMultiblock();
	public static final IMultiblock EXCAVATOR_DEMO = CRUSHER;
	public static final IMultiblock FEEDTHROUGH = CRUSHER;
	public static final IMultiblock FERMENTER = CRUSHER;
	public static final IMultiblock LIGHTNING_ROD = CRUSHER;
	public static final IMultiblock METAL_PRESS = new MetalPressMultiblock();
	public static final IMultiblock MIXER = CRUSHER;
	public static final IMultiblock REFINERY = CRUSHER;
	public static final IMultiblock SHEETMETAL_TANK = CRUSHER;
	public static final IMultiblock SILO = CRUSHER;
	public static final IMultiblock SQUEEZER = CRUSHER;
}
