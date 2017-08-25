package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.common.CommonProxy;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IGuiItem;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ItemSpeedloader extends ItemInternalStorage implements ITool, IGuiItem
{
	public ItemSpeedloader()
	{
		super("speedloader", 1);
	}

	@Override
	public int getSlotCount(ItemStack stack)
	{
		return 8;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		if(!world.isRemote)
			CommonProxy.openGuiForItem(player, hand==EnumHand.MAIN_HAND? EntityEquipmentSlot.MAINHAND:EntityEquipmentSlot.OFFHAND);
		return new ActionResult(EnumActionResult.SUCCESS, stack);
	}

	public boolean isEmpty(ItemStack stack)
	{
		IItemHandler inv = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		if (inv!=null)
			for (int i = 0; i < inv.getSlots(); i++)
			{
				ItemStack b = inv.getStackInSlot(i);
				if(!b.isEmpty() && b.getItem() instanceof ItemBullet && ItemNBTHelper.hasKey(b, "bullet"))
					return false;
			}
		return true;
	}

	@Override
	public int getGuiID(ItemStack stack)
	{
		return Lib.GUIID_Speedloader;
	}

	@Override
	public boolean isTool(ItemStack item)
	{
		return true;
	}
}