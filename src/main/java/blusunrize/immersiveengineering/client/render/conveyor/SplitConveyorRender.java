/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.conveyor;

import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorWall;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelConveyor;
import blusunrize.immersiveengineering.client.utils.ModelUtils;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.SplitConveyor;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

import static blusunrize.immersiveengineering.common.util.Utils.withCoordinate;

public class SplitConveyorRender extends BasicConveyorRender<SplitConveyor>
{
	public SplitConveyorRender(ResourceLocation active, ResourceLocation inactive)
	{
		super(active, inactive);
	}

	@Override
	public boolean shouldRenderWall(Direction facing, ConveyorWall wall, RenderContext<SplitConveyor> context)
	{
		return false;
	}

	public static ResourceLocation texture_casing = new ResourceLocation("immersiveengineering:block/conveyor/split_wall");

	@Override
	public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel, RenderContext<SplitConveyor> context, @Nullable RenderType renderType)
	{
		if(renderType!=null&&renderType!=RenderType.cutout())
			return super.modifyQuads(baseModel, context, renderType);
		TextureAtlasSprite tex_casing0 = ClientUtils.getSprite(texture_casing);
		Direction facing = context!=null?context.getFacing(): Direction.NORTH;
		Matrix4 matrix = new Matrix4(facing);
		Transformation tMatrix = matrix.toTransformationMatrix();
		float[] colour = {1, 1, 1, 1};

		Vec3[] vertices = {new Vec3(.0625f, 0, 0), new Vec3(.0625f, 0, 1), new Vec3(.9375f, 0, 1), new Vec3(.9375f, 0, 0)};

		// replace bottom with casing
		baseModel.set(0, ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(tMatrix, vertices), Utils.rotateFacingTowardsDir(Direction.DOWN, facing), ClientUtils.getSprite(ModelConveyor.rl_casing[3]), new double[]{1, 0, 15, 16}, colour, true));

		vertices = new Vec3[]{new Vec3(.0625f, .1875f, 0), new Vec3(.0625f, .1875f, 1), new Vec3(.9375f, .1875f, 1), new Vec3(.9375f, .1875f, 0)};
		baseModel.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(tMatrix, vertices), Direction.UP, tex_casing0, new double[]{1, 16, 15, 0}, colour, false));

		// replace front with casing
		vertices = new Vec3[]{new Vec3(.0625f, 0, 0), new Vec3(.0625f, .1875f, 0), new Vec3(.9375f, .1875f, 0), new Vec3(.9375f, 0, 0)};
		baseModel.set(5, ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(tMatrix, vertices), facing, ClientUtils.getSprite(ModelConveyor.rl_casing[1]), new double[]{1, 16, 15, 13}, colour, false));

		vertices = new Vec3[]{new Vec3(.0625f, .125f, 0), new Vec3(.0625f, .1875f, 0), new Vec3(.9375f, .1875f, 0), new Vec3(.9375f, .125f, 0)};
		Vec3[] vertices2 = new Vec3[]{new Vec3(.5f, .125f, 0), new Vec3(.5f, .125f, .5f), new Vec3(.5f, .1875f, .5f), new Vec3(.5f, .1875f, 0)};
		Vec3[] vertices3 = new Vec3[]{new Vec3(.5f, .125f, 0), new Vec3(.5f, .125f, .5f), new Vec3(.5f, .1875f, .5f), new Vec3(.5f, .1875f, 0)};
		for(int i = 0; i < 8; i++)
		{
			for(int iv = 0; iv < vertices.length; iv++)
			{
				vertices[iv] = withCoordinate(vertices[iv], Direction.Axis.Z, (i+1)*.0625f);
				vertices2[iv] = vertices2[iv].add(.0625f, 0, 0);
				vertices3[iv] = vertices3[iv].add(-.0625f, 0, 0);
			}
			double v = 16-i;
			baseModel.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(tMatrix, vertices), facing, tex_casing0, new double[]{1, v-1, 15, v}, colour, true));
			if(i < 7)
			{
				double u = 8-i;
				baseModel.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(tMatrix, vertices2), facing, tex_casing0, new double[]{u-1, 16, u, 8}, colour, true));
				baseModel.add(ModelUtils.createBakedQuad(ClientUtils.applyMatrixToVertices(tMatrix, vertices3), facing, tex_casing0, new double[]{u-1, 16, u, 8}, colour, false));
			}
		}
		return super.modifyQuads(baseModel, context, renderType);
	}
}
