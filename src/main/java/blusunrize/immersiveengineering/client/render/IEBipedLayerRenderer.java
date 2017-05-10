package blusunrize.immersiveengineering.client.render;

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

public class IEBipedLayerRenderer implements LayerRenderer<EntityLivingBase>
{
	public static boolean rendersAssigned = false;

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

		if(!living.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty() && ItemNBTHelper.hasKey(living.getItemStackFromSlot(EntityEquipmentSlot.HEAD), "IE:Earmuffs"))
		{
			ItemStack earmuffs = ItemNBTHelper.getItemStack(living.getItemStackFromSlot(EntityEquipmentSlot.HEAD), "IE:Earmuffs");
			if(!earmuffs.isEmpty())
			{
				GlStateManager.pushMatrix();
				ModelBiped model = IEContent.itemEarmuffs.getArmorModel(living, earmuffs, EntityEquipmentSlot.HEAD, null);
				ClientUtils.bindTexture(IEContent.itemEarmuffs.getArmorTexture(earmuffs, living, EntityEquipmentSlot.HEAD, "overlay"));
				model.render(living, limbSwing, prevLimbSwing, rotation, yaw, pitch, scale);
				int colour = ((IColouredItem)earmuffs.getItem()).getColourForIEItem(earmuffs, 0);
				GlStateManager.color((colour >> 16 & 255) / 255f, (colour >> 8 & 255) / 255f, (colour & 255) / 255f);
				ClientUtils.bindTexture(IEContent.itemEarmuffs.getArmorTexture(earmuffs, living, EntityEquipmentSlot.HEAD, null));
				model.render(living, limbSwing, prevLimbSwing, rotation, yaw, pitch, scale);
				GlStateManager.popMatrix();
			}
		}
	}

	@Override
	public boolean shouldCombineTextures()
	{
		return false;
	}
}