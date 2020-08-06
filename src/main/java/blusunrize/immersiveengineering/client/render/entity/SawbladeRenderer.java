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
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.AmbientOcclusionStatus;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

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
	public void doRender(SawbladeEntity entity, double x, double y, double z, float f0, float f1)
	{
		this.bindEntityTexture(entity);
		Tessellator tessellator = ClientUtils.tes();
		BufferBuilder worldRenderer = ClientUtils.tes().getBuffer();

		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockPos blockPos = entity.getPosition();
		BlockState state = entity.getEntityWorld().getBlockState(blockPos);
		IBakedModel model = this.model.get(null);
		IEObjState objState = new IEObjState(VisibilityList.show("blade"));

		ClientUtils.bindAtlas();

		GlStateManager.pushMatrix();
		GlStateManager.translated(x, y, z);
		GlStateManager.enableRescaleNormal();
		GlStateManager.scalef(.75f, .75f, .75f);

		double yaw = entity.prevRotationYaw+(entity.rotationYaw-entity.prevRotationYaw)*f1-90.0F;
		double pitch = entity.prevRotationPitch+(entity.rotationPitch-entity.prevRotationPitch)*f1;
		GlStateManager.rotatef((float)yaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotatef((float)pitch, 0.0F, 0.0F, 1.0F);

		if(!entity.inGround)
		{
			float spin = ((entity.ticksExisted+f1)%10)/10f*360;
			GlStateManager.rotatef(spin, 0, 1, 0);
		}
		RenderHelper.disableStandardItemLighting();

		AmbientOcclusionStatus aoStat = ClientUtils.mc().gameSettings.ambientOcclusionStatus;
		ClientUtils.mc().gameSettings.ambientOcclusionStatus = AmbientOcclusionStatus.OFF;

		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		if(Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(7425);
		else
			GlStateManager.shadeModel(7424);

		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation(-blockPos.getX(), -blockPos.getY(), -blockPos.getZ());
		worldRenderer.color(255, 255, 255, 255);
		blockRenderer.getBlockModelRenderer().renderModel(entity.getEntityWorld(), model, state, blockPos, worldRenderer, true,
				entity.getEntityWorld().rand, 0, new SinglePropertyModelData<>(objState, Model.IE_OBJ_STATE));
		worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();

		ClientUtils.mc().gameSettings.ambientOcclusionStatus = aoStat;

		RenderHelper.enableStandardItemLighting();

		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(@Nonnull SawbladeEntity p_110775_1_)
	{
		return SAWBLADE;
	}

}
