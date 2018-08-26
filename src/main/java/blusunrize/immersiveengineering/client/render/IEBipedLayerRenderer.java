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
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IEBipedLayerRenderer implements LayerRenderer<EntityLivingBase>
{
	public static boolean rendersAssigned = false;
	public static Map<UUID, Pair<ItemStack, Integer>> POWERPACK_PLAYERS = new HashMap<>();

	@Override
	public void doRenderLayer(EntityLivingBase living, float limbSwing, float prevLimbSwing, float partialTicks, float rotation, float yaw, float pitch, float scale)
	{
//		if(Lib.BAUBLES && living instanceof EntityPlayer)
//		{
//			ItemStack belt = BaublesHelper.getBauble((EntityPlayer)living,3);
//			if(belt!=null && belt.getItem().equals(IEContent.itemManeuverGear))
//			{
//				GlStateManager.pushMatrix();
//				ModelBiped model = IEContent.itemManeuverGear.getArmorModel((EntityPlayer)living, belt, 2, null);
//				ClientUtils.bindTexture(IEContent.itemManeuverGear.getArmorTexture(belt, (EntityPlayer)living, 2, null));
//				model.render(living, limbSwing, prevLimbSwing, rotation, yaw, pitch, scale);
//				GlStateManager.popMatrix();
//			}
//		}

		if(!living.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty()&&ItemNBTHelper.hasKey(living.getItemStackFromSlot(EntityEquipmentSlot.HEAD), "IE:Earmuffs"))
		{
			ItemStack earmuffs = ItemNBTHelper.getItemStack(living.getItemStackFromSlot(EntityEquipmentSlot.HEAD), Lib.NBT_Earmuffs);
			if(!earmuffs.isEmpty())
			{
				GlStateManager.pushMatrix();
				ModelBiped model = IEContent.itemEarmuffs.getArmorModel(living, earmuffs, EntityEquipmentSlot.HEAD, null);
				ClientUtils.bindTexture(IEContent.itemEarmuffs.getArmorTexture(earmuffs, living, EntityEquipmentSlot.HEAD, "overlay"));
				model.render(living, limbSwing, prevLimbSwing, rotation, yaw, pitch, scale);
				int colour = ((IColouredItem)earmuffs.getItem()).getColourForIEItem(earmuffs, 0);
				GlStateManager.color((colour >> 16&255)/255f, (colour >> 8&255)/255f, (colour&255)/255f);
				ClientUtils.bindTexture(IEContent.itemEarmuffs.getArmorTexture(earmuffs, living, EntityEquipmentSlot.HEAD, null));
				model.render(living, limbSwing, prevLimbSwing, rotation, yaw, pitch, scale);
				GlStateManager.popMatrix();
			}
		}

		if(!living.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty()&&ItemNBTHelper.hasKey(living.getItemStackFromSlot(EntityEquipmentSlot.CHEST), "IE:Powerpack"))
		{
			ItemStack powerpack = ItemNBTHelper.getItemStack(living.getItemStackFromSlot(EntityEquipmentSlot.CHEST), Lib.NBT_Powerpack);
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

	public static void addWornPowerpack(EntityLivingBase living, ItemStack powerpack)
	{
		POWERPACK_PLAYERS.put(living.getUniqueID(), Pair.of(powerpack, 5));
	}

	private void renderPowerpack(ItemStack powerpack, EntityLivingBase living, float limbSwing, float prevLimbSwing, float partialTicks, float rotation, float yaw, float pitch, float scale)
	{
		if(!powerpack.isEmpty())
		{
			GlStateManager.pushMatrix();
			ModelBiped model = IEContent.itemPowerpack.getArmorModel(living, powerpack, EntityEquipmentSlot.CHEST, null);
			ClientUtils.bindTexture(IEContent.itemPowerpack.getArmorTexture(powerpack, living, EntityEquipmentSlot.CHEST, null));
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