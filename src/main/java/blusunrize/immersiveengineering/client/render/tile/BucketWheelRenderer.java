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
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.BucketWheelTileEntity;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
public class BucketWheelRenderer extends IEBlockEntityRenderer<BucketWheelTileEntity>
{
	public static DynamicModel<Void> WHEEL;
	private static final Cache<List<String>, IVertexBufferHolder> CACHED_BUFFERS = CacheBuilder.newBuilder()
			.maximumSize(100)
			.expireAfterAccess(1, TimeUnit.MINUTES)
			.<Object, IVertexBufferHolder>removalListener(rem -> rem.getValue().reset())
			.build();

	@Override
	public void render(BucketWheelTileEntity tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!tile.formed||!tile.getLevelNonnull().hasChunkAt(tile.getBlockPos())||tile.isDummy())
			return;
		Map<String, String> texMap = new HashMap<>();
		List<String> textures = new ArrayList<>(8);
		synchronized(tile.digStacks)
		{
			for(int i = 0; i < tile.digStacks.size(); i++)
			{
				ItemStack stackAtIndex = tile.digStacks.get(i);
				if(!stackAtIndex.isEmpty())
				{
					Block b = Block.byItem(stackAtIndex.getItem());
					BlockState digState = b!=Blocks.AIR?b.defaultBlockState(): Blocks.COBBLESTONE.defaultBlockState();
					BakedModel digModel = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(digState);
					String texture = digModel.getParticleIcon().getName().toString();
					texMap.put("dig"+i, texture);
					textures.add(texture);
				}
				else
					textures.add("");
			}
		}
		matrixStack.pushPose();

		matrixStack.translate(.5, .5, .5);
		matrixStack.mulPose(new Quaternion(new Vector3f(0, 1, 0), 90, true)); //to mirror different plane. compensate on dir rotate
		TileRenderUtils.mirror(tile, matrixStack);
		float dir = tile.getFacing()==Direction.SOUTH?0: tile.getFacing()==Direction.NORTH?180: tile.getFacing()==Direction.EAST?90: -90;
		matrixStack.mulPose(new Quaternion(new Vector3f(0, 1, 0), dir, true));
		float rot = tile.rotation+(float)(tile.active?IEServerConfig.MACHINES.excavator_speed.get()*partialTicks: 0);
		matrixStack.mulPose(new Quaternion(new Vector3f(1, 0, 0), rot, true));

		matrixStack.translate(-.5, -.5, -.5);
		try
		{
			CACHED_BUFFERS.get(textures, () -> IVertexBufferHolder.create(() -> {
				BakedModel model = WHEEL.get(null);
				BlockState state = Multiblocks.bucketWheel.defaultBlockState();
				List<String> list = Lists.newArrayList("bucketWheel");
				list.addAll(texMap.keySet());
				IEObjState objState = new IEObjState(VisibilityList.show(list));
				if(model instanceof IESmartObjModel)
					return ((IESmartObjModel)model).getQuads(state, objState, texMap, true, EmptyModelData.INSTANCE);
				else
					return model.getQuads(state, null, Utils.RAND, EmptyModelData.INSTANCE);
			})).render(RenderType.solid(), combinedLightIn, combinedOverlayIn, bufferIn, matrixStack, tile.getIsMirrored());
		} catch(ExecutionException ex)
		{
			throw new RuntimeException(ex);
		}
		matrixStack.popPose();
	}

	public static void reset()
	{
		CACHED_BUFFERS.invalidateAll();
	}
}