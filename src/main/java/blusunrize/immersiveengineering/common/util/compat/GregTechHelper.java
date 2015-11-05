package blusunrize.immersiveengineering.common.util.compat;

import gregtech.api.interfaces.tileentity.IBasicEnergyContainer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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
	}

	@Override
	public void postInit()
	{
	}


	//	static Class c_IEnergyConnected;
	//	static Class c_IBasicEnergyContainer;
	//	static Method m_IEnergyConnected;
	public static boolean gregtech_isValidEnergyOutput(TileEntity tile)
	{
		if(!Lib.GREG)
			return false;
		//		try{
		//			if(c_IBasicEnergyContainer==null)
		//				c_IBasicEnergyContainer = Class.forName("gregtech.api.interfaces.tileentity.IBasicEnergyContainer");
		//		}catch(Exception e){
		//			e.printStackTrace();
		//		}
		//		if(c_IBasicEnergyContainer!=null)
		//			return c_IBasicEnergyContainer.isAssignableFrom(tile.getClass());
		return tile instanceof IBasicEnergyContainer && ((IBasicEnergyContainer)tile).getEUCapacity()>0;
	}
	public static long gregtech_outputGTPower(Object energyContainer, byte side, long volt, long amp, boolean simulate)
	{
		if(!Lib.GREG)
			return 0;
		//		try{
		//			if(c_IBasicEnergyContainer==null)
		//				c_IBasicEnergyContainer = Class.forName("gregtech.api.interfaces.tileentity.IBasicEnergyContainer");
		//			if(m_IEnergyConnected==null)
		//				m_IEnergyConnected = (c_IBasicEnergyContainer!=null?c_IBasicEnergyContainer.getDeclaredMethod("injectEnergyUnits", byte.class,long.class,long.class): null);
		//			
		//			if(m_IEnergyConnected!=null)
		//			{
		//				long i =  (Long) m_IEnergyConnected.invoke(energyConnected, (byte)side,128,volt);
		//				return i;
		//			}
		//		}catch(Exception e){
		//			e.printStackTrace();
		//		}
		if(energyContainer instanceof IBasicEnergyContainer)
		{
			//			System.out.println("Space: "+space+"("+cap+"; "+stored+")");
			long in = ((IBasicEnergyContainer)energyContainer).injectEnergyUnits(side, volt, 1);
//			if(simulate && in==1)
//				((IBasicEnergyContainer)energyContainer).drainEnergyUnits((byte)6, insert, 1);

//			System.out.println((simulate?"SIMULATED":"REAL")+": Inserting "+insert+", "+in);
//			try
//			{
//				throw new RuntimeException("THROW!");
//			}
//			catch(Exception e)
//			{
//				e.printStackTrace();
//			}
			return in==1?volt:0;
		}

		return 0;
	}
}
