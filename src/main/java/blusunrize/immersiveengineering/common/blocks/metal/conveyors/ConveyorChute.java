/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author BluSunrize - 18.07.2019
 */
public class ConveyorChute extends ConveyorVertical
{
	private static final String NBT_POS = "immersiveengineering:chutePos";
	private static final String NBT_TIME = "immersiveengineering:chuteTime";

	private int sheetmetalType = BlockTypes_MetalsAll.IRON.getMeta();
	private boolean diagonal = false;


	private ConveyorChute(int sheetmetalType)
	{
		this.sheetmetalType = sheetmetalType;
	}

	@Override
	public String getModelCacheKey(TileEntity tile, EnumFacing facing)
	{
		String key = ConveyorHandler.reverseClassRegistry.get(this.getClass()).toString();
		key += "f"+facing.ordinal();
		key += "a"+(isActive(tile)?1: 0);
		key += "c"+getDyeColour();
		key += "t"+sheetmetalType;
		key += "d"+diagonal;
		key += "w";
		for(EnumFacing d : EnumFacing.HORIZONTALS)
			key += renderWall(tile, d, facing)?"1": "0";
		return key;
	}

	@Override
	public EnumFacing[] sigTransportDirections(TileEntity conveyorTile, EnumFacing facing)
	{
		return new EnumFacing[]{EnumFacing.DOWN, facing};
	}

	@Override
	public void onEntityCollision(TileEntity tile, Entity entity, EnumFacing facing)
	{
		boolean contact = false;
		if(!diagonal)
			contact = entity.posY-tile.getPos().getY() <= .125;
		else if(facing==EnumFacing.NORTH)
			contact = entity.posZ-tile.getPos().getZ() <= .125;
		else if(facing==EnumFacing.SOUTH)
			contact = entity.posZ-tile.getPos().getZ() >= .875;
		else if(facing==EnumFacing.WEST)
			contact = entity.posX-tile.getPos().getX() <= .125;
		else if(facing==EnumFacing.EAST)
			contact = entity.posX-tile.getPos().getX() >= .875;
		if(this.diagonal&&entity.posY-tile.getPos().getY() <= .625)
		{
			long time = System.currentTimeMillis();
			String hash = Integer.toHexString(tile.getPos().hashCode());
			if(entity.width > 0.75||entity.height > 0.75)
			{
				double py = entity.height > 1?tile.getPos().getY()-(entity.height-1): entity.posY;
				entity.setPosition(entity.posX+facing.getXOffset(), py, entity.posZ+facing.getZOffset());
			}
			else
			{
				if(entity.motionY==0)
					entity.motionY = 0.015;
				entity.motionX = facing.getXOffset();
				entity.motionZ = facing.getZOffset();
			}

			if(!contact&&(!entity.getEntityData().hasKey(NBT_POS)||!hash.equals(entity.getEntityData().getString(NBT_POS))||time-entity.getEntityData().getLong(NBT_TIME) > 1000))
			{
				tile.getWorld().playSound(null, entity.posX, entity.posY, entity.posZ, IESounds.chute, SoundCategory.BLOCKS, .6f+(.4f*tile.getWorld().rand.nextFloat()), .5f+(.5f*tile.getWorld().rand.nextFloat()));
				entity.getEntityData().setString(NBT_POS, hash);
				entity.getEntityData().setLong(NBT_TIME, time);
			}
		}

		if(entity instanceof EntityItem)
		{
			EntityItem item = (EntityItem)entity;
			item.setPickupDelay(10);

			if(!contact)
			{
				item.setNoDespawn(); //misnamed, actually sets despawn timer to 10 minutes
				ConveyorHandler.applyMagnetSupression(entity, (IConveyorTile)tile);
			}
			else
			{
				TileEntity inventoryTile = Utils.getExistingTileEntity(tile.getWorld(), diagonal?tile.getPos().offset(facing): tile.getPos().down());
				if(!(inventoryTile instanceof IConveyorTile))
				{
					ConveyorHandler.revertMagnetSupression(entity, (IConveyorTile)tile);
					if(!tile.getWorld().isRemote&&inventoryTile!=null)
					{
						ItemStack stack = item.getItem();
						if(!stack.isEmpty())
						{
							ItemStack ret = Utils.insertStackIntoInventory(inventoryTile, stack, diagonal?facing.getOpposite(): EnumFacing.UP);
							if(ret.isEmpty())
								entity.setDead();
							else if(ret.getCount() < stack.getCount())
								item.setItem(ret);
						}
					}
				}
			}
		}
	}

	@Override
	public void onItemDeployed(TileEntity tile, EntityItem entity, EnumFacing facing)
	{
		entity.setPickupDelay(10);
	}

	@Override
	public boolean playerInteraction(TileEntity tile, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ, EnumFacing side)
	{
		if(Utils.isHammer(heldItem)&&player.isSneaking())
		{
			this.diagonal = !this.diagonal;
			return true;
		}
		return false;
	}

	static final List<AxisAlignedBB> selectionBoxes = Collections.singletonList(Block.FULL_BLOCK_AABB);

	@Override
	public List<AxisAlignedBB> getSelectionBoxes(TileEntity tile, EnumFacing facing)
	{
		return selectionBoxes;
	}

	static final AxisAlignedBB[] bounds = {new AxisAlignedBB(0, 0, 0, 1, 1, .0625), new AxisAlignedBB(0, 0, .9375, 1, 1, 1), new AxisAlignedBB(0, 0, 0, .0625, 1, 1), new AxisAlignedBB(.9375, 0, 0, 1, 1, 1)};

	@Override
	public List<AxisAlignedBB> getColisionBoxes(TileEntity tile, EnumFacing facing)
	{
		ArrayList<AxisAlignedBB> list = new ArrayList<>();
		for(EnumFacing f : EnumFacing.HORIZONTALS)
			if(!isInwardConveyor(tile, f)&&(!diagonal||f!=facing))
				list.add(bounds[f.ordinal()-2]);
		if(diagonal)
			list.add(conveyorBounds);
		return list;
	}

	@SideOnly(Side.CLIENT)
	private static IBakedModel[][] chuteModel;
	@SideOnly(Side.CLIENT)
	private static Function<ResourceLocation, TextureAtlasSprite>[] TEXTURE_GETTERS;
	@SideOnly(Side.CLIENT)
	private static boolean initTextures;

	@SideOnly(Side.CLIENT)
	public static void clientInit()
	{
		chuteModel = new IBakedModel[BlockTypes_MetalsAll.values().length][];
		TEXTURE_GETTERS = new Function[BlockTypes_MetalsAll.values().length];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel, @Nullable TileEntity tile, EnumFacing facing)
	{
		if(!initTextures)
		{
			for(BlockTypes_MetalsAll metal : BlockTypes_MetalsAll.values())
				TEXTURE_GETTERS[metal.getMeta()] = rl -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("immersiveengineering:blocks/sheetmetal_"+metal.getName());
			initTextures = true;
		}
		if(this.sheetmetalType < 0||this.sheetmetalType >= TEXTURE_GETTERS.length)
			return baseModel;

		Function<ResourceLocation, TextureAtlasSprite> textureGetter = TEXTURE_GETTERS[this.sheetmetalType];

		if(chuteModel[this.sheetmetalType]==null)
			try
			{
				IModel iModel = ModelLoaderRegistry.getModel(new ResourceLocation("immersiveengineering:block/metal_device/chute.obj"));
				chuteModel[this.sheetmetalType] = new IBakedModel[]{
						iModel.bake(new OBJState(ImmutableList.of("base"), true, ModelRotation.X0_Y180), DefaultVertexFormats.ITEM, textureGetter),
						iModel.bake(new OBJState(ImmutableList.of("base"), true, ModelRotation.X0_Y0), DefaultVertexFormats.ITEM, textureGetter),
						iModel.bake(new OBJState(ImmutableList.of("base"), true, ModelRotation.X0_Y90), DefaultVertexFormats.ITEM, textureGetter),
						iModel.bake(new OBJState(ImmutableList.of("base"), true, ModelRotation.X0_Y270), DefaultVertexFormats.ITEM, textureGetter)
				};
			} catch(Exception ignored)
			{
			}

		float[] colour = {1, 1, 1, 1};
		Matrix4 matrix = new Matrix4(facing);

		baseModel.clear();
		if(diagonal)
		{
			IBlockState bs = tile!=null&&tile.getWorld()!=null?tile.getWorld().getBlockState(tile.getPos()): IEContent.blockConveyor.getDefaultState();
			if(chuteModel[this.sheetmetalType]!=null&&facing.ordinal() >= 2)
				baseModel.addAll(chuteModel[this.sheetmetalType][facing.ordinal()-2].getQuads(bs, null, 0));
		}
		else
		{
			Function<EnumFacing, TextureAtlasSprite> getSprite = f -> textureGetter.apply(null);
			baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0, 0, 0), new Vector3f(.0625f, 1, .0625f), matrix, facing, getSprite, colour));
			baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0, 0, .9375f), new Vector3f(.0625f, 1, 1), matrix, facing, getSprite, colour));
			baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(.9375f, 0, 0), new Vector3f(1, 1, .0625f), matrix, facing, getSprite, colour));
			baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(.9375f, 0, .9375f), new Vector3f(1, 1, 1), matrix, facing, getSprite, colour));
			if(renderWall(tile, EnumFacing.NORTH, facing))
				baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(.0625f, 0, 0), new Vector3f(.9375f, 1, .0625f), matrix, facing, getSprite, colour));
			if(renderWall(tile, EnumFacing.SOUTH, facing))
				baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(.0625f, 0, .9375f), new Vector3f(.9375f, 1, 1), matrix, facing, getSprite, colour));
			if(renderWall(tile, EnumFacing.WEST, facing))
				baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0, 0, .0625f), new Vector3f(.0625f, 1, .9375f), matrix, facing, getSprite, colour));
			if(renderWall(tile, EnumFacing.EAST, facing))
				baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(.9375f, 0, .0625f), new Vector3f(1, 1, .9375f), matrix, facing, getSprite, colour));
		}
		return baseModel;
	}

	private boolean renderWall(@Nullable TileEntity tile, EnumFacing direction, EnumFacing facing)
	{
		return tile==null||!isInwardConveyor(tile, Utils.rotateFacingTowardsDir(direction, facing));
	}

	@Override
	public NBTTagCompound writeConveyorNBT()
	{
		NBTTagCompound nbt = super.writeConveyorNBT();
		nbt.setInteger("sheetmetalType", sheetmetalType);
		nbt.setBoolean("diagonal", diagonal);
		return nbt;
	}

	@Override
	public void readConveyorNBT(NBTTagCompound nbt)
	{
		super.readConveyorNBT(nbt);
		sheetmetalType = nbt.getInteger("sheetmetalType");
		diagonal = nbt.getBoolean("diagonal");
	}

	public static class ConveyorChuteIron extends ConveyorChute
	{
		public ConveyorChuteIron()
		{
			super(BlockTypes_MetalsAll.IRON.getMeta());
		}
	}

	public static class ConveyorChuteSteel extends ConveyorChute
	{
		public ConveyorChuteSteel()
		{
			super(BlockTypes_MetalsAll.STEEL.getMeta());
		}
	}

	public static class ConveyorChuteAluminum extends ConveyorChute
	{
		public ConveyorChuteAluminum()
		{
			super(BlockTypes_MetalsAll.ALUMINUM.getMeta());
		}
	}

	public static class ConveyorChuteCopper extends ConveyorChute
	{
		public ConveyorChuteCopper()
		{
			super(BlockTypes_MetalsAll.COPPER.getMeta());
		}
	}
}
