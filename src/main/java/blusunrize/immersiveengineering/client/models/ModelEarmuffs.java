/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;

public class ModelEarmuffs<T extends LivingEntity> extends ModelIEArmorBase<T>
{
	public ModelEarmuffs(ModelPart part)
	{
		super(part, RenderType::entitySolid);

		hat.visible = false;
		body.visible = false;
		leftLeg.visible = false;
		rightLeg.visible = false;
	}

	public static LayerDefinition createLayers()
	{
		MeshDefinition data = new MeshDefinition();
		PartDefinition root = data.getRoot();
		PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
		root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
		root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
		root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.ZERO);
		root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.ZERO);
		root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);
		root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);

		PartDefinition part0 = head.addOrReplaceChild(
				"part0", CubeListBuilder.create().texOffs(0, 16).addBox(-4.5f, -8.5f, 0f, 9, 1, 1), PartPose.ZERO
		);
		part0.addOrReplaceChild(
				"part1", CubeListBuilder.create().texOffs(0, 18).addBox(-4.5f, -7.5f, 0f, 1, 4, 1), PartPose.ZERO
		);
		part0.addOrReplaceChild(
				"part2", CubeListBuilder.create().texOffs(0, 18).addBox(3.5f, -7.5f, 0f, 1, 4, 1), PartPose.ZERO
		);
		PartDefinition part3 = head.addOrReplaceChild(
				"part3", CubeListBuilder.create().texOffs(0, 16).addBox(-4.5f, -8.5f, -1.5f, 9, 1, 1), PartPose.ZERO
		);
		part3.addOrReplaceChild(
				"part4", CubeListBuilder.create().texOffs(0, 18).addBox(-4.5f, -7.5f, -1.5f, 1, 4, 1), PartPose.ZERO
		);
		part3.addOrReplaceChild(
				"part5", CubeListBuilder.create().texOffs(0, 18).addBox(3.5f, -7.5f, -1.5f, 1, 4, 1), PartPose.ZERO
		);

		head.addOrReplaceChild(
				"part6", CubeListBuilder.create().texOffs(20, 24).addBox(-4.375f, -5.5f, -1.75f, 1, 4, 3), PartPose.ZERO
		);
		PartDefinition coloured0 = head.addOrReplaceChild(
				"coloured0", CubeListBuilder.create().texOffs(20, 16).addBox(-4.875f, -6.5f, -1.75f, 1, 5, 3), PartPose.ZERO
		);
		coloured0.addOrReplaceChild(
				"coloured1", CubeListBuilder.create().texOffs(28, 16).addBox(-5.25f, -5f, -1.25f, 1, 3, 2), PartPose.ZERO
		);

		head.addOrReplaceChild(
				"part7", CubeListBuilder.create().texOffs(20, 24).addBox(3.375f, -5.5f, -1.75f, 1, 4, 3), PartPose.ZERO
		);
		PartDefinition coloured2 = head.addOrReplaceChild(
				"coloured2", CubeListBuilder.create().texOffs(20, 16).addBox(3.875f, -6.5f, -1.75f, 1, 5, 3), PartPose.ZERO
		);
		coloured2.addOrReplaceChild(
				"coloured3", CubeListBuilder.create().texOffs(28, 16).addBox(4.25f, -5f, -1.25f, 1, 3, 2), PartPose.ZERO
		);

		return LayerDefinition.create(data, 64, 32);
	}
}