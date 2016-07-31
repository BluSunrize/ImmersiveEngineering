package blusunrize.immersiveengineering.api.tool;

import java.util.Map;

import blusunrize.immersiveengineering.common.util.IEDamageSources.TeslaDamageSource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public interface ITeslaEquipment
{
	/**
	 * Called whenever a Tesla coil attempts to damage the player
	 * @param s The current ItemStack
	 * @param eqSlot The equipment slot the Item is in
	 * @param p The entity wearing/holding the item
	 * @param cache A way for different ITeslaEquipment items to communicate with each other. It is empty when starting to check the equipment and is discarded after checking is done
	 * @param dmg The damage source that would be used. Set the amount to 0 to prevent any damage from being done
	 */
	void onStrike(ItemStack s, EntityEquipmentSlot eqSlot, EntityLivingBase p, Map<String, Object> cache, TeslaDamageSource dmg);
}
