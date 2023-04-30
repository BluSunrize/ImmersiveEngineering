/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.render.IEBipedLayerRenderer;
import blusunrize.immersiveengineering.common.entities.illager.Fusilier;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.AbstractIllager.IllagerArmPose;

public class FusilierRenderer extends IllagerRenderer<Fusilier>
{
	private static final ResourceLocation TEXTURE = new ResourceLocation(ImmersiveEngineering.MODID, "textures/entity/illager/fusilier.png");

	public FusilierRenderer(EntityRendererProvider.Context p_174354_)
	{
		super(p_174354_, new IllagerModel<>(p_174354_.bakeLayer(ModelLayers.PILLAGER)){
			@Override
			public void setupAnim(Fusilier entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
			{
				super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
				if(entity.getArmPose() == IllagerArmPose.NEUTRAL) {
					ModelPart rightArm = this.root().getChild("right_arm");
					rightArm.xRot = -.87266f;
				}
			}
		}, 0.5F);
		this.model.getHat().visible = true;
		this.addLayer(new ItemInHandLayer<>(this, p_174354_.getItemInHandRenderer()));
		this.addLayer(new IEBipedLayerRenderer<>(this, p_174354_.getModelSet()));
	}

	/**
	 * Returns the location of an entity's texture.
	 */
	public ResourceLocation getTextureLocation(Fusilier entity)
	{
		return TEXTURE;
	}
}
