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
			{
				long i =  (Long) m_IEnergyConnected.invoke(energyConnected, (byte)side,128,volt);
				return i;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return 0;
	}
}
