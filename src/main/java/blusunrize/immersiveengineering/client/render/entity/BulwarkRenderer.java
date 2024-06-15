/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.entities.illager.Bulwark;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class BulwarkRenderer extends IllagerRenderer<Bulwark>
{
	private static final ResourceLocation TEXTURE = IEApi.ieLoc("textures/entity/illager/bulwark.png");

	public BulwarkRenderer(EntityRendererProvider.Context p_174354_)
	{
		super(p_174354_, new IllagerModel<>(createBodyLayer().bakeRoot())
		{
			@Override
			public void setupAnim(Bulwark entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
			{
				super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
				boolean isLefthanded = entity.isLeftHanded();
				ModelPart rightArm = this.root().getChild("right_arm");
				ModelPart leftArm = this.root().getChild("left_arm");
				ModelPart head = this.root().getChild("head");
				if(entity.isBlocking())
					if(isLefthanded)
					{
						rightArm.xRot = rightArm.xRot*0.5F-1.11701F;
						rightArm.yRot = (-(float)Math.PI/4F);
					}
					else
					{
						leftArm.xRot = leftArm.xRot*0.5F-1.11701F;
						leftArm.yRot = ((float)Math.PI/4F);
					}
				if(isLefthanded)
					leftArm.xRot = -.523599f;
				else
					rightArm.xRot = -.523599f;
			}
		}, 0.5F);
		this.model.getHat().visible = true;
		this.addLayer(new ItemInHandLayer<>(this, p_174354_.getItemInHandRenderer()));
	}


	public static LayerDefinition createBodyLayer()
	{
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
		partdefinition1.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 12.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.ZERO);
		partdefinition1.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F), PartPose.offset(0.0F, -2.0F, 0.0F));
		partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
						.texOffs(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F)
						.texOffs(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F, new CubeDeformation(1F)),
				PartPose.offset(0.0F, 0.0F, 0.0F)
		);
		PartDefinition arms = partdefinition.addOrReplaceChild("arms", CubeListBuilder.create().texOffs(44, 22).addBox(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F).texOffs(40, 38).addBox(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F), PartPose.offsetAndRotation(0.0F, 3.0F, -1.0F, -0.75F, 0.0F, 0.0F));
		arms.addOrReplaceChild("left_shoulder", CubeListBuilder.create().texOffs(44, 22).mirror().addBox(4.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F), PartPose.ZERO);
		partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create()
						.texOffs(0, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(.5F))
						.texOffs(44, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(1F)),
				PartPose.offset(-2.0F, 12.0F, 0.0F)
		);
		partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create()
						.texOffs(0, 22).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(.5F))
						.texOffs(44, 22).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(1F)),
				PartPose.offset(2.0F, 12.0F, 0.0F)
		);
		partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create()
						.texOffs(28, 40).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(.5F))
						.texOffs(44, 40).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(1F)),
				PartPose.offset(-5.0F, 2.0F, 0.0F)
		);
		partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create()
						.texOffs(28, 40).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(.5F))
						.texOffs(44, 40).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(1F)),
				PartPose.offset(5.0F, 2.0F, 0.0F)
		);
		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public ResourceLocation getTextureLocation(Bulwark entity)
	{
		return TEXTURE;
	}
}
