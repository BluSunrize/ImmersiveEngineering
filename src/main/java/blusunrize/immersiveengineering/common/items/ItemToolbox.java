package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.util.Lib;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemToolbox extends ItemInternalStorage
{
	public ItemToolbox()
	{
		super("toolbox", 1);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack , World world, EntityPlayer player)
	{
		if(!world.isRemote)
			player.openGui(ImmersiveEngineering.instance, Lib.GUIID_Toolbox, world, (int)player.posX,(int)player.posY,(int)player.posZ);
		return stack;
	}

	@Override
	public int getInternalSlots(ItemStack stack)
	{
		return 23;
	}
}
