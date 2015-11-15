package blusunrize.immersiveengineering.common.util.compat;

import gregtech.api.interfaces.tileentity.IBasicEnergyContainer;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.IERecipes;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices;
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
		IERecipes.addOredictRecipe(new ItemStack(IEContent.blockMetalDevice,8, BlockMetalDevices.META_conveyorBelt), "LLL","IRI", 'I',"ingotIron",'R',"dustRedstone",'L',"sheetRubber");
		IERecipes.addOredictRecipe(new ItemStack(IEContent.blockMetalDevice,1, BlockMetalDevices.META_thermoelectricGen), "III","CBC","CCC", 'I',"ingotSteel",'C',"ingotCupronickel",'B',new ItemStack(IEContent.blockStorage,1,8));
		if(Config.getBoolean("hardmodeBulletRecipes"))
			BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,1,3), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,"ingotSteel","ingotCupronickel");
		else
			BlueprintCraftingRecipe.addRecipe("bullet", new ItemStack(IEContent.itemBullet,1,3), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,"nuggetSteel","nuggetSteel","nuggetCupronickel","nuggetCupronickel");
		
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

		IERecipes.addOreDictAlloyingRecipe("ingotHotNichrome",5, "Chrome", 400,4096, "dustNickel","dustNickel","dustNickel","dustNickel");
		IERecipes.addOreDictAlloyingRecipe("ingotHotKanthal",3, "Iron", 500,4096, "dustAluminium","dustChrome");
		IERecipes.addOreDictAlloyingRecipe("ingotHotKanthal",3, "Aluminium", 500,4096, "dustIron","dustChrome");
		IERecipes.addOreDictAlloyingRecipe("ingotHotKanthal",3, "Chrome", 500,4096, "dustAluminium","dustIron");
		IERecipes.addOreDictAlloyingRecipe("ingotMagnalium",3, "Magnesium", 200,512, "dustAluminium","dustAluminium");
		IERecipes.addOreDictAlloyingRecipe("ingotBatteryAlloy",5, "Antimony", 200,512, "dustLead","dustLead","dustLead","dustLead");
		IERecipes.addOreDictAlloyingRecipe("ingotHotTungstenSteel",2, "Tungsten", 800,4096, "dustSteel");
		IERecipes.addOreDictAlloyingRecipe("ingotHotTungstenSteel",2, "Steel", 800,4096, "dustTungsten");
		IERecipes.addOreDictAlloyingRecipe("ingotHotTungstenCarbide",2, "Tungsten", 1000,4096, "dustCoke");
		
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
