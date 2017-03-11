package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.tool.BelljarHandler;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Field;
import java.util.HashMap;

public class HarvestcraftHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void init()
	{
	}

	static HashMap<String, Item> seeds = new HashMap<String, Item>();
	static HashMap<String, Item> foods = new HashMap<String, Item>();
	static HashMap<String, Block> crops = new HashMap<String, Block>();
	@Override
	public void postInit()
	{
		try
		{
			Class c_Types = Class.forName("com.pam.harvestcraft.blocks.CropRegistry");
			if(c_Types!=null)
			{
				Field f_seeds  = c_Types.getDeclaredField("seeds");
				Field f_foods  = c_Types.getDeclaredField("foods");
				Field f_crops = c_Types.getDeclaredField("crops");
				f_seeds.setAccessible(true);
				f_foods.setAccessible(true);
				f_crops.setAccessible(true);
				seeds = (HashMap<String, Item>)f_seeds.get(null);
				foods = (HashMap<String, Item>)f_foods.get(null);
				crops = (HashMap<String, Block>)f_crops.get(null);

				Field f_cropNames = c_Types.getDeclaredField("cropNames");
				for(String type : (String[])f_cropNames.get(null))
					addType(type);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	static void addType(String type)
	{
		Item itemSeeds = seeds.get(type);
		Item itemFood = foods.get(type);
		Block blockCrop = crops.get(type);
		if(itemSeeds!=null && itemFood!=null && blockCrop!=null)
			BelljarHandler.cropHandler.register(new ItemStack(itemSeeds), new ItemStack[]{new ItemStack(itemFood,3),new ItemStack(itemSeeds)}, new ItemStack(Blocks.DIRT), blockCrop.getDefaultState());
	}
}