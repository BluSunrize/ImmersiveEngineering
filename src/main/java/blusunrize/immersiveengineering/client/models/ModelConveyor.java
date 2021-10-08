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
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorBelt;
import blusunrize.immersiveengineering.api.utils.client.CombinedModelData;
import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.ModelUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

@SuppressWarnings("deprecation")
public class ModelConveyor extends BakedIEModel
{
	private static final ModelProperty<IConveyorBelt> CONVEYOR_MODEL_DATA = new ModelProperty<>();
	public static HashMap<String, List<BakedQuad>> modelCache = new HashMap<>();
	public static ResourceLocation[] rl_casing = {
			new ResourceLocation(ImmersiveEngineering.MODID, "block/conveyor/casing_top"),
			new ResourceLocation(ImmersiveEngineering.MODID, "block/conveyor/casing_side"),
			new ResourceLocation(ImmersiveEngineering.MODID, "block/conveyor/casing_walls"),
			new ResourceLocation(ImmersiveEngineering.MODID, "block/conveyor/casing_full")
	};

	@Nullable
	private final IConveyorBelt defaultBelt;

	public ModelConveyor(@Nullable IConveyorBelt defaultBelt)
	{
		this.defaultBelt = defaultBelt;
	}

	public ModelConveyor()
	{
		this(null);
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData)
	{
		BlockEntity tile = null;
		String key = "default";
		Direction facing = Direction.NORTH;
		IConveyorBelt conveyor = defaultBelt;
		if(blockState==null)
			key = conveyor!=null?ConveyorHandler.reverseClassRegistry.get(conveyor.getClass()).toString(): "immersiveengineering:conveyor";
		else
		{
			facing = blockState.getValue(IEProperties.FACING_HORIZONTAL);
			if(extraData.hasProperty(CONVEYOR_MODEL_DATA))
				conveyor = extraData.getData(CONVEYOR_MODEL_DATA);
			if(extraData.hasProperty(PrivateProperties.TILEENTITY_PASSTHROUGH))
			{
				tile = extraData.getData(PrivateProperties.TILEENTITY_PASSTHROUGH);
				if(tile instanceof IDirectionalTile)
					facing = ((IDirectionalTile)tile).getFacing();
			}
			if(conveyor==null)
				conveyor = ConveyorHandler.getConveyor(new ResourceLocation(key), tile);
			if(conveyor!=null&&tile!=null)
				key = conveyor.getModelCacheKey();
		}
		List<BakedQuad> cachedQuads = modelCache.get(key);
		if(cachedQuads==null)
		{
			cachedQuads = Collections.synchronizedList(Lists.newArrayList());
			Transformation matrix = ClientUtils.rotateTo(facing);
			if(conveyor!=null)
				matrix = conveyor.modifyBaseRotationMatrix(matrix);
			ConveyorDirection conDir = conveyor!=null?conveyor.getConveyorDirection(): ConveyorDirection.HORIZONTAL;
			boolean[] walls = conveyor!=null&&tile!=null?new boolean[]{conveyor.renderWall(facing, 0), conveyor.renderWall(facing, 1)}: new boolean[]{true, true};
			TextureAtlasSprite tex_conveyor = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(MissingTextureAtlasSprite.getLocation());
			TextureAtlasSprite tex_conveyor_colour = null;
			DyeColor colourStripes = null;
			if(conveyor!=null)
			{
				tex_conveyor = ClientUtils.getSprite(tile!=null?(conveyor.isActive()?conveyor.getActiveTexture(): conveyor.getInactiveTexture()): conveyor.getActiveTexture());
				if((colourStripes = conveyor.getDyeColour())!=null)
					tex_conveyor_colour = ClientUtils.getSprite(conveyor.getColouredStripesTexture());
			}
			cachedQuads.addAll(getBaseConveyor(facing, 1, matrix, conDir, tex_conveyor, walls, new boolean[]{true, true}, tex_conveyor_colour, colourStripes));
			if(conveyor!=null)
				cachedQuads = conveyor.modifyQuads(cachedQuads);
			modelCache.put(key, ImmutableList.copyOf(cachedQuads));
		}
		return ImmutableList.copyOf(cachedQuads);
	}

	public static List<BakedQuad> getBaseConveyor(Direction facing, float length, Transformation matrix, ConveyorDirection conDir,
												  TextureAtlasSprite tex_conveyor, boolean[] walls, boolean[] corners,
												  TextureAtlasSprite tex_conveyor_colour, @Nullable DyeColor stripeColour)
	{
		List<BakedQuad> quads = new ArrayList<>();

		Vec3[] vertices = {new Vec3(.0625f, 0, 1-length), new Vec3(.0625f, 0, 1), new Vec3(.9375f, 0, 1), new Vec3(.9375f, 0, 1-length)};
		TextureAtlasSprite tex_casing0 = ClientUtils.getSprite(rl_casing[0]);
		TextureAtlasSprite tex_casing1 = ClientUtils.getSprite(rl_casing[1]);
		TextureAtlasSprite tex_casing2 = ClientUtils.getSprite(rl_casing[2]);
		float[] colour = {1, 1, 1, 1};
		float[] colourStripes = {1, 1, 1, 1};
		if(stripeColour!=null)
			System.arraycopy(stripeColour.getTextureDiffuseColors(), 0, colourStripes, 0, 3);
		final TextureAtlasSprite topTexture = tex_conveyor_colour!=null?tex_conveyor_colour: tex_casing2;
		final float[] topColor = stripeColour!=null?colourStripes: colour;

		/**
		 * Bottom & Top
		 */
		//Shift if up/down
		for(int i = 0; i < 4; i++)
			if((i==0||i==3)?conDir==ConveyorDirection.UP: conDir==ConveyorDirection.DOWN)
				vertices[i] = vertices[i].add(0, length, 0);
		//Draw bottom
		quads.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(Direction.DOWN, facing), tex_conveyor, new double[]{1, 0, 15, length*16}, colour, true));
		//Expand verts to side
		for(int i = 0; i < vertices.length; i++)
		{
			Vec3 v = vertices[i];
			vertices[i] = new Vec3(v.x < .5?0: 1, v.y, v.z);
		}
		//Draw bottom casing
		quads.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(Direction.DOWN, facing), tex_casing2, new double[]{0, 0, 16, length*16}, colour, true));
		//Shift verts to top
		for(int i = 0; i < vertices.length; i++)
			vertices[i] = vertices[i].add(0, .125, 0);
		//Draw top
		quads.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(Direction.UP, facing), tex_conveyor, new double[]{0, length*16, 16, 0}, colour, false));
		if(corners[0])
		{
			vertices = new Vec3[]{new Vec3(0, .1875f, .9375f), new Vec3(0, .1875f, 1), new Vec3(1, .1875f, 1), new Vec3(1, .1875f, .9375f)};
			//Shift if up/down
			for(int i = 0; i < 4; i++)
				vertices[i] = vertices[i].add(0,
						i==0||i==3?(conDir==ConveyorDirection.UP?.0625f: conDir==ConveyorDirection.DOWN?length-.0625f: 0): (conDir==ConveyorDirection.DOWN?length: 0),
						0);
			//Draw top casing back
			quads.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(Direction.UP, facing), topTexture, new double[]{0, 1, 16, 0}, topColor, false));
		}
		if(corners[1])
		{
			vertices = new Vec3[]{new Vec3(0, .1875f, 1-length), new Vec3(0, .1875f, 1.0625f-length), new Vec3(1, .1875f, 1.0625f-length), new Vec3(1, .1875f, 1-length)};
			//Shift if up/down
			for(int i = 0; i < 4; i++)
				vertices[i] = vertices[i].add(0, i==1||i==2?(conDir==ConveyorDirection.UP?length-.0625f: conDir==ConveyorDirection.DOWN?.0625f: 0): (conDir==ConveyorDirection.UP?length: 0), 0);
			//Draw top casing front
			quads.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(Direction.UP, facing), topTexture, new double[]{0, 1, 16, 0}, topColor, false));
		}

		/**
		 * Sides
		 */
		vertices = new Vec3[]{new Vec3(0, 0, 1-length), new Vec3(0, 0, 1), new Vec3(0, .125f, 1), new Vec3(0, .125f, 1-length)};
		for(int i = 0; i < 4; i++)
			if((i==0||i==3)?conDir==ConveyorDirection.UP: conDir==ConveyorDirection.DOWN)
				vertices[i] = vertices[i].add(0, length, 0);
		//Draw left side
		quads.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(Direction.WEST, facing), tex_casing1, new double[]{0, 0, 2, length*16}, colour, false));

		//Shift to the other side
		for(int i = 0; i < 4; i++)
		{
			Vec3 v = vertices[i];
			vertices[i] = new Vec3(1, v.y, v.z);
		}
		//Draw right side
		quads.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(Direction.EAST, facing), tex_casing1, new double[]{0, 0, 2, length*16}, colour, true));
		//Shift upwards
		for(int i = 0; i < 4; i++)
		{
			Vec3 v = vertices[i];
			vertices[i] = new Vec3(v.x, v.y+((i==0||i==1)?.125f: .0625f), v.z);
		}
		/**
		 * Corners
		 */
		if(corners[0])
		{
			vertices = new Vec3[]{new Vec3(0, .125f, .9375f), new Vec3(0, .125f, 1), new Vec3(0, .1875f, 1), new Vec3(0, .1875f, .9375f)};
			if(conDir!=ConveyorDirection.HORIZONTAL)
				for(int i = 0; i < 4; i++)
					vertices[i] = vertices[i].add(0, i==0||i==3?(conDir==ConveyorDirection.UP?.0625f: conDir==ConveyorDirection.DOWN?length-.0625f: 0): (conDir==ConveyorDirection.DOWN?length: 0), 0);
			//Back left
			quads.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(Direction.WEST, facing), tex_casing0, new double[]{0, 0, 1, 1}, colour, false));
			for(int i = 0; i < vertices.length; i++)
			{
				vertices[i] = vertices[i].add(.0625f, 0, 0);
			}
			quads.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(Direction.EAST, facing), tex_casing0, new double[]{0, 0, 1, 1}, colour, true));
			//Shift right
			for(int i = 0; i < vertices.length; i++)
			{
				Vec3 tmp = vertices[i];
				vertices[i] = new Vec3(1, tmp.y, tmp.z);
			}
			//Back right
			quads.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(Direction.EAST, facing), tex_casing0, new double[]{0, 0, 1, 1}, colour, true));
			for(int i = 0; i < vertices.length; i++)
			{
				vertices[i] = vertices[i].add(-.0625f, 0, 0);
			}
			quads.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(Direction.WEST, facing), tex_casing0, new double[]{0, 0, 1, 1}, colour, false));
		}
		if(corners[1])
		{
			vertices = new Vec3[]{new Vec3(0, .125f, 1-length), new Vec3(0, .125f, 1.0625f-length), new Vec3(0, .1875f, 1.0625f-length), new Vec3(0, .1875f, 1-length)};
			if(conDir!=ConveyorDirection.HORIZONTAL)
				for(int i = 0; i < 4; i++)
					vertices[i] = vertices[i].add(0, i==1||i==2?(conDir==ConveyorDirection.UP?length-.0625f: conDir==ConveyorDirection.DOWN?.0625f: 0): (conDir==ConveyorDirection.UP?length: 0), 0);
			//Front left
			quads.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(Direction.WEST, facing), tex_casing0, new double[]{0, 15, 1, 16}, colour, false));
			for(int i = 0; i < vertices.length; i++)
			{
				vertices[i] = vertices[i].add(.0625f, 0, 0);
			}
			quads.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(Direction.EAST, facing), tex_casing0, new double[]{0, 15, 1, 16}, colour, true));
			//Shift right
			for(int i = 0; i < vertices.length; i++)
			{
				Vec3 tmp = vertices[i];
				vertices[i] = new Vec3(1, tmp.y, tmp.z);
			}
			//Front right
			quads.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(Direction.EAST, facing), tex_casing0, new double[]{0, 15, 1, 16}, colour, true));
			for(int i = 0; i < vertices.length; i++)
			{
				vertices[i] = vertices[i].add(-.0625f, 0, 0);
			}
			quads.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(Direction.WEST, facing), tex_casing0, new double[]{0, 15, 1, 16}, colour, false));
		}


		/**
		 * Front & Back
		 */
		vertices = new Vec3[]{new Vec3(.0625f, 0, 1-length), new Vec3(.0625f, .125f, 1-length), new Vec3(.9375f, .125f, 1-length), new Vec3(.9375f, 0, 1-length)};
		//Shift if up/down
		if(conDir==ConveyorDirection.UP)
			for(int i = 0; i < vertices.length; i++)
			{
				vertices[i] = vertices[i].add(0, length, 0);
			}
		//Draw front
		double frontUMax = (1-length)*16;
		quads.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertices), facing, tex_conveyor, new double[]{1, frontUMax+2, 15, frontUMax}, colour, false));
		//Expand to side and up
		for(int i = 0; i < 4; i++)
			vertices[i] = new Vec3(vertices[i].x() < .5f?0: 1, vertices[i].y()+(i==1||i==2?.0625f: 0), vertices[i].z);
		//Draw front casing
		quads.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertices), facing, tex_casing2, new double[]{0, 3, 16, 0}, colour, false));
		for(int i = 0; i < vertices.length; i++)
		{
			vertices[i] = vertices[i].add(0, (conDir==ConveyorDirection.UP?-.0625f: conDir==ConveyorDirection.DOWN?.0625f: 0), .0625f);
		}
		quads.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertices), facing, tex_casing2, new double[]{0, 3, 16, 0}, colour, true));
		//Undo expand, shift if up/down, shift to back
		for(int i = 0; i < 4; i++)
		{
			Vec3 v = vertices[i];
			v = new Vec3(v.x() < .5f?.0625f: .9375f, v.y()-(i==1||i==2?.0625f: 0), v.z);
			if(conDir==ConveyorDirection.UP)
				v = v.add(0, -(length-.0625f), 0);
			if(conDir==ConveyorDirection.DOWN)
				v = v.add(0, (length-.0625f), 0);
			v = v.add(0, 0, length-.0625f);
			vertices[i] = v;
		}
		//Draw back
		quads.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertices), facing.getOpposite(), tex_conveyor, new double[]{1, 0, 15, 2}, colour, true));
		//Expand to side and up
		for(int i = 0; i < 4; i++)
			vertices[i] = new Vec3(vertices[i].x() < .5f?0: 1, vertices[i].y()+(i==1||i==2?.0625f: 0), vertices[i].z);
		//Draw back casing
		quads.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertices), facing.getOpposite(), tex_casing2, new double[]{0, 0, 16, 3}, colour, true));
		for(int i = 0; i < vertices.length; i++)
		{
			vertices[i] = vertices[i].add(0, conDir==ConveyorDirection.UP?.0625f: conDir==ConveyorDirection.DOWN?-.0625f: 0, -.0625f);
		}
		quads.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertices), facing.getOpposite(), tex_casing2, new double[]{0, 0, 16, 3}, colour, false));

		/**
		 * Walls
		 */
		float wallLength = length-.125f;
		vertices = new Vec3[]{
				new Vec3(0, .1875f, .9375f-wallLength),
				new Vec3(0, .1875f, .9375f),
				new Vec3(.0625f, .1875f, .9375f),
				new Vec3(.0625f, .1875f, .9375f-wallLength)
		};
		Vec3[] vertices2 = new Vec3[]{
				new Vec3(0, .125f, .9375f-wallLength),
				new Vec3(0, .125f, .9375f),
				new Vec3(0, .1875f, .9375f),
				new Vec3(0, .1875f, .9375f-wallLength)
		};
		Vec3[] vertices3 = new Vec3[]{
				new Vec3(.0625f, .125f, .9375f-wallLength),
				new Vec3(.0625f, .125f, .9375f),
				new Vec3(.0625f, .1875f, .9375f),
				new Vec3(.0625f, .1875f, .9375f-wallLength)
		};
		for(int i = 0; i < 4; i++)
			if(conDir!=ConveyorDirection.HORIZONTAL)
			{
				float f = (i==0||i==3)?(conDir==ConveyorDirection.UP?length-.0625f: .0625f): (conDir==ConveyorDirection.UP?.0625f: length-.0625f);
				vertices[i] = vertices[i].add(0, f, 0);
				vertices2[i] = vertices2[i].add(0, f, 0);
				vertices3[i] = vertices3[i].add(0, f, 0);
			}
		//Draw left walls
		if(walls[0])
		{
			quads.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(Direction.UP, facing), topTexture, new double[]{0, 15, 1, 1}, topColor, false));
			quads.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertices2), Utils.rotateFacingTowardsDir(Direction.WEST, facing), tex_casing1, new double[]{2, 15, 3, 1}, colour, false));
			quads.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertices3), Utils.rotateFacingTowardsDir(Direction.EAST, facing), tex_casing1, new double[]{2, 15, 3, 1}, colour, true));
		}
		for(int i = 0; i < 4; i++)
		{
			vertices[i] = vertices[i].add(.9375f, 0, 0);
			vertices2[i] = vertices2[i].add(.9375f, 0, 0);
			vertices3[i] = vertices3[i].add(.9375f, 0, 0);
		}
		//Draw right walls
		if(walls[1])
		{
			quads.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(Direction.UP, facing), topTexture, new double[]{15, 15, 16, 1}, topColor, false));
			quads.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertices2), Utils.rotateFacingTowardsDir(Direction.WEST, facing), tex_casing1, new double[]{2, 15, 3, 1}, colour, false));
			quads.add(ModelUtils.createBakedQuad(DefaultVertexFormat.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertices3), Utils.rotateFacingTowardsDir(Direction.EAST, facing), tex_casing1, new double[]{2, 15, 3, 1}, colour, true));
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

	@Override
	public TextureAtlasSprite getParticleIcon()
	{
		if(tex_particle==null)
			tex_particle = ClientUtils.getSprite(new ResourceLocation(ImmersiveEngineering.MODID, "block/conveyor/off"));
		return tex_particle;
	}

	@Override
	public ItemTransforms getTransforms()
	{
		return ItemTransforms.NO_TRANSFORMS;
	}

	@Override
	public ItemOverrides getOverrides()
	{
		return overrideList;
	}

	static HashMap<String, BakedModel> itemModelCache = new HashMap<>();
	ItemOverrides overrideList = new ItemOverrides()
	{
		@Nullable
		@Override
		public BakedModel resolve(@Nonnull BakedModel originalModel, @Nonnull ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity)
		{
			String key = "default";
			if(stack.getItem() instanceof BlockItem)
			{
				Block b = ((BlockItem)stack.getItem()).getBlock();
				ResourceLocation rlKey = ConveyorHandler.getType(b);
				if(rlKey!=null)
					key = rlKey.toString();
			}
			BakedModel model = itemModelCache.get(key);
			if(model==null)
			{
				model = new ModelConveyor(ConveyorHandler.getConveyor(new ResourceLocation(key), null));
				itemModelCache.put(key, model);
			}
			return model;
		}
	};

	static HashMap<TransformType, Matrix4> transformationMap = new HashMap<>();

	static
	{
		transformationMap.put(TransformType.FIRST_PERSON_LEFT_HAND, new Matrix4().scale(.5, .5, .5).translate(0, .25, 0).rotate(Math.toRadians(-45), 0, 1, 0));
		transformationMap.put(TransformType.FIRST_PERSON_RIGHT_HAND, new Matrix4().scale(.5, .5, .5).translate(0, .25, 0).rotate(Math.toRadians(-45), 0, 1, 0));
		transformationMap.put(TransformType.THIRD_PERSON_LEFT_HAND, new Matrix4().translate(0, .0625, -.125).scale(.3125, .3125, .3125).rotate(Math.toRadians(30), 1, 0, 0).rotate(Math.toRadians(130), 0, 1, 0));
		transformationMap.put(TransformType.THIRD_PERSON_RIGHT_HAND, new Matrix4().translate(0, .0625, -.125).scale(.3125, .3125, .3125).rotate(Math.toRadians(30), 1, 0, 0).rotate(Math.toRadians(130), 0, 1, 0));
		transformationMap.put(TransformType.GUI, new Matrix4().scale(.625, .625, .625).rotate(Math.toRadians(-45), 0, 1, 0).rotate(Math.toRadians(-20), 0, 0, 1).rotate(Math.toRadians(20), 1, 0, 0));
		transformationMap.put(TransformType.FIXED, new Matrix4().scale(.625, .625, .625).rotate(Math.PI, 0, 1, 0).translate(0, 0, .3125));
		transformationMap.put(TransformType.GROUND, new Matrix4().scale(.25, .25, .25));
	}

	@Override
	public BakedModel handlePerspective(TransformType cameraTransformType, PoseStack mat)
	{
		Matrix4 matrix = transformationMap.containsKey(cameraTransformType)?transformationMap.get(cameraTransformType): new Matrix4();
		matrix.toTransformationMatrix().push(mat);
		return this;
	}

	@Nonnull
	@Override
	public IModelData getModelData(@Nonnull BlockAndTintGetter world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData)
	{
		Block b = state.getBlock();
		ResourceLocation conveyorName = ConveyorHandler.getType(b);
		if(conveyorName!=null)
		{
			IConveyorBelt belt = ConveyorHandler.getConveyor(conveyorName, world.getBlockEntity(pos));
			return CombinedModelData.combine(tileData, new SinglePropertyModelData<>(belt, CONVEYOR_MODEL_DATA));
		}
		else
			return tileData;
	}

	public static class RawConveyorModel implements IModelGeometry<RawConveyorModel>
	{

		@Override
		public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter,
								ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation)
		{
			return new ModelConveyor();
		}

		@Override
		public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors)
		{
			//TODO?
			return ImmutableList.of();
		}
	}

	public static class ConveyorLoader implements IModelLoader<RawConveyorModel>
	{
		public static final ResourceLocation LOCATION = new ResourceLocation(ImmersiveEngineering.MODID, "models/conveyor");

		@Override
		public void onResourceManagerReload(ResourceManager resourceManager)
		{
		}

		@Override
		public RawConveyorModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents)
		{
			return new RawConveyorModel();
		}
	}
}