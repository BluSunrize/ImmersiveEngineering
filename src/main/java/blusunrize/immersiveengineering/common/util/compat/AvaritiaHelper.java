package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import net.minecraft.item.EnumRarity;

public class AvaritiaHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void init()
	{
		ShaderRegistry.rarityWeightMap.put(EnumRarity.valueOf("COSMIC"),1);
	}
	@Override
	public void postInit()
	{
	}

	@Override
	public void serverStarting()
	{
		EnumRarity trash = EnumRarity.valueOf("TRASH");
		if(trash!=null)
			ShaderRegistry.rarityWeightMap.put(trash,11);
	}
}