package blusunrize.immersiveengineering.common.util.compat;

import java.lang.reflect.Method;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.IERecipes;
import blusunrize.immersiveengineering.common.util.Lib;

public class GregTechHelper extends IECompatModule
{
	public GregTechHelper()
	{
		super("gregtech");
	}

	@Override
	public void init()
	{
//		IERecipes.addOreDictCrusherRecipe("Lithium", null,0);
//		IERecipes.addOreDictCrusherRecipe("Beryllium", Items.emerald,.1f);
//		IERecipes.addOreDictCrusherRecipe("Magnesium", "gemPeridot",.1f);
//		IERecipes.addOreDictCrusherRecipe("Silicon", "dustSiliconDioxide",.1f);
//		IERecipes.addOreDictCrusherRecipe("Phosphor", "dustPhosphate",.1f);
//		IERecipes.addOreDictCrusherRecipe("Sulfur", "dustSulfur",.1f);
//		IERecipes.addOreDictCrusherRecipe("Scandium", "dustScandium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Titanium", new ItemStack(IEContent.itemMetal,1,11),.1f);
//		IERecipes.addOreDictCrusherRecipe("Vanadium", "dustVanadium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Chrome", "dustIron",.1f);
//		IERecipes.addOreDictCrusherRecipe("Manganese", "dustChrome",.1f);
//		IERecipes.addOreDictCrusherRecipe("Arsenic", "dustArsenic",.1f);
//		IERecipes.addOreDictCrusherRecipe("Rubidium", "dustRubidium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Strontium", "dustStrontium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Yttrium", "dustYttrium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Niobium", "dustNiobium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Molybdenum", "dustMolybdenum",.1f);
//		IERecipes.addOreDictCrusherRecipe("Palladium", "dustPalladium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Cadmium", "dustCadmium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Indium", "dustIndium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Antimony", "dustAntimony",.1f);
//		IERecipes.addOreDictCrusherRecipe("Tellurium", "dustTellurium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Caesium", "dustCaesium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Barium", "dustBarium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Lanthanum", "dustLanthanum",.1f);
//		IERecipes.addOreDictCrusherRecipe("Cerium", "dustCerium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Praseodymium", "dustPraseodymium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Neodymium", "dustNeodymium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Promethium", "dustPromethium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Samarium", "dustSamarium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Europium", "dustEuropium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Gadolinium", "dustGadolinium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Terbium", "dustTerbium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Dysprosium", "dustDysprosium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Holmium", "dustHolmium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Erbium", "dustErbium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Thulium", "dustThulium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Ytterbium", "dustYtterbium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Lutetium", "dustLutetium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Tantalum", "dustTantalum",.1f);
//		IERecipes.addOreDictCrusherRecipe("Bismuth", "dustBismuth",.1f);
//		IERecipes.addOreDictCrusherRecipe("Thorium", "dustUranium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Uranium235", "dustUranium235",.1f);
//		IERecipes.addOreDictCrusherRecipe("Plutonium241", "dustPlutonium241",.1f);
//		IERecipes.addOreDictCrusherRecipe("Americum", "dustAmericum",.1f);
//		IERecipes.addOreDictCrusherRecipe("Neutronium", "dustNeutronium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Electrum", "dustGold",.1f);
//		IERecipes.addOreDictCrusherRecipe("PigIron", "dustIron",.1f);
//		IERecipes.addOreDictCrusherRecipe("Admamantium", "dustAdmamantium",.1f);
//		IERecipes.addOreDictCrusherRecipe("InfusedGold", "dustInfusedGold",.1f);
//		IERecipes.addOreDictCrusherRecipe("Naquadah", "dustNaquadahEnriched",.1f);
//		IERecipes.addOreDictCrusherRecipe("NaquadahEnriched", "dustNaquadah",.1f);
//		IERecipes.addOreDictCrusherRecipe("Naquadria", "dustNaquadria",.1f);
//		IERecipes.addOreDictCrusherRecipe("Midasium", "dustGold",.1f);
//		IERecipes.addOreDictCrusherRecipe("AstralSilver", "dustSilver",.1f);
//		IERecipes.addOreDictCrusherRecipe("ShadowIron", "dustIron",.1f);
//		IERecipes.addOreDictCrusherRecipe("MeteoricIron", "dustIron",.1f);
//		IERecipes.addOreDictCrusherRecipe("Barium", "dustBarium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Barium", "dustBarium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Barium", "dustBarium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Barium", "dustBarium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Barium", "dustBarium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Barium", "dustBarium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Barium", "dustBarium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Barium", "dustBarium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Barium", "dustBarium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Barium", "dustBarium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Barium", "dustBarium",.1f);
//		IERecipes.addOreDictCrusherRecipe("Barium", "dustBarium",.1f);
	}

	@Override
	public void postInit()
	{
	}


	static Class c_IEnergyConnected;
	static Method m_IEnergyConnected;
	public static boolean gregtech_isEnergyConnected(TileEntity tile)
	{
		if(!Lib.GREG)
			return false;
		try{
			if(c_IEnergyConnected==null)
				c_IEnergyConnected = Class.forName("gregtech.api.interfaces.tileentity.IEnergyConnected");
		}catch(Exception e){
			e.printStackTrace();
		}
		if(c_IEnergyConnected!=null)
			return c_IEnergyConnected.isAssignableFrom(tile.getClass());
		return false;
	}
	public static long gregtech_outputGTPower(Object energyConnected, byte side, long volt, long amp)
	{
		if(!Lib.GREG)
			return 0;
		try{
			if(c_IEnergyConnected==null)
				c_IEnergyConnected = Class.forName("gregtech.api.interfaces.tileentity.IEnergyConnected");
			if(m_IEnergyConnected==null)
				m_IEnergyConnected = (c_IEnergyConnected!=null?c_IEnergyConnected.getDeclaredMethod("injectEnergyUnits", byte.class,long.class,long.class): null);

			if(m_IEnergyConnected!=null)
				return (Long) m_IEnergyConnected.invoke(energyConnected, (byte)side,volt,amp);
		}catch(Exception e){
			e.printStackTrace();
		}
		return 0;
	}
}
