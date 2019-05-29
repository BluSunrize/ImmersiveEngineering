package blusunrize.immersiveengineering.common.items.tools;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.ResourceLocation;

/**
 * @author BluSunrize - 08.07.2018
 */
public class ItemIESword extends ItemSword
{
	private final ResourceLocation oreDict;

	public ItemIESword(IItemTier material, ResourceLocation oreDict)
	{
		super(material, 3, -2.4F, new Properties().group(ImmersiveEngineering.itemGroup).maxStackSize(1));
		this.oreDict = oreDict;
		IEContent.registeredIEItems.add(this);
	}

	@Override
	public boolean getIsRepairable(ItemStack itemToRepair, ItemStack stack)
	{
		if(this.oreDict!=null)
			return Utils.isInTag(stack, oreDict);
		return false;
	}
}
