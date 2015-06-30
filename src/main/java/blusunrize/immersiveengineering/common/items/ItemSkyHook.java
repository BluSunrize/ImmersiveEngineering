package blusunrize.immersiveengineering.common.items;

import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.api.IUpgrade;
import blusunrize.immersiveengineering.api.IUpgrade.UpgradeType;
import blusunrize.immersiveengineering.api.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.entities.EntityZiplineHook;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.gui.InventoryStorageItem;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.ZiplineHelper;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class ItemSkyhook extends ItemUpgradeableTool
{
	public ItemSkyhook()
	{
		super("skyhook", 1, UpgradeType.SKYHOOK);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		list.add(StatCollector.translateToLocal(Lib.DESC_FLAVOUR+"skyhook"));
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity ent, int slot, boolean inHand)
	{

	}
	@Override
	public Multimap getAttributeModifiers(ItemStack stack)
	{
		float dmg = 5+ItemNBTHelper.getFloat(stack, "fallDamageBoost");
		Multimap multimap = super.getAttributeModifiers(stack);
		multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", dmg, 0));
		return multimap;
	}

	public static HashMap<String, EntityZiplineHook> existingHooks = new HashMap<String, EntityZiplineHook>();

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		TileEntity connector = null;
		double lastDist = 0;
		Connection line = null;
		double py = player.posY+player.getEyeHeight();
		for(int xx=-2; xx<=2; xx++)
			for(int zz=-2; zz<=2; zz++)
				for(int yy=0; yy<=3; yy++)
				{
					TileEntity tile = world.getTileEntity((int)player.posX+xx, (int)py+yy, (int)player.posZ+zz);
					if(tile!=null)
					{
						Connection con = ZiplineHelper.getTargetConnection(world, tile.xCoord,tile.yCoord,tile.zCoord, player, null);
						if(con!=null)
						{
							double d = tile.getDistanceFrom(player.posX,py,player.posZ);
							if(connector==null || d<lastDist)
							{
								connector=tile;
								lastDist=d;
								line=con;
							}
						}
					}
				}
		if(line!=null&&connector!=null)
		{
			ZiplineHelper.spawnHook(player, connector, line);
			player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
		}
		return stack;
	}

	public float getSkylineSpeed(ItemStack stack)
	{
		return .2f+this.getUpgrades(stack).getFloat("speed");
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 72000;
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int ticks)
	{
		if(existingHooks.containsKey(player.getCommandSenderName()))
		{
			existingHooks.get(player.getCommandSenderName()).setDead();
			existingHooks.remove(player.getCommandSenderName());
		}
	}

	@Override
	public boolean canModify(ItemStack stack)
	{
		return true;
	}

	@Override
	public Slot[] getWorkbenchSlots(Container container, ItemStack stack, InventoryStorageItem invItem)
	{
		return new Slot[]
				{
				new IESlot.Upgrades(container, invItem,0, 102,42, IUpgrade.UpgradeType.SKYHOOK, stack, true),
				new IESlot.Upgrades(container, invItem,1, 102,22, IUpgrade.UpgradeType.SKYHOOK, stack, true),
				};
	}

	@Override
	public int getInternalSlots(ItemStack stack)
	{
		return 2;
	}

}