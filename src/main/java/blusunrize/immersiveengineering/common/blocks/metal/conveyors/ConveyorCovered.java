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
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration1;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_WoodenDecoration;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.collect.Lists;
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
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author BluSunrize - 29.03.2017
 */
public class ConveyorCovered extends ConveyorBasic
{
	public static ArrayList<com.google.common.base.Function<ItemStack, Boolean>> validCoveyorCovers = new ArrayList();

	static
	{
		final ArrayList<ItemStack> scaffolds = Lists.newArrayList(
				new ItemStack(IEContent.blockWoodenDecoration, 1, BlockTypes_WoodenDecoration.SCAFFOLDING.getMeta()),
				new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_1.getMeta()),
				new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_2.getMeta()),
				new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_0.getMeta()),
				new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_1.getMeta()),
				new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_2.getMeta()));
		validCoveyorCovers.add(new com.google.common.base.Function<ItemStack, Boolean>()
		{
			@Nullable
			@Override
			public Boolean apply(@Nullable ItemStack input)
			{
				if(input==null)
					return Boolean.FALSE;
				for(ItemStack stack : scaffolds)
					if(OreDictionary.itemMatches(stack, input, false))
						return Boolean.TRUE;
				return Boolean.FALSE;
			}
		});
		validCoveyorCovers.add(input -> input==null?Boolean.FALSE: Utils.compareToOreName(input, "blockGlass"));
	}

	public ItemStack cover = ItemStack.EMPTY;

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
	public String getModelCacheKey(TileEntity tile, EnumFacing facing)
	{
		String key = super.getModelCacheKey(tile, facing);
		if(!cover.isEmpty())
			key += "s"+cover.getItem().getRegistryName()+cover.getMetadata();
		return key;
	}

	static final ItemStack defaultCover = new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());

	@Override
	@SideOnly(Side.CLIENT)
	public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel, @Nullable TileEntity tile, EnumFacing facing)
	{
		addCoverToQuads(baseModel, tile, facing, () -> this.cover, getConveyorDirection(), new boolean[]{
				tile==null||this.renderWall(tile, facing, 0), tile==null||this.renderWall(tile, facing, 1)
		});
		return baseModel;
	}

	static void addCoverToQuads(List<BakedQuad> baseModel, @Nullable TileEntity tile, EnumFacing facing, Supplier<ItemStack> coverGet, ConveyorDirection conDir, boolean[] walls)
	{
		ItemStack cover = !coverGet.get().isEmpty()?coverGet.get(): defaultCover;
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
			Function<EnumFacing, TextureAtlasSprite> getSpriteHorizontal = f -> f.getAxis()==Axis.Y?null: sprites.containsKey(f)?sprites.get(f): sprite;

			float[] colour = {1, 1, 1, 1};
			Matrix4 matrix = new Matrix4(facing);

			Function<Vector3f[], Vector3f[]> vertexTransformer = conDir==ConveyorDirection.HORIZONTAL?vertices -> vertices: vertices -> {
				Vector3f[] ret = new Vector3f[vertices.length];
				for(int i = 0; i < ret.length; i++)
					ret[i] = new Vector3f(vertices[i].x, vertices[i].y+(vertices[i].z==(conDir==ConveyorDirection.UP?0: 1)?1: 0), vertices[i].z);
				return ret;
			};

			baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0, .75f, 0), new Vector3f(1, 1, 1), matrix, facing, vertexTransformer, getSprite, colour));
			if(walls[0])
				baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0, .1875f, 0), new Vector3f(.0625f, .75f, 1), matrix, facing, vertexTransformer, getSpriteHorizontal, colour));
			else
			{
				baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0, .1875f, 0), new Vector3f(.0625f, .75f, .0625f), matrix, facing, getSpriteHorizontal, colour));
				baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0, .1875f, .9375f), new Vector3f(.0625f, .75f, 1), matrix, facing, getSpriteHorizontal, colour));
			}
			if(walls[1])
				baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(.9375f, .1875f, 0), new Vector3f(1, .75f, 1), matrix, facing, vertexTransformer, getSpriteHorizontal, colour));
			else
			{
				baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(.9375f, .1875f, 0), new Vector3f(1, .75f, .0625f), matrix, facing, getSpriteHorizontal, colour));
				baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(.9375f, .1875f, .9375f), new Vector3f(1, .75f, 1), matrix, facing, getSpriteHorizontal, colour));
			}
		}
	}


	@Override
	public boolean playerInteraction(TileEntity tile, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ, EnumFacing side)
	{
		return handleCoverInteraction(tile, player, hand, heldItem, () -> cover, (itemStack -> cover = itemStack));
	}

	static boolean handleCoverInteraction(TileEntity tile, EntityPlayer player, EnumHand hand, ItemStack heldItem, Supplier<ItemStack> coverGet, Consumer<ItemStack> coverSet)
	{
		final ItemStack cover = coverGet.get();
		if(heldItem.isEmpty()&&player.isSneaking()&&!cover.isEmpty())
		{
			if(!tile.getWorld().isRemote&&tile.getWorld().getGameRules().getBoolean("doTileDrops"))
			{
				EntityItem entityitem = player.dropItem(cover.copy(), false);
				if(entityitem!=null)
					entityitem.setNoPickupDelay();
			}
			coverSet.accept(ItemStack.EMPTY);
			return true;
		}
		else if(!heldItem.isEmpty()&&!player.isSneaking())
			for(com.google.common.base.Function<ItemStack, Boolean> func : validCoveyorCovers)
				if(func.apply(heldItem)==Boolean.TRUE)
				{
					if(!OreDictionary.itemMatches(cover, heldItem, true))
					{
						if(!tile.getWorld().isRemote&&!cover.isEmpty()&&tile.getWorld().getGameRules().getBoolean("doTileDrops"))
						{
							EntityItem entityitem = player.dropItem(cover.copy(), false);
							if(entityitem!=null)
								entityitem.setNoPickupDelay();
						}
						coverSet.accept(Utils.copyStackWithAmount(heldItem, 1));
						heldItem.shrink(1);
						if(heldItem.getCount() <= 0)
							player.setHeldItem(hand, heldItem);
						return true;
					}
				}
		return false;
	}

	static final AxisAlignedBB topBox = new AxisAlignedBB(0, .75, 0, 1, 1, 1);

	@Override
	public List<AxisAlignedBB> getColisionBoxes(TileEntity tile, EnumFacing facing)
	{
		List<AxisAlignedBB> list = Lists.newArrayList(conveyorBounds);
		if(getConveyorDirection()==ConveyorDirection.HORIZONTAL)
			list.add(topBox);
		else
		{
			boolean up = getConveyorDirection()==ConveyorDirection.UP;
			list.add(new AxisAlignedBB((facing==EnumFacing.WEST&&!up)||(facing==EnumFacing.EAST&&up)?.5: 0, 1.75, (facing==EnumFacing.NORTH&&!up)||(facing==EnumFacing.SOUTH&&up)?.5: 0, (facing==EnumFacing.WEST&&up)||(facing==EnumFacing.EAST&&!up)?.5: 1, 2, (facing==EnumFacing.NORTH&&up)||(facing==EnumFacing.SOUTH&&!up)?.5: 1));
			list.add(new AxisAlignedBB((facing==EnumFacing.WEST&&up)||(facing==EnumFacing.EAST&&!up)?.5: 0, 1.25, (facing==EnumFacing.NORTH&&up)||(facing==EnumFacing.SOUTH&&!up)?.5: 0, (facing==EnumFacing.WEST&&!up)||(facing==EnumFacing.EAST&&up)?.5: 1, 1.5, (facing==EnumFacing.NORTH&&!up)||(facing==EnumFacing.SOUTH&&up)?.5: 1));
		}
		return list;
	}

	@Override
	public List<AxisAlignedBB> getSelectionBoxes(TileEntity tile, EnumFacing facing)
	{
		if(getConveyorDirection()==ConveyorDirection.HORIZONTAL)
			return Lists.newArrayList(Block.FULL_BLOCK_AABB);
		else
		{
			boolean up = getConveyorDirection()==ConveyorDirection.UP;
			List<AxisAlignedBB> list = Lists.newArrayList(
					new AxisAlignedBB((facing==EnumFacing.WEST&&!up)||(facing==EnumFacing.EAST&&up)?.5: 0, .5, (facing==EnumFacing.NORTH&&!up)||(facing==EnumFacing.SOUTH&&up)?.5: 0, (facing==EnumFacing.WEST&&up)||(facing==EnumFacing.EAST&&!up)?.5: 1, 2, (facing==EnumFacing.NORTH&&up)||(facing==EnumFacing.SOUTH&&!up)?.5: 1),
					new AxisAlignedBB((facing==EnumFacing.WEST&&up)||(facing==EnumFacing.EAST&&!up)?.5: 0, 0, (facing==EnumFacing.NORTH&&up)||(facing==EnumFacing.SOUTH&&!up)?.5: 0, (facing==EnumFacing.WEST&&!up)||(facing==EnumFacing.EAST&&up)?.5: 1, 1.5, (facing==EnumFacing.NORTH&&!up)||(facing==EnumFacing.SOUTH&&up)?.5: 1));
			return list;
		}
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