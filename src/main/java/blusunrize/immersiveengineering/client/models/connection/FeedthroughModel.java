/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.connection;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.wires.WireApi;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.BakedIEModel;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Connectors;
import blusunrize.immersiveengineering.common.blocks.metal.FeedthroughTileEntity.FeedthroughData;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static blusunrize.immersiveengineering.api.wires.WireApi.INFOS;
import static blusunrize.immersiveengineering.client.ClientUtils.mc;
import static blusunrize.immersiveengineering.common.blocks.metal.FeedthroughTileEntity.MIDDLE_STATE;
import static blusunrize.immersiveengineering.common.blocks.metal.FeedthroughTileEntity.WIRE;
import static net.minecraft.util.Direction.Axis.Y;

public class FeedthroughModel extends BakedIEModel
{
	public static final Cache<FeedthroughCacheKey, SpecificFeedthroughModel> CACHE = CacheBuilder.newBuilder()
			.expireAfterAccess(2, TimeUnit.MINUTES)
			.maximumSize(100)
			.build();

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData)
	{
		BlockState baseState = Blocks.STONE.getDefaultState();
		WireType wire = WireType.COPPER;
		Direction facing = Direction.NORTH;
		int offset = 1;
		Int2IntFunction colorMultiplier = i -> 0xffffffff;

		if(extraData.hasProperty(Model.FEEDTHROUGH))
			{
				FeedthroughData data = extraData.getData(Model.FEEDTHROUGH);
				assert (data!=null);
				baseState = data.baseState;
				wire = data.wire;
				facing = data.facing;
				offset = data.offset;
				colorMultiplier = data.colorMultiplier;
			}
		final Int2IntFunction colorMultiplierFinal = colorMultiplier;
		FeedthroughCacheKey key = new FeedthroughCacheKey(wire, baseState, offset, facing, MinecraftForgeClient.getRenderLayer(), colorMultiplier);
		try
		{
			return CACHE.get(key,
					() -> new SpecificFeedthroughModel(key, colorMultiplierFinal)).getQuads(state, side, rand);
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


		@Nonnull
		@Override
		public IBakedModel getModelWithOverrides(@Nonnull IBakedModel originalModel, ItemStack stack,
												 @Nullable World world, @Nullable LivingEntity entity)
		{
			Item connItem = Item.getItemFromBlock(Connectors.feedthrough);
			if(stack.getItem()==connItem)
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
		final BlockState baseState;
		final int offset;
		final Direction facing;
		final BlockRenderLayer layer;
		@Nullable
		final Int2IntMap usedColorMultipliers;
		@Nullable
		final Int2IntFunction allColorMultipliers;

		public FeedthroughCacheKey(WireType type, BlockState baseState, int offset, Direction facing,
								   BlockRenderLayer layer)
		{
			this.type = type;
			this.baseState = baseState;
			this.offset = offset;
			this.facing = facing;
			this.layer = layer;
			this.allColorMultipliers = null;
			this.usedColorMultipliers = new Int2IntOpenHashMap();
		}

		public FeedthroughCacheKey(WireType type, BlockState baseState, int offset, Direction facing,
								   BlockRenderLayer layer, Int2IntFunction colorMultiplier)
		{
			this.type = type;
			this.baseState = baseState;
			this.offset = offset;
			this.facing = facing;
			this.layer = layer;
			this.allColorMultipliers = colorMultiplier;
			this.usedColorMultipliers = null;
		}

		@Override
		public boolean equals(Object o)
		{
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;
			FeedthroughCacheKey that = (FeedthroughCacheKey)o;
			return offset==that.offset&&
					Objects.equals(type, that.type)&&
					baseState.equals(that.baseState)&&
					facing==that.facing&&
					Objects.equals(layer, that.layer)&&
					sameColorMultipliersAs(that);
		}

		private boolean sameColorMultipliersAs(FeedthroughCacheKey that)
		{
			if(that.usedColorMultipliers!=null&&this.usedColorMultipliers!=null)
				return this.usedColorMultipliers.equals(that.usedColorMultipliers);
			else if(that.usedColorMultipliers!=null&&this.allColorMultipliers!=null)
			{
				for(int i : that.usedColorMultipliers.keySet())
					if(this.allColorMultipliers.get(i)!=that.usedColorMultipliers.get(i))
						return false;
				return true;
			}
			else if(that.allColorMultipliers!=null&&this.usedColorMultipliers!=null)
				return that.sameColorMultipliersAs(this);
			else
				throw new IllegalStateException("Can't compare FeedthroughCacheKey's that use functions!");
		}

		@Override
		public int hashCode()
		{
			int ret = Utils.hashBlockstate(baseState);
			return 31*ret+Objects.hash(type, offset, facing, layer);
		}
	}

	private static class SpecificFeedthroughModel extends FeedthroughModel
	{
		private static final float[] WHITE = {1, 1, 1, 1};
		private static final Vec3d[] vertices = {
				new Vec3d(.75F, .001F, .75F), new Vec3d(.75F, .001F, .25F),
				new Vec3d(.25F, .001F, .25F), new Vec3d(.25F, .001F, .75F)
		};
		List<List<BakedQuad>> quads = new ArrayList<>(6);

		public SpecificFeedthroughModel(ItemStack stack)
		{
			WireType w = WireType.getValue(ItemNBTHelper.getString(stack, WIRE));
			BlockState state = NBTUtil.readBlockState(ItemNBTHelper.getTagCompound(stack, MIDDLE_STATE));
			init(new FeedthroughCacheKey(w, state, Integer.MAX_VALUE, Direction.NORTH, null),
					i -> mc().getItemColors().getColor(stack, i));
		}

		public SpecificFeedthroughModel(FeedthroughCacheKey key, Int2IntFunction colorMultiplier)
		{
			init(key, colorMultiplier);
		}

		private void init(FeedthroughCacheKey k, Int2IntFunction colorMultiplierBasic)
		{
			IBakedModel model = mc().getBlockRendererDispatcher().getBlockModelShapes()
					.getModel(k.baseState);
			if(colorMultiplierBasic==null)
			{
				ItemColors colors = mc().getItemColors();
				ItemStack stack = new ItemStack(k.baseState.getBlock(), 1);
				colorMultiplierBasic = (i) -> colors.getColor(stack, i);
			}
			Int2IntFunction colorMultiplierFinal = colorMultiplierBasic;
			Int2IntFunction colorMultiplier = i -> {
				int ret = colorMultiplierFinal.get(i);
				k.usedColorMultipliers.put(i, ret);
				return ret;
			};
			for(int j = 0; j < 7; j++)
			{
				Direction side = j < 6?Direction.VALUES[j]: null;
				Direction facing = k.facing;
				switch(k.offset)
				{
					case 0:
						if(k.layer==null||k.baseState.getBlock().canRenderInLayer(k.baseState, k.layer))
						{
							Function<BakedQuad, BakedQuad> tintTransformer = ApiUtils.transformQuad(new Matrix4(),
									DefaultVertexFormats.ITEM, colorMultiplier);
							quads.add(model.getQuads(k.baseState, side, Utils.RAND).stream().map(tintTransformer)
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
						all.addAll(model.getQuads(k.baseState, side, Utils.RAND).stream().map(tintTransformer)
								.collect(Collectors.toCollection(ArrayList::new)));
						quads.add(all);
						break;
				}
				if(quads.size() <= j)
					quads.add(ImmutableList.of());
			}
		}

		private List<BakedQuad> getConnQuads(Direction facing, Direction side, WireType type, Matrix4 mat)
		{
			//connector model+feedthrough border
			WireApi.FeedthroughModelInfo info = INFOS.get(type);
			mat.translate(.5, .5, .5);
			if(facing.getAxis()==Y)
			{
				if(facing==Direction.UP)
					mat.rotate(Math.PI, 1, 0, 0);
			}
			else
			{
				Direction rotateAround = facing.rotateAround(Y);
				mat.rotate(Math.PI/2, rotateAround.getXOffset(), rotateAround.getYOffset(),
						rotateAround.getZOffset());
			}
			mat.translate(-.5, -.5, -.5);
			List<BakedQuad> conn = new ArrayList<>(info.model.getQuads(null, side, Utils.RAND));
			if(side==facing)
				conn.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, vertices, Direction.UP, info.tex, info.uvs, WHITE, false));
			Function<BakedQuad, BakedQuad> transf = ApiUtils.transformQuad(mat, null,
					null);//I hope no one uses tint index for connectors
			if(transf!=null)
				return conn.stream().map(transf).collect(Collectors.toList());
			else
				return conn;
		}
	}
}
