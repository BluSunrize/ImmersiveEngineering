package blusunrize.immersiveengineering.common.util.compat;

import java.util.Map;

import gregtech.api.interfaces.tileentity.IBasicEnergyContainer;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.IERecipes;
import blusunrize.immersiveengineering.common.util.Lib;

public class GregTechHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void init()
	{
		IERecipes.oreOutputSecondaries.put("Beryllium", new Object[]{Items.emerald,.1f});
		IERecipes.oreOutputSecondaries.put("Magnesium", new Object[]{"gemPeridot",.1f});
		IERecipes.oreOutputSecondaries.put("Silicon", new Object[]{"dustSiliconDioxide",.1f});
		IERecipes.oreOutputSecondaries.put("Phosphor", new Object[]{"dustPhosphate",.1f});
		IERecipes.oreOutputSecondaries.put("Titanium", new Object[]{new ItemStack(IEContent.itemMetal,1,11),.1f});
		IERecipes.oreOutputSecondaries.put("Chrome", new Object[]{"dustIron",.1f});
		IERecipes.oreOutputSecondaries.put("Manganese", new Object[]{"dustChrome",.1f});
		IERecipes.oreOutputSecondaries.put("Thorium", new Object[]{"dustUranium",.1f});
		IERecipes.oreOutputSecondaries.put("PigIron", new Object[]{"dustIron",.1f});
		IERecipes.oreOutputSecondaries.put("Naquadah", new Object[]{"dustNaquadahEnriched",.1f});
		IERecipes.oreOutputSecondaries.put("NaquadahEnriched", new Object[]{"dustNaquadah",.1f});


		for(MineralMix min : ExcavatorHandler.mineralList.keySet())
			if(min.name.equalsIgnoreCase("Magnetite"))
			{
				min.ores = new String[]{"oreMagnetite","oreIron","oreVanadiumMagnetite"};
				min.chances = new float[]{.75f,.20f,.05f};
			}
		ExcavatorHandler.addMineral("Wolframite", 15, .2f, new String[]{"oreTungsten","oreIron","oreManganese"}, new float[]{.55f,.3f,.15f});

	}

	@Override
	public void postInit()
	{
	}

	public static boolean gregtech_isValidEnergyOutput(TileEntity tile)
	{
		if(!Lib.GREG)
			return false;
		return tile instanceof IBasicEnergyContainer && ((IBasicEnergyContainer)tile).getEUCapacity()>0;
	}
	public static long gregtech_outputGTPower(Object energyContainer, byte side, long volt, long amp, boolean simulate)
	{
		if(!Lib.GREG)
			return 0;
		if(energyContainer instanceof IBasicEnergyContainer)
		{

			IBasicEnergyContainer container = (IBasicEnergyContainer)energyContainer;
			if(!container.inputEnergyFrom(side) || container.getStoredEU() >= container.getEUCapacity())
				return 0L;

			if(volt>container.getInputVoltage() && container instanceof IGregTechTileEntity)
			{
				((IGregTechTileEntity)container).doExplosion(volt);
				return 0L;
			}

			long voltRound = volt;
			//			int lowestDiff = Integer.MAX_VALUE;
			//			for (int i : new int[]{32,64,128,256})
			//			{
			//				int diff = (int)Math.abs(volt - i); // use API to get absolute diff
			//				if (diff < lowestDiff)
			//				{
			//					lowestDiff = diff;
			//					voltRound = i;
			//				}
			//			}
			long in = 0;
			if(((IBasicEnergyContainer)energyContainer).increaseStoredEnergyUnits(voltRound, false));
			{
				if(simulate)
					((IBasicEnergyContainer)energyContainer).decreaseStoredEnergyUnits(voltRound,true);
				in++;
			}
			return in>0?volt:0;
		}

		return 0;
	}
}
