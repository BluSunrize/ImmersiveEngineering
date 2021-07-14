/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.connection;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.client.ICacheKeyProvider;
import blusunrize.immersiveengineering.api.utils.QuadTransformer;
import blusunrize.immersiveengineering.api.utils.client.CombinedModelData;
import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import blusunrize.immersiveengineering.api.wires.WireApi;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.client.models.BakedIEModel;
import blusunrize.immersiveengineering.client.models.connection.FeedthroughModel.FeedthroughCacheKey;
import blusunrize.immersiveengineering.client.utils.ModelUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Connectors;
import blusunrize.immersiveengineering.common.blocks.metal.FeedthroughTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.FeedthroughTileEntity.FeedthroughData;
import blusunrize.immersiveengineering.common.util.DirectionUtils;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

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

public class FeedthroughModel extends BakedIEModel implements ICacheKeyProvider<FeedthroughCacheKey>
{
	public static final Cache<FeedthroughCacheKey, SpecificFeedthroughModel> CACHE = CacheBuilder.newBuilder()
			.expireAfterAccess(2, TimeUnit.MINUTES)
			.maximumSize(100)
			.build();
	private static final ModelProperty<FeedthroughData> FEEDTHROUGH = new ModelProperty<>();

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData)
	{
		FeedthroughCacheKey key = getKey(state, side, rand, extraData);
		SpecificFeedthroughModel ret = CACHE.getIfPresent(key);
		if(ret==null)
		{
			ret = new SpecificFeedthroughModel(key, s -> key.defaultColorMultipliers);
			CACHE.put(key, ret);
		}
		return ret.getQuads(state, side, rand);
	}

	@Nonnull
	@Override
	public IModelData getModelData(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData)
	{
		List<IModelData> ret = new ArrayList<>();
		ret.add(tileData);
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof FeedthroughTileEntity)
		{
			FeedthroughTileEntity feedthrough = (FeedthroughTileEntity)te;
			int color = Minecraft.getInstance().getBlockColors().getColor(feedthrough.stateForMiddle, world, pos, 0);
			FeedthroughData d = new FeedthroughData(
					feedthrough.stateForMiddle,
					feedthrough.reference,
					state.get(IEProperties.FACING_ALL),
					feedthrough.offset,
					color
			);
			ret.add(new SinglePropertyModelData<>(d, FEEDTHROUGH));
		}
		return CombinedModelData.combine(ret.toArray(new IModelData[0]));
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
		return ModelLoader.White.instance();
	}

	private static final ItemCameraTransforms transform = new ItemCameraTransforms(
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

	@Nonnull
	@Override
	public FeedthroughCacheKey getKey(
			@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData
	)
	{
		BlockState baseState = Blocks.STONE.getDefaultState();
		WireType wire = WireType.COPPER;
		Direction facing = Direction.NORTH;
		int offset = 1;
		int colorMultiplier = 0xffffffff;

		if(extraData.hasProperty(FEEDTHROUGH))
		{
			FeedthroughData data = extraData.getData(FEEDTHROUGH);
			assert (data!=null);
			baseState = data.baseState;
			wire = data.wire;
			facing = data.facing;
			offset = data.offset;
			colorMultiplier = data.colorMultiplier;
		}
		return new FeedthroughCacheKey(
				wire, baseState, offset, facing, MinecraftForgeClient.getRenderLayer(), colorMultiplier
		);
	}

	private static class FeedthroughItemOverride extends ItemOverrideList
	{
		private static final Cache<ItemStack, FeedthroughModel> ITEM_MODEL_CACHE = CacheBuilder.newBuilder()
				.maximumSize(100)
				.expireAfterAccess(60, TimeUnit.SECONDS)
				.build();

		@Nonnull
		@Override
		public IBakedModel getOverrideModel(@Nonnull IBakedModel originalModel, ItemStack stack,
											@Nullable ClientWorld world, @Nullable LivingEntity entity)
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

	public static class FeedthroughCacheKey
	{
		final WireType type;
		final BlockState baseState;
		final int offset;
		final Direction facing;
		final RenderType layer;
		final int defaultColorMultipliers;
		final Int2IntMap specificColorMultipliers;

		public FeedthroughCacheKey(WireType type, BlockState baseState, int offset, Direction facing,
								   RenderType layer)
		{
			this(type, baseState, offset, facing, layer, -1);
		}

		public FeedthroughCacheKey(WireType type, BlockState baseState, int offset, Direction facing,
								   RenderType layer, int colorMultiplier)
		{
			this.type = type;
			this.baseState = baseState;
			this.offset = offset;
			this.facing = facing;
			this.layer = layer;
			this.defaultColorMultipliers = colorMultiplier;
			this.specificColorMultipliers = new Int2IntOpenHashMap();
		}

		@Override
		public boolean equals(Object o)
		{
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;
			FeedthroughCacheKey that = (FeedthroughCacheKey)o;
			return offset==that.offset&&
					defaultColorMultipliers==that.defaultColorMultipliers&&
					type.equals(that.type)&&
					baseState.equals(that.baseState)&&
					facing==that.facing&&
					Objects.equals(layer, that.layer);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(type, baseState, offset, facing, layer, defaultColorMultipliers);
		}
	}

	private static class SpecificFeedthroughModel extends FeedthroughModel
	{
		private static final float[] WHITE = {1, 1, 1, 1};
		private static final Vector3d[] vertices = {
				new Vector3d(.75F, .001F, .75F), new Vector3d(.75F, .001F, .25F),
				new Vector3d(.25F, .001F, .25F), new Vector3d(.25F, .001F, .75F)
		};
		List<List<BakedQuad>> quads = new ArrayList<>(6);

		public SpecificFeedthroughModel(ItemStack stack)
		{
			WireType w = WireType.getValue(ItemNBTHelper.getString(stack, WIRE));
			BlockState state = NBTUtil.readBlockState(ItemNBTHelper.getTagCompound(stack, MIDDLE_STATE));
			if(state.getBlock()==Blocks.AIR)
				state = Blocks.BOOKSHELF.getDefaultState();
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
				colorMultiplierBasic = i -> colors.getColor(stack, i);
			}
			Int2IntFunction colorMultiplierFinal = colorMultiplierBasic;
			Int2IntFunction colorMultiplier = i -> {
				int ret = colorMultiplierFinal.get(i);
				k.specificColorMultipliers.put(i, ret);
				return ret;
			};
			for(int j = 0; j < 7; j++)
			{
				Direction side = j < 6?DirectionUtils.VALUES[j]: null;
				Direction facing = k.facing;
				switch(k.offset)
				{
					case 0:
						if(k.layer==null||RenderTypeLookup.canRenderInLayer(k.baseState, k.layer))
						{
							Function<BakedQuad, BakedQuad> tintTransformer = new QuadTransformer(TransformationMatrix.identity(),
									colorMultiplier);
							quads.add(model.getQuads(k.baseState, side, Utils.RAND, EmptyModelData.INSTANCE)
									.stream()
									.map(tintTransformer)
									.collect(Collectors.toCollection(ArrayList::new)));
						}
						break;
					case 1:
						facing = facing.getOpposite();
					case -1:
						if(k.layer==RenderType.getSolid())
							quads.add(getConnQuads(facing, side, k.type, new Matrix4()));
						break;
					case Integer.MAX_VALUE:
						Matrix4 mat = new Matrix4();
						mat.translate(0, 0, 1);
						List<BakedQuad> all = new ArrayList<>(getConnQuads(facing, side, k.type, mat));
						mat = new Matrix4();
						mat.translate(0, 0, -1);
						all.addAll(getConnQuads(facing.getOpposite(), side, k.type, mat));
						Function<BakedQuad, BakedQuad> tintTransformer = new QuadTransformer(TransformationMatrix.identity(),
								colorMultiplier);
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
				Direction rotateAround = DirectionUtils.rotateAround(facing, Y);
				mat.rotate(Math.PI/2, rotateAround.getXOffset(), rotateAround.getYOffset(),
						rotateAround.getZOffset());
			}
			mat.translate(-.5, -.5, -.5);
			IBakedModel model = mc().getBlockRendererDispatcher().getBlockModelShapes()
					.getModel(info.conn.get().with(IEProperties.FACING_ALL, Direction.DOWN));
			List<BakedQuad> conn = new ArrayList<>(model.getQuads(null, side, Utils.RAND, EmptyModelData.INSTANCE));
			if(side==facing)
				conn.add(ModelUtils.createBakedQuad(DefaultVertexFormats.BLOCK, vertices, Direction.UP, info.tex, info.uvs, WHITE, false));
			Function<BakedQuad, BakedQuad> transf = new QuadTransformer(new TransformationMatrix(mat.toMatrix4f()), null);//I hope no one uses tint index for connectors
			if(transf!=null)
				return conn.stream().map(transf).collect(Collectors.toList());
			else
				return conn;
		}

		@Nonnull
		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData)
		{
			return quads.get(side==null?6: side.getIndex());
		}
	}
}
