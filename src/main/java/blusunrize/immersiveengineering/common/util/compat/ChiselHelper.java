package blusunrize.immersiveengineering.common.util.compat;

import cpw.mods.fml.common.event.FMLInterModComms;

public class ChiselHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void init()
	{
		FMLInterModComms.sendMessage("chisel", "variation:add", "treated_wood|ImmersiveEngineering:treatedWood|0");
		FMLInterModComms.sendMessage("chisel", "variation:add", "treated_wood|ImmersiveEngineering:treatedWood|1");
		FMLInterModComms.sendMessage("chisel", "variation:add", "treated_wood|ImmersiveEngineering:treatedWood|2");
		FMLInterModComms.sendMessage("chisel", "variation:add", "steel_scaffold|ImmersiveEngineering:metalDecoration|1");
		FMLInterModComms.sendMessage("chisel", "variation:add", "steel_scaffold|ImmersiveEngineering:metalDecoration|11");
		FMLInterModComms.sendMessage("chisel", "variation:add", "aluminum_scaffold|ImmersiveEngineering:metalDecoration|13");
		FMLInterModComms.sendMessage("chisel", "variation:add", "aluminum_scaffold|ImmersiveEngineering:metalDecoration|14");
	}

	@Override
	public void postInit()
	{
	}
}