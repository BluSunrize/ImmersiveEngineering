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
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;

public class ModelGlider extends ModelIEArmorBase
{
	public ModelGlider(ModelPart part)
	{
		super(part, RenderType::entityTranslucentCull);
	}

	public static LayerDefinition createLayers()
	{
		MeshDefinition data = new MeshDefinition();
		PartDefinition root = data.getRoot();
		root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
		root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
		root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.ZERO);
		root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.ZERO);
		root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);
		root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);

		// harness
		PartDefinition body = root.addOrReplaceChild("body",
				CubeListBuilder.create()
						.texOffs(0, 30)
						.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F),
				PartPose.ZERO
		);

		// glider main
		PartDefinition glider = body.addOrReplaceChild("glider",
				CubeListBuilder.create()
						// crossbar
						.texOffs(24, 30)
						.addBox(-6.0F, 10.0F, 3.0F, 12.0F, 2.0F, 2.0F)
						// bottom frame
						.texOffs(0, 28)
						.addBox(-15.0F, 22.0F, 5.0F, 30.0F, 1.0F, 1.0F)
						// handle left
						.texOffs(24, 36)
						.addBox(-8.0F, 10.0F, -4.0F, 2.0F, 2.0F, 8.0F)
						// handle right
						.texOffs(24, 36)
						.addBox(6.0F, 10.0F, -4.0F, 2.0F, 2.0F, 8.0F)
						// canvas
						.texOffs(0, 0)
						.addBox(-15.0F, -6.0F, 5.5F, 30.0F, 28.0F, 0.0F),
				PartPose.ZERO
		);

		// diagonal frame
		glider.addOrReplaceChild("frame_right",
				CubeListBuilder.create()
						.texOffs(0, 28)
						.addBox(-3.25F, -15.1F, 5.0F, 30.0F, 1.0F, 1.0F),
				PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, -1.2392F)
		);
		glider.addOrReplaceChild("frame_left",
				CubeListBuilder.create()
						.texOffs(0, 28)
						.addBox(-26.75F, -15.1F, 5.0F, 30.0F, 1.0F, 1.0F),
				PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, 1.2392F)
		);
		// crossbars
		glider.addOrReplaceChild("crossbar_left",
				CubeListBuilder.create()
						.texOffs(0, 28)
						.addBox(-21.0F, -11.8F, 5.0F, 29.0F, 1.0F, 1.0F),
				PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, 0.7854F)
		);
		glider.addOrReplaceChild("crossbar_right",
				CubeListBuilder.create()
						.texOffs(0, 28)
						.addBox(-8.0F, -11.8F, 5.0F, 29.0F, 1.0F, 1.0F),
				PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, -0.7854F)
		);
		return LayerDefinition.create(data, 64, 48);
	}
}