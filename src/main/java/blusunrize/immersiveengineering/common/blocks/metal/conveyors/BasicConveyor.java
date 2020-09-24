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
import blusunrize.immersiveengineering.api.utils.EntityCollisionTracker;
import blusunrize.immersiveengineering.api.utils.shapes.CachedVoxelShapes;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDecoration;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.WoodenDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.ConveyorBeltTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.collect.ImmutableList;
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
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author BluSunrize - 20.08.2016
 */
public class BasicConveyor implements IConveyorBelt
{
	public static final ResourceLocation NAME = new ResourceLocation(ImmersiveEngineering.MODID, "basic");

	public Block cover = Blocks.AIR;

	ConveyorDirection direction = ConveyorDirection.HORIZONTAL;
	@Nullable
	DyeColor dyeColour = null;
	private final TileEntity tile;
	protected final EntityCollisionTracker collisionTracker = new EntityCollisionTracker(10);

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
		return true;
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
	public void onEntityCollision(@Nonnull Entity entity)
	{
		collisionTracker.onEntityCollided(entity);
		IConveyorBelt.super.onEntityCollision(entity);
		if(allowCovers()&&entity instanceof ItemEntity)
			((ItemEntity)entity).setPickupDelay(10);
	}

	@Override
	public boolean isBlocked()
	{
		return collisionTracker.getCollidedInRange(getTile().getWorld().getGameTime()) > 2;
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

	private static final AxisAlignedBB topBox = new AxisAlignedBB(0, .75, 0, 1, 1, 1);

	private static final CachedVoxelShapes<ShapeKey> SHAPES = new CachedVoxelShapes<>(BasicConveyor::getBoxes);

	@Override
	public VoxelShape getCollisionShape()
	{
		VoxelShape baseShape = IConveyorBelt.super.getCollisionShape();
		if(allowCovers())
			return SHAPES.get(new ShapeKey(this, false, baseShape));
		return baseShape;
	}

	@Override
	public VoxelShape getSelectionShape()
	{
		if(allowCovers())
			return SHAPES.get(new ShapeKey(this, false, null));
		return IConveyorBelt.super.getSelectionShape();
	}

	private static List<AxisAlignedBB> getBoxes(ShapeKey key)
	{
		List<AxisAlignedBB> ret = new ArrayList<>();
		if(key.superShape!=null)
			ret.addAll(key.superShape.toBoundingBoxList());
		if(key.direction==ConveyorDirection.HORIZONTAL)
		{
			if(!key.collision)
				return ImmutableList.of(FULL_BLOCK.getBoundingBox());
			else
				ret.add(topBox);
		}
		else
		{
			boolean up = key.direction==ConveyorDirection.UP;
			boolean collision = key.collision;
			Direction facing = key.facing;
			ret.add(new AxisAlignedBB(
					(facing==Direction.WEST&&!up)||(facing==Direction.EAST&&up)?.5: 0,
					collision?1.75: .5,
					(facing==Direction.NORTH&&!up)||(facing==Direction.SOUTH&&up)?.5: 0,
					(facing==Direction.WEST&&up)||(facing==Direction.EAST&&!up)?.5: 1,
					2,
					(facing==Direction.NORTH&&up)||(facing==Direction.SOUTH&&!up)?.5: 1
			));
			ret.add(new AxisAlignedBB(
					(facing==Direction.WEST&&up)||(facing==Direction.EAST&&!up)?.5: 0,
					collision?1.25: 0,
					(facing==Direction.NORTH&&up)||(facing==Direction.SOUTH&&!up)?.5: 0,
					(facing==Direction.WEST&&!up)||(facing==Direction.EAST&&up)?.5: 1,
					1.5,
					(facing==Direction.NORTH&&!up)||(facing==Direction.SOUTH&&up)?.5: 1
			));
		}
		return ret;
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

			Function<Vector3d[], Vector3d[]> vertexTransformer = conDir==ConveyorDirection.HORIZONTAL?vertices -> vertices: vertices -> {
				Vector3d[] ret = new Vector3d[vertices.length];
				for(int i = 0; i < ret.length; i++)
					ret[i] = new Vector3d(vertices[i].x, vertices[i].y+(vertices[i].z==(conDir==ConveyorDirection.UP?0: 1)?1: 0), vertices[i].z);
				return ret;
			};

			baseModel.addAll(ClientUtils.createBakedBox(new Vector3d(0, .75f, 0), new Vector3d(1, 1, 1), matrix, facing, vertexTransformer, getSprite, colour));

			if(getTile()==null||this.renderWall(getFacing(), 0))
				baseModel.addAll(ClientUtils.createBakedBox(new Vector3d(0, .1875f, 0), new Vector3d(.0625f, .75f, 1), matrix, facing, vertexTransformer, getSpriteHorizontal, colour));
			else
			{
				baseModel.addAll(ClientUtils.createBakedBox(new Vector3d(0, .1875f, 0), new Vector3d(.0625f, .75f, .0625f), matrix, facing, getSpriteHorizontal, colour));
				baseModel.addAll(ClientUtils.createBakedBox(new Vector3d(0, .1875f, .9375f), new Vector3d(.0625f, .75f, 1), matrix, facing, getSpriteHorizontal, colour));
			}

			if(getTile()==null||this.renderWall(getFacing(), 1))
				baseModel.addAll(ClientUtils.createBakedBox(new Vector3d(.9375f, .1875f, 0), new Vector3d(1, .75f, 1), matrix, facing, vertexTransformer, getSpriteHorizontal, colour));
			else
			{
				baseModel.addAll(ClientUtils.createBakedBox(new Vector3d(.9375f, .1875f, 0), new Vector3d(1, .75f, .0625f), matrix, facing, getSpriteHorizontal, colour));
				baseModel.addAll(ClientUtils.createBakedBox(new Vector3d(.9375f, .1875f, .9375f), new Vector3d(1, .75f, 1), matrix, facing, getSpriteHorizontal, colour));
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

	protected final boolean isPowered()
	{
		TileEntity te = getTile();
		if(te instanceof ConveyorBeltTileEntity)
			return ((ConveyorBeltTileEntity)te).isRSPowered();
		else
			return te.getWorld().getRedstonePowerFromNeighbors(te.getPos()) > 0;
	}

	private static class ShapeKey
	{
		private final ConveyorDirection direction;
		private final boolean collision;
		private final Direction facing;
		@Nullable
		private final VoxelShape superShape;

		public ShapeKey(BasicConveyor conveyor, boolean collision, @Nullable VoxelShape superShape)
		{
			this.direction = conveyor.getConveyorDirection();
			this.collision = collision;
			this.facing = conveyor.getFacing();
			this.superShape = superShape;
		}

		@Override
		public boolean equals(Object o)
		{
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;
			ShapeKey shapeKey = (ShapeKey)o;
			return collision==shapeKey.collision&&
					direction==shapeKey.direction&&
					facing==shapeKey.facing&&
					Objects.equals(superShape, shapeKey.superShape);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(direction, collision, facing, superShape);
		}
	}
}