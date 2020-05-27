/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IEBipedLayerRenderer<E extends LivingEntity, M extends BipedModel<E>> extends LayerRenderer<E, M>
{
	public static boolean rendersAssigned = false;
	public static Map<UUID, Pair<ItemStack, Integer>> POWERPACK_PLAYERS = new HashMap<>();

	public IEBipedLayerRenderer(IEntityRenderer<E, M> entityRendererIn)
	{
		super(entityRendererIn);
	}

	@Override
	@ParametersAreNonnullByDefault
	public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, E living, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		ItemStack head = living.getItemStackFromSlot(EquipmentSlotType.HEAD);
		if(!head.isEmpty()&&(head.getItem()==Misc.earmuffs||ItemNBTHelper.hasKey(head, Lib.NBT_Earmuffs)))
		{
			ItemStack earmuffs = head.getItem()==Misc.earmuffs?head: ItemNBTHelper.getItemStack(head, Lib.NBT_Earmuffs);
			if(!earmuffs.isEmpty())
			{
				BipedModel<E> model = Misc.earmuffs.getArmorModel(living, earmuffs, EquipmentSlotType.HEAD, null);
				model.setRotationAngles(living, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
				model.render(matrixStackIn, bufferIn.getBuffer(RenderType.getEntitySolid(new ResourceLocation(Misc.earmuffs.getArmorTexture(earmuffs, living, EquipmentSlotType.HEAD, "overlay")))), packedLightIn, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
				int colour = ((IColouredItem)earmuffs.getItem()).getColourForIEItem(earmuffs, 0);
				model.render(matrixStackIn, bufferIn.getBuffer(RenderType.getEntitySolid(new ResourceLocation(Misc.earmuffs.getArmorTexture(earmuffs, living, EquipmentSlotType.HEAD, null)))), packedLightIn, OverlayTexture.NO_OVERLAY, (colour >> 16&255)/255f, (colour >> 8&255)/255f, (colour&255)/255f, 1F);
			}
		}

		ItemStack chest = living.getItemStackFromSlot(EquipmentSlotType.CHEST);
		if(!chest.isEmpty()&&(chest.getItem()==Misc.powerpack||ItemNBTHelper.hasKey(chest, Lib.NBT_Powerpack)))
		{
			ItemStack powerpack = chest.getItem()==Misc.powerpack?chest: ItemNBTHelper.getItemStack(chest, Lib.NBT_Powerpack);
			addWornPowerpack(living, powerpack);
		}

		if(POWERPACK_PLAYERS.containsKey(living.getUniqueID()))
		{
			Pair<ItemStack, Integer> entry = POWERPACK_PLAYERS.get(living.getUniqueID());
			renderPowerpack(entry.getLeft(), matrixStackIn, bufferIn, packedLightIn, living, limbSwing,limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
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

	private void renderPowerpack(ItemStack powerpack, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, E living, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(!powerpack.isEmpty())
		{
			BipedModel<E> model = Misc.powerpack.getArmorModel(living, powerpack, EquipmentSlotType.CHEST, null);
			model.setRotationAngles(living, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			model.render(matrixStackIn, bufferIn.getBuffer(RenderType.getEntitySolid(new ResourceLocation(Misc.powerpack.getArmorTexture(powerpack, living, EquipmentSlotType.CHEST, null)))), packedLightIn, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
		}
	}
}