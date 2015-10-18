package blusunrize.immersiveengineering.common.util.compat;

import java.lang.reflect.Field;
import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration;


public class CarpentersHelper extends IECompatModule
{
	@Override
	public void init()
	{
		try
		{
			Class c_FeatureRegistry = Class.forName("com.carpentersblocks.util.registry.FeatureRegistry");
			Field f_coverExceptions = c_FeatureRegistry.getField("coverExceptions");
			ArrayList<String> list = (ArrayList<String>)f_coverExceptions.get(null);
			list.add(new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_scaffolding).getDisplayName());
			list.add(new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_radiator).getDisplayName());
			list.add(new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_heavyEngineering).getDisplayName());
			list.add(new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_generator).getDisplayName());
			list.add(new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_lightEngineering).getDisplayName());
			list.add(new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_sheetMetal).getDisplayName());
			list.add(new ItemStack(IEContent.blockWoodenDecoration,1,5).getDisplayName());
		}
		catch(Exception e)
		{
			System.out.println("Wro-wro");
			e.printStackTrace();
		}
	}

	@Override
	public void postInit()
	{
	}
}