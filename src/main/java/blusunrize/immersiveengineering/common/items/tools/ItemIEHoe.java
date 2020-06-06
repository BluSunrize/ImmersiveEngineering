package blusunrize.immersiveengineering.common.items.tools;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;

/**
 * @author BluSunrize - 08.07.2018
 */
public class ItemIEHoe extends ItemHoe
{
	private final String oreDict;

	public ItemIEHoe(ToolMaterial materialIn, String name, String oreDict)
	{
		super(materialIn);
		this.oreDict = oreDict;
		this.setTranslationKey(ImmersiveEngineering.MODID+"."+name);
		this.setCreativeTab(ImmersiveEngineering.creativeTab);
		IEContent.registeredIEItems.add(this);
	}

	@Override
	public boolean getIsRepairable(ItemStack itemToRepair, ItemStack stack)
	{
		if(this.oreDict!=null)
			return Utils.compareToOreName(stack, oreDict);
		return false;
	}
}
