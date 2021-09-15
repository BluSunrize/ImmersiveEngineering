/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.api.client.IVertexBufferHolder;
import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import blusunrize.immersiveengineering.common.blocks.wooden.WindmillBlockEntity;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDevices;
import blusunrize.immersiveengineering.common.util.Utils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraftforge.client.model.data.IModelData;

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
				IModelData data = new SinglePropertyModelData<>(
						new IEObjState(VisibilityList.show(parts)), IEProperties.Model.IE_OBJ_STATE);
				return model.getQuads(WoodenDevices.WINDMILL.defaultBlockState(), null, Utils.RAND, data);
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

		float dir = tile.getFacing()==Direction.SOUTH?0: tile.getFacing()==Direction.NORTH?180: tile.getFacing()==Direction.EAST?90: -90;
		float rot = (float)(360*(tile.rotation+partialTicks*tile.getActualTurnSpeed()));

		transform.mulPose(new Quaternion(new Vector3f(tile.getFacing().getAxis()==Axis.X?1: 0, 0, tile.getFacing().getAxis()==Axis.Z?1: 0), rot, true));
		transform.mulPose(new Quaternion(new Vector3f(0, 1, 0), dir, true));

		transform.translate(-.5, -.5, -.5);
		getBufferHolder(tile.sails)
				.render(RenderType.cutoutMipped(), combinedLightIn, combinedOverlayIn, bufferIn, transform);
		transform.popPose();
	}

	public static void reset()
	{
		for(IVertexBufferHolder vbo : BUFFERS)
			if(vbo!=null)
				vbo.reset();
	}
}