package blusunrize.immersiveengineering.client.render.conveyor;

import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelConveyor;
import blusunrize.immersiveengineering.client.utils.ModelUtils;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.ConveyorBase;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.VerticalConveyor;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Function;

public class VerticalConveyorRender extends BasicConveyorRender<VerticalConveyor>
{
	public VerticalConveyorRender(ResourceLocation active, ResourceLocation inactive)
	{
		super(active, inactive);
	}

	@Override
	public boolean shouldRenderWall(Direction facing, int wall, RenderContext<VerticalConveyor> context)
	{
		return true;
	}

	@Override
	public String getModelCacheKey(RenderContext<VerticalConveyor> context)
	{
		String key = super.getModelCacheKey(context);
		VerticalConveyor instance = context.instance();
		if(instance==null)
			return key;
		BlockEntity blockEntity = instance.getBlockEntity();
		Direction facing = context.getFacing();
		if(VerticalConveyor.renderBottomBelt(blockEntity, facing))
		{
			key += "b";
			key += VerticalConveyor.isInwardConveyor(blockEntity, facing.getOpposite())?"1": "0";
			key += renderBottomWall(facing, 0, context)?"1": "0";
			key += renderBottomWall(facing, 1, context)?"1": "0";
		}
		return key;
	}

	@Override
	public Transformation modifyBaseRotationMatrix(Transformation matrix)
	{
		return matrix.compose(new Transformation(
				new Vector3f(0, 1, 0), new Quaternion((float)Math.PI/2, 0, 0, false), null, null
		));
	}

	public boolean renderBottomWall(Direction facing, int wall, RenderContext<VerticalConveyor> context)
	{
		return super.shouldRenderWall(facing, wall, context);
	}

	@Override
	public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel, RenderContext<VerticalConveyor> context)
	{
		VerticalConveyor instance = context.instance();
		if(instance==null)
			return baseModel;
		BlockEntity blockEntity = instance.getBlockEntity();
		Direction facing = context.getFacing();

		boolean[] walls = {true, true};
		if(VerticalConveyor.renderBottomBelt(blockEntity, facing))
		{
			TextureAtlasSprite sprite = ClientUtils.getSprite(
					instance.isActive()?ConveyorBase.texture_on: ConveyorBase.texture_off
			);
			TextureAtlasSprite spriteColour = ClientUtils.getSprite(getColouredStripesTexture());
			walls = new boolean[]{renderBottomWall(facing, 0, context), renderBottomWall(facing, 1, context)};
			baseModel.addAll(ModelConveyor.getBaseConveyor(
					facing, .875f, ClientUtils.rotateTo(facing), ConveyorDirection.HORIZONTAL, sprite, walls,
					new boolean[]{true, false}, spriteColour, instance.getDyeColour()
			));
		}
		addCoverQuads(baseModel, context, walls);
		return baseModel;
	}

	private void addCoverQuads(List<BakedQuad> baseModel, RenderContext<VerticalConveyor> context, boolean[] walls)
	{
		Block b = context.getCover();
		if(b==Blocks.AIR)
			return;
		Direction facing = context.getFacing();
		VerticalConveyor conveyor = context.instance();
		BlockEntity blockEntity = conveyor!=null?conveyor.getBlockEntity(): null;
		boolean renderBottom = conveyor!=null&&VerticalConveyor.renderBottomBelt(blockEntity, facing);
		Function<Direction, TextureAtlasSprite> getSprite = makeTextureGetter(b);
		float[] colour = {1, 1, 1, 1};
		Matrix4 matrix = new Matrix4(facing);

		if(!renderBottom)//just vertical
		{
			baseModel.addAll(ModelUtils.createBakedBox(new Vec3(0, 0, .75f), new Vec3(1, 1, 1), matrix, facing, getSprite, colour));
			baseModel.addAll(ModelUtils.createBakedBox(new Vec3(0, 0, .1875f), new Vec3(.0625f, 1, .75f), matrix, facing, getSprite, colour));
			baseModel.addAll(ModelUtils.createBakedBox(new Vec3(.9375f, 0, .1875f), new Vec3(1, 1, .75f), matrix, facing, getSprite, colour));
		}
		else
		{
			boolean straightInput = VerticalConveyor.isInwardConveyor(blockEntity, facing.getOpposite());
			baseModel.addAll(ModelUtils.createBakedBox(new Vec3(0, .9375f, .75f), new Vec3(1, 1, 1), matrix, facing, getSprite, colour));
			if(!straightInput)
				baseModel.addAll(ModelUtils.createBakedBox(new Vec3(0, .1875f, .9375f), new Vec3(1, 1f, 1), matrix, facing, getSprite, colour));
			else//has direct input, needs a cutout
			{
				baseModel.addAll(ModelUtils.createBakedBox(new Vec3(0, .75f, .9375f), new Vec3(1, 1, 1), matrix, facing, getSprite, colour));
				baseModel.addAll(ModelUtils.createBakedBox(new Vec3(0, .1875f, .9375f), new Vec3(.0625f, .75f, 1), matrix, facing, getSprite, colour));
				baseModel.addAll(ModelUtils.createBakedBox(new Vec3(.9375f, .1875f, .9375f), new Vec3(1, .75f, 1), matrix, facing, getSprite, colour));
			}

			if(walls[0])//wall to the left
				baseModel.addAll(ModelUtils.createBakedBox(new Vec3(0, .1875f, .1875f), new Vec3(.0625f, 1, .9375f), matrix, facing, getSprite, colour));
			else//cutout to the left
				baseModel.addAll(ModelUtils.createBakedBox(new Vec3(0, .75f, .1875f), new Vec3(.0625f, 1, .9375f), matrix, facing, getSprite, colour));

			if(walls[1])//wall to the right
				baseModel.addAll(ModelUtils.createBakedBox(new Vec3(.9375f, .1875f, .1875f), new Vec3(1, 1, .9375f), matrix, facing, getSprite, colour));
			else//cutout to the right
				baseModel.addAll(ModelUtils.createBakedBox(new Vec3(.9375f, .75f, .1875f), new Vec3(1, 1, .9375f), matrix, facing, getSprite, colour));
		}
	}
}
