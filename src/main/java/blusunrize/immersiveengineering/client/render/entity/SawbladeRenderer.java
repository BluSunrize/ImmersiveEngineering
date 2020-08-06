/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.render.tile.DynamicModel;
import blusunrize.immersiveengineering.client.render.tile.DynamicModel.ModelType;
import blusunrize.immersiveengineering.client.utils.SinglePropertyModelData;
import blusunrize.immersiveengineering.common.entities.SawbladeEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.settings.AmbientOcclusionStatus;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public class SawbladeRenderer extends EntityRenderer<SawbladeEntity>
{
	private final DynamicModel<Void> model = DynamicModel.createSimple(
			new ResourceLocation(ImmersiveEngineering.MODID, "item/buzzsaw_diesel.obj.ie"),
			"sawblade_entity", ModelType.IE_OBJ);

	public static final ResourceLocation SAWBLADE = new ResourceLocation(ImmersiveEngineering.MODID, "item/sawblade_blade");

	public SawbladeRenderer(EntityRendererManager renderManager)
	{
		super(renderManager);
	}

	@Override
	public void render(SawbladeEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
	{
		IVertexBuilder builder = bufferIn.getBuffer(RenderType.getEntityTranslucent(SAWBLADE));

		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockPos blockPos = entity.getPosition();
		BlockState state = entity.getEntityWorld().getBlockState(blockPos);
		IBakedModel model = this.model.get(null);
		IEObjState objState = new IEObjState(VisibilityList.show("blade"));

		ClientUtils.bindAtlas();

		matrixStackIn.push();
		matrixStackIn.scale(.75f, .75f, .75f);

		double yaw = entity.prevRotationYaw+(entity.rotationYaw-entity.prevRotationYaw)*partialTicks-90.0F;
		double pitch = entity.prevRotationPitch+(entity.rotationPitch-entity.prevRotationPitch)*partialTicks;
		matrixStackIn.rotate(new Quaternion(new Vector3f(0.0F, 1.0F, 0.0F), (float)yaw, true));
		matrixStackIn.rotate(new Quaternion(new Vector3f(0.0F, 0.0F, 1.0F), (float)pitch, true));

		if(!entity.inGround)
		{
			float spin = ((entity.ticksExisted+partialTicks)%10)/10f*360;
			matrixStackIn.rotate(new Quaternion(new Vector3f(0, 1, 0), spin, true));
		}
		RenderHelper.disableStandardItemLighting();

		AmbientOcclusionStatus aoStat = ClientUtils.mc().gameSettings.ambientOcclusionStatus;
		ClientUtils.mc().gameSettings.ambientOcclusionStatus = AmbientOcclusionStatus.OFF;

		matrixStackIn.translate(-0.5, -0.5, -0.5);
		blockRenderer.getBlockModelRenderer().renderModel(entity.getEntityWorld(), model, state, blockPos,
				matrixStackIn, builder, true,
				entity.getEntityWorld().rand, 0, 0, new SinglePropertyModelData<>(objState, Model.IE_OBJ_STATE));

		ClientUtils.mc().gameSettings.ambientOcclusionStatus = aoStat;

		RenderHelper.enableStandardItemLighting();

		matrixStackIn.pop();
	}

	@Override
	public ResourceLocation getEntityTexture(@Nonnull SawbladeEntity p_110775_1_)
	{
		return SAWBLADE;
	}

}
