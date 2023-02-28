/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.conveyor;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.tool.conveyor.BasicConveyorCacheData;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorWall;
import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorModelRender;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.ModelUtils;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.ConveyorBase;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.ExtractConveyor;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class ExtractConveyorRender extends BasicConveyorRender<ExtractConveyor>
{
	public ExtractConveyorRender(ResourceLocation active, ResourceLocation inactive)
	{
		super(active, inactive);
	}

	@Override
	public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel, RenderContext<ExtractConveyor> context, @Nullable RenderType renderType)
	{
		if(renderType!=null&&renderType!=RenderType.cutout())
			return super.modifyQuads(baseModel, context, renderType);

		final TextureAtlasSprite texture_steel = ClientUtils.getSprite(new ResourceLocation(ImmersiveEngineering.MODID, "block/metal/storage_steel"));
		final TextureAtlasSprite texture_casing = ClientUtils.getSprite(new ResourceLocation(ImmersiveEngineering.MODID, "block/wooden_device/turntable_bottom"));
		final TextureAtlasSprite texture_curtain = ClientUtils.getSprite(new ResourceLocation(ImmersiveEngineering.MODID, "block/stripcurtain"));
		final TextureAtlasSprite texture_assembler = ClientUtils.getSprite(new ResourceLocation(ImmersiveEngineering.MODID, "block/multiblocks/assembler"));

		float[] colour = {1, 1, 1, 1};
		ExtractConveyor instance = context.instance();
		Matrix4 matrix = new Matrix4(instance==null?Direction.SOUTH: instance.getExtractDirection());
		Transformation tMatrix = matrix.toTransformationMatrix();
		final double extend = instance==null?0: instance.getCurrentExtension();

		Function<Direction, TextureAtlasSprite> getCasingSprite = enumFacing -> enumFacing.getAxis()==Axis.Z?texture_steel: texture_casing;

		Function<Vec3[], Vec3[]> vertexTransformer = vertices -> {
			if(extend==0)
				return vertices;
			Vec3[] ret = new Vec3[vertices.length];
			for(int i = 0; i < ret.length; i++)
				ret[i] = new Vec3(vertices[i].x, vertices[i].y, vertices[i].z-extend);
			return ret;
		};
		Function<Vec3[], Vec3[]> casingTransformer = vertices -> {
			Vec3[] ret = new Vec3[vertices.length];
			for(int i = 0; i < ret.length; i++)
				ret[i] = new Vec3(vertices[i].x, vertices[i].y-.25f, vertices[i].z-.625f-extend);
			return ret;
		};

		Direction facing = context.getFacing();
		baseModel.addAll(ModelUtils.createBakedBox(new Vec3(.0625f, .375f, .625f), new Vec3(.1875f, 1f, 1f), matrix, facing, casingTransformer, getCasingSprite, colour));
		baseModel.addAll(ModelUtils.createBakedBox(new Vec3(.8125f, .375f, .625f), new Vec3(.9375f, 1f, 1f), matrix, facing, casingTransformer, getCasingSprite, colour));
		baseModel.addAll(ModelUtils.createBakedBox(new Vec3(.1875f, .875f, .625f), new Vec3(.8125f, 1f, 1f), matrix, facing, casingTransformer, getCasingSprite, colour));

		if(extend > 0)
		{
			TextureAtlasSprite tex_conveyor = ClientUtils.getSprite(
					instance.isActive()?ConveyorBase.texture_on: ConveyorBase.texture_off
			);
			Function<Direction, TextureAtlasSprite> getExtensionSprite = enumFacing -> enumFacing.getAxis()==Axis.Y?null: enumFacing.getAxis()==Axis.Z?texture_steel: texture_casing;

			Vec3[] vertices = {new Vec3(.0625f, 0, -extend), new Vec3(.0625f, 0, 0), new Vec3(.9375f, 0, 0), new Vec3(.9375f, 0, -extend)};
			baseModel.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(tMatrix, vertices), Utils.rotateFacingTowardsDir(Direction.DOWN, facing), tex_conveyor, new double[]{15, extend*16, 1, 0}, colour, true));
			for(int i = 0; i < vertices.length; i++)
				vertices[i] = Utils.withCoordinate(vertices[i], Axis.Y, .125);
			baseModel.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(tMatrix, vertices), Utils.rotateFacingTowardsDir(Direction.UP, facing), tex_conveyor, new double[]{15, (1-extend)*16, 1, 16}, colour, false));
			baseModel.addAll(ModelUtils.createBakedBox(new Vec3(.0625f, .25f, .625f), new Vec3(.9375f, .375f, .625f+extend), matrix, facing, casingTransformer, getExtensionSprite, colour));
		}


		Vec3[] vertices = new Vec3[]{new Vec3(.8125f, .625f, .03125f), new Vec3(.8125f, .125f, .03125f), new Vec3(.1875f, .125f, .03125f), new Vec3(.1875f, .625f, .03125f)};
		baseModel.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(tMatrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.NORTH, facing), texture_assembler, new double[]{15.25, 13.25, 12.75, 15.25}, colour, false));
		for(int i = 0; i < vertices.length; i++)
			vertices[i] = Utils.withCoordinate(vertices[i], Axis.Z, .0625);
		baseModel.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(tMatrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.SOUTH, facing), texture_assembler, new double[]{12.75, 13.25, 15.25, 15.25}, colour, true));

		for(int i = 0; i < 5; i++)
		{
			float off = i*.125f;
			baseModel.addAll(ModelUtils.createBakedBox(new Vec3(.203125f+off, .1875f, .09375f), new Vec3(.296875f+off, .625f, .125f), matrix, facing, vertexTransformer, (facing1) -> texture_curtain, colour));
		}

		return super.modifyQuads(baseModel, context, renderType);
	}

	@Override
	public Object getModelCacheKey(RenderContext<ExtractConveyor> context)
	{
		BasicConveyorCacheData basic = IConveyorModelRender.getDefaultData(this, context);
		ExtractConveyor instance = context.instance();
		if(instance==null)
			return basic;
		record Key(BasicConveyorCacheData basic, Direction extractFrom, double extension)
		{
		}
		return new Key(basic, instance.getExtractDirection(), instance.getCurrentExtension());
	}

	@Override
	public boolean shouldRenderWall(Direction facing, ConveyorWall wall, RenderContext<ExtractConveyor> context)
	{
		ExtractConveyor instance = context.instance();
		if(instance==null)
			return true;
		Direction side = wall.getWallSide(facing);
		return side!=instance.getExtractDirection()&&super.shouldRenderWall(facing, wall, context);
	}
}
