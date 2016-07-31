package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.CommonProxy;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IGuiItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemToolbox extends ItemInternalStorage implements IGuiItem
{
	public ItemToolbox()
	{
		super("toolbox", 1);
	}

	@Override
	public int getGuiID(ItemStack stack)
	{
		return Lib.GUIID_Toolbox;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack stack , World world, EntityPlayer player, EnumHand hand)
	{
		if(!world.isRemote)
			CommonProxy.openGuiForItem(player, hand==EnumHand.MAIN_HAND? EntityEquipmentSlot.MAINHAND:EntityEquipmentSlot.OFFHAND);
		return new ActionResult(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public int getInternalSlots(ItemStack stack)
	{
		return 23;
	}
}
