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
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author BluSunrize - 19.05.2017
 */
public class ConveyorExtractCovered extends ConveyorExtract
{
	public ItemStack cover = ItemStack.EMPTY;

	public ConveyorExtractCovered(EnumFacing conveyorDir)
	{
		super(conveyorDir);
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
		ConveyorHandler.applyMagnetSupression(entity, (IConveyorTile)tile);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel, @Nullable TileEntity tile, EnumFacing facing)
	{
		baseModel = super.modifyQuads(baseModel, tile, facing);
		ConveyorCovered.addCoverToQuads(baseModel, tile, facing, () -> cover, ConveyorDirection.HORIZONTAL, new boolean[]{
				tile==null||this.renderWall(tile, facing, 0), tile==null||this.renderWall(tile, facing, 1)
		});
		return baseModel;
	}

	@Override
	public String getModelCacheKey(TileEntity tile, EnumFacing facing)
	{
		String key = super.getModelCacheKey(tile, facing);
		if(!cover.isEmpty())
			key += "s"+cover.getItem().getRegistryName()+cover.getMetadata();
		return key;
	}


	@Override
	public boolean playerInteraction(TileEntity tile, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ, EnumFacing side)
	{
		if(super.playerInteraction(tile, player, hand, heldItem, hitX, hitY, hitZ, side))
			return true;
		return ConveyorCovered.handleCoverInteraction(tile, player, hand, heldItem, () -> cover, (itemStack -> cover = itemStack));
	}

	static final AxisAlignedBB topBox = new AxisAlignedBB(0, .75, 0, 1, 1, 1);

	@Override
	public List<AxisAlignedBB> getColisionBoxes(TileEntity tile, EnumFacing facing)
	{
		List<AxisAlignedBB> list = Lists.newArrayList(conveyorBounds);
		list.add(topBox);
		return list;
	}

	@Override
	public List<AxisAlignedBB> getSelectionBoxes(TileEntity tile, EnumFacing facing)
	{
		return Lists.newArrayList(Block.FULL_BLOCK_AABB);
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