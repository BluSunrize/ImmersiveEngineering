/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelConveyor;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import blusunrize.immersiveengineering.common.util.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.common.util.shapes.CachedVoxelShapes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

/**
 * @author BluSunrize - 20.08.2016
 */
public class VerticalCoveredConveyor extends VerticalConveyor
{
	public static final ResourceLocation NAME = new ResourceLocation(MODID, "verticalcovered");

	public VerticalCoveredConveyor(TileEntity tile)
	{
		super(tile);
	}

	@Override
	protected boolean allowCovers()
	{
		return true;
	}

	private static final CachedShapesWithTransform<Boolean, Direction> SHAPES =
			CachedShapesWithTransform.createDirectional(VerticalCoveredConveyor::getBoxes);

	@Override
	public VoxelShape getCollisionShape()
	{
		return SHAPES.get(Pair.of(renderBottomBelt(getTile(), getFacing()), getFacing()));
	}

	private static List<AxisAlignedBB> getBoxes(Boolean bottom)
	{
		List<AxisAlignedBB> list = new ArrayList<>();
		list.add(new AxisAlignedBB(0, 0, 0, 1, 1, .125f));
		list.add(new AxisAlignedBB(0, 0, 0, 0.0625, 1, 1));
		list.add(new AxisAlignedBB(0.9375, 0, 0, 1, 1, 1));
		if(bottom)
			list.add(new AxisAlignedBB(0, .75, .75, 1, 1, 1));
		else
			list.add(new AxisAlignedBB(0, 0, .75, 1, 1, 1));
		if(bottom||list.isEmpty())
			list.add(conveyorBounds.getBoundingBox());
		return list;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel)
	{
		boolean renderBottom = getTile()!=null&&renderBottomBelt(getTile(), getFacing());
		boolean[] walls;
		if(renderBottom)
		{
			TextureAtlasSprite sprite = ClientUtils.getSprite(isActive()?BasicConveyor.texture_on: BasicConveyor.texture_off);
			TextureAtlasSprite spriteColour = ClientUtils.getSprite(getColouredStripesTexture());
			walls = new boolean[]{renderBottomWall(getTile(), getFacing(), 0), renderBottomWall(getTile(), getFacing(), 1)};
			baseModel.addAll(ModelConveyor.getBaseConveyor(getFacing(), .875f, new Matrix4(getFacing()), ConveyorDirection.HORIZONTAL, sprite, walls, new boolean[]{true, false}, spriteColour, getDyeColour()));
		}
		else
			walls = new boolean[]{true, true};

		Block b = this.cover!=Blocks.AIR?this.cover: getDefaultCover();
		BlockState state = b.getDefaultState();
		IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(state);
		if(model!=null)
		{
			TextureAtlasSprite sprite = model.getParticleTexture(EmptyModelData.INSTANCE);
			HashMap<Direction, TextureAtlasSprite> sprites = new HashMap<>();

			for(Direction f : Direction.VALUES)
				for(BakedQuad q : model.getQuads(state, f, Utils.RAND))
					if(q!=null&&q.getSprite()!=null)
						sprites.put(f, q.getSprite());
			for(BakedQuad q : model.getQuads(state, null, Utils.RAND))
				if(q!=null&&q.getSprite()!=null&&q.getFace()!=null)
					sprites.put(q.getFace(), q.getSprite());

			Function<Direction, TextureAtlasSprite> getSprite = f -> sprites.containsKey(f)?sprites.get(f): sprite;

			float[] colour = {1, 1, 1, 1};
			Matrix4 matrix = new Matrix4(getFacing());

			if(!renderBottom)//just vertical
			{
				baseModel.addAll(ClientUtils.createBakedBox(new Vec3d(0, 0, .75f), new Vec3d(1, 1, 1), matrix, getFacing(), getSprite, colour));
				baseModel.addAll(ClientUtils.createBakedBox(new Vec3d(0, 0, .1875f), new Vec3d(.0625f, 1, .75f), matrix, getFacing(), getSprite, colour));
				baseModel.addAll(ClientUtils.createBakedBox(new Vec3d(.9375f, 0, .1875f), new Vec3d(1, 1, .75f), matrix, getFacing(), getSprite, colour));
			}
			else
			{
				boolean straightInput = getTile()!=null&&isInwardConveyor(getTile(), getFacing().getOpposite());
				baseModel.addAll(ClientUtils.createBakedBox(new Vec3d(0, .9375f, .75f), new Vec3d(1, 1, 1), matrix, getFacing(), getSprite, colour));
				if(!straightInput)
					baseModel.addAll(ClientUtils.createBakedBox(new Vec3d(0, .1875f, .9375f), new Vec3d(1, 1f, 1), matrix, getFacing(), getSprite, colour));
				else//has direct input, needs a cutout
				{
					baseModel.addAll(ClientUtils.createBakedBox(new Vec3d(0, .75f, .9375f), new Vec3d(1, 1, 1), matrix, getFacing(), getSprite, colour));
					baseModel.addAll(ClientUtils.createBakedBox(new Vec3d(0, .1875f, .9375f), new Vec3d(.0625f, .75f, 1), matrix, getFacing(), getSprite, colour));
					baseModel.addAll(ClientUtils.createBakedBox(new Vec3d(.9375f, .1875f, .9375f), new Vec3d(1, .75f, 1), matrix, getFacing(), getSprite, colour));
				}

				if(walls[0])//wall to the left
					baseModel.addAll(ClientUtils.createBakedBox(new Vec3d(0, .1875f, .1875f), new Vec3d(.0625f, 1, .9375f), matrix, getFacing(), getSprite, colour));
				else//cutout to the left
					baseModel.addAll(ClientUtils.createBakedBox(new Vec3d(0, .75f, .1875f), new Vec3d(.0625f, 1, .9375f), matrix, getFacing(), getSprite, colour));

				if(walls[1])//wall to the right
					baseModel.addAll(ClientUtils.createBakedBox(new Vec3d(.9375f, .1875f, .1875f), new Vec3d(1, 1, .9375f), matrix, getFacing(), getSprite, colour));
				else//cutout to the right
					baseModel.addAll(ClientUtils.createBakedBox(new Vec3d(.9375f, .75f, .1875f), new Vec3d(1, 1, .9375f), matrix, getFacing(), getSprite, colour));
			}
		}
		return baseModel;
	}
}
