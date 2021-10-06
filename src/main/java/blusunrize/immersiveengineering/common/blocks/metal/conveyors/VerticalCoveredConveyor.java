/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelConveyor;
import blusunrize.immersiveengineering.client.utils.ModelUtils;
import blusunrize.immersiveengineering.common.util.DirectionUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
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

	public VerticalCoveredConveyor(BlockEntity tile)
	{
		super(tile);
	}

	@Override
	public boolean isCovered()
	{
		return true;
	}

	private static final CachedShapesWithTransform<Byte, Direction> SHAPES =
			CachedShapesWithTransform.createDirectional(VerticalCoveredConveyor::getBoxes);

	@Override
	public VoxelShape getCollisionShape()
	{
		return SHAPES.get(Pair.of(buildCollisionState(), getFacing()));
	}

	private byte buildCollisionState()
	{
		byte out = 0;
		if(renderBottomBelt(getTile(), getFacing()))
			out |= 1;
		if(renderBottomWall(getTile(), getFacing(), 0))
			out |= 2;
		if(renderBottomWall(getTile(), getFacing(), 1))
			out |= 4;
		if(!isInwardConveyor(getTile(), getFacing().getOpposite()))
			out |= 8;
		return out;
	}

	private static List<AABB> getBoxes(Byte state)
	{
		boolean bottom = (state&1)!=0;
		boolean left = (state&2)!=0;
		boolean right = (state&4)!=0;
		boolean front = (state&8)!=0;

		List<AABB> list = new ArrayList<>();
		// back
		list.add(new AABB(0, 0, 0, 1, 1, .125f));
		//left
		list.add(new AABB(0, left?0: .75, 0, 0.0625, 1, 1));
		// right
		list.add(new AABB(0.9375, right?0: .75, 0, 1, 1, 1));
		// front
		list.add(new AABB(0, front?0: .75, .9375, 1, 1, 1));
		if(bottom||list.isEmpty())
			list.add(conveyorBounds.bounds());
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
			baseModel.addAll(ModelConveyor.getBaseConveyor(getFacing(), .875f,
					ClientUtils.rotateTo(getFacing()),
					ConveyorDirection.HORIZONTAL, sprite, walls, new boolean[]{true, false}, spriteColour, getDyeColour()));
		}
		else
			walls = new boolean[]{true, true};

		Block b = this.cover!=Blocks.AIR?this.cover: getDefaultCover();
		BlockState state = b.defaultBlockState();
		BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(state);
		if(model!=null)
		{
			TextureAtlasSprite sprite = model.getParticleTexture(EmptyModelData.INSTANCE);
			HashMap<Direction, TextureAtlasSprite> sprites = new HashMap<>();

			for(Direction f : DirectionUtils.VALUES)
				for(BakedQuad q : model.getQuads(state, f, Utils.RAND))
					if(q!=null&&q.a()!=null)
						sprites.put(f, q.a());
			for(BakedQuad q : model.getQuads(state, null, Utils.RAND))
				if(q!=null&&q.a()!=null&&q.getDirection()!=null)
					sprites.put(q.getDirection(), q.a());

			Function<Direction, TextureAtlasSprite> getSprite = f -> sprites.getOrDefault(f, sprite);

			float[] colour = {1, 1, 1, 1};
			Matrix4 matrix = new Matrix4(getFacing());

			if(!renderBottom)//just vertical
			{
				baseModel.addAll(ModelUtils.createBakedBox(new Vec3(0, 0, .75f), new Vec3(1, 1, 1), matrix, getFacing(), getSprite, colour));
				baseModel.addAll(ModelUtils.createBakedBox(new Vec3(0, 0, .1875f), new Vec3(.0625f, 1, .75f), matrix, getFacing(), getSprite, colour));
				baseModel.addAll(ModelUtils.createBakedBox(new Vec3(.9375f, 0, .1875f), new Vec3(1, 1, .75f), matrix, getFacing(), getSprite, colour));
			}
			else
			{
				boolean straightInput = getTile()!=null&&isInwardConveyor(getTile(), getFacing().getOpposite());
				baseModel.addAll(ModelUtils.createBakedBox(new Vec3(0, .9375f, .75f), new Vec3(1, 1, 1), matrix, getFacing(), getSprite, colour));
				if(!straightInput)
					baseModel.addAll(ModelUtils.createBakedBox(new Vec3(0, .1875f, .9375f), new Vec3(1, 1f, 1), matrix, getFacing(), getSprite, colour));
				else//has direct input, needs a cutout
				{
					baseModel.addAll(ModelUtils.createBakedBox(new Vec3(0, .75f, .9375f), new Vec3(1, 1, 1), matrix, getFacing(), getSprite, colour));
					baseModel.addAll(ModelUtils.createBakedBox(new Vec3(0, .1875f, .9375f), new Vec3(.0625f, .75f, 1), matrix, getFacing(), getSprite, colour));
					baseModel.addAll(ModelUtils.createBakedBox(new Vec3(.9375f, .1875f, .9375f), new Vec3(1, .75f, 1), matrix, getFacing(), getSprite, colour));
				}

				if(walls[0])//wall to the left
					baseModel.addAll(ModelUtils.createBakedBox(new Vec3(0, .1875f, .1875f), new Vec3(.0625f, 1, .9375f), matrix, getFacing(), getSprite, colour));
				else//cutout to the left
					baseModel.addAll(ModelUtils.createBakedBox(new Vec3(0, .75f, .1875f), new Vec3(.0625f, 1, .9375f), matrix, getFacing(), getSprite, colour));

				if(walls[1])//wall to the right
					baseModel.addAll(ModelUtils.createBakedBox(new Vec3(.9375f, .1875f, .1875f), new Vec3(1, 1, .9375f), matrix, getFacing(), getSprite, colour));
				else//cutout to the right
					baseModel.addAll(ModelUtils.createBakedBox(new Vec3(.9375f, .75f, .1875f), new Vec3(1, 1, .9375f), matrix, getFacing(), getSprite, colour));
			}
		}
		return baseModel;
	}
}
