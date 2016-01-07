package blusunrize.immersiveengineering.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityDieselGenerator;
import blusunrize.immersiveengineering.common.util.compat.GeneralComputerHelper;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import cpw.mods.fml.common.registry.GameRegistry;

public class OCHelper extends IECompatModule
{

	@Override
	public void preInit()
	{}

	@Override
	public void init()
	{
		GameRegistry.registerTileEntity(TileEntityDieselGeneratorOC.class, ImmersiveEngineering.MODID+":DieselGeneratorOC");
		TileEntityDieselGenerator.isOCLoaded = true;
	}

	@Override
	public void postInit()
	{
		GeneralComputerHelper.addComputerManualContent();
	}

}
