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
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.BlockConveyor;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("deprecation")
public class ModelConveyor implements IBakedModel
{
	static List<BakedQuad> emptyQuads = Lists.newArrayList();
	public static HashMap<String, List<BakedQuad>> modelCache = new HashMap<>();
	public static ResourceLocation[] rl_casing = {new ResourceLocation(ImmersiveEngineering.MODID, "blocks/conveyor_casing_top"), new ResourceLocation(ImmersiveEngineering.MODID, "blocks/conveyor_casing_side"), new ResourceLocation(ImmersiveEngineering.MODID, "blocks/conveyor_casing_walls")};

	@Nullable
	final IConveyorBelt defaultBelt;

	public ModelConveyor(IConveyorBelt defaultBelt)
	{
		this.defaultBelt = defaultBelt;
	}

	public ModelConveyor()
	{
		this(null);
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState blockState, @Nullable EnumFacing side, long rand)
	{
		TileEntity tile = null;
		String key = "default";
		EnumFacing facing = EnumFacing.NORTH;
		IConveyorBelt conveyor = defaultBelt;
		if(blockState==null)
			key = conveyor!=null?ConveyorHandler.reverseClassRegistry.get(conveyor.getClass()).toString(): "immersiveengineering:conveyor";
		else
		{
			facing = blockState.getValue(IEProperties.FACING_ALL);
			if(blockState instanceof IExtendedBlockState)
			{
				IExtendedBlockState exState = (IExtendedBlockState)blockState;
				if(exState.getUnlistedNames().contains(BlockConveyor.ICONEYOR_PASSTHROUGH))
					conveyor = ((IExtendedBlockState)blockState).getValue(BlockConveyor.ICONEYOR_PASSTHROUGH);
				if(exState.getUnlistedNames().contains(IEProperties.TILEENTITY_PASSTHROUGH))
					tile = ((IExtendedBlockState)blockState).getValue(IEProperties.TILEENTITY_PASSTHROUGH);
				if(conveyor!=null&&tile!=null)
					key = conveyor.getModelCacheKey(tile, facing);
			}
		}
		List<BakedQuad> cachedQuads = modelCache.get(key);
		if(cachedQuads!=null)
			return ImmutableList.copyOf(cachedQuads);
		else
		{
			if(conveyor==null)
				conveyor = ConveyorHandler.getConveyor(new ResourceLocation(key), tile);
			cachedQuads = Collections.synchronizedList(Lists.newArrayList());
			Matrix4f facingMatrix = TRSRTransformation.getMatrix(facing);
			if(conveyor!=null)
				facingMatrix = conveyor.modifyBaseRotationMatrix(facingMatrix, tile, facing);
			Matrix4 matrix = new Matrix4(facingMatrix);
			ConveyorDirection conDir = conveyor!=null?conveyor.getConveyorDirection(): ConveyorDirection.HORIZONTAL;
			boolean[] walls = conveyor!=null&&tile!=null?new boolean[]{conveyor.renderWall(tile, facing, 0), conveyor.renderWall(tile, facing, 1)}: new boolean[]{true, true};
			TextureAtlasSprite tex_conveyor = ClientUtils.mc().getTextureMapBlocks().getMissingSprite();
			TextureAtlasSprite tex_conveyor_colour = null;
			int colourStripes = -1;
			if(conveyor!=null)
			{
				tex_conveyor = ClientUtils.getSprite(tile!=null?(conveyor.isActive(tile)?conveyor.getActiveTexture(): conveyor.getInactiveTexture()): conveyor.getActiveTexture());
				if((colourStripes = conveyor.getDyeColour()) >= 0)
					tex_conveyor_colour = ClientUtils.getSprite(conveyor.getColouredStripesTexture());
			}
			cachedQuads.addAll(getBaseConveyor(facing, 1, matrix, conDir, tex_conveyor, walls, new boolean[]{true, true}, tex_conveyor_colour, colourStripes));
			if(conveyor!=null)
				cachedQuads = conveyor.modifyQuads(cachedQuads, tile, facing);
			modelCache.put(key, ImmutableList.copyOf(cachedQuads));
			return ImmutableList.copyOf(cachedQuads);
		}
	}

	public static List<BakedQuad> getBaseConveyor(EnumFacing facing, float length, Matrix4 matrix, ConveyorDirection conDir,
												  TextureAtlasSprite tex_conveyor, boolean[] walls, boolean[] corners,
												  TextureAtlasSprite tex_conveyor_colour, int stripeColour)
	{
		List<BakedQuad> quads = new ArrayList<BakedQuad>();

		Vector3f[] vertices = {new Vector3f(.0625f, 0, 1-length), new Vector3f(.0625f, 0, 1), new Vector3f(.9375f, 0, 1), new Vector3f(.9375f, 0, 1-length)};
		TextureAtlasSprite tex_casing0 = ClientUtils.getSprite(rl_casing[0]);
		TextureAtlasSprite tex_casing1 = ClientUtils.getSprite(rl_casing[1]);
		TextureAtlasSprite tex_casing2 = ClientUtils.getSprite(rl_casing[2]);
		float[] colour = {1, 1, 1, 1};
		float[] colourStripes = {(stripeColour >> 16&255)/255f, (stripeColour >> 8&255)/255f, (stripeColour&255)/255f, 1};

		/**
		 * Bottom & Top
		 */
		//Shift if up/down
		for(int i = 0; i < 4; i++)
			if((i==0||i==3)?conDir==ConveyorDirection.UP: conDir==ConveyorDirection.DOWN)
				vertices[i].translate(0, length, 0);
		//Draw bottom
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(EnumFacing.DOWN, facing), tex_conveyor, new double[]{1, 0, 15, length*16}, colour, true));
		//Expand verts to side
		for(Vector3f v : vertices)
			v.setX(v.getX() < .5f?0: 1);
		//Draw bottom casing
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(EnumFacing.DOWN, facing), tex_casing2, new double[]{0, 0, 16, length*16}, colour, true));
		//Shift verts to top
		for(Vector3f v : vertices)
			v.translate(0, .125f, 0);
		//Draw top
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(EnumFacing.UP, facing), tex_conveyor, new double[]{0, length*16, 16, 0}, colour, false));
		if(corners[0])
		{
			vertices = new Vector3f[]{new Vector3f(0, .1875f, .9375f), new Vector3f(0, .1875f, 1), new Vector3f(1, .1875f, 1), new Vector3f(1, .1875f, .9375f)};
			//Shift if up/down
			for(int i = 0; i < 4; i++)
				vertices[i].translate(0, i==0||i==3?(conDir==ConveyorDirection.UP?.0625f: conDir==ConveyorDirection.DOWN?length-.0625f: 0): (conDir==ConveyorDirection.DOWN?length: 0), 0);
			//Draw top casing back
			quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(EnumFacing.UP, facing), tex_casing0, new double[]{0, 1, 16, 0}, colour, false));
		}
		if(corners[1])
		{
			vertices = new Vector3f[]{new Vector3f(0, .1875f, 1-length), new Vector3f(0, .1875f, 1.0625f-length), new Vector3f(1, .1875f, 1.0625f-length), new Vector3f(1, .1875f, 1-length)};
			//Shift if up/down
			for(int i = 0; i < 4; i++)
				vertices[i].translate(0, i==1||i==2?(conDir==ConveyorDirection.UP?length-.0625f: conDir==ConveyorDirection.DOWN?.0625f: 0): (conDir==ConveyorDirection.UP?length: 0), 0);
			//Draw top casing front
			quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(EnumFacing.UP, facing), tex_casing0, new double[]{0, 1, 16, 0}, colour, false));
		}

		/**
		 * Sides
		 */
		vertices = new Vector3f[]{new Vector3f(0, 0, 1-length), new Vector3f(0, 0, 1), new Vector3f(0, .125f, 1), new Vector3f(0, .125f, 1-length)};
		for(int i = 0; i < 4; i++)
			if((i==0||i==3)?conDir==ConveyorDirection.UP: conDir==ConveyorDirection.DOWN)
				vertices[i].translate(0, length, 0);
		//Draw left side
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(EnumFacing.WEST, facing), tex_casing1, new double[]{0, 0, 2, length*16}, colour, false));
		//Shift upwards
		for(int i = 0; i < 4; i++)
			vertices[i].setY(vertices[i].getY()+((i==0||i==1)?.125f: .0625f));

		//Shift back down and to the other side
		for(int i = 0; i < 4; i++)
			vertices[i].set(1, vertices[i].getY()-((i==0||i==1)?.125f: .0625f));
		//Draw right side
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(EnumFacing.EAST, facing), tex_casing1, new double[]{0, 0, 2, length*16}, colour, true));
		//Shift upwards
		for(int i = 0; i < 4; i++)
			vertices[i].setY(vertices[i].getY()+((i==0||i==1)?.125f: .0625f));
		/**
		 * Corners
		 */
		if(corners[0])
		{
			vertices = new Vector3f[]{new Vector3f(0, .125f, .9375f), new Vector3f(0, .125f, 1), new Vector3f(0, .1875f, 1), new Vector3f(0, .1875f, .9375f)};
			if(conDir!=ConveyorDirection.HORIZONTAL)
				for(int i = 0; i < 4; i++)
					vertices[i].translate(0, i==0||i==3?(conDir==ConveyorDirection.UP?.0625f: conDir==ConveyorDirection.DOWN?length-.0625f: 0): (conDir==ConveyorDirection.DOWN?length: 0), 0);
			//Back left
			quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(EnumFacing.WEST, facing), tex_casing0, new double[]{0, 0, 1, 1}, colour, false));
			for(Vector3f v : vertices)
				v.translate(.0625f, 0, 0);
			quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(EnumFacing.EAST, facing), tex_casing0, new double[]{0, 0, 1, 1}, colour, true));
			//Shift right
			for(Vector3f v : vertices)
				v.setX(1);
			//Back right
			quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(EnumFacing.EAST, facing), tex_casing0, new double[]{0, 0, 1, 1}, colour, true));
			for(Vector3f v : vertices)
				v.translate(-.0625f, 0, 0);
			quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(EnumFacing.WEST, facing), tex_casing0, new double[]{0, 0, 1, 1}, colour, false));
		}
		if(corners[1])
		{
			vertices = new Vector3f[]{new Vector3f(0, .125f, 1-length), new Vector3f(0, .125f, 1.0625f-length), new Vector3f(0, .1875f, 1.0625f-length), new Vector3f(0, .1875f, 1-length)};
			if(conDir!=ConveyorDirection.HORIZONTAL)
				for(int i = 0; i < 4; i++)
					vertices[i].translate(0, i==1||i==2?(conDir==ConveyorDirection.UP?length-.0625f: conDir==ConveyorDirection.DOWN?.0625f: 0): (conDir==ConveyorDirection.UP?length: 0), 0);
			//Front left
			quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(EnumFacing.WEST, facing), tex_casing0, new double[]{0, 15, 1, 16}, colour, false));
			for(Vector3f v : vertices)
				v.translate(.0625f, 0, 0);
			quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(EnumFacing.EAST, facing), tex_casing0, new double[]{0, 15, 1, 16}, colour, true));
			//Shift right
			for(Vector3f v : vertices)
				v.setX(1);
			//Front right
			quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(EnumFacing.EAST, facing), tex_casing0, new double[]{0, 15, 1, 16}, colour, true));
			for(Vector3f v : vertices)
				v.translate(-.0625f, 0, 0);
			quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(EnumFacing.WEST, facing), tex_casing0, new double[]{0, 15, 1, 16}, colour, false));
		}


		/**
		 * Front & Back
		 */
		vertices = new Vector3f[]{new Vector3f(.0625f, 0, 1-length), new Vector3f(.0625f, .125f, 1-length), new Vector3f(.9375f, .125f, 1-length), new Vector3f(.9375f, 0, 1-length)};
		//Shift if up/down
		if(conDir==ConveyorDirection.UP)
			for(Vector3f v : vertices)
				v.translate(0, length, 0);
		//Draw front
		double frontUMax = (1-length)*16;
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices), facing, tex_conveyor, new double[]{1, frontUMax+2, 15, frontUMax}, colour, false));
		//Expand to side and up
		for(int i = 0; i < 4; i++)
			vertices[i].set(vertices[i].getX() < .5f?0: 1, vertices[i].getY()+(i==1||i==2?.0625f: 0));
		//Draw front casing
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices), facing, tex_casing2, new double[]{0, 3, 16, 0}, colour, false));
		for(Vector3f v : vertices)
			v.translate(0, (conDir==ConveyorDirection.UP?-.0625f: conDir==ConveyorDirection.DOWN?.0625f: 0), .0625f);
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices), facing, tex_casing2, new double[]{0, 3, 16, 0}, colour, true));
		//Undo expand, shift if up/down, shift to back
		for(int i = 0; i < 4; i++)
		{
			Vector3f v = vertices[i];
			v.setX(v.getX() < .5f?.0625f: .9375f);
			v.setY(v.getY()-(i==1||i==2?.0625f: 0));
			if(conDir==ConveyorDirection.UP)
				v.translate(0, -(length-.0625f), 0);
			if(conDir==ConveyorDirection.DOWN)
				v.translate(0, (length-.0625f), 0);
			v.translate(0, 0, length-.0625f);
		}
		//Draw back
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices), facing.getOpposite(), tex_conveyor, new double[]{1, 0, 15, 2}, colour, true));
		//Expand to side and up
		for(int i = 0; i < 4; i++)
			vertices[i].set(vertices[i].getX() < .5f?0: 1, vertices[i].getY()+(i==1||i==2?.0625f: 0));
		//Draw back casing
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices), facing.getOpposite(), tex_casing2, new double[]{0, 0, 16, 3}, colour, true));
		for(Vector3f v : vertices)
			v.translate(0, conDir==ConveyorDirection.UP?.0625f: conDir==ConveyorDirection.DOWN?-.0625f: 0, -.0625f);
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices), facing.getOpposite(), tex_casing2, new double[]{0, 0, 16, 3}, colour, false));

		/**
		 * Walls
		 */
		float wallLength = length-.125f;
		vertices = new Vector3f[]{new Vector3f(0, .1875f, .9375f-wallLength), new Vector3f(0, .1875f, .9375f), new Vector3f(.0625f, .1875f, .9375f), new Vector3f(.0625f, .1875f, .9375f-wallLength)};
		Vector3f[] vertices2 = new Vector3f[]{new Vector3f(0, .125f, .9375f-wallLength), new Vector3f(0, .125f, .9375f), new Vector3f(0, .1875f, .9375f), new Vector3f(0, .1875f, .9375f-wallLength)};
		Vector3f[] vertices3 = new Vector3f[]{new Vector3f(.0625f, .125f, .9375f-wallLength), new Vector3f(.0625f, .125f, .9375f), new Vector3f(.0625f, .1875f, .9375f), new Vector3f(.0625f, .1875f, .9375f-wallLength)};
		Vector3f[] verticesColour = new Vector3f[]{new Vector3f(0, .1876f, corners[1]?(1-length): (.9375f-wallLength)), new Vector3f(0, .1876f, corners[0]?1: .9375f), new Vector3f(.0625f, .1876f, corners[0]?1: .9375f), new Vector3f(.0625f, .1876f, corners[1]?(1-length): (.9375f-wallLength))};
		for(int i = 0; i < 4; i++)
			if(conDir!=ConveyorDirection.HORIZONTAL)
			{
				float f = (i==0||i==3)?(conDir==ConveyorDirection.UP?length-.0625f: .0625f): (conDir==ConveyorDirection.UP?.0625f: length-.0625f);
				vertices[i].translate(0, f, 0);
				vertices2[i].translate(0, f, 0);
				vertices3[i].translate(0, f, 0);
				verticesColour[i].translate(0, f, 0);
			}
		//Draw left walls
		if(walls[0])
		{
			quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(EnumFacing.UP, facing), tex_casing2, new double[]{0, 15, 1, 1}, colour, false));
			if(tex_conveyor_colour!=null&&stripeColour >= 0)
				quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, verticesColour), Utils.rotateFacingTowardsDir(EnumFacing.UP, facing), tex_conveyor_colour, new double[]{0, 15, 1, 1}, colourStripes, false));
			quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices2), Utils.rotateFacingTowardsDir(EnumFacing.WEST, facing), tex_casing1, new double[]{2, 15, 3, 1}, colour, false));
			quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices3), Utils.rotateFacingTowardsDir(EnumFacing.EAST, facing), tex_casing1, new double[]{2, 15, 3, 1}, colour, true));
		}
		for(int i = 0; i < 4; i++)
		{
			vertices[i].translate(.9375f, 0, 0);
			vertices2[i].translate(.9375f, 0, 0);
			vertices3[i].translate(.9375f, 0, 0);
			verticesColour[i].translate(.9375f, 0, 0);
		}
		//Draw right walls
		if(walls[1])
		{
			quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices), Utils.rotateFacingTowardsDir(EnumFacing.UP, facing), tex_casing2, new double[]{15, 15, 16, 1}, colour, false));
			if(tex_conveyor_colour!=null&&stripeColour >= 0)
				quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, verticesColour), Utils.rotateFacingTowardsDir(EnumFacing.UP, facing), tex_conveyor_colour, new double[]{15, 15, 16, 1}, colourStripes, false));
			quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices2), Utils.rotateFacingTowardsDir(EnumFacing.WEST, facing), tex_casing1, new double[]{2, 15, 3, 1}, colour, false));
			quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, ClientUtils.applyMatrixToVertices(matrix, vertices3), Utils.rotateFacingTowardsDir(EnumFacing.EAST, facing), tex_casing1, new double[]{2, 15, 3, 1}, colour, true));
		}

		return quads;
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

	TextureAtlasSprite tex_particle;

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		if(tex_particle==null)
			tex_particle = ClientUtils.getSprite(new ResourceLocation(ImmersiveEngineering.MODID, "blocks/conveyor_off"));
		return tex_particle;
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return overrideList;
	}

	static HashMap<String, IBakedModel> itemModelCache = new HashMap<String, IBakedModel>();
	ItemOverrideList overrideList = new ItemOverrideList(new ArrayList())
	{
		@Override
		public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity)
		{
			String key = ItemNBTHelper.getString(stack, "conveyorType");
			IBakedModel model = itemModelCache.get(key);
			if(model==null)
			{
				model = new ModelConveyor(ConveyorHandler.getConveyor(new ResourceLocation(key), null));
				itemModelCache.put(key, model);
			}
			return model;
		}
	};

	static HashMap<TransformType, Matrix4> transformationMap = new HashMap<TransformType, Matrix4>();

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
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType)
	{
		Matrix4 matrix = transformationMap.containsKey(cameraTransformType)?transformationMap.get(cameraTransformType): new Matrix4();
		return Pair.of(this, matrix.toMatrix4f());
	}
}