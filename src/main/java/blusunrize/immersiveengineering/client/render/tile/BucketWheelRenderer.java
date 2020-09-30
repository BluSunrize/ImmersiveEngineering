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
import blusunrize.immersiveengineering.api.client.IVertexBufferHolder;
import blusunrize.immersiveengineering.client.models.obj.IESmartObjModel;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.BucketWheelTileEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
public class BucketWheelRenderer extends TileEntityRenderer<BucketWheelTileEntity>
{
	public static DynamicModel<Void> WHEEL;
	private static final Cache<List<String>, IVertexBufferHolder> CACHED_BUFFERS = CacheBuilder.newBuilder()
			.maximumSize(100)
			.expireAfterAccess(1, TimeUnit.MINUTES)
			.<Object, IVertexBufferHolder>removalListener(rem -> rem.getValue().reset())
			.build();

	public BucketWheelRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}

	@Override
	public void render(BucketWheelTileEntity tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!tile.formed||!tile.getWorldNonnull().isBlockLoaded(tile.getPos())||tile.isDummy())
			return;
		Map<String, String> texMap = new HashMap<>();
		List<String> textures = new ArrayList<>(8);
		int offset = 0;
		synchronized(tile.digStacks)
		{
			for(int i = 0; i < tile.digStacks.size(); i++)
				if(!tile.digStacks.get(i).isEmpty())
				{
					offset = i;
					break;
				}
			for(int i = 0; i < tile.digStacks.size(); i++)
			{
				int realIndex = (i+offset)%tile.digStacks.size();
				ItemStack stackAtIndex = tile.digStacks.get(realIndex);
				if(!stackAtIndex.isEmpty())
				{
					Block b = Block.getBlockFromItem(stackAtIndex.getItem());
					BlockState digState = b!=Blocks.AIR?b.getDefaultState(): Blocks.COBBLESTONE.getDefaultState();
					IBakedModel digModel = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(digState);
					String texture = digModel.getParticleTexture().getName().toString();
					texMap.put("dig"+i, texture);
					textures.add(texture);
				}
				else
					textures.add("");
			}
		}
		matrixStack.push();

		matrixStack.translate(.5, .5, .5);
		matrixStack.rotate(new Quaternion(new Vector3f(0, 1, 0), 90, true)); //to mirror different plane. compensate on dir rotate
		TileRenderUtils.mirror(tile, matrixStack);
		float dir = tile.getFacing()==Direction.SOUTH?0: tile.getFacing()==Direction.NORTH?180: tile.getFacing()==Direction.EAST?90: -90;
		matrixStack.rotate(new Quaternion(new Vector3f(0, 1, 0), dir, true));
		float rot = tile.rotation-45*offset+(float)(tile.active?IEConfig.MACHINES.excavator_speed.get()*partialTicks: 0);
		matrixStack.rotate(new Quaternion(new Vector3f(1, 0, 0), rot, true));

		matrixStack.translate(-.5, -.5, -.5);
		try
		{
			CACHED_BUFFERS.get(textures, () -> IVertexBufferHolder.create(() -> {
				IBakedModel model = WHEEL.get(null);
				BlockState state = Multiblocks.bucketWheel.getDefaultState();
				List<String> list = Lists.newArrayList("bucketWheel");
				list.addAll(texMap.keySet());
				IEObjState objState = new IEObjState(VisibilityList.show(list));
				if(model instanceof IESmartObjModel)
					return ((IESmartObjModel)model).getQuads(state, objState, texMap, true, EmptyModelData.INSTANCE);
				else
					return model.getQuads(state, null, Utils.RAND, EmptyModelData.INSTANCE);
			})).render(RenderType.getSolid(), combinedLightIn, combinedOverlayIn, bufferIn, matrixStack, tile.getIsMirrored());
		} catch(ExecutionException ex)
		{
			throw new RuntimeException(ex);
		}
		matrixStack.pop();
	}

	public static void reset()
	{
		CACHED_BUFFERS.invalidateAll();
	}
}