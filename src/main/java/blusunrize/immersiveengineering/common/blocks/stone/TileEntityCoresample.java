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
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nullable;
import java.util.Locale;

public class TileEntityCoresample extends TileEntityIEBase implements IDirectionalTile, ITileDrop, IPlayerInteraction, IBlockOverlayText
{
	public ItemStack coresample = ItemStack.EMPTY;
	public EnumFacing facing = EnumFacing.NORTH;

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		coresample = new ItemStack(nbt.getCompoundTag("coresample"));
		facing = EnumFacing.byIndex(nbt.getInteger("facing"));
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setTag("coresample", coresample.writeToNBT(new NBTTagCompound()));
		nbt.setInteger("facing", facing.ordinal());
	}

	@Override
	public EnumFacing getFacing()
	{
		return facing;
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 2;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return true;
	}

	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return true;
	}

	@Override
	public boolean canRotate(EnumFacing axis)
	{
		return true;
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(player.isSneaking())
		{
			if(!getWorld().isRemote)
			{
				EntityItem entityitem = new EntityItem(getWorld(), getPos().getX()+.5, getPos().getY()+.5, getPos().getZ()+.5, getTileDrop(player, getWorld().getBlockState(getPos())));
				entityitem.setDefaultPickupDelay();
				getWorld().setBlockToAir(getPos());
				getWorld().spawnEntity(entityitem);
			}
			return true;
		}
		else if(!heldItem.isEmpty()&&heldItem.getItem()==Items.FILLED_MAP&&ItemNBTHelper.hasKey(coresample, "coords"))
		{
			if(!getWorld().isRemote)
			{
				MapData mapData = ((ItemMap)heldItem.getItem()).getMapData(heldItem, player.getEntityWorld());
				if(mapData!=null)
				{
					int[] coords = ItemNBTHelper.getIntArray(coresample, "coords");
					String ident = "ie:coresample_"+coords[0]+";"+coords[1]+";"+coords[2];
					NBTTagCompound mapTagCompound = ItemNBTHelper.getTag(heldItem);
					NBTTagList nbttaglist = mapTagCompound.getTagList("Decorations", 10);

					for(int i=0; i<nbttaglist.tagCount(); i++)
					{
						NBTTagCompound tagCompound = (NBTTagCompound)nbttaglist.get(i);
						if(ident.equalsIgnoreCase(tagCompound.getString("id")))
						{
							nbttaglist.removeTag(i);
							mapTagCompound.setTag("Decorations", nbttaglist);
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
						NBTTagCompound tagCompound = new NBTTagCompound();
						tagCompound.setString("id", ident);
						tagCompound.setByte("type", MapDecoration.Type.TARGET_POINT.getIcon());
						tagCompound.setDouble("x", sampleX);
						tagCompound.setDouble("z", sampleZ);
						tagCompound.setDouble("rot", 180.0);

						nbttaglist.appendTag(tagCompound);
						mapTagCompound.setTag("Decorations", nbttaglist);
					}
					else
						player.sendMessage(new TextComponentTranslation(Lib.CHAT_INFO+"coresample.mapFail"));
				}
			}
			return true;
		}
		return false;
	}

	@Override
	@Nullable
	public ITextComponent getDisplayName()
	{
		return coresample.hasDisplayName()?new TextComponentString(coresample.getDisplayName()): new TextComponentTranslation("item.immersiveengineering.coresample.name");
	}

	@Override
	public ItemStack getTileDrop(EntityPlayer player, IBlockState state)
	{
		return this.coresample;
	}

	@Override
	public void readOnPlacement(EntityLivingBase placer, ItemStack stack)
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
	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer)
	{
		if(coresample!=null&&ItemNBTHelper.hasKey(coresample, "coords"))
		{
			if(overlay==null)
			{
				overlay = new String[3];
				int[] coords = ItemNBTHelper.getIntArray(coresample, "coords");
				overlay[0] = I18n.format(Lib.CHAT_INFO+"coresample.noMineral");
				if(ItemNBTHelper.hasKey(coresample, "mineral"))
				{
					String mineral = ItemNBTHelper.getString(coresample, "mineral");
					String unloc = Lib.DESC_INFO+"mineral."+mineral;
					String loc = I18n.format(unloc);
					overlay[0] = TextFormatting.GOLD+I18n.format(Lib.CHAT_INFO+"coresample.mineral", (unloc.equals(loc)?mineral: loc));
				}

				World world = DimensionManager.getWorld(coords[0]);
				String s0 = (coords[1]*16)+", "+(coords[2]*16);
				String s1 = (coords[1]*16+16)+", "+(coords[2]*16+16);
				if(world!=null&&world.provider!=null)
				{
					String name = world.provider.getDimensionType().getName();
					if(name.toLowerCase(Locale.ENGLISH).startsWith("the "))
						name = name.substring(4);
					overlay[1] = name;
				}
				else
					overlay[1] = "Dimension "+coords[0];
				overlay[2] = I18n.format(Lib.CHAT_INFO+"coresample.pos", s0, s1);
			}
			return overlay;
		}
		return new String[0];
	}

	@Override
	public boolean useNixieFont(EntityPlayer player, RayTraceResult mop)
	{
		return false;
	}
}