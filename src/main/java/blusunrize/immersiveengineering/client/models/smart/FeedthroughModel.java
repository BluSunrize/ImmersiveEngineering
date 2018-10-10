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
import blusunrize.immersiveengineering.api.energy.wires.WireApi;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_Connector;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFeedthrough;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static blusunrize.immersiveengineering.api.energy.wires.WireApi.INFOS;
import static blusunrize.immersiveengineering.common.blocks.metal.TileEntityFeedthrough.MIDDLE_STATE;
import static blusunrize.immersiveengineering.common.blocks.metal.TileEntityFeedthrough.WIRE;
import static net.minecraft.util.EnumFacing.Axis.Y;

public class FeedthroughModel implements IBakedModel
{
	public static final Cache<FeedthroughCacheKey, SpecificFeedthroughModel> CACHE = CacheBuilder.newBuilder()
			.expireAfterAccess(2, TimeUnit.MINUTES)
			.maximumSize(100)
			.build();

	public FeedthroughModel()
	{
		//TODO find a better place to put this
		Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter =
				(rl) -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(rl.toString());
		for(WireApi.FeedthroughModelInfo f : INFOS.values())
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
		BlockPos p = null;
		World w = null;
		if(state instanceof IExtendedBlockState)
		{
			TileEntity te = ((IExtendedBlockState)state).getValue(IEProperties.TILEENTITY_PASSTHROUGH);
			if(te instanceof TileEntityFeedthrough)
			{
				baseState = ((TileEntityFeedthrough)te).stateForMiddle;
				wire = ((TileEntityFeedthrough)te).reference;
				facing = ((TileEntityFeedthrough)te).getFacing();
				offset = ((TileEntityFeedthrough)te).offset;
				p = te.getPos();
				w = te.getWorld();
			}
		}
		final BlockPos pFinal = p;
		final World wFinal = w;
		FeedthroughCacheKey key = new FeedthroughCacheKey(wire, baseState, offset, facing, MinecraftForgeClient.getRenderLayer());
		try
		{
			return CACHE.get(key,
					() -> new SpecificFeedthroughModel(key, wFinal, pFinal)).getQuads(state, side, rand);
		} catch(ExecutionException e)
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
		return ModelLoader.White.INSTANCE;
	}

	private ItemCameraTransforms transform = new ItemCameraTransforms(
			new ItemTransformVec3f(new Vector3f(75, 45, 0), new Vector3f(0, 0, 0), new Vector3f(.375F, .375F, .375F)),//3Left
			new ItemTransformVec3f(new Vector3f(75, 45, 0), new Vector3f(0, 0, 0), new Vector3f(.375F, .375F, .375F)),//3Right
			new ItemTransformVec3f(new Vector3f(0, 225, 0), new Vector3f(0, 0, 0), new Vector3f(.4F, .4F, .4F)),//1Left
			new ItemTransformVec3f(new Vector3f(0, 45, 0), new Vector3f(0, 0, 0), new Vector3f(.4F, .4F, .4F)),//1Right
			new ItemTransformVec3f(new Vector3f(), new Vector3f(), new Vector3f()),//Head?
			new ItemTransformVec3f(new Vector3f(30, 225, 0), new Vector3f(0, 0, 0), new Vector3f(.6F, .6F, .6F)),//GUI
			new ItemTransformVec3f(new Vector3f(), new Vector3f(0, .3F, 0), new Vector3f(.25F, .25F, .25F)),//Ground
			new ItemTransformVec3f(new Vector3f(0, 180, 45), new Vector3f(0, 0, -.1875F), new Vector3f(.5F, .5F, .5F)));

	@Nonnull
	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return transform;
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

		@Nonnull
		@Override
		public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity)
		{
			Item connItem = Item.getItemFromBlock(IEContent.blockConnectors);
			if(stack!=null&&stack.getItem()==connItem&&stack.getMetadata()==BlockTypes_Connector.FEEDTHROUGH.ordinal())
			{
				try
				{
					return ITEM_MODEL_CACHE.get(stack, () ->
							new SpecificFeedthroughModel(stack));
				} catch(ExecutionException e)
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
		final BlockRenderLayer layer;

		public FeedthroughCacheKey(WireType type, IBlockState baseState, int offset, EnumFacing facing,
								   BlockRenderLayer layer)
		{
			this.type = type;
			this.baseState = baseState;
			this.offset = offset;
			this.facing = facing;
			this.layer = layer;
		}

		@Override
		public boolean equals(Object o)
		{
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;
			FeedthroughCacheKey that = (FeedthroughCacheKey)o;
			return offset==that.offset&&
					Objects.equals(type, that.type)&&
					Utils.areStatesEqual(baseState, that.baseState, ImmutableSet.of(), false)&&
					facing==that.facing&&
					Objects.equals(layer, that.layer);
		}

		@Override
		public int hashCode()
		{
			int ret = Utils.hashBlockstate(baseState, ImmutableSet.of(), false);
			return 31*ret+Objects.hash(type, offset, facing, layer);
		}
	}

	private static class SpecificFeedthroughModel extends FeedthroughModel
	{
		private static final float[] WHITE = {1, 1, 1, 1};
		private static final Vector3f[] vertices = {
				new Vector3f(.75F, .001F, .75F), new Vector3f(.75F, .001F, .25F),
				new Vector3f(.25F, .001F, .25F), new Vector3f(.25F, .001F, .75F)
		};
		List<List<BakedQuad>> quads = new ArrayList<>(6);

		public SpecificFeedthroughModel(ItemStack stack)
		{
			WireType w = WireType.getValue(ItemNBTHelper.getString(stack, WIRE));
			IBlockState state = Utils.stateFromNBT(ItemNBTHelper.getTagCompound(stack, MIDDLE_STATE));
			init(new FeedthroughCacheKey(w, state, Integer.MAX_VALUE, EnumFacing.NORTH, null), null, null);
		}

		public SpecificFeedthroughModel(FeedthroughCacheKey key, World w, BlockPos p)
		{
			init(key, w, p);
		}

		private void init(FeedthroughCacheKey k, @Nullable World world, @Nullable BlockPos pos)
		{
			IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes()
					.getModelForState(k.baseState);
			Function<Integer, Integer> colorMultiplier = null;
			if(world!=null&&pos!=null)
			{
				BlockColors colors = Minecraft.getMinecraft().getBlockColors();
				colorMultiplier = (i) -> colors.colorMultiplier(k.baseState, world, pos, i);
			}
			else
			{
				ItemColors colors = Minecraft.getMinecraft().getItemColors();
				ItemStack stack = new ItemStack(k.baseState.getBlock(), 1, k.baseState.getBlock().getMetaFromState(k.baseState));
				colorMultiplier = (i) -> colors.colorMultiplier(stack, i);
			}
			for(int j = 0; j < 7; j++)
			{
				EnumFacing side = j < 6?EnumFacing.VALUES[j]: null;
				EnumFacing facing = k.facing;
				switch(k.offset)
				{
					case 0:
						if(k.layer==null||k.baseState.getBlock().canRenderInLayer(k.baseState, k.layer))
						{
							Function<BakedQuad, BakedQuad> tintTransformer = ApiUtils.transformQuad(new Matrix4(),
									DefaultVertexFormats.ITEM, colorMultiplier);
							quads.add(model.getQuads(k.baseState, side, 0).stream().map(tintTransformer)
									.collect(Collectors.toCollection(ArrayList::new)));
						}
						break;
					case 1:
						facing = facing.getOpposite();
					case -1:
						if(k.layer==BlockRenderLayer.SOLID)
							quads.add(getConnQuads(facing, side, k.type, new Matrix4()));
						break;
					case Integer.MAX_VALUE:
						Matrix4 mat = new Matrix4();
						mat.translate(0, 0, 1);
						List<BakedQuad> all = new ArrayList<>(getConnQuads(facing, side, k.type, mat));
						mat = new Matrix4();
						mat.translate(0, 0, -1);
						all.addAll(getConnQuads(facing.getOpposite(), side, k.type, mat));
						Function<BakedQuad, BakedQuad> tintTransformer = ApiUtils.transformQuad(new Matrix4(),
								DefaultVertexFormats.ITEM, colorMultiplier);
						all.addAll(model.getQuads(k.baseState, side, 0).stream().map(tintTransformer)
								.collect(Collectors.toCollection(ArrayList::new)));
						quads.add(all);
						break;
				}
				if(quads.size() <= j)
					quads.add(ImmutableList.of());
			}
		}

		private List<BakedQuad> getConnQuads(EnumFacing facing, EnumFacing side, WireType type, Matrix4 mat)
		{
			//connector model+feedthrough border
			WireApi.FeedthroughModelInfo info = INFOS.get(type);
			mat.translate(.5, .5, .5);
			if(facing.getAxis()==Y)
			{
				if(facing==EnumFacing.UP)
					mat.rotate(Math.PI, 1, 0, 0);
			}
			else
			{
				EnumFacing rotateAround = facing.rotateAround(Y);
				mat.rotate(Math.PI/2, rotateAround.getXOffset(), rotateAround.getYOffset(),
						rotateAround.getZOffset());
			}
			mat.translate(-.5, -.5, -.5);
			List<BakedQuad> conn = new ArrayList<>(info.model.getQuads(null, side, 0));
			if(side==facing)
				conn.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, vertices, EnumFacing.UP, info.tex, info.uvs, WHITE, false));
			Function<BakedQuad, BakedQuad> transf = ApiUtils.transformQuad(mat, null,
					null);//I hope no one uses tint index for connectors
			if(transf!=null)
				return conn.stream().map(transf).collect(Collectors.toList());
			else
				return conn;
		}

		@Nonnull
		@Override
		public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
		{
			return quads.get(side==null?6: side.getIndex());
		}
	}
}
