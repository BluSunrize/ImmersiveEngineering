package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.tool.BelljarHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Method;

public class MysticalAgradditionsHelper extends IECompatModule
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
		try
		{
			Class c_Types = Class.forName("com.blakebr0.mysticalagradditions.lib.CropType$Type");
			Method m_isEnabled = c_Types.getMethod("isEnabled");
			Method m_getRoot = c_Types.getMethod("getRoot");
			Method m_getRootMeta = c_Types.getMethod("getRootMeta");
			if(c_Types!=null && m_isEnabled!=null && m_getRoot!=null && m_getRootMeta!=null)
				for(Object type : c_Types.getEnumConstants())
					if((Boolean)m_isEnabled.invoke(type)) {
						IBlockState state = (IBlockState)m_getRoot.invoke(type);
						int meta = (int)m_getRootMeta.invoke(type);

						addType(((IStringSerializable)type).getName(), state, meta);
					}

			addInferiumTier6();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	static void addType(String type, IBlockState state, int meta)
	{
		Item itemSeeds = Item.REGISTRY.getObject(new ResourceLocation("mysticalagradditions:"+type+"_seeds"));
		Item itemEssence = Item.REGISTRY.getObject(new ResourceLocation("mysticalagradditions:"+type+"_essence"));
		Block blockCrop = Block.REGISTRY.getObject(new ResourceLocation("mysticalagradditions:"+type+"_crop"));
		if(itemSeeds!=null && itemEssence!=null && blockCrop!=null)
			BelljarHandler.cropHandler.register(new ItemStack(itemSeeds), new ItemStack[]{new ItemStack(itemEssence),new ItemStack(itemSeeds)}, new ItemStack(state.getBlock(), 1, meta), blockCrop.getDefaultState());
	}

	static void addInferiumTier6()
	{
		Item itemSeeds = Item.REGISTRY.getObject(new ResourceLocation("mysticalagradditions:insanium"));
		Item itemEssence = Item.REGISTRY.getObject(new ResourceLocation("mysticalagriculture:inferium_essence"));
		Block blockCrop = Block.REGISTRY.getObject(new ResourceLocation("mysticalagradditions:tier6_inferium_crop"));
		if(itemSeeds!=null && itemEssence!=null && blockCrop!=null)
			BelljarHandler.cropHandler.register(new ItemStack(itemSeeds, 1, 1), new ItemStack[]{new ItemStack(itemEssence, 6),new ItemStack(itemSeeds, 1, 1)}, new ItemStack(Blocks.DIRT), blockCrop.getDefaultState());
	}
}
