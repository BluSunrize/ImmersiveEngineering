package blusunrize.immersiveengineering.common.util.compat;

import cpw.mods.fml.common.event.FMLInterModComms;

public class ChiselHelper extends IECompatModule
{
	@Override
	public void init()
	{
		FMLInterModComms.sendMessage("chisel", "variation:add", "treated_wood|ImmersiveEngineering:treatedWood|0");
		FMLInterModComms.sendMessage("chisel", "variation:add", "treated_wood|ImmersiveEngineering:treatedWood|1");
		FMLInterModComms.sendMessage("chisel", "variation:add", "treated_wood|ImmersiveEngineering:treatedWood|2");
	}

	@Override
	public void postInit()
	{
	}
}