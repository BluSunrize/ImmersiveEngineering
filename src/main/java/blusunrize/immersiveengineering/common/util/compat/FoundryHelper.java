package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_RandomTeleport;
import blusunrize.immersiveengineering.common.util.IEPotions;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public class FoundryHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void init()
	{
		ChemthrowerHandler.registerEffect("liquidenderpearl", new ChemthrowerEffect_RandomTeleport(null,0, .25f));
		ChemthrowerHandler.registerEffect("liquidredstone", new ChemthrowerEffect_Potion(null,0, IEPotions.conductive,100,1));
		ChemthrowerHandler.registerEffect("liquidglowstone", new ChemthrowerEffect_Potion(null,0, new PotionEffect(MobEffects.GLOWING,200,0),new PotionEffect(MobEffects.SPEED,200,0),new PotionEffect(MobEffects.JUMP_BOOST,200,0)));
	}

	@Override
	public void postInit()
	{
	}
}