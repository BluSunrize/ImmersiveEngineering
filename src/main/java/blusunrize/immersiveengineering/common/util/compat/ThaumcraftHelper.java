package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Damage;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntity;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.AspectList;
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
		boolean canSmelt(NBTTagCompound nbt )
		{
			NBTTagList inventoryList = nbt.getTagList("Items", 10);
			ItemStack input = null;
			for(int i=0; i<inventoryList.tagCount(); i++)
			{
				NBTTagCompound itemTag = inventoryList.getCompoundTagAt(i);
				if(itemTag.getByte("Slot")==0)
					input = ItemStack.loadItemStackFromNBT(itemTag);
			}

			if(input==null)
				return false;
			AspectList al = ThaumcraftApiHelper.getObjectAspects(input);
			al = ThaumcraftApiHelper.getBonusObjectTags(input, al);
			if(al==null || al.size()<=0)
				return false;
			AspectList storedAspects = new AspectList();
			storedAspects.readFromNBT(nbt);
			if(al.visSize()>(50-storedAspects.size()))
				return false;
			return true;
		}

		@Override
		public int doHeatTick(TileEntity tileEntity, int energyAvailable, boolean redstone)
		{
			int energyConsumed = 0;

			NBTTagCompound nbt = new NBTTagCompound();
			tileEntity.writeToNBT(nbt);
			short time = nbt.getShort("BurnTime");
			boolean canSmelt = redstone?true:canSmelt(nbt);
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
				nbt.setShort("BurnTime", time);
				tileEntity.readFromNBT(nbt);
			}
			return energyConsumed;
		}
	}
}