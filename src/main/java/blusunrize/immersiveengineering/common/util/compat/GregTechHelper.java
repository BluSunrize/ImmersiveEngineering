package blusunrize.immersiveengineering.common.util.compat;

import java.lang.reflect.Method;

import net.minecraft.tileentity.TileEntity;
import blusunrize.immersiveengineering.common.util.Lib;

public class GregTechHelper
{
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
