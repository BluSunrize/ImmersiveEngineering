/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IEBipedLayerRenderer implements LayerRenderer<LivingEntity>
{
	public static boolean rendersAssigned = false;
	public static Map<UUID, Pair<ItemStack, Integer>> POWERPACK_PLAYERS = new HashMap<>();


	@Override
	public void render(LivingEntity living, float limbSwing, float prevLimbSwing, float partialTicks, float rotation, float yaw, float pitch, float scale)
	{
		if(!living.getItemStackFromSlot(EquipmentSlotType.HEAD).isEmpty()&&ItemNBTHelper.hasKey(living.getItemStackFromSlot(EquipmentSlotType.HEAD), "IE:Earmuffs"))
		{
			ItemStack earmuffs = ItemNBTHelper.getItemStack(living.getItemStackFromSlot(EquipmentSlotType.HEAD), Lib.NBT_Earmuffs);
			if(!earmuffs.isEmpty())
			{
				GlStateManager.pushMatrix();
				BipedModel model = Misc.earmuffs.getArmorModel(living, earmuffs, EquipmentSlotType.HEAD, null);
				ClientUtils.bindTexture(Misc.earmuffs.getArmorTexture(earmuffs, living, EquipmentSlotType.HEAD, "overlay"));
				model.render(living, limbSwing, prevLimbSwing, rotation, yaw, pitch, scale);
				int colour = ((IColouredItem)earmuffs.getItem()).getColourForIEItem(earmuffs, 0);
				GlStateManager.color3f((colour >> 16&255)/255f, (colour >> 8&255)/255f, (colour&255)/255f);
				ClientUtils.bindTexture(Misc.earmuffs.getArmorTexture(earmuffs, living, EquipmentSlotType.HEAD, null));
				model.render(living, limbSwing, prevLimbSwing, rotation, yaw, pitch, scale);
				GlStateManager.popMatrix();
			}
		}

		if(!living.getItemStackFromSlot(EquipmentSlotType.CHEST).isEmpty()&&ItemNBTHelper.hasKey(living.getItemStackFromSlot(EquipmentSlotType.CHEST), "IE:Powerpack"))
		{
			ItemStack powerpack = ItemNBTHelper.getItemStack(living.getItemStackFromSlot(EquipmentSlotType.CHEST), Lib.NBT_Powerpack);
			addWornPowerpack(living, powerpack);
		}

		if(POWERPACK_PLAYERS.containsKey(living.getUniqueID()))
		{
			Pair<ItemStack, Integer> entry = POWERPACK_PLAYERS.get(living.getUniqueID());
			renderPowerpack(entry.getLeft(), living, limbSwing, prevLimbSwing, partialTicks, rotation, yaw, pitch, scale);
			int time = entry.getValue()-1;
			if(time <= 0)
				POWERPACK_PLAYERS.remove(living.getUniqueID());
			else
				POWERPACK_PLAYERS.put(living.getUniqueID(), Pair.of(entry.getLeft(), time));
		}
	}

	public static void addWornPowerpack(LivingEntity living, ItemStack powerpack)
	{
		POWERPACK_PLAYERS.put(living.getUniqueID(), Pair.of(powerpack, 5));
	}

	private void renderPowerpack(ItemStack powerpack, LivingEntity living, float limbSwing, float prevLimbSwing, float partialTicks, float rotation, float yaw, float pitch, float scale)
	{
		if(!powerpack.isEmpty())
		{
			GlStateManager.pushMatrix();
			BipedModel model = Misc.powerpack.getArmorModel(living, powerpack, EquipmentSlotType.CHEST, null);
			ClientUtils.bindTexture(Misc.powerpack.getArmorTexture(powerpack, living, EquipmentSlotType.CHEST, null));
			model.render(living, limbSwing, prevLimbSwing, rotation, yaw, pitch, scale);
			GlStateManager.popMatrix();
		}
	}

	@Override
	public boolean shouldCombineTextures()
	{
		return false;
	}
}