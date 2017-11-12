/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat;

import net.minecraftforge.fml.common.event.FMLInterModComms;

public class ChiselHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void init()
	{
		FMLInterModComms.sendMessage("chisel", "add_variation", "treated_wood|ImmersiveEngineering:treatedWood|0");
		FMLInterModComms.sendMessage("chisel", "add_variation", "treated_wood|ImmersiveEngineering:treatedWood|1");
		FMLInterModComms.sendMessage("chisel", "add_variation", "treated_wood|ImmersiveEngineering:treatedWood|2");
		FMLInterModComms.sendMessage("chisel", "add_variation", "steel_scaffold|ImmersiveEngineering:metalDecoration1|1");
		FMLInterModComms.sendMessage("chisel", "add_variation", "steel_scaffold|ImmersiveEngineering:metalDecoration1|2");
		FMLInterModComms.sendMessage("chisel", "add_variation", "steel_scaffold|ImmersiveEngineering:metalDecoration1|3");
		FMLInterModComms.sendMessage("chisel", "add_variation", "aluminum_scaffold|ImmersiveEngineering:metalDecoration1|5");
		FMLInterModComms.sendMessage("chisel", "add_variation", "aluminum_scaffold|ImmersiveEngineering:metalDecoration1|6");
		FMLInterModComms.sendMessage("chisel", "add_variation", "aluminum_scaffold|ImmersiveEngineering:metalDecoration1|7");
	}

	@Override
	public void postInit()
	{
	}
}