/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.smart;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_Connector;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFeedthrough;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFeedthrough.ModelInfo;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static blusunrize.immersiveengineering.common.blocks.metal.TileEntityFeedthrough.INFOS;
import static blusunrize.immersiveengineering.common.blocks.metal.TileEntityFeedthrough.MIDDLE_STATE;
import static blusunrize.immersiveengineering.common.blocks.metal.TileEntityFeedthrough.WIRE;
import static net.minecraft.util.EnumFacing.Axis.Y;

public class FeedthroughModel implements IBakedModel
{
	static final Cache<FeedthroughCacheKey, SpecificFeedthroughModel> CACHE = CacheBuilder.newBuilder()
			.expireAfterAccess(2, TimeUnit.MINUTES)
			.maximumSize(100)
			.build();
	public FeedthroughModel()
	{
		//TODO find a better place to put this
		Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter =
				(rl) -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(rl.toString());
		for (TileEntityFeedthrough.ModelInfo f : INFOS.values())
			f.onResourceReload(bakedTextureGetter, DefaultVertexFormats.ITEM);
	}


	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
	{
		IBlockState baseState = Blocks.STONE.getDefaultState();
		WireType wire = WireType.COPPER;
		EnumFacing facing = EnumFacing.NORTH;
		int offset = 1;
		if (state instanceof IExtendedBlockState)
		{
			TileEntity te = ((IExtendedBlockState) state).getValue(IEProperties.TILEENTITY_PASSTHROUGH);
			if (te instanceof TileEntityFeedthrough)
			{
				baseState = ((TileEntityFeedthrough) te).stateForMiddle;
				wire = ((TileEntityFeedthrough) te).reference;
				facing = ((TileEntityFeedthrough) te).getFacing();
				offset = ((TileEntityFeedthrough) te).offset;
			}
		}
		FeedthroughCacheKey key = new FeedthroughCacheKey(wire, baseState, offset, facing);
		try
		{
			return CACHE.get(key,
					()->new SpecificFeedthroughModel(key)).getQuads(state, side, rand);
		}
		catch (ExecutionException e)
		{
			e.printStackTrace();
			return ImmutableList.of();
		}
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return true;
	}

	@Override
	public boolean isGui3d()
	{
		return true;
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return false;
	}

	@Nonnull
	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return ModelLoader.White.INSTANCE;//TODO
	}

	@Nonnull
	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return new ItemCameraTransforms(new ItemTransformVec3f(new Vector3f(45, 0, 0), new Vector3f(0, .2F, 0), new Vector3f(.5F, .5F, .5F)),//3Left
				new ItemTransformVec3f(new Vector3f(45, 0, 0), new Vector3f(0, .2F, 0), new Vector3f(.5F, .5F, .5F)),//3Right
				new ItemTransformVec3f(new Vector3f(), new Vector3f(0, .2F, 0), new Vector3f(.5F, .5F, .5F)),//1Left
				new ItemTransformVec3f(new Vector3f(), new Vector3f(0, .2F, 0), new Vector3f(.5F, .5F, .5F)),//1Right
				new ItemTransformVec3f(new Vector3f(), new Vector3f(), new Vector3f()),//Head?
				new ItemTransformVec3f(new Vector3f(30, 45, 0), new Vector3f(0, .125F, 0), new Vector3f(.6F, .6F, .6F)),//GUI
				new ItemTransformVec3f(new Vector3f(), new Vector3f(0, .1F, 0), new Vector3f(.25F, .25F, .25F)),//Ground
				new ItemTransformVec3f(new Vector3f(0, 180, 45), new Vector3f(0, 0, -.1875F), new Vector3f(.5F, .5F, .5F)));
	}

	private static final FeedthroughItemOverride INSTANCE = new FeedthroughItemOverride();

	@Nonnull
	@Override
	public ItemOverrideList getOverrides()
	{
		return INSTANCE;
	}
	private static class FeedthroughItemOverride extends ItemOverrideList
	{

		private static Cache<ItemStack, FeedthroughModel> ITEM_MODEL_CACHE = CacheBuilder.newBuilder()
				.maximumSize(100)
				.expireAfterAccess(60, TimeUnit.SECONDS)
				.build();


		public FeedthroughItemOverride()
		{
			super(ImmutableList.of());
		}

		Item connItem = Item.getItemFromBlock(IEContent.blockConnectors);

		@Nonnull
		@Override
		public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity)
		{
			Item connItem = Item.getItemFromBlock(IEContent.blockConnectors);
			if (stack != null && stack.getItem() == connItem && stack.getMetadata() == BlockTypes_Connector.FEEDTHROUGH.ordinal())
			{
				try
				{
					//TODO remove!!
					ITEM_MODEL_CACHE.invalidateAll();
					return ITEM_MODEL_CACHE.get(stack, () ->
							new SpecificFeedthroughModel(stack));
				}
				catch (ExecutionException e)
				{
					e.printStackTrace();
				}
			}
			return originalModel;
		}

	}

	private static class FeedthroughCacheKey
	{
		final WireType type;
		final IBlockState baseState;
		final int offset;
		final EnumFacing facing;

		public FeedthroughCacheKey(WireType type, IBlockState baseState, int offset, EnumFacing facing)
		{
			this.type = type;
			this.baseState = baseState;
			this.offset = offset;
			this.facing = facing;
		}
		//TODO equals+hashCode
	}
	private static class SpecificFeedthroughModel extends FeedthroughModel
	{
		List<List<BakedQuad>> quads = new ArrayList<>(6);
		public SpecificFeedthroughModel(ItemStack stack)
		{
			WireType w = WireType.getValue(ItemNBTHelper.getString(stack, WIRE));
			IBlockState state = Utils.stateFromNBT(ItemNBTHelper.getTagCompound(stack, MIDDLE_STATE));
			init(new FeedthroughCacheKey(w, state, Integer.MAX_VALUE, EnumFacing.NORTH));
		}

		public SpecificFeedthroughModel(FeedthroughCacheKey key)
		{
			init(key);
		}

		private void init(FeedthroughCacheKey k)
		{
			IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes()
					.getModelForState(k.baseState);
			sideLoop: for (int j = 0; j < 7; j++)
			{
				EnumFacing side = j<6?EnumFacing.VALUES[j]:null;
				EnumFacing facing = k.facing;
				switch (k.offset)
				{
					case 0:
						quads.add(model.getQuads(k.baseState, side, 0));
						break;
					case 1:
						facing = facing.getOpposite();
					case -1:
						quads.add(getConnQuads(facing, side, k.type, new Matrix4()));
						break;
					case Integer.MAX_VALUE:
						Matrix4 mat = new Matrix4();
						mat.translate(0, 0, 1);
						List<BakedQuad> all = new ArrayList<>(getConnQuads(facing, side, k.type, mat));
						mat = new Matrix4();
						mat.translate(0, 0, -1);
						all.addAll(getConnQuads(facing.getOpposite(), side, k.type, mat));
						all.addAll(model.getQuads(k.baseState, side, 0));
						quads.add(all);
						break;
				}
				if (quads.size()<=j)
					quads.add(ImmutableList.of());
			}
		}

		private List<BakedQuad> getConnQuads(EnumFacing facing, EnumFacing side, WireType type, Matrix4 mat)
		{
			//connector model+feedthrough border
			ModelInfo info = INFOS.get(type);
			mat.translate(.5, .5, .5);
			if (facing.getAxis() == Y)
			{
				if (facing == EnumFacing.UP)
					mat.rotate(Math.PI, 1, 0, 0);
			}
			else
			{
				EnumFacing rotateAround = facing.rotateAround(Y);
				mat.rotate(Math.PI / 2, rotateAround.getFrontOffsetX(), rotateAround.getFrontOffsetY(),
						rotateAround.getFrontOffsetZ());
			}
			mat.translate(-.5, -.5, -.5);
			List<BakedQuad> conn = new ArrayList<>(info.model.getQuads(null, side, 0));
			if (side == facing)
			{
				UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(DefaultVertexFormats.ITEM);
				TextureAtlasSprite tex = info.tex;
				builder.setTexture(tex);
				builder.setQuadOrientation(facing);
				builder.setQuadTint(-1);
				for (int i = 0; i < 4; i++)
				{
					int x = i < 2 ? 1 : 0;
					int y = i == 0 || i == 3 ? 1 : 0;
					builder.put(0, .25F + .5F * x, .001F, .25F + .5F * y);
					builder.put(1, 1, 1, 1, 1);
					builder.put(2, tex.getInterpolatedU(info.uvs[x * 2]), tex.getInterpolatedV(info.uvs[y * 2 + 1]));
					builder.put(3, 0, 1, 0);
					builder.put(4, 0);
				}
				conn.add(builder.build());
			}
			Function<BakedQuad, BakedQuad> transf = ApiUtils.applyMatrixToQuad(mat, DefaultVertexFormats.ITEM);
			if (transf != null)
				return conn.stream().map(transf).collect(Collectors.toList());
			else
				return conn;
		}

		@Nonnull
		@Override
		public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
		{
			return quads.get(side==null?6:side.getIndex());
		}
	}
}
