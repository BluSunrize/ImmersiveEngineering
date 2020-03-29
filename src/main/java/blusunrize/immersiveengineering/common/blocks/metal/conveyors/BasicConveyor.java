/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorBelt;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDecoration;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.WoodenDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author BluSunrize - 20.08.2016
 */
//TODO support covers in here, rather than having to make a covered and a non-covered version of every belt?
public class BasicConveyor implements IConveyorBelt
{
	public static final ResourceLocation NAME = new ResourceLocation(ImmersiveEngineering.MODID, "basic");

	public Block cover = Blocks.AIR;

	ConveyorDirection direction = ConveyorDirection.HORIZONTAL;
	@Nullable
	DyeColor dyeColour = null;
	private final TileEntity tile;

	public BasicConveyor(TileEntity tile)
	{
		this.tile = tile;
	}

	@Override
	public TileEntity getTile()
	{
		return tile;
	}

	@Override
	public ConveyorDirection getConveyorDirection()
	{
		return direction;
	}

	@Override
	public boolean changeConveyorDirection()
	{
		if(!tile.getWorld().isRemote)
			direction = direction==ConveyorDirection.HORIZONTAL?ConveyorDirection.UP: direction==ConveyorDirection.UP?ConveyorDirection.DOWN: ConveyorDirection.HORIZONTAL;
		return true;
	}

	@Override
	public boolean setConveyorDirection(ConveyorDirection dir)
	{
		direction = dir;
		return true;
	}

	@Override
	public boolean isActive()
	{
		return tile.getWorld().getRedstonePowerFromNeighbors(tile.getPos()) <= 0;
	}

	protected boolean allowCovers()
	{
		return false;
	}

	public static Block getDefaultCover()
	{
		return MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD);
	}

	@Override
	public void onEntityCollision(Entity entity)
	{
		IConveyorBelt.super.onEntityCollision(entity);
		if(allowCovers()&&entity instanceof ItemEntity)
			((ItemEntity)entity).setPickupDelay(10);
	}

	@Override
	public void onItemDeployed(ItemEntity entity)
	{
		IConveyorBelt.super.onItemDeployed(entity);
		if(allowCovers())
			entity.setPickupDelay(10);
	}

	@Override
	public boolean playerInteraction(PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ, Direction side)
	{
		if(allowCovers())
			return handleCoverInteraction(player, hand, heldItem);
		return false;
	}

	/* ============ NBT ============ */

	@Override
	public CompoundNBT writeConveyorNBT()
	{
		CompoundNBT nbt = new CompoundNBT();
		nbt.putInt("direction", direction.ordinal());
		if(dyeColour!=null)
			nbt.putInt("dyeColour", dyeColour.getId());
		if(allowCovers()&&cover!=Blocks.AIR)
			nbt.putString("cover", ForgeRegistries.BLOCKS.getKey(cover).toString());
		return nbt;
	}

	@Override
	public void readConveyorNBT(CompoundNBT nbt)
	{
		direction = ConveyorDirection.values()[nbt.getInt("direction")];
		if(nbt.contains("dyeColour", NBT.TAG_INT))
			dyeColour = DyeColor.byId(nbt.getInt("dyeColour"));
		else
			dyeColour = null;
		if(allowCovers())
			cover = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(nbt.getString("cover")));
	}

	/* ============ RENDERING ============ */

	public static ResourceLocation texture_on = new ResourceLocation("immersiveengineering:block/conveyor/conveyor");
	public static ResourceLocation texture_off = new ResourceLocation("immersiveengineering:block/conveyor/off");

	@Override
	public ResourceLocation getActiveTexture()
	{
		return texture_on;
	}

	@Override
	public ResourceLocation getInactiveTexture()
	{
		return texture_off;
	}

	@Override
	public boolean canBeDyed()
	{
		return true;
	}

	@Override
	public boolean setDyeColour(DyeColor colour)
	{
		if(colour==this.dyeColour)
			return false;
		this.dyeColour = colour;
		return true;
	}

	@Override
	public DyeColor getDyeColour()
	{
		return this.dyeColour;
	}

	@Override
	public String getModelCacheKey()
	{
		String key = IConveyorBelt.super.getModelCacheKey();
		if(allowCovers()&&cover!=Blocks.AIR)
			key += "s"+cover.getRegistryName();
		return key;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel)
	{
		if(allowCovers())
			addCoverToQuads(baseModel);
		return baseModel;
	}

	/* ============ AABB ============ */

	static final AxisAlignedBB topBox = new AxisAlignedBB(0, .75, 0, 1, 1, 1);

	@Override
	public List<AxisAlignedBB> getColisionBoxes()
	{
		List<AxisAlignedBB> list = IConveyorBelt.super.getColisionBoxes();
		if(allowCovers())
			if(getConveyorDirection()==ConveyorDirection.HORIZONTAL)
				list.add(topBox);
			else
			{
				boolean up = getConveyorDirection()==ConveyorDirection.UP;
				list.add(new AxisAlignedBB((getFacing()==Direction.WEST&&!up)||(getFacing()==Direction.EAST&&up)?.5: 0, 1.75, (getFacing()==Direction.NORTH&&!up)||(getFacing()==Direction.SOUTH&&up)?.5: 0, (getFacing()==Direction.WEST&&up)||(getFacing()==Direction.EAST&&!up)?.5: 1, 2, (getFacing()==Direction.NORTH&&up)||(getFacing()==Direction.SOUTH&&!up)?.5: 1));
				list.add(new AxisAlignedBB((getFacing()==Direction.WEST&&up)||(getFacing()==Direction.EAST&&!up)?.5: 0, 1.25, (getFacing()==Direction.NORTH&&up)||(getFacing()==Direction.SOUTH&&!up)?.5: 0, (getFacing()==Direction.WEST&&!up)||(getFacing()==Direction.EAST&&up)?.5: 1, 1.5, (getFacing()==Direction.NORTH&&!up)||(getFacing()==Direction.SOUTH&&up)?.5: 1));
			}
		return list;
	}

	@Override
	public List<AxisAlignedBB> getSelectionBoxes()
	{
		if(allowCovers())
			if(getConveyorDirection()==ConveyorDirection.HORIZONTAL)
				return Lists.newArrayList(VoxelShapes.fullCube().getBoundingBox());
			else
			{
				boolean up = getConveyorDirection()==ConveyorDirection.UP;
				return Lists.newArrayList(
						new AxisAlignedBB((getFacing()==Direction.WEST&&!up)||(getFacing()==Direction.EAST&&up)?.5: 0, .5, (getFacing()==Direction.NORTH&&!up)||(getFacing()==Direction.SOUTH&&up)?.5: 0, (getFacing()==Direction.WEST&&up)||(getFacing()==Direction.EAST&&!up)?.5: 1, 2, (getFacing()==Direction.NORTH&&up)||(getFacing()==Direction.SOUTH&&!up)?.5: 1),
						new AxisAlignedBB((getFacing()==Direction.WEST&&up)||(getFacing()==Direction.EAST&&!up)?.5: 0, 0, (getFacing()==Direction.NORTH&&up)||(getFacing()==Direction.SOUTH&&!up)?.5: 0, (getFacing()==Direction.WEST&&!up)||(getFacing()==Direction.EAST&&up)?.5: 1, 1.5, (getFacing()==Direction.NORTH&&!up)||(getFacing()==Direction.SOUTH&&up)?.5: 1));
			}
		return IConveyorBelt.super.getSelectionBoxes();
	}


	/* ============ COVER UTILITY METHODS ============ */

	public static ArrayList<Predicate<Block>> validCoveyorCovers = new ArrayList<>();

	static
	{
		validCoveyorCovers.add(IETags.scaffoldingAlu::contains);
		validCoveyorCovers.add(IETags.scaffoldingSteel::contains);
		validCoveyorCovers.add(input -> input==WoodenDecoration.treatedScaffolding);
		validCoveyorCovers.add(Tags.Blocks.GLASS::contains);
	}

	protected void addCoverToQuads(List<BakedQuad> baseModel)
	{
		Block b = this.cover!=Blocks.AIR?this.cover: getDefaultCover();
		BlockState state = b.getDefaultState();
		IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(state);
		if(model!=null)
		{
			Direction facing = this.getFacing();
			ConveyorDirection conDir = this.getConveyorDirection();

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
			Function<Direction, TextureAtlasSprite> getSpriteHorizontal = f -> f.getAxis()==Axis.Y?null: sprites.containsKey(f)?sprites.get(f): sprite;

			float[] colour = {1, 1, 1, 1};
			Matrix4 matrix = new Matrix4(facing);

			Function<Vec3d[], Vec3d[]> vertexTransformer = conDir==ConveyorDirection.HORIZONTAL?vertices -> vertices: vertices -> {
				Vec3d[] ret = new Vec3d[vertices.length];
				for(int i = 0; i < ret.length; i++)
					ret[i] = new Vec3d(vertices[i].x, vertices[i].y+(vertices[i].z==(conDir==ConveyorDirection.UP?0: 1)?1: 0), vertices[i].z);
				return ret;
			};

			baseModel.addAll(ClientUtils.createBakedBox(new Vec3d(0, .75f, 0), new Vec3d(1, 1, 1), matrix, facing, vertexTransformer, getSprite, colour));

			if(getTile()==null||this.renderWall(getFacing(), 0))
				baseModel.addAll(ClientUtils.createBakedBox(new Vec3d(0, .1875f, 0), new Vec3d(.0625f, .75f, 1), matrix, facing, vertexTransformer, getSpriteHorizontal, colour));
			else
			{
				baseModel.addAll(ClientUtils.createBakedBox(new Vec3d(0, .1875f, 0), new Vec3d(.0625f, .75f, .0625f), matrix, facing, getSpriteHorizontal, colour));
				baseModel.addAll(ClientUtils.createBakedBox(new Vec3d(0, .1875f, .9375f), new Vec3d(.0625f, .75f, 1), matrix, facing, getSpriteHorizontal, colour));
			}

			if(getTile()==null||this.renderWall(getFacing(), 1))
				baseModel.addAll(ClientUtils.createBakedBox(new Vec3d(.9375f, .1875f, 0), new Vec3d(1, .75f, 1), matrix, facing, vertexTransformer, getSpriteHorizontal, colour));
			else
			{
				baseModel.addAll(ClientUtils.createBakedBox(new Vec3d(.9375f, .1875f, 0), new Vec3d(1, .75f, .0625f), matrix, facing, getSpriteHorizontal, colour));
				baseModel.addAll(ClientUtils.createBakedBox(new Vec3d(.9375f, .1875f, .9375f), new Vec3d(1, .75f, 1), matrix, facing, getSpriteHorizontal, colour));
			}
		}
	}

	public void dropCover(PlayerEntity player)
	{
		if(tile!=null&&!tile.getWorld().isRemote&&cover!=Blocks.AIR&&tile.getWorld().getGameRules().getBoolean(GameRules.DO_TILE_DROPS))
		{
			ItemEntity entityitem = player.dropItem(new ItemStack(cover), false);
			if(entityitem!=null)
				entityitem.setNoPickupDelay();
		}
	}

	protected boolean handleCoverInteraction(PlayerEntity player, Hand hand, ItemStack heldItem)
	{
		if(heldItem.isEmpty()&&player.isSneaking()&&cover!=Blocks.AIR)
		{
			dropCover(player);
			this.cover = Blocks.AIR;
			return true;
		}
		else if(!heldItem.isEmpty()&&!player.isSneaking())
		{
			Block heldBlock = Block.getBlockFromItem(heldItem.getItem());
			if(heldBlock!=Blocks.AIR)
				for(Predicate<Block> func : validCoveyorCovers)
					if(func.test(heldBlock))
					{
						if(heldBlock!=cover)
						{
							dropCover(player);
							this.cover = heldBlock;
							heldItem.shrink(1);
							if(heldItem.getCount() <= 0)
								player.setHeldItem(hand, heldItem);
							return true;
						}
					}
		}
		return false;
	}
}