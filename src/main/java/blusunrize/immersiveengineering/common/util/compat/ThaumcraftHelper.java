/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;
import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInterModComms;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ThaumcraftHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void init()
	{
		//		ChemthrowerHandler.registerEffect("fluiddeath", new ChemthrowerEffect_Damage(DamageSourceThaumcraft.dissolve,4));
		//		for(Potion potion : Potion.potionTypes)
		//			if(potion!=null && potion.getName().equals("potion.warpward"))
		//				ChemthrowerHandler.registerEffect("fluidpure", new ChemthrowerEffect_Potion(null,0, potion,100,0));
		//			else if(potion!=null && potion.getName().equals("potion.visexhaust"))
		//				ChemthrowerHandler.registerEffect("fluxgoo", new ChemthrowerEffect_Potion(null,0, potion,100,0));

		FMLInterModComms.sendMessage("thaumcraft", "harvestStackedCrop", new ItemStack(IEContent.blockCrop, 5));

		try
		{
			Class c_TileSmelter = Class.forName("thaumcraft.common.tiles.essentia.TileSmelter");
			if(c_TileSmelter!=null)
				ExternalHeaterHandler.registerHeatableAdapter(c_TileSmelter, new AlchemyFurnaceAdapter(c_TileSmelter));
		} catch(Exception e)
		{
		}
	}

	@Override
	public void postInit()
	{
	}

	public static class AlchemyFurnaceAdapter extends ExternalHeaterHandler.HeatableAdapter
	{
		Class c_TileSmelter;
		Method m_canSmelt;
		Field f_furnaceBurnTime;
		Method m_isEnabled;
		Method m_setFurnaceState;

		public AlchemyFurnaceAdapter(Class _class)
		{
			try
			{
				c_TileSmelter = _class;
				m_canSmelt = c_TileSmelter.getDeclaredMethod("canSmelt");
				m_canSmelt.setAccessible(true);
				f_furnaceBurnTime = c_TileSmelter.getDeclaredField("furnaceBurnTime");
				Class c_BlockStateUtils = Class.forName("thaumcraft.common.lib.utils.BlockStateUtils");
				m_isEnabled = c_BlockStateUtils.getMethod("isEnabled", IBlockState.class);
				Class c_BlockSmelter = Class.forName("thaumcraft.common.blocks.essentia.BlockSmelter");
				m_setFurnaceState = c_BlockSmelter.getMethod("setFurnaceState", World.class, BlockPos.class, boolean.class);
			} catch(Exception e)
			{
			}
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
			try
			{
				int time = f_furnaceBurnTime.getInt(tileEntity);
				boolean canSmelt = redstone||canSmelt(tileEntity);
				if(canSmelt)
				{
					if(time < 200)
					{
						int heatAttempt = Math.min(4, 200-time);
						int heatEnergyRatio = Math.max(1, ExternalHeaterHandler.defaultFurnaceEnergyCost);
						int energyToUse = Math.min(energyAvailable, heatAttempt*heatEnergyRatio);
						int heat = energyToUse/heatEnergyRatio;
						if(heat > 0)
						{
							time += heat;
							energyConsumed += heat*heatEnergyRatio;
							boolean enabled = (Boolean)m_isEnabled.invoke(null, tileEntity.getWorld().getBlockState(tileEntity.getPos()));
							if(!enabled)
								m_setFurnaceState.invoke(null, tileEntity.getWorld(), tileEntity.getPos(), true);
						}
					}
					f_furnaceBurnTime.setInt(tileEntity, time);
				}
			} catch(Exception e)
			{
				e.printStackTrace();
			}
			return energyConsumed;
		}
	}
}