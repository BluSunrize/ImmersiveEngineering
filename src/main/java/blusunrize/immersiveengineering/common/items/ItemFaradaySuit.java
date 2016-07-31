package blusunrize.immersiveengineering.common.items;

import java.util.Map;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.tool.ITeslaEquipment;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.IEDamageSources.TeslaDamageSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ItemFaradaySuit extends ItemArmor implements ITeslaEquipment
{
	public static ArmorMaterial mat;
	public ItemFaradaySuit(EntityEquipmentSlot type)
	{
		super(mat, 0, type);
		String name = "faradaySuit_"+type.getName().toLowerCase();
		this.setUnlocalizedName(ImmersiveEngineering.MODID+"."+name);
		this.setCreativeTab(ImmersiveEngineering.creativeTab);
		this.setMaxStackSize(1);
		ImmersiveEngineering.register(this, name);
		IEContent.registeredIEItems.add(this);
	}
	
	@Override
	public void onStrike(ItemStack s, EntityEquipmentSlot eqSlot, EntityLivingBase p, Map<String, Object> cache, TeslaDamageSource dmg)
	{
		if(dmg.isLowPower)
		{
			if (cache.containsKey("faraday"))
				cache.put("faraday", (1<<armorType.ordinal())|((Integer)cache.get("faraday")));
			else
				cache.put("faraday", 1<<armorType.ordinal());
			if(cache.containsKey("faraday")&&(Integer)cache.get("faraday")==(1<<4)-1)
				dmg.dmg = 0;
		}
		else
		{
			dmg.dmg*=1.2;
			if((!(p instanceof EntityPlayer)||!((EntityPlayer)p).capabilities.isCreativeMode)&&s.attemptDamageItem(2, itemRand))
				p.setItemStackToSlot(eqSlot, null);
		}
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type)
	{
		return "immersiveengineering:textures/models/armor_faraday"+(slot==EntityEquipmentSlot.LEGS?"_legs":"")+".png";
	}
}