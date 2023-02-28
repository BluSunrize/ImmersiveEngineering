/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.conveyor;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorWall;
import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorModelRender;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.client.utils.ModelUtils;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.ConveyorBase;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class BasicConveyorRender<T extends ConveyorBase> implements IConveyorModelRender<T>
{
	private final ResourceLocation active;
	private final ResourceLocation inactive;

	public BasicConveyorRender(ResourceLocation active, ResourceLocation inactive)
	{
		this.active = active;
		this.inactive = inactive;
	}

	@Override
	public ResourceLocation getActiveTexture()
	{
		return active;
	}

	@Override
	public ResourceLocation getInactiveTexture()
	{
		return inactive;
	}

	@Override
	public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel, RenderContext<T> context, @Nullable RenderType renderType)
	{
		if(renderType==null||renderType==RenderType.translucent())
			addCoverToQuads(baseModel, context);
		return baseModel;
	}

	protected void addCoverToQuads(List<BakedQuad> baseModel, RenderContext<T> context)
	{
		Block b = context.getCover();
		if(b==Blocks.AIR)
			return;
		Function<Direction, TextureAtlasSprite> getSprite = makeTextureGetter(b);
		Direction facing = context.getFacing();
		ConveyorDirection conDir = context.getConveyorDirection();

		Function<Direction, TextureAtlasSprite> getSpriteHorizontal = f -> f.getAxis()==Axis.Y?null: getSprite.apply(f);

		float[] colour = {1, 1, 1, 1};
		Matrix4 matrix = new Matrix4(facing);

		Function<Vec3[], Vec3[]> vertexTransformer = conDir==ConveyorDirection.HORIZONTAL?vertices -> vertices: vertices -> {
			Vec3[] ret = new Vec3[vertices.length];
			for(int i = 0; i < ret.length; i++)
				ret[i] = new Vec3(vertices[i].x, vertices[i].y+(vertices[i].z==(conDir==ConveyorDirection.UP?0: 1)?1: 0), vertices[i].z);
			return ret;
		};

		baseModel.addAll(ModelUtils.createBakedBox(new Vec3(0, .75f, 0), new Vec3(1, 1, 1), matrix, facing, vertexTransformer, getSprite, colour));

		if(shouldRenderWall(facing, ConveyorWall.LEFT, context))
			baseModel.addAll(ModelUtils.createBakedBox(new Vec3(0, .1875f, 0), new Vec3(.0625f, .75f, 1), matrix, facing, vertexTransformer, getSpriteHorizontal, colour));
		else
		{
			baseModel.addAll(ModelUtils.createBakedBox(new Vec3(0, .1875f, 0), new Vec3(.0625f, .75f, .0625f), matrix, facing, getSpriteHorizontal, colour));
			baseModel.addAll(ModelUtils.createBakedBox(new Vec3(0, .1875f, .9375f), new Vec3(.0625f, .75f, 1), matrix, facing, getSpriteHorizontal, colour));
		}

		if(shouldRenderWall(facing, ConveyorWall.RIGHT, context))
			baseModel.addAll(ModelUtils.createBakedBox(new Vec3(.9375f, .1875f, 0), new Vec3(1, .75f, 1), matrix, facing, vertexTransformer, getSpriteHorizontal, colour));
		else
		{
			baseModel.addAll(ModelUtils.createBakedBox(new Vec3(.9375f, .1875f, 0), new Vec3(1, .75f, .0625f), matrix, facing, getSpriteHorizontal, colour));
			baseModel.addAll(ModelUtils.createBakedBox(new Vec3(.9375f, .1875f, .9375f), new Vec3(1, .75f, 1), matrix, facing, getSpriteHorizontal, colour));
		}
	}

	protected static Function<Direction, TextureAtlasSprite> makeTextureGetter(Block b)
	{
		BlockState state = b.defaultBlockState();
		BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(state);
		TextureAtlasSprite sprite = model.getParticleIcon(ModelData.EMPTY);
		Map<Direction, TextureAtlasSprite> sprites = new EnumMap<>(Direction.class);

		for(Direction f : DirectionUtils.VALUES)
			for(BakedQuad q : model.getQuads(state, f, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null))
				if(q!=null&&q.getSprite()!=null)
					sprites.put(f, q.getSprite());
		for(BakedQuad q : model.getQuads(state, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null))
			if(q!=null&&q.getSprite()!=null&&q.getDirection()!=null)
				sprites.put(q.getDirection(), q.getSprite());
		return d -> sprites.getOrDefault(d, sprite);
	}
}
