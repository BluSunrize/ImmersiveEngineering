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
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

/**
 * @author BluSunrize - 20.08.2016
 */
public class ConveyorVerticalCovered extends ConveyorVertical
{
	public ItemStack cover = ItemStack.EMPTY;

	@Override
	public String getModelCacheKey(TileEntity tile, EnumFacing facing)
	{
		String key = ConveyorHandler.reverseClassRegistry.get(this.getClass()).toString();
		key += "f"+facing.ordinal();
		key += "a"+(isActive(tile)?1: 0);
		key += "b"+(renderBottomBelt(tile, facing)?("1"+(isInwardConveyor(tile, facing.getOpposite())?"1": "0")+(renderBottomWall(tile, facing, 0)?"1": "0")+(renderBottomWall(tile, facing, 1)?"1": "0")): "0000");
		key += "c"+getDyeColour();
		if(!cover.isEmpty())
			key += "s"+cover.getItem().getRegistryName()+cover.getMetadata();
		return key;
	}

	@Override
	public void onEntityCollision(TileEntity tile, Entity entity, EnumFacing facing)
	{
		super.onEntityCollision(tile, entity, facing);
		if(entity instanceof EntityItem)
			((EntityItem)entity).setPickupDelay(10);
	}

	@Override
	public void onItemDeployed(TileEntity tile, EntityItem entity, EnumFacing facing)
	{
		entity.setPickupDelay(10);
	}

	@Override
	public boolean playerInteraction(TileEntity tile, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ, EnumFacing side)
	{
		return ConveyorCovered.handleCoverInteraction(tile, player, hand, heldItem, () -> cover, (itemStack -> cover = itemStack));
	}

	static final List<AxisAlignedBB> selectionBoxes = Collections.singletonList(Block.FULL_BLOCK_AABB);

	@Override
	public List<AxisAlignedBB> getSelectionBoxes(TileEntity tile, EnumFacing facing)
	{
		return selectionBoxes;
	}

	static final AxisAlignedBB[] topBounds = {new AxisAlignedBB(0, 0, .75, 1, 1, 1), new AxisAlignedBB(0, 0, 0, 1, 1, .25), new AxisAlignedBB(.75, 0, 0, 1, 1, 1), new AxisAlignedBB(0, 0, 0, .25, 1, 1)};
	static final AxisAlignedBB[] topBoundsCorner = {new AxisAlignedBB(0, .75, .75, 1, 1, 1), new AxisAlignedBB(0, .75, 0, 1, 1, .25), new AxisAlignedBB(.75, .75, 0, 1, 1, 1), new AxisAlignedBB(0, .75, 0, .25, 1, 1)};

	@Override
	public List<AxisAlignedBB> getColisionBoxes(TileEntity tile, EnumFacing facing)
	{
		ArrayList list = new ArrayList();
		boolean bottom = renderBottomBelt(tile, facing);
		if(facing.ordinal() > 1)
		{
			list.add(verticalBounds[facing.ordinal()-2]);
			list.add((bottom?topBoundsCorner: topBounds)[facing.ordinal()-2]);
		}
		if(bottom||list.isEmpty())
			list.add(conveyorBounds);
		return list;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel, @Nullable TileEntity tile, EnumFacing facing)
	{
		boolean renderBottom = tile!=null&&this.renderBottomBelt(tile, facing);
		boolean[] walls;
		if(renderBottom)
		{
			TextureAtlasSprite sprite = ClientUtils.getSprite(isActive(tile)?ConveyorBasic.texture_on: ConveyorBasic.texture_off);
			TextureAtlasSprite spriteColour = ClientUtils.getSprite(getColouredStripesTexture());
			walls = new boolean[]{renderBottomWall(tile, facing, 0), renderBottomWall(tile, facing, 1)};
			baseModel.addAll(ModelConveyor.getBaseConveyor(facing, .875f, new Matrix4(facing), ConveyorDirection.HORIZONTAL, sprite, walls, new boolean[]{true, false}, spriteColour, getDyeColour()));
		}
		else
			walls = new boolean[]{true, true};

		ItemStack cover = !this.cover.isEmpty()?this.cover: ConveyorCovered.defaultCover;
		Block b = Block.getBlockFromItem(cover.getItem());
		IBlockState state = !cover.isEmpty()?b.getStateFromMeta(cover.getMetadata()): Blocks.STONE.getDefaultState();
		IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);
		if(model!=null)
		{
			TextureAtlasSprite sprite = model.getParticleTexture();
			HashMap<EnumFacing, TextureAtlasSprite> sprites = new HashMap<>();

			for(EnumFacing f : EnumFacing.VALUES)
				for(BakedQuad q : model.getQuads(state, f, 0))
					if(q!=null&&q.getSprite()!=null)
						sprites.put(f, q.getSprite());
			for(BakedQuad q : model.getQuads(state, null, 0))
				if(q!=null&&q.getSprite()!=null&&q.getFace()!=null)
					sprites.put(q.getFace(), q.getSprite());

			Function<EnumFacing, TextureAtlasSprite> getSprite = f -> sprites.containsKey(f)?sprites.get(f): sprite;

			float[] colour = {1, 1, 1, 1};
			Matrix4 matrix = new Matrix4(facing);

			if(!renderBottom)//just vertical
			{
				baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0, 0, .75f), new Vector3f(1, 1, 1), matrix, facing, getSprite, colour));
				baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0, 0, .1875f), new Vector3f(.0625f, 1, .75f), matrix, facing, getSprite, colour));
				baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(.9375f, 0, .1875f), new Vector3f(1, 1, .75f), matrix, facing, getSprite, colour));
			}
			else
			{
				boolean straightInput = tile!=null&&isInwardConveyor(tile, facing.getOpposite());
				baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0, .9375f, .75f), new Vector3f(1, 1, 1), matrix, facing, getSprite, colour));
				if(!straightInput)
					baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0, .1875f, .9375f), new Vector3f(1, 1f, 1), matrix, facing, getSprite, colour));
				else//has direct input, needs a cutout
				{
					baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0, .75f, .9375f), new Vector3f(1, 1, 1), matrix, facing, getSprite, colour));
					baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0, .1875f, .9375f), new Vector3f(.0625f, .75f, 1), matrix, facing, getSprite, colour));
					baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(.9375f, .1875f, .9375f), new Vector3f(1, .75f, 1), matrix, facing, getSprite, colour));
				}

				if(walls[0])//wall to the left
					baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0, .1875f, .1875f), new Vector3f(.0625f, 1, .9375f), matrix, facing, getSprite, colour));
				else//cutout to the left
					baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0, .75f, .1875f), new Vector3f(.0625f, 1, .9375f), matrix, facing, getSprite, colour));

				if(walls[1])//wall to the right
					baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(.9375f, .1875f, .1875f), new Vector3f(1, 1, .9375f), matrix, facing, getSprite, colour));
				else//cutout to the right
					baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(.9375f, .75f, .1875f), new Vector3f(1, 1, .9375f), matrix, facing, getSprite, colour));
			}
		}
		return baseModel;
	}

	@Override
	public NBTTagCompound writeConveyorNBT()
	{
		NBTTagCompound nbt = super.writeConveyorNBT();
		if(cover!=null)
			nbt.setTag("cover", cover.writeToNBT(new NBTTagCompound()));
		return nbt;
	}

	@Override
	public void readConveyorNBT(NBTTagCompound nbt)
	{
		super.readConveyorNBT(nbt);
		cover = new ItemStack(nbt.getCompoundTag("cover"));
	}
}
