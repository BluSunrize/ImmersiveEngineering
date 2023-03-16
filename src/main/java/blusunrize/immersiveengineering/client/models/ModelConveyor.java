/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.conveyor.*;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.IConveyorBlockEntity;
import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorModelRender.RenderContext;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.ModelUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalBE;
import blusunrize.immersiveengineering.common.blocks.metal.ConveyorBlock;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.ForgeRenderTypes;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class ModelConveyor<T extends IConveyorBelt> extends BakedIEModel
{
	private static final ModelProperty<IConveyorBelt> CONVEYOR_MODEL_DATA = new ModelProperty<>();
	public static final ResourceLocation[] rl_casing = {
			new ResourceLocation(ImmersiveEngineering.MODID, "block/conveyor/casing_top"),
			new ResourceLocation(ImmersiveEngineering.MODID, "block/conveyor/casing_side"),
			new ResourceLocation(ImmersiveEngineering.MODID, "block/conveyor/casing_walls"),
			new ResourceLocation(ImmersiveEngineering.MODID, "block/conveyor/casing_full")
	};

	private final Map<RenderType, Cache<Object, List<BakedQuad>>> modelCache = new HashMap<>();

	{
		modelCache.put(RenderType.translucent(), CacheBuilder.newBuilder().maximumSize(100).build());
		modelCache.put(RenderType.cutout(), CacheBuilder.newBuilder().maximumSize(100).build());
		modelCache.put(null, CacheBuilder.newBuilder().maximumSize(100).build());
	}

	private final IConveyorType<T> type;
	private final Block fallbackCover;

	public ModelConveyor(IConveyorType<T> type, Block fallbackCover)
	{
		this.type = type;
		this.fallbackCover = fallbackCover;
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(
			@Nullable BlockState blockState,
			@Nullable Direction side,
			@Nonnull RandomSource rand,
			@Nonnull ModelData extraData,
			@Nullable RenderType layer
	)
	{
		if(side!=null)
			return List.of();
		Direction facing = Direction.NORTH;
		T conveyor = null;
		if(blockState!=null)
		{
			facing = blockState.getValue(IEProperties.FACING_HORIZONTAL);
			if(extraData.has(CONVEYOR_MODEL_DATA))
				conveyor = (T)extraData.get(CONVEYOR_MODEL_DATA);
			if(conveyor!=null)
			{
				BlockEntity tile = conveyor.getBlockEntity();
				if(tile instanceof IDirectionalBE)
					facing = ((IDirectionalBE)tile).getFacing();
			}
		}
		IConveyorModelRender<T> clientData = ClientConveyors.getData(type);
		IConveyorModelRender.RenderContext<T> context = new RenderContext<>(type, conveyor, fallbackCover);
		Object key = clientData.getModelCacheKey(context);
		Cache<Object, List<BakedQuad>> layerCache = modelCache.get(layer);
		List<BakedQuad> cachedQuads = layerCache.getIfPresent(key);
		if(cachedQuads==null)
		{
			cachedQuads = Collections.synchronizedList(Lists.newArrayList());
			Transformation matrix = ClientUtils.rotateTo(facing);
			matrix = clientData.modifyBaseRotationMatrix(matrix);
			ConveyorDirection conDir = conveyor!=null?conveyor.getConveyorDirection(): ConveyorDirection.HORIZONTAL;
			boolean[] walls = new boolean[]{
					clientData.shouldRenderWall(facing, ConveyorWall.LEFT, context),
					clientData.shouldRenderWall(facing, ConveyorWall.RIGHT, context)
			};
			TextureAtlasSprite tex_conveyor = ClientUtils.getSprite(context.isActiveOr(false)?clientData.getActiveTexture(): clientData.getInactiveTexture());
			DyeColor colourStripes = null;
			TextureAtlasSprite tex_conveyor_colour = null;
			if(conveyor!=null&&(colourStripes = conveyor.getDyeColour())!=null)
				tex_conveyor_colour = ClientUtils.getSprite(clientData.getColouredStripesTexture());
			if(layer==null||layer==RenderType.cutout())
				cachedQuads.addAll(getBaseConveyor(facing, 1, matrix, conDir, tex_conveyor, walls, new boolean[]{true, true}, tex_conveyor_colour, colourStripes));
			cachedQuads = clientData.modifyQuads(cachedQuads, context, layer);
			layerCache.put(key, ImmutableList.copyOf(cachedQuads));
		}
		return ImmutableList.copyOf(cachedQuads);
	}

	public static List<BakedQuad> getBaseConveyor(Direction facing, float length, Transformation matrix, ConveyorDirection conDir,
												  TextureAtlasSprite tex_conveyor, boolean[] walls, boolean[] corners,
												  TextureAtlasSprite tex_conveyor_colour, @Nullable DyeColor stripeColour)
	{
		List<BakedQuad> quads = new ArrayList<>();

		TextureAtlasSprite tex_casing1 = ClientUtils.getSprite(rl_casing[1]);
		TextureAtlasSprite tex_casing2 = ClientUtils.getSprite(rl_casing[2]);
		float[] colour = {1, 1, 1, 1};
		float[] colourStripes = {1, 1, 1, 1};
		if(stripeColour!=null)
			System.arraycopy(stripeColour.getTextureDiffuseColors(), 0, colourStripes, 0, 3);
		final TextureAtlasSprite topTexture = tex_conveyor_colour!=null?tex_conveyor_colour: tex_casing2;
		final float[] topColor = stripeColour!=null?colourStripes: colour;

		// Establish all the vertices we will use
		double zLength = 1-length;
		Vec3[] bottom = {new Vec3(0, 0, zLength), new Vec3(0, 0, 1), new Vec3(1, 0, 1), new Vec3(1, 0, zLength)};
		Vec3[] bottomBelt = {new Vec3(.0625, 0, zLength), new Vec3(.0625, 0, 1), new Vec3(.9375, 0, 1), new Vec3(.9375, 0, zLength)};
		Vec3[] top = {new Vec3(0, .125, zLength), new Vec3(0, .125, 1), new Vec3(1, .125, 1), new Vec3(1, .125, zLength)};
		Vec3[] topBelt = {new Vec3(.0625, .125, zLength), new Vec3(.0625, .125, 1), new Vec3(.9375, .125, 1), new Vec3(.9375, .125, zLength)};
		Vec3[] corner = {new Vec3(0, .1875, zLength), new Vec3(0, .1875, 1), new Vec3(1, .1875, 1), new Vec3(1, .1875, zLength)};
		Vec3[] cornerBelt = {new Vec3(.0625, .1875, zLength), new Vec3(.0625, .1875, 1), new Vec3(.9375, .1875, 1), new Vec3(.9375, .1875, zLength)};
		double zLengthWall = zLength+.0625;
		Vec3[] wallOuter = {new Vec3(0, .125, zLengthWall), new Vec3(0, .125, .9375), new Vec3(1, .125, .9375), new Vec3(1, .125, zLengthWall)};
		Vec3[] wallInner = {new Vec3(.0625, .125, zLengthWall), new Vec3(.0625, .125, .9375), new Vec3(.9375, .125, .9375), new Vec3(.9375, .125, zLengthWall)};
		Vec3[] wallOuterTop = {new Vec3(0, .1875, zLengthWall), new Vec3(0, .1875, .9375), new Vec3(1, .1875, .9375), new Vec3(1, .1875, zLengthWall)};
		Vec3[] wallInnerTop = {new Vec3(.0625, .1875, zLengthWall), new Vec3(.0625, .1875, .9375), new Vec3(.9375, .1875, .9375), new Vec3(.9375, .1875, zLengthWall)};

		//Handle sloping
		if(conDir!=ConveyorDirection.HORIZONTAL)
		{
			int[] elevatedVerts = conDir==ConveyorDirection.UP?new int[]{0, 3}: new int[]{1, 2};
			for(int i : elevatedVerts)
				for(Vec3[] array : new Vec3[][]{bottom, bottomBelt, top, topBelt, corner, cornerBelt})
					array[i] = array[i].add(0, length, 0);
			for(Vec3[] array : new Vec3[][]{wallOuter, wallInner, wallOuterTop, wallInnerTop})
				for(int i = 0; i < array.length; i++)
				{
					double f = (i==0||i==3)?(conDir==ConveyorDirection.UP?length-.0625: .0625): (conDir==ConveyorDirection.UP?.0625: length-.0625);
					array[i] = array[i].add(0, f, 0);
				}
		}

		// basic shape
		quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, bottomBelt), Utils.rotateFacingTowardsDir(Direction.DOWN, facing), tex_conveyor, new double[]{1, 0, 15, length*16}, colour, true));
		quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, bottom), Utils.rotateFacingTowardsDir(Direction.DOWN, facing), tex_casing2, new double[]{0, 0, 16, length*16}, colour, true));
		quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, top), Utils.rotateFacingTowardsDir(Direction.UP, facing), tex_conveyor, new double[]{0, length*16, 16, 0}, colour, false));
		quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, bottom[0], bottom[1], top[1], top[0]), Utils.rotateFacingTowardsDir(Direction.WEST, facing), tex_casing1, new double[]{0, 0, 2, length*16}, colour, false));
		quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, bottom[2], bottom[3], top[3], top[2]), Utils.rotateFacingTowardsDir(Direction.EAST, facing), tex_casing1, new double[]{0, 0, 2, length*16}, colour, false));
		double frontUMax = (1-length)*16;
		quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, bottomBelt[0], topBelt[0], topBelt[3], bottomBelt[3]), facing, tex_conveyor, new double[]{1, frontUMax+2, 15, frontUMax}, colour, false));
		quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, bottom[0], top[0], top[3], bottom[3]), facing, tex_casing2, new double[]{0, 2, 16, 0}, colour, false));
		quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, bottomBelt[1], topBelt[1], topBelt[2], bottomBelt[2]), facing.getOpposite(), tex_conveyor, new double[]{1, 0, 15, 2}, colour, true));
		quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, bottom[1], top[1], top[2], bottom[2]), facing.getOpposite(), tex_casing2, new double[]{0, 0, 16, 2}, colour, true));

		// back corners
		if(corners[0])
		{
			// top
			quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, wallOuterTop[1], corner[1], cornerBelt[1], wallInnerTop[1]), Utils.rotateFacingTowardsDir(Direction.UP, facing), topTexture, new double[]{0, 1, 1, 0}, topColor, false));
			quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, wallOuterTop[2], corner[2], cornerBelt[2], wallInnerTop[2]), Utils.rotateFacingTowardsDir(Direction.UP, facing), topTexture, new double[]{15, 1, 16, 0}, topColor, true));
			// front & back
			quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, top[1], corner[1], cornerBelt[1], topBelt[1]), facing.getOpposite(), tex_casing2, new double[]{0, 2, 1, 3}, colour, true));
			quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, top[2], corner[2], cornerBelt[2], topBelt[2]), facing.getOpposite(), tex_casing2, new double[]{15, 2, 16, 3}, colour, false));
			quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, wallOuter[1], wallOuterTop[1], wallInnerTop[1], wallInner[1]), facing.getOpposite(), tex_casing2, new double[]{0, 2, 1, 3}, colour, false));
			quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, wallOuter[2], wallOuterTop[2], wallInnerTop[2], wallInner[2]), facing.getOpposite(), tex_casing2, new double[]{15, 2, 16, 3}, colour, true));
			// sides
			quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, wallOuter[1], top[1], corner[1], wallOuterTop[1]), Utils.rotateFacingTowardsDir(Direction.WEST, facing), tex_casing1, new double[]{0, 2, 1, 3}, colour, false));
			quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, wallInner[1], topBelt[1], cornerBelt[1], wallInnerTop[1]), Utils.rotateFacingTowardsDir(Direction.EAST, facing), tex_casing1, new double[]{15, 2, 16, 3}, colour, true));
			quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, top[2], wallOuter[2], wallOuterTop[2], corner[2]), Utils.rotateFacingTowardsDir(Direction.EAST, facing), tex_casing1, new double[]{15, 2, 16, 3}, colour, false));
			quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, topBelt[2], wallInner[2], wallInnerTop[2], cornerBelt[2]), Utils.rotateFacingTowardsDir(Direction.WEST, facing), tex_casing1, new double[]{0, 2, 1, 3}, colour, true));
		}
		// front corners
		if(corners[1])
		{
			// top
			quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, wallOuterTop[3], corner[3], cornerBelt[3], wallInnerTop[3]), Utils.rotateFacingTowardsDir(Direction.UP, facing), topTexture, new double[]{0, 16, 1, 15}, topColor, false));
			quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, wallOuterTop[0], corner[0], cornerBelt[0], wallInnerTop[0]), Utils.rotateFacingTowardsDir(Direction.UP, facing), topTexture, new double[]{15, 16, 16, 15}, topColor, true));
			// front & back
			quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, top[3], corner[3], cornerBelt[3], topBelt[3]), facing.getOpposite(), tex_casing2, new double[]{0, 2, 1, 3}, colour, true));
			quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, top[0], corner[0], cornerBelt[0], topBelt[0]), facing.getOpposite(), tex_casing2, new double[]{15, 2, 16, 3}, colour, false));
			quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, wallOuter[3], wallOuterTop[3], wallInnerTop[3], wallInner[3]), facing.getOpposite(), tex_casing2, new double[]{0, 2, 1, 3}, colour, false));
			quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, wallOuter[0], wallOuterTop[0], wallInnerTop[0], wallInner[0]), facing.getOpposite(), tex_casing2, new double[]{15, 2, 16, 3}, colour, true));
			// sides
			quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, wallOuter[3], top[3], corner[3], wallOuterTop[3]), Utils.rotateFacingTowardsDir(Direction.WEST, facing), tex_casing1, new double[]{0, 2, 1, 3}, colour, false));
			quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, wallInner[3], topBelt[3], cornerBelt[3], wallInnerTop[3]), Utils.rotateFacingTowardsDir(Direction.EAST, facing), tex_casing1, new double[]{15, 2, 16, 3}, colour, true));
			quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, top[0], wallOuter[0], wallOuterTop[0], corner[0]), Utils.rotateFacingTowardsDir(Direction.EAST, facing), tex_casing1, new double[]{15, 2, 16, 3}, colour, false));
			quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, topBelt[0], wallInner[0], wallInnerTop[0], cornerBelt[0]), Utils.rotateFacingTowardsDir(Direction.WEST, facing), tex_casing1, new double[]{0, 2, 1, 3}, colour, true));
		}

		// left wall
		if(walls[0])
		{
			quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, wallOuterTop[0], wallOuterTop[1], wallInnerTop[1], wallInnerTop[0]), Utils.rotateFacingTowardsDir(Direction.UP, facing), topTexture, new double[]{0, 15, 1, 1}, topColor, false));
			quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, wallOuter[0], wallOuter[1], wallOuterTop[1], wallOuterTop[0]), Utils.rotateFacingTowardsDir(Direction.WEST, facing), tex_casing1, new double[]{2, 15, 3, 1}, colour, false));
			quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, wallInner[0], wallInner[1], wallInnerTop[1], wallInnerTop[0]), Utils.rotateFacingTowardsDir(Direction.EAST, facing), tex_casing1, new double[]{2, 15, 3, 1}, colour, true));
		}
		// right wall
		if(walls[1])
		{
			quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, wallOuterTop[2], wallOuterTop[3], wallInnerTop[3], wallInnerTop[2]), Utils.rotateFacingTowardsDir(Direction.UP, facing), topTexture, new double[]{15, 15, 16, 1}, topColor, false));
			quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, wallInner[3], wallInner[2], wallInnerTop[2], wallInnerTop[3]), Utils.rotateFacingTowardsDir(Direction.WEST, facing), tex_casing1, new double[]{2, 15, 3, 1}, colour, false));
			quads.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, wallOuter[3], wallOuter[2], wallOuterTop[2], wallOuterTop[3]), Utils.rotateFacingTowardsDir(Direction.EAST, facing), tex_casing1, new double[]{2, 15, 3, 1}, colour, true));
		}
		return quads;
	}

	@Override
	public boolean useAmbientOcclusion()
	{
		return true;
	}

	@Override
	public boolean isGui3d()
	{
		return true;
	}

	@Override
	public boolean isCustomRenderer()
	{
		return false;
	}

	TextureAtlasSprite tex_particle;

	@Nonnull
	@Override
	public TextureAtlasSprite getParticleIcon()
	{
		if(tex_particle==null)
			tex_particle = ClientUtils.getSprite(new ResourceLocation(ImmersiveEngineering.MODID, "block/conveyor/off"));
		return tex_particle;
	}

	@Nonnull
	@Override
	public ItemTransforms getTransforms()
	{
		return ItemTransforms.NO_TRANSFORMS;
	}

	@Nonnull
	@Override
	public ItemOverrides getOverrides()
	{
		return overrideList;
	}

	private static final ItemOverrides overrideList = new ItemOverrides()
	{
		static record Key(IConveyorType<?> type, Block defaultCover)
		{
		}

		private final LoadingCache<Key, BakedModel> itemModelCache = CacheBuilder.newBuilder()
				.maximumSize(100)
				.build(CacheLoader.from(key -> new ModelConveyor<>(key.type(), key.defaultCover())));

		@Override
		public BakedModel resolve(@Nonnull BakedModel originalModel, @Nonnull ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int unused)
		{
			if(stack.getItem() instanceof BlockItem asBlock)
			{
				Block b = asBlock.getBlock();
				IConveyorType<?> conveyorType = ConveyorHandler.getType(b);
				if(conveyorType!=null)
				{
					Block defaultCover = ConveyorBlock.getCover(stack);
					return itemModelCache.getUnchecked(new Key(conveyorType, defaultCover));
				}
			}
			return Minecraft.getInstance().getModelManager().getMissingModel();
		}
	};

	// TODO this needs to move to JSON at some points, like all other transforms
	private static final Map<ItemDisplayContext, Transformation> TRANSFORMATION_MAP;

	static
	{
		Map<ItemDisplayContext, Matrix4> matrixMap = new EnumMap<>(ItemDisplayContext.class);
		matrixMap.put(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, new Matrix4().scale(.5, .5, .5).translate(0, .25, 0).rotate(Math.toRadians(-45), 0, 1, 0));
		matrixMap.put(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, new Matrix4().scale(.5, .5, .5).translate(0, .25, 0).rotate(Math.toRadians(-45), 0, 1, 0));
		matrixMap.put(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, new Matrix4().translate(0, .0625, -.125).scale(.3125, .3125, .3125).rotate(Math.toRadians(30), 1, 0, 0).rotate(Math.toRadians(130), 0, 1, 0));
		matrixMap.put(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, new Matrix4().translate(0, .0625, -.125).scale(.3125, .3125, .3125).rotate(Math.toRadians(30), 1, 0, 0).rotate(Math.toRadians(130), 0, 1, 0));
		matrixMap.put(ItemDisplayContext.GUI, new Matrix4().scale(.625, .625, .625).rotate(Math.toRadians(-45), 0, 1, 0).rotate(Math.toRadians(-20), 0, 0, 1).rotate(Math.toRadians(20), 1, 0, 0));
		matrixMap.put(ItemDisplayContext.FIXED, new Matrix4().scale(.625, .625, .625).rotate(Math.PI, 0, 1, 0).translate(0, 0, .3125));
		matrixMap.put(ItemDisplayContext.GROUND, new Matrix4().scale(.25, .25, .25));
		TRANSFORMATION_MAP = matrixMap.entrySet()
				.stream()
				.map(e -> Pair.of(e.getKey(), e.getValue().toTransformationMatrix()))
				.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
	}

	@Nonnull
	@Override
	public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack stack, boolean applyLeftHandTransform)
	{
		Transformation transform = TRANSFORMATION_MAP.get(transformType);
		if(transform!=null)
		{
			Vector3f translate = transform.getTranslation();
			stack.translate(translate.x(), translate.y(), translate.z());
			stack.mulPose(transform.getLeftRotation());
			Vector3f scale = transform.getScale();
			stack.scale(scale.x(), scale.y(), scale.z());
			stack.mulPose(transform.getRightRotation());
		}
		return this;
	}

	@Nonnull
	@Override
	public ModelData getModelData(@Nonnull BlockAndTintGetter world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull ModelData tileData)
	{
		Block b = state.getBlock();
		IConveyorType<?> conveyorName = ConveyorHandler.getType(b);
		if(conveyorName==null)
			return tileData;
		BlockEntity bEntity = world.getBlockEntity(pos);
		if(!(bEntity instanceof IConveyorBlockEntity<?>))
			return tileData;
		return tileData.derive()
				.with(CONVEYOR_MODEL_DATA, ConveyorHandler.getConveyor(conveyorName, bEntity))
				.build();
	}

	@Override
	public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous)
	{
		return List.of(ForgeRenderTypes.ITEM_LAYERED_CUTOUT.get());
	}

	@Override
	public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data)
	{
		return ChunkRenderTypeSet.of(RenderType.cutout(), RenderType.translucent());
	}

	@EventBusSubscriber(value = Dist.CLIENT, modid = Lib.MODID, bus = Bus.MOD)
	public record RawConveyorModel(IConveyorType<?> type) implements IUnbakedGeometry<RawConveyorModel>
	{
		private static final AtomicBoolean REFRESHED_SINCE_BAKE = new AtomicBoolean(false);

		@Override
		public BakedModel bake(
				IGeometryBakingContext context,
				ModelBaker bakery,
				Function<Material, TextureAtlasSprite> spriteGetter,
				ModelState modelState,
				ItemOverrides overrides,
				ResourceLocation modelLocation
		)
		{
			if(!REFRESHED_SINCE_BAKE.getAndSet(true))
				for(final var conveyorType : ConveyorHandler.getConveyorTypes())
					ClientConveyors.getData(conveyorType).updateCachedModels(bakery, spriteGetter);
			return new ModelConveyor<>(type, Blocks.AIR);
		}

		@SubscribeEvent
		public static void onModelBakingDone(ModelEvent.BakingCompleted ev)
		{
			REFRESHED_SINCE_BAKE.set(false);
		}
	}

	public static class ConveyorLoader implements IGeometryLoader<RawConveyorModel>
	{
		public static final ResourceLocation LOCATION = new ResourceLocation(ImmersiveEngineering.MODID, "models/conveyor");
		public static final String TYPE_KEY = "conveyorType";

		@Nonnull
		@Override
		public RawConveyorModel read(JsonObject modelContents, @Nonnull JsonDeserializationContext deserializationContext)
		{
			String typeName = modelContents.get(TYPE_KEY).getAsString();
			IConveyorType<?> type = ConveyorHandler.getConveyorType(new ResourceLocation(typeName));
			return new RawConveyorModel(Objects.requireNonNull(type));
		}
	}
}
