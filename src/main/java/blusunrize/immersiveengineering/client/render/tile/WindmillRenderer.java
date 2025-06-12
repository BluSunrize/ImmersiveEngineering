/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.api.client.IVertexBufferHolder;
import blusunrize.immersiveengineering.api.utils.client.ModelDataUtils;
import blusunrize.immersiveengineering.client.models.obj.callback.DynamicSubmodelCallbacks;
import blusunrize.immersiveengineering.common.blocks.wooden.WindmillBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.ArrayList;
import java.util.List;

public class WindmillRenderer extends IEBlockEntityRenderer<WindmillBlockEntity>
{
	public static final String NAME = "windmill";
	public static DynamicModel MODEL;
	private static final IVertexBufferHolder[] BUFFERS = new IVertexBufferHolder[9];

	private static IVertexBufferHolder getBufferHolder(int sails)
	{
		if(BUFFERS[sails]==null)
			BUFFERS[sails] = IVertexBufferHolder.create(() -> {
				BakedModel model = MODEL.get();
				List<String> parts = new ArrayList<>();
				parts.add("base");
				for(int i = 1; i <= sails; i++)
					parts.add("sail_"+i);
				ModelData data = ModelDataUtils.single(DynamicSubmodelCallbacks.getProperty(), VisibilityList.show(parts));
				return model.getQuads(null, null, ApiUtils.RANDOM_SOURCE, data, RenderType.cutout());
			});
		return BUFFERS[sails];
	}

	@Override
	public void render(WindmillBlockEntity tile, float partialTicks, PoseStack transform, MultiBufferSource bufferIn,
					   int combinedLightIn, int combinedOverlayIn)
	{
		if(!tile.getLevelNonnull().hasChunkAt(tile.getBlockPos()))
			return;
		transform.pushPose();
		transform.translate(.5, .5, .5);

		float dir = tile.getFacing()==Direction.SOUTH?0: tile.getFacing()==Direction.NORTH?Mth.PI: tile.getFacing()==Direction.EAST?Mth.HALF_PI: -Mth.HALF_PI;
		float rot = (float)(Mth.TWO_PI*(tile.rotation+partialTicks*tile.getActualTurnSpeed()));

		transform.mulPose(new Quaternionf()
				.rotateAxis(rot, new Vector3f(tile.getFacing().getAxis()==Axis.X?1: 0, 0, tile.getFacing().getAxis()==Axis.Z?1: 0))
				.rotateY(dir)
		);

		transform.translate(-.5, -.5, -.5);
		getBufferHolder(tile.sails)
				.render(RenderType.cutout(), combinedLightIn, combinedOverlayIn, bufferIn, transform);
		transform.popPose();
	}

	public static void reset()
	{
		for(IVertexBufferHolder vbo : BUFFERS)
			if(vbo!=null)
				vbo.reset();
	}

	@Override
	public AABB getRenderBoundingBox(WindmillBlockEntity windmill)
	{
		if(windmill.renderAABB==null)
		{
			Direction facing = windmill.getFacing();
			BlockPos pos = windmill.getBlockPos();
			windmill.renderAABB = new AABB(
					pos.getX()-(facing.getAxis()==Axis.Z?6: 0),
					pos.getY()-6,
					pos.getZ()-(facing.getAxis()==Axis.Z?0: 6),
					pos.getX()+(facing.getAxis()==Axis.Z?7: 0),
					pos.getY()+7,
					pos.getZ()+(facing.getAxis()==Axis.Z?0: 7)
			);
		}
		return windmill.renderAABB;
	}

}