/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.models.ModelEarmuffs;
import blusunrize.immersiveengineering.client.models.ModelGlider;
import blusunrize.immersiveengineering.client.models.ModelPowerpack;
import blusunrize.immersiveengineering.client.render.entity.IEModelLayers;
import blusunrize.immersiveengineering.common.items.EarmuffsItem;
import blusunrize.immersiveengineering.common.items.GliderItem;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.items.PowerpackItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

public class IEBipedLayerRenderer<E extends LivingEntity, M extends EntityModel<E>> extends RenderLayer<E, M>
{
	private static ModelEarmuffs earmuffModel;
	private static ModelGlider gliderModel;

	public IEBipedLayerRenderer(RenderLayerParent<E, M> entityRendererIn, EntityModelSet models)
	{
		super(entityRendererIn);
		if(earmuffModel==null)
			earmuffModel = new ModelEarmuffs(models.bakeLayer(IEModelLayers.EARMUFFS));
		if(gliderModel==null)
			gliderModel = new ModelGlider(models.bakeLayer(IEModelLayers.GLIDER));
	}

	private static final ResourceLocation EARMUFF_OVERLAY = ImmersiveEngineering.rl("textures/models/earmuffs_overlay.png");
	private static final ResourceLocation EARMUFF_TEXTURE = ImmersiveEngineering.rl("textures/models/earmuffs.png");
	private static final ResourceLocation GLIDER_TEXTURE = ImmersiveEngineering.rl("textures/models/glider.png");

	@Override
	@ParametersAreNonnullByDefault
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, E living, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		ItemStack earmuffs = EarmuffsItem.EARMUFF_GETTERS.getFrom(living);
		if(!earmuffs.isEmpty())
		{
			earmuffModel.setupAnim(living, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			RenderType type = earmuffModel.renderType(EARMUFF_OVERLAY);
			earmuffModel.renderToBuffer(matrixStackIn, bufferIn.getBuffer(type), packedLightIn, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
			int colour = ((IColouredItem)earmuffs.getItem()).getColourForIEItem(earmuffs, 0);
			type = earmuffModel.renderType(EARMUFF_TEXTURE);
			earmuffModel.renderToBuffer(matrixStackIn, bufferIn.getBuffer(type), packedLightIn, OverlayTexture.NO_OVERLAY,
					(colour>>16&255)/255f, (colour>>8&255)/255f, (colour&255)/255f, 1F);
		}

		ItemStack powerpack = PowerpackItem.POWERPACK_GETTER.getFrom(living);
		if(!powerpack.isEmpty())
			renderPowerpack(powerpack, matrixStackIn, bufferIn, packedLightIn, living, limbSwing, limbSwingAmount,partialTicks, ageInTicks, netHeadYaw, headPitch);

		ItemStack chest = living.getItemBySlot(EquipmentSlot.CHEST);
		if(chest.getItem() instanceof GliderItem)
		{
			gliderModel.setupAnim(living, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			gliderModel.body.getChild("glider").visible = living.isFallFlying();
			RenderType type = gliderModel.renderType(GLIDER_TEXTURE);
			gliderModel.renderToBuffer(
					matrixStackIn, bufferIn.getBuffer(type), packedLightIn, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1
			);
		}
	}

	private void renderPowerpack(ItemStack powerpack, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, E living, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(!powerpack.isEmpty())
			ModelPowerpack.render(
					living, powerpack,
					matrixStackIn, bufferIn, packedLightIn, OverlayTexture.NO_OVERLAY,
					limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch
			);
	}
}