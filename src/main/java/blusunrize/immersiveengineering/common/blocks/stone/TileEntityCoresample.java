/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;

import javax.annotation.Nullable;
import java.util.Locale;

public class TileEntityCoresample extends TileEntityIEBase implements IDirectionalTile, ITileDrop, IPlayerInteraction,
		IBlockOverlayText
{
	public static TileEntityType<TileEntityCoresample> TYPE;
	
	public ItemStack coresample = ItemStack.EMPTY;
	public Direction facing = Direction.NORTH;

	public TileEntityCoresample()
	{
		super(TYPE);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		coresample = ItemStack.read(nbt.getCompound("coresample"));
		facing = Direction.byIndex(nbt.getInt("facing"));
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.put("coresample", coresample.write(new CompoundNBT()));
		nbt.putInt("facing", facing.ordinal());
	}

	@Override
	public Direction getFacing()
	{
		return facing;
	}

	@Override
	public void setFacing(Direction facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 2;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return true;
	}

	@Override
	public boolean canHammerRotate(Direction side, float hitX, float hitY, float hitZ, LivingEntity entity)
	{
		return true;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return true;
	}

	@Override
	public boolean interact(Direction side, PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(player.isSneaking())
		{
			if(!world.isRemote)
			{
				ItemEntity entityitem = new ItemEntity(world, getPos().getX()+.5, getPos().getY()+.5, getPos().getZ()+.5, getTileDrop(player, world.getBlockState(getPos())));
				entityitem.setDefaultPickupDelay();
				world.removeBlock(getPos());
				world.spawnEntity(entityitem);
			}
			return true;
		}
		else if(!heldItem.isEmpty()&&heldItem.getItem()==Items.FILLED_MAP&&ItemNBTHelper.hasKey(coresample, "coords"))
		{
			if(!world.isRemote)
			{
				MapData mapData = FilledMapItem.getMapData(heldItem, player.getEntityWorld());
				if(mapData!=null)
				{
					int[] coords = ItemNBTHelper.getIntArray(coresample, "coords");
					String ident = "ie:coresample_"+coords[0]+";"+coords[1]+";"+coords[2];
					CompoundNBT mapTagCompound = ItemNBTHelper.getTag(heldItem);
					ListNBT nbttaglist = mapTagCompound.getList("Decorations", 10);

					for(int i = 0; i < nbttaglist.size(); i++)
					{
						CompoundNBT tagCompound = (CompoundNBT)nbttaglist.get(i);
						if(ident.equalsIgnoreCase(tagCompound.getString("id")))
						{
							nbttaglist.removeTag(i);
							mapTagCompound.put("Decorations", nbttaglist);
							mapData.mapDecorations.remove(ident);
							return true;
						}
					}

					double sampleX = coords[1]*16+8.5;
					double sampleZ = coords[2]*16+8.5;

					int mapScale = 1<<mapData.scale;
					float distX = (float)(sampleX-mapData.xCenter)/(float)mapScale;
					float distZ = (float)(sampleZ-mapData.zCenter)/(float)mapScale;
					if(distX >= -63&&distX <= 63&&distZ >= -63&&distZ <= 63)
					{
						CompoundNBT tagCompound = new CompoundNBT();
						tagCompound.putString("id", ident);
						tagCompound.setByte("type", MapDecoration.Type.TARGET_POINT.getIcon());
						tagCompound.setDouble("x", sampleX);
						tagCompound.setDouble("z", sampleZ);
						tagCompound.setDouble("rot", 180.0);

						nbttaglist.add(tagCompound);
						mapTagCompound.put("Decorations", nbttaglist);
					}
					else
						player.sendMessage(new TranslationTextComponent(Lib.CHAT_INFO+"coresample.mapFail"));
				}
			}
			return true;
		}
		return false;
	}

	//TODO @Override
	@Nullable
	public ITextComponent getDisplayName()
	{
		if(coresample.hasDisplayName())
			return coresample.getDisplayName();
		else
			return new TranslationTextComponent("item.immersiveengineering.coresample.name");
	}

	@Override
	public ItemStack getTileDrop(PlayerEntity player, BlockState state)
	{
		return this.coresample;
	}

	@Override
	public void readOnPlacement(LivingEntity placer, ItemStack stack)
	{
		this.coresample = stack.copy();
	}

	@Override
	public boolean preventInventoryDrop()
	{
		return true;
	}

	private String[] overlay = null;

	@Override
	public String[] getOverlayText(PlayerEntity player, RayTraceResult mop, boolean hammer)
	{
		if(coresample!=null&&ItemNBTHelper.hasKey(coresample, "coords"))
		{
			if(overlay==null)
			{
				overlay = new String[3];
				int[] coords = ItemNBTHelper.getIntArray(coresample, "coords");
				String dimName = ItemNBTHelper.getString(coresample, "dimension");
				overlay[0] = I18n.format(Lib.CHAT_INFO+"coresample.noMineral");
				if(ItemNBTHelper.hasKey(coresample, "mineral"))
				{
					String mineral = ItemNBTHelper.getString(coresample, "mineral");
					String unloc = Lib.DESC_INFO+"mineral."+mineral;
					String loc = I18n.format(unloc);
					overlay[0] = TextFormatting.GOLD+I18n.format(Lib.CHAT_INFO+"coresample.mineral", (unloc.equals(loc)?mineral: loc));
				}

				String s0 = (coords[1]*16)+", "+(coords[2]*16);
				String s1 = (coords[1]*16+16)+", "+(coords[2]*16+16);
				String name = dimName;//TODO
				if(name.toLowerCase(Locale.ENGLISH).startsWith("the "))
					name = name.substring(4);
				overlay[1] = name;
				overlay[2] = I18n.format(Lib.CHAT_INFO+"coresample.pos", s0, s1);
			}
			return overlay;
		}
		return new String[0];
	}

	@Override
	public boolean useNixieFont(PlayerEntity player, RayTraceResult mop)
	{
		return false;
	}
}