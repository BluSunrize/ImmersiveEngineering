/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat112;

import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_RandomTeleport;
import blusunrize.immersiveengineering.common.util.IEPotions;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;

public class FoundryHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void registerRecipes()
	{
	}

	@Override
	public void init()
	{
		ChemthrowerHandler.registerEffect("liquidenderpearl", new ChemthrowerEffect_RandomTeleport(null, 0, .25f));
		ChemthrowerHandler.registerEffect("liquidredstone", new ChemthrowerEffect_Potion(null, 0, IEPotions.conductive, 100, 1));
		ChemthrowerHandler.registerEffect("liquidglowstone", new ChemthrowerEffect_Potion(null, 0, new EffectInstance(Effects.GLOWING, 200, 0), new EffectInstance(Effects.SPEED, 200, 0), new EffectInstance(Effects.JUMP_BOOST, 200, 0)));
	}

	@Override
	public void postInit()
	{
	}
}