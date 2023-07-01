/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import blusunrize.lib.manual.gui.ManualScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.Lazy;
import org.joml.Quaternionf;

import javax.annotation.Nullable;

public class ManualElementEntity extends SpecialManualElements
{
	private final Lazy<RenderData> renderData;

	public ManualElementEntity(ManualInstance helper, EntityType<?> entityType, @Nullable CompoundTag entityData)
	{
		super(helper);
		this.renderData = Lazy.of(() -> new RenderData(entityType, entityData));
	}

	@Override
	public void render(GuiGraphics graphics, ManualScreen gui, int x, int y, int mx, int my)
	{
		// Entity rendering code was largely borrowed from JustEnoughResources by way2muchnoise

		Entity entity = renderData.get().entity;
		float yOff = renderData.get().ySize-4;
		float scale = renderData.get().scale;

		PoseStack modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushPose();
		modelViewStack.mulPoseMatrix(graphics.pose().last().pose());
		modelViewStack.translate(x+60, y+yOff, 50);
		modelViewStack.scale(-scale, scale, scale);

		PoseStack mobPoseStack = new PoseStack();
		mobPoseStack.mulPose(new Quaternionf().rotateAxis(Mth.PI, 0, 0, 1));

		float pitch = (yOff/2)-my;
		float yaw = 60-mx;

        mobPoseStack.mulPose(new Quaternionf().rotateAxis((float)Math.atan(pitch/40f), -1, 0, 0));
		entity.yo = (float)Math.atan(yaw/40f)*20f;
		float yRot = (float)Math.atan(yaw/40f)*40f;
		float xRot = -((float)Math.atan(pitch/40f))*20f;
		entity.setYRot(yRot);
		entity.setYRot(yRot);
		entity.setXRot(xRot);
		if(entity instanceof LivingEntity)
		{
			((LivingEntity)entity).yHeadRot = yRot;
			((LivingEntity)entity).yHeadRotO = yRot;
			((LivingEntity)entity).yBodyRot = yRot;
		}

		RenderSystem.applyModelViewMatrix();
		EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
		entityRenderDispatcher.setRenderShadow(false);
		MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
		RenderSystem.runAsFancy(() -> {
			entityRenderDispatcher.render(entity, 0, 0, 0, 0, 1, mobPoseStack, bufferSource, 15728880);
		});
		bufferSource.endBatch();
		entityRenderDispatcher.setRenderShadow(true);
		modelViewStack.popPose();
		RenderSystem.applyModelViewMatrix();
	}

	@Override
	public boolean listForSearch(String searchTag)
	{
		return false;
	}

	@Override
	public int getPixelsTaken()
	{
		return renderData.get().ySize;
	}

	private static class RenderData
	{
		final Entity entity;
		final float entitySize;
		final float scale;
		final int ySize;

		RenderData(EntityType<?> entityType, CompoundTag entityData)
		{
			this.entity = entityType.create(Minecraft.getInstance().level);
			if(entityData!=null)
				this.entity.load(entityData);
			this.entitySize = Math.max(entity.getBbWidth(), entity.getBbHeight());
			this.scale = entitySize <= 1?36: entitySize <= 3?28: 26f-entitySize;
			this.ySize = entitySize <= 2?60: 90;
		}
	}

}
