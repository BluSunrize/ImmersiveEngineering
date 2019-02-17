/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.tool.BelljarHandler;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Method;

public class MysticalAgricultureHelper extends IECompatModule
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
	}

	@Override
	public void postInit()
	{
		Item itemEssence = Item.REGISTRY.getObject(new ResourceLocation("mysticalagriculture", "crafting"));
		if(itemEssence!=null)
			for(int i = 1; i <= 5; i++)
			{
				Item itemSeeds = Item.REGISTRY.getObject(new ResourceLocation("mysticalagriculture", "tier"+i+"_inferium_seeds"));
				Block blockCrop = Block.REGISTRY.getObject(new ResourceLocation("mysticalagriculture", "tier"+i+"_inferium_crop"));
				if(itemSeeds!=null&&blockCrop!=null)
					BelljarHandler.cropHandler.register(new ItemStack(itemSeeds), new ItemStack[]{new ItemStack(itemEssence, i)}, new ItemStack(Blocks.DIRT), blockCrop.getDefaultState());
			}

		try
		{
			Class c_Types = Class.forName("com.blakebr0.mysticalagriculture.lib.CropType$Type");
			Method m_isEnabled = c_Types.getMethod("isEnabled");
			if(c_Types!=null&&m_isEnabled!=null)
				for(Object type : c_Types.getEnumConstants())
					if((Boolean)m_isEnabled.invoke(type))
						addType(((IStringSerializable)type).getName());
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	static void addType(String type)
	{
		Item itemSeeds = Item.REGISTRY.getObject(new ResourceLocation("mysticalagriculture:"+type+"_seeds"));
		Item itemEssence = Item.REGISTRY.getObject(new ResourceLocation("mysticalagriculture:"+type+"_essence"));
		Block blockCrop = Block.REGISTRY.getObject(new ResourceLocation("mysticalagriculture:"+type+"_crop"));
		if(itemSeeds!=null&&itemEssence!=null&&blockCrop!=null)
			BelljarHandler.cropHandler.register(new ItemStack(itemSeeds), new ItemStack[]{new ItemStack(itemEssence)}, new ItemStack(Blocks.DIRT), blockCrop.getDefaultState());
	}
}