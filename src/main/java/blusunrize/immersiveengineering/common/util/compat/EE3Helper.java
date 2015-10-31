package blusunrize.immersiveengineering.common.util.compat;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;

import com.pahimar.ee3.api.exchange.EnergyValueRegistryProxy;
import com.pahimar.ee3.api.knowledge.AbilityRegistryProxy;

public class EE3Helper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}
	
	@Override
	public void init()
	{
	}

	@Override
	public void postInit()
	{
		int mode = Config.getInt("ee3mode");
		if (mode==0)
			return;
		addValue(new ItemStack(IEContent.itemMetal,1,0), 128, mode==2);//Copper
		addValue(new ItemStack(IEContent.itemMetal,1,1), 192, mode==2);//Aluminium
		addValue(new ItemStack(IEContent.itemMetal,1,2), 512, mode==2);//Lead
		addValue(new ItemStack(IEContent.itemMetal,1,3), 512, mode==2);//Silver
		addValue(new ItemStack(IEContent.itemMetal,1,4), 1024, mode==2);//Nickel
		addValue(new ItemStack(IEContent.itemMetal,1,5), 576, mode==2);//Constantan
		AbilityRegistryProxy.setAsNotLearnable(new ItemStack(IEContent.itemMetal,1,5));
		addValue(new ItemStack(IEContent.itemMetal,1,6), 1280, mode==2);//Electrum
		AbilityRegistryProxy.setAsNotLearnable(new ItemStack(IEContent.itemMetal,1,6));
		addValue(new ItemStack(IEContent.itemMetal,1,7), 320, mode==2);//Steel
		addValue(new ItemStack(IEContent.itemMetal,1,20), 384, mode==2);//Graphite
		AbilityRegistryProxy.setAsNotLearnable(new ItemStack(IEContent.itemMetal,1,20));

		addValue(IEContent.itemSeeds, 24, mode==2);//Hemp Seeds
		addValue(new ItemStack(IEContent.itemMaterial,1,3), 12, mode==2);//HempFiber
		addValue(new ItemStack(IEContent.itemMaterial,1,6), 48, mode==2);//Coke
		addValue(new ItemStack(IEContent.itemMaterial,1,13), 1, mode==2);//Slag

		addValue(new ItemStack(IEContent.blockTreatedWood,1,OreDictionary.WILDCARD_VALUE), 24, mode==2);
		addValue(new ItemStack(IEContent.blockStoneDevice,1,4), 265, mode==2);//Insulated Glass

		addValue(new ItemStack(IEContent.itemBullet,1,0), 213, mode==2);//Casing
		addValue(new ItemStack(IEContent.itemBullet,1,1), 96, mode==2);//Shell
		//All these recipes use gunpowder and casings/shells
		addValue(new ItemStack(IEContent.itemBullet,1,2), 213+192 +getBulletMetal(512), mode==2);//lead
		addValue(new ItemStack(IEContent.itemBullet,1,3), 213+192 +getBulletMetal(320)+getBulletMetal(576), mode==2);//2 steel+constantan nuggets
		addValue(new ItemStack(IEContent.itemBullet,1,4), 96 +192 +256, mode==2);//1 iron dust
		addValue(new ItemStack(IEContent.itemBullet,1,5), 213+192 +964, mode==2);//1 TNT
		addValue(new ItemStack(IEContent.itemBullet,1,6), 96 +192 +192*2, mode==2);// 2 aluminium dust
		int homingVal = 213+192 + getBulletMetal(10496);//going by 10496 for Terrasteel
		addValue(new ItemStack(IEContent.itemBullet,1,7), homingVal, mode==2);
		addValue(new ItemStack(IEContent.itemBullet,1,8), 96 +192 +homingVal*4, mode==2);
		float silverNugget = (512/9f);
		addValue(new ItemStack(IEContent.itemBullet,1,9), 213+192 +getBulletMetal(512)+(int)(silverNugget*(Config.getBoolean("hardmodeBulletRecipes")?3:1)), mode==2);
		addValue(new ItemStack(IEContent.itemBullet,1,10),213+192 +256+1, mode==2);//Quartz+Glass

		addValue(IEContent.fluidCreosote, 128, mode==2);
		addValue(IEContent.fluidEthanol, 400, mode==2);
		addValue(IEContent.fluidPlantoil, 200, mode==2);
		addValue(IEContent.fluidBiodiesel, 600, mode==2);

	}

	//for hardmode bullets
	static int getBulletMetal(int ingot)
	{
		if(Config.getBoolean("hardmodeBulletRecipes"))
			return ingot;
		else
			return (int)((ingot/9f)*2);
	}

	static void addValue(Object o, int val, boolean pre)
	{
		if (pre)
			EnergyValueRegistryProxy.addPreAssignedEnergyValue(o,val);
		else
			EnergyValueRegistryProxy.addPostAssignedEnergyValue(o,val);
	}
}