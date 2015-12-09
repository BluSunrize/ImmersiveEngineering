package blusunrize.immersiveengineering.common.util.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Damage;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntity;
import thaumcraft.api.damagesource.DamageSourceThaumcraft;

public class ThaumcraftHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void init()
	{
		ChemthrowerHandler.registerEffect("fluiddeath", new ChemthrowerEffect_Damage(DamageSourceThaumcraft.dissolve,4));
		for(Potion potion : Potion.potionTypes)
			if(potion!=null && potion.getName().equals("potion.warpward"))
				ChemthrowerHandler.registerEffect("fluidpure", new ChemthrowerEffect_Potion(null,0, potion,100,0));
			else if(potion!=null && potion.getName().equals("potion.visexhaust"))
				ChemthrowerHandler.registerEffect("fluxgoo", new ChemthrowerEffect_Potion(null,0, potion,100,0));

		try{
			Class c_TileAlchemyFurnace = Class.forName("thaumcraft.common.tiles.TileAlchemyFurnace");
			if(c_TileAlchemyFurnace!=null)
				ExternalHeaterHandler.registerHeatableAdapter(c_TileAlchemyFurnace, new AlchemyFurnaceAdapter());
		}catch(Exception e){}
	}

	@Override
	public void postInit()
	{
	}

	public static class AlchemyFurnaceAdapter extends ExternalHeaterHandler.HeatableAdapter
	{
		Method m_canSmelt;
		Field f_furnaceBurnTime;
		public AlchemyFurnaceAdapter()
		{
			try{
				Class c_TileEvaporator = Class.forName("thaumcraft.common.tiles.TileAlchemyFurnace");
				m_canSmelt = c_TileEvaporator.getDeclaredMethod("canSmelt");
				m_canSmelt.setAccessible(true);
				f_furnaceBurnTime = c_TileEvaporator.getDeclaredField("furnaceBurnTime");
			}catch(Exception e){}
		}
		boolean canSmelt(TileEntity tileEntity) throws Exception
		{
			if(m_canSmelt!=null)
				return (boolean)m_canSmelt.invoke(tileEntity);
			return false;
		}

		@Override
		public int doHeatTick(TileEntity tileEntity, int energyAvailable, boolean redstone)
		{
			int energyConsumed = 0;
			try{
				int time = f_furnaceBurnTime.getInt(tileEntity);
				boolean canSmelt = redstone?true:canSmelt(tileEntity);
				if(canSmelt)
				{
					boolean burning = time==0;
					if(time<200)
					{
						int heatAttempt = 4;
						int heatEnergyRatio = Math.max(1, ExternalHeaterHandler.defaultFurnaceEnergyCost);
						int energyToUse = Math.min(energyAvailable, heatAttempt*heatEnergyRatio);
						int heat = energyToUse/heatEnergyRatio;
						if(heat>0)
						{
							time += heat;
							energyConsumed += heat*heatEnergyRatio;
							if(!burning)
								tileEntity.getWorldObj().markBlockForUpdate(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
						}
					}
					f_furnaceBurnTime.setInt(tileEntity, time);
				}
			}catch(Exception e){}
			return energyConsumed;
		}
	}
}