/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.api.utils.client.ModelDataUtils;
import blusunrize.immersiveengineering.client.models.obj.callback.DynamicSubmodelCallbacks;
import blusunrize.immersiveengineering.client.render.tile.DynamicModel;
import blusunrize.immersiveengineering.common.entities.SawbladeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.List;

public class SawbladeRenderer extends EntityRenderer<SawbladeEntity>
{
	public static final String NAME = "sawblade_entity";
	public static DynamicModel MODEL;

	public static final ResourceLocation SAWBLADE = new ResourceLocation(ImmersiveEngineering.MODID, "item/sawblade_blade");
	private static final VisibilityList DYNAMIC_GROUPS = VisibilityList.show("blade");
	private final ItemRenderer itemRenderer;

	public SawbladeRenderer(Context renderManager)
	{
		super(renderManager);
		itemRenderer = renderManager.getItemRenderer();
	}

	@Override
	public void render(SawbladeEntity entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn)
	{
		VertexConsumer builder = bufferIn.getBuffer(RenderType.translucent());

		final BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
		BlockPos blockPos = entity.blockPosition();
		BlockState state = entity.getCommandSenderWorld().getBlockState(blockPos);
		BakedModel model = MODEL.get();

		matrixStackIn.pushPose();
		matrixStackIn.scale(.75f, .75f, .75f);

		double yaw = entity.yRotO+(entity.getYRot()-entity.yRotO)*partialTicks-90.0F;
		double pitch = entity.xRotO+(entity.getXRot()-entity.xRotO)*partialTicks;
		matrixStackIn.mulPose(
				new Quaternionf()
						.rotateY((float)Math.toRadians(yaw))
						.rotateZ((float)Math.toRadians(pitch))
		);

		if(!entity.isInGround())
		{
			float spin = ((entity.tickCount+partialTicks)%10)/10f*Mth.TWO_PI;
			matrixStackIn.mulPose(new Quaternionf().rotateY(spin));
		}

		RenderType renderType = Sheets.cutoutBlockSheet();
		List<BakedQuad> quads = model.getQuads(state, null, RandomSource.create(), ModelDataUtils.single(DynamicSubmodelCallbacks.getProperty(), DYNAMIC_GROUPS), renderType);
		this.itemRenderer.renderQuadList(matrixStackIn, bufferIn.getBuffer(renderType), quads, entity.getAmmo(), packedLightIn, OverlayTexture.NO_OVERLAY);

		matrixStackIn.popPose();
	}

	@Override
	public ResourceLocation getTextureLocation(@Nonnull SawbladeEntity entity)
	{
		return SAWBLADE;
	}

}
