package blusunrize.immersiveengineering.common.util.compat;

import net.minecraft.potion.Potion;
import thaumcraft.api.damagesource.DamageSourceThaumcraft;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Damage;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;

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
	}

	@Override
	public void postInit()
	{
	}
}