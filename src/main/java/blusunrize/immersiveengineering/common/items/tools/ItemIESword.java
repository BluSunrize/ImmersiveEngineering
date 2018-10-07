package blusunrize.immersiveengineering.common.items.tools;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

/**
 * @author BluSunrize - 08.07.2018
 */
public class ItemIESword extends ItemSword
{
	private final String oreDict;

	public ItemIESword(ToolMaterial material, String name, String oreDict)
	{
		super(material);
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
