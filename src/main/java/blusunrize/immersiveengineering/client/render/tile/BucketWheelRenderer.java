/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.client.models.obj.IESmartObjModel;
import blusunrize.immersiveengineering.client.utils.SinglePropertyModelData;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.BucketWheelTileEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.IModelData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class BucketWheelRenderer extends TileEntityRenderer<BucketWheelTileEntity>
{
	public static DynamicModel<Void> WHEEL;

	public BucketWheelRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}

	@Override
	public void render(BucketWheelTileEntity tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!tile.formed||!tile.getWorldNonnull().isBlockLoaded(tile.getPos())||tile.isDummy())
			return;
		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockState state = tile.getWorldNonnull().getBlockState(tile.getPos());
		if(state.getBlock()!=Multiblocks.bucketWheel)
			return;
		IBakedModel model = WHEEL.get(null);
		Map<String, String> texMap = new HashMap<>();
		List<String> list = Lists.newArrayList("bucketWheel");
		synchronized(tile.digStacks)
		{
			for(int i = 0; i < tile.digStacks.size(); i++)
				if(!tile.digStacks.get(i).isEmpty())
				{
					list.add("dig"+i);
					Block b = Block.getBlockFromItem(tile.digStacks.get(i).getItem());
					BlockState digState = b!=Blocks.AIR?b.getDefaultState(): Blocks.COBBLESTONE.getDefaultState();
					IBakedModel digModel = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(digState);
					digModel.getParticleTexture();
					texMap.put("dig"+i, digModel.getParticleTexture().getName().toString());
				}
		}
		IEObjState objState = new IEObjState(VisibilityList.show(list));

		matrixStack.push();

		matrixStack.translate(.5, .5, .5);
		bufferIn = TileRenderUtils.mirror(tile, matrixStack, bufferIn);
		float dir = tile.getFacing()==Direction.SOUTH?90: tile.getFacing()==Direction.NORTH?-90: tile.getFacing()==Direction.EAST?180: 0;
		matrixStack.rotate(new Quaternion(new Vector3f(0, 1, 0), dir, true));
		float rot = tile.rotation+(float)(tile.active?IEConfig.MACHINES.excavator_speed.get()*partialTicks: 0);
		matrixStack.rotate(new Quaternion(new Vector3f(1, 0, 0), rot, true));

		matrixStack.translate(-.5, -.5, -.5);
		IVertexBuilder builder = bufferIn.getBuffer(RenderType.getSolid());
		IModelData modelData = new SinglePropertyModelData<>(tile, IOBJModelCallback.PROPERTY);
		List<BakedQuad> quads;
		if(model instanceof IESmartObjModel)
			quads = ((IESmartObjModel)model).getQuads(state, objState, texMap,
					true, modelData);
		else
			quads = model.getQuads(state, null, Utils.RAND, modelData);
		ClientUtils.renderModelTESRFast(quads, builder, matrixStack, combinedLightIn);
		matrixStack.pop();
	}
}