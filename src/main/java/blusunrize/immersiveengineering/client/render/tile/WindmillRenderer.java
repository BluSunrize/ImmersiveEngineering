/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.render.tile.DynamicModel.ModelType;
import blusunrize.immersiveengineering.client.utils.SinglePropertyModelData;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.WoodenDevices;
import blusunrize.immersiveengineering.common.blocks.wooden.WindmillTileEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.IModelData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//TODO maybe replace with Forge animations?
public class WindmillRenderer extends TileEntityRenderer<WindmillTileEntity>
{
	private static List<BakedQuad>[] quads = new List[9];
	private final DynamicModel<Void> model = DynamicModel.createSimple(
			new ResourceLocation(ImmersiveEngineering.MODID, "block/wooden_device/windmill.obj.ie"),
			"windmill", ModelType.IE_OBJ);

	public WindmillRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}

	@Override
	public void render(WindmillTileEntity tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!tile.getWorldNonnull().isBlockLoaded(tile.getPos()))
			return;
		BlockPos blockPos = tile.getPos();
		if(quads[tile.sails]==null)
		{
			BlockState state = tile.getWorld().getBlockState(blockPos);
			if(state.getBlock()!=WoodenDevices.windmill)
				return;
			state = state.with(IEProperties.FACING_HORIZONTAL, Direction.NORTH);
			IBakedModel model = this.model.get(null);
			List<String> parts = new ArrayList<>();
			parts.add("base");
			for(int i = 1; i <= tile.sails; i++)
				parts.add("sail_"+i);
			IModelData data = new SinglePropertyModelData<>(
					new IEObjState(VisibilityList.show(parts)), IEProperties.Model.IE_OBJ_STATE);
			quads[tile.sails] = model.getQuads(state, null, Utils.RAND, data);
		}
		matrixStack.push();
		matrixStack.translate(.5, .5, .5);

		float dir = tile.getFacing()==Direction.SOUTH?0: tile.getFacing()==Direction.NORTH?180: tile.getFacing()==Direction.EAST?90: -90;
		float rot = 360*(tile.rotation+(!tile.canTurn||tile.rotation==0?0: partialTicks)*tile.perTick);

		matrixStack.rotate(new Quaternion(new Vector3f(tile.getFacing().getAxis()==Axis.X?1: 0, 0, tile.getFacing().getAxis()==Axis.Z?1: 0), rot, true));
		matrixStack.rotate(new Quaternion(new Vector3f(0, 1, 0), dir, true));

		matrixStack.translate(-.5, -.5, -.5);
		IVertexBuilder builder = bufferIn.getBuffer(RenderType.getCutout());
		ClientUtils.renderModelTESRFast(quads[tile.sails], builder, matrixStack, combinedLightIn);
		matrixStack.pop();
	}

	public static void reset()
	{
		Arrays.fill(quads, null);
	}
}