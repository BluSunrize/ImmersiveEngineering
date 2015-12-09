package blusunrize.immersiveengineering.common.util.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;
import blusunrize.immersiveengineering.common.util.IEPotions;
import net.minecraft.tileentity.TileEntity;

public class CuttingEdgeHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void init()
	{
		ChemthrowerHandler.registerEffect("rubbersap", new ChemthrowerEffect_Potion(null,0, IEPotions.sticky,100,1));
		ChemthrowerHandler.registerEffect("maplesap", new ChemthrowerEffect_Potion(null,0, IEPotions.sticky,100,0));

		try{
			Class c_TileEvaporator = Class.forName("ttftcuts.cuttingedge.treetap.TileEvaporator");
			if(c_TileEvaporator!=null)
				ExternalHeaterHandler.registerHeatableAdapter(c_TileEvaporator, new EvaporatorAdapter());
		}catch(Exception e){}
	}

	@Override
	public void postInit()
	{
	}


	public static class EvaporatorAdapter extends ExternalHeaterHandler.HeatableAdapter
	{
		Method m_canEvaporate;
		Field f_burnTime;
		Field f_burning;
		Field f_cookTime;
		public EvaporatorAdapter()
		{
			try{
				Class c_TileEvaporator = Class.forName("ttftcuts.cuttingedge.treetap.TileEvaporator");
				m_canEvaporate = c_TileEvaporator.getDeclaredMethod("canEvaporate");
				m_canEvaporate.setAccessible(true);
				f_burnTime = c_TileEvaporator.getDeclaredField("burnTime");
				f_burning = c_TileEvaporator.getDeclaredField("burning");
				f_cookTime = c_TileEvaporator.getDeclaredField("cookTime");
			}catch(Exception e){}
		}
		boolean canEvaporate(TileEntity tileEntity) throws Exception
		{
			if(m_canEvaporate!=null)
				return (boolean)m_canEvaporate.invoke(tileEntity);
			return false;
		}

		@Override
		public int doHeatTick(TileEntity tileEntity, int energyAvailable, boolean redstone)
		{
			if(f_burnTime==null)
				return 0;
			int energyConsumed = 0;
			try{
				int burnTime = f_burnTime.getInt(tileEntity);
				boolean canEvaporate = canEvaporate(tileEntity);
				if(canEvaporate || redstone)
				{
					boolean active = burnTime==0;
					if(burnTime<200)
					{
						int heatAttempt = 4;
						int heatEnergyRatio = Math.max(1, ExternalHeaterHandler.defaultFurnaceEnergyCost);
						int energyToUse = Math.min(energyAvailable, heatAttempt*heatEnergyRatio);
						int heat = energyToUse/heatEnergyRatio;
						if(heat>0)
						{
							burnTime += heat;
							energyConsumed += heat*heatEnergyRatio;
							if(!active)
							{
								f_burning.setBoolean(tileEntity, true);
								tileEntity.getWorldObj().markBlockForUpdate(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
							}
						}
					}
					int cookTime = f_cookTime.getInt(tileEntity);
					if(canEvaporate&&burnTime>=200&&cookTime<199)
					{
//						System.out.println("SPEEDUP!!!!");
						int energyToUse = ExternalHeaterHandler.defaultFurnaceSpeedupCost;
						if(energyAvailable-energyConsumed > energyToUse)
						{
							energyConsumed += energyToUse;
							cookTime += 1;
							f_cookTime.setInt(tileEntity, cookTime);
						}
					}
					f_burnTime.setInt(tileEntity, burnTime);
				}
			}catch(Exception e){}
			return energyConsumed;
		}
	}
}