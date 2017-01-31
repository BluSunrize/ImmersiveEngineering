package blusunrize.immersiveengineering.common.util.compat;

import com.pahimar.ee3.init.Abilities;
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
		addValue(new ItemStack(IEContent.itemMetal,1,0), 128);//Copper
		addValue(new ItemStack(IEContent.itemMetal,1,1), 192);//Aluminium
		addValue(new ItemStack(IEContent.itemMetal,1,2), 512);//Lead
		addValue(new ItemStack(IEContent.itemMetal,1,3), 512);//Silver
		addValue(new ItemStack(IEContent.itemMetal,1,4), 1024);//Nickel
		addValue(new ItemStack(IEContent.itemMetal,1,5), 576);//Constantan
		AbilityRegistryProxy.setAsNotLearnable(new ItemStack(IEContent.itemMetal,1,5));
		addValue(new ItemStack(IEContent.itemMetal,1,6), 1280);//Electrum
		AbilityRegistryProxy.setAsNotLearnable(new ItemStack(IEContent.itemMetal,1,6));
		addValue(new ItemStack(IEContent.itemMetal,1,7), 320);//Steel
		addValue(new ItemStack(IEContent.itemMetal,1,20), 384);//Graphite
		AbilityRegistryProxy.setAsNotLearnable(new ItemStack(IEContent.itemMetal,1,20));

		addValue(IEContent.itemSeeds, 24);//Hemp Seeds
		addValue(new ItemStack(IEContent.itemMaterial,1,3), 12);//HempFiber
		addValue(new ItemStack(IEContent.itemMaterial,1,6), 48);//Coke
		addValue(new ItemStack(IEContent.itemMaterial,1,13), 1);//Slag

		addValue(new ItemStack(IEContent.blockTreatedWood,1,OreDictionary.WILDCARD_VALUE), 24);
		addValue(new ItemStack(IEContent.blockStoneDevice,1,4), 265);//Insulated Glass

		addValue(new ItemStack(IEContent.itemBullet,1,0), 213);//Casing
		addValue(new ItemStack(IEContent.itemBullet,1,1), 96);//Shell
		//All these recipes use gunpowder and casings/shells
		addValue(new ItemStack(IEContent.itemBullet,1,2), 213+192 +getBulletMetal(512));//lead
		addValue(new ItemStack(IEContent.itemBullet,1,3), 213+192 +getBulletMetal(320)+getBulletMetal(576));//2 steel+constantan nuggets
		addValue(new ItemStack(IEContent.itemBullet,1,4), 96 +192 +256);//1 iron dust
		addValue(new ItemStack(IEContent.itemBullet,1,5), 213+192 +964);//1 TNT
		addValue(new ItemStack(IEContent.itemBullet,1,6), 96 +192 +192*2);// 2 aluminium dust
		int homingVal = 213+192 + getBulletMetal(10496);//going by 10496 for Terrasteel
		addValue(new ItemStack(IEContent.itemBullet,1,7), homingVal);
		addValue(new ItemStack(IEContent.itemBullet,1,8), 96 +192 +homingVal*4);
		float silverNugget = (512/9f);
		addValue(new ItemStack(IEContent.itemBullet,1,9), 213+192 +getBulletMetal(512)+(int)(silverNugget*(Config.getBoolean("hardmodeBulletRecipes")?3:1)));
		addValue(new ItemStack(IEContent.itemBullet,1,10),213+192 +256+1);//Quartz+Glass

		addValue(IEContent.fluidCreosote, 0.128f);
		addValue(IEContent.fluidEthanol, 0.4f);
		addValue(IEContent.fluidPlantoil, 0.2f);
		addValue(IEContent.fluidBiodiesel, 0.6f);

	}

	//for hardmode bullets
	static int getBulletMetal(int ingot)
	{
		if(Config.getBoolean("hardmodeBulletRecipes"))
			return ingot;
		else
			return (int)((ingot/9f)*2);
	}

	static void addValue(Object o, float val)
	{
		EnergyValueRegistryProxy.addPreAssignedEnergyValue(o,val);
	}
}