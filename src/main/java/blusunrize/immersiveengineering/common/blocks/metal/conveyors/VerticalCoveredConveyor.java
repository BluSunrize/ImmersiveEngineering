/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelConveyor;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.ArrayList;
import java.util.Collections;
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
	public ItemStack cover = ItemStack.EMPTY;

	public VerticalCoveredConveyor(TileEntity tile)
	{
		super(tile);
	}

	@Override
	public String getModelCacheKey()
	{
		String key = ConveyorHandler.reverseClassRegistry.get(this.getClass()).toString();
		key += "f"+getFacing().ordinal();
		key += "a"+(isActive()?1: 0);
		key += "b"+(renderBottomBelt(getTile(), getFacing())?("1"+(isInwardConveyor(getTile(), getFacing().getOpposite())?"1": "0")+(renderBottomWall(getTile(), getFacing(), 0)?"1": "0")+(renderBottomWall(getTile(), getFacing(), 1)?"1": "0")): "0000");
		key += "c"+getDyeColour();
		if(!cover.isEmpty())
			key += "s"+cover.getItem().getRegistryName();
		return key;
	}

	@Override
	public void onEntityCollision(Entity entity)
	{
		super.onEntityCollision(entity);
		if(entity instanceof ItemEntity)
			((ItemEntity)entity).setPickupDelay(10);
	}

	@Override
	public void onItemDeployed(ItemEntity entity)
	{
		entity.setPickupDelay(10);
	}

	@Override
	public boolean playerInteraction(PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ, Direction side)
	{
		return CoveredConveyor.handleCoverInteraction(getTile(), player, hand, heldItem, () -> cover, (itemStack -> cover = itemStack));
	}

	static final List<AxisAlignedBB> selectionBoxes = Collections.singletonList(VoxelShapes.fullCube().getBoundingBox());

	@Override
	public List<AxisAlignedBB> getSelectionBoxes()
	{
		return selectionBoxes;
	}

	static final AxisAlignedBB[] topBounds = {new AxisAlignedBB(0, 0, .75, 1, 1, 1), new AxisAlignedBB(0, 0, 0, 1, 1, .25), new AxisAlignedBB(.75, 0, 0, 1, 1, 1), new AxisAlignedBB(0, 0, 0, .25, 1, 1)};
	static final AxisAlignedBB[] topBoundsCorner = {new AxisAlignedBB(0, .75, .75, 1, 1, 1), new AxisAlignedBB(0, .75, 0, 1, 1, .25), new AxisAlignedBB(.75, .75, 0, 1, 1, 1), new AxisAlignedBB(0, .75, 0, .25, 1, 1)};

	@Override
	public List<AxisAlignedBB> getColisionBoxes()
	{
		ArrayList list = new ArrayList();
		boolean bottom = renderBottomBelt(getTile(), getFacing());
		if(getFacing().ordinal() > 1)
		{
			list.add(verticalBounds[getFacing().ordinal()-2]);
			list.add((bottom?topBoundsCorner: topBounds)[getFacing().ordinal()-2]);
		}
		if(bottom||list.isEmpty())
			list.add(conveyorBounds);
		return list;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel)
	{
		boolean renderBottom = getTile()!=null&&this.renderBottomBelt(getTile(), getFacing());
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

		ItemStack cover = !this.cover.isEmpty()?this.cover: CoveredConveyor.getDefaultCover();
		Block b = Block.getBlockFromItem(cover.getItem());
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

	@Override
	public CompoundNBT writeConveyorNBT()
	{
		CompoundNBT nbt = super.writeConveyorNBT();
		if(cover!=null)
			nbt.put("cover", cover.write(new CompoundNBT()));
		return nbt;
	}

	@Override
	public void readConveyorNBT(CompoundNBT nbt)
	{
		super.readConveyorNBT(nbt);
		cover = ItemStack.read(nbt.getCompound("cover"));
	}
}
