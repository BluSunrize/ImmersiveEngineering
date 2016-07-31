package blusunrize.immersiveengineering.common.items;

import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Multimap;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.common.entities.EntitySkylineHook;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.SkylineHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemSkyhook extends ItemUpgradeableTool implements ITool
{
	public ItemSkyhook()
	{
		super("skyhook", 1, "SKYHOOK");
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		list.add(I18n.format(Lib.DESC_FLAVOUR+"skyhook"));
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity ent, int slot, boolean inHand)
	{
		if(getUpgrades(stack).getBoolean("fallBoost"))
		{
			float dmg = (float)Math.ceil(ent.fallDistance/5);
			ItemNBTHelper.setFloat(stack, "fallDamageBoost", dmg);
		}
	}
	@Override
	public Multimap getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack)
	{
		float dmg = 5+ItemNBTHelper.getFloat(stack, "fallDamageBoost");
		Multimap multimap = super.getAttributeModifiers(slot, stack);
		multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getAttributeUnlocalizedName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", dmg, 0));
		return multimap;
	}

	public static HashMap<String, EntitySkylineHook> existingHooks = new HashMap<String, EntitySkylineHook>();

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand)
	{
		TileEntity connector = null;
		double lastDist = 0;
		Connection line = null;
		double py = player.posY+player.getEyeHeight();
		for(int xx=-2; xx<=2; xx++)
			for(int zz=-2; zz<=2; zz++)
				for(int yy=0; yy<=3; yy++)
				{
					TileEntity tile = world.getTileEntity( new BlockPos(player.posX+xx, py+yy, player.posZ+zz));
					if(tile!=null)
					{
						Connection con = SkylineHelper.getTargetConnection(world, tile.getPos(), player, null);
						if(con!=null)
						{
							double d = tile.getDistanceSq(player.posX,py,player.posZ);
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
			SkylineHelper.spawnHook(player, connector, line);
			player.setActiveHand(hand);
			return new ActionResult(EnumActionResult.SUCCESS, stack);
		}
		return new ActionResult(EnumActionResult.PASS,stack);
	}

	public float getSkylineSpeed(ItemStack stack)
	{
		return 3f+this.getUpgrades(stack).getFloat("speed");
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 72000;
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase player, int ticks)
	{
		if(existingHooks.containsKey(player.getName()))
		{
			EntitySkylineHook hook = existingHooks.get(player.getName());
			//			player.motionX = hook.motionX;
			//			player.motionY = hook.motionY;
			//			player.motionZ = hook.motionZ;
			//			IELogger.debug("player motion: "+player.motionX+","+player.motionY+","+player.motionZ);
			hook.setDead();
			existingHooks.remove(player.getName());
		}
	}

	@Override
	public boolean canModify(ItemStack stack)
	{
		return true;
	}

	@Override
	public Slot[] getWorkbenchSlots(Container container, ItemStack stack, IInventory invItem)
	{
		return new Slot[]
				{
				new IESlot.Upgrades(container, invItem,0, 102,42, "SKYHOOK", stack, true),
				new IESlot.Upgrades(container, invItem,1, 102,22, "SKYHOOK", stack, true),
				};
	}

	@Override
	public int getInternalSlots(ItemStack stack)
	{
		return 2;
	}

	@Override
	public boolean isTool(ItemStack item)
	{
		return true;
	}
}