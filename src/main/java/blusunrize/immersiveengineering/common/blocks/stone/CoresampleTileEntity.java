/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.items.CoresampleItem;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import net.minecraft.world.storage.loot.LootContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

public class CoresampleTileEntity extends IEBaseTileEntity implements IStateBasedDirectional, ITileDrop, IPlayerInteraction,
		IBlockOverlayText, IBlockBounds
{
	public static TileEntityType<CoresampleTileEntity> TYPE;
	
	public ItemStack coresample = ItemStack.EMPTY;

	public CoresampleTileEntity()
	{
		super(TYPE);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		coresample = ItemStack.read(nbt.getCompound("coresample"));
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.put("coresample", coresample.write(new CompoundNBT()));
	}

	@Override
	public EnumProperty<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return true;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3d hit, LivingEntity entity)
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
		DimensionChunkCoords coords = CoresampleItem.getCoords(coresample);
		if(player.isSneaking())
		{
			if(!world.isRemote)
			{
				ItemEntity entityitem = new ItemEntity(world, getPos().getX()+.5, getPos().getY()+.5, getPos().getZ()+.5,
						coresample);
				entityitem.setDefaultPickupDelay();
				world.removeBlock(pos, false);
				world.addEntity(entityitem);
			}
			return true;
		}
		else if(!heldItem.isEmpty()&&heldItem.getItem()==Items.FILLED_MAP&&coords!=null)
		{
			if(!world.isRemote)
			{
				MapData mapData = FilledMapItem.getMapData(heldItem, player.getEntityWorld());
				if(mapData!=null)
				{
					String ident = "ie:coresample_"+coords.toString();
					CompoundNBT mapTagCompound = heldItem.getOrCreateTag();
					ListNBT nbttaglist = mapTagCompound.getList("Decorations", 10);

					for(int i = 0; i < nbttaglist.size(); i++)
					{
						CompoundNBT tagCompound = (CompoundNBT)nbttaglist.get(i);
						if(ident.equalsIgnoreCase(tagCompound.getString("id")))
						{
							nbttaglist.remove(i);
							mapTagCompound.put("Decorations", nbttaglist);
							mapData.mapDecorations.remove(ident);
							return true;
						}
					}

					double sampleX = coords.x*16+8.5;
					double sampleZ = coords.z*16+8.5;

					int mapScale = 1<<mapData.scale;
					float distX = (float)(sampleX-mapData.xCenter)/(float)mapScale;
					float distZ = (float)(sampleZ-mapData.zCenter)/(float)mapScale;
					if(distX >= -63&&distX <= 63&&distZ >= -63&&distZ <= 63)
					{
						CompoundNBT tagCompound = new CompoundNBT();
						tagCompound.putString("id", ident);
						tagCompound.putByte("type", MapDecoration.Type.TARGET_POINT.getIcon());
						tagCompound.putDouble("x", sampleX);
						tagCompound.putDouble("z", sampleZ);
						tagCompound.putDouble("rot", 180.0);
						MineralMix mineral = CoresampleItem.getMix(coresample);
						tagCompound.putString("mineral", mineral.getId().toString());

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
	public List<ItemStack> getTileDrops(LootContext context)
	{
		return ImmutableList.of(this.coresample);
	}

	@Override
	public void readOnPlacement(LivingEntity placer, ItemStack stack)
	{
		this.coresample = stack.copy();
	}

	private String[] overlay = null;

	@Override
	public String[] getOverlayText(PlayerEntity player, RayTraceResult mop, boolean hammer)
	{
		DimensionChunkCoords dimPos = CoresampleItem.getCoords(coresample);
		if(dimPos!=null)
		{
			if(overlay==null)
			{
				overlay = new String[3];
				overlay[0] = I18n.format(Lib.CHAT_INFO+"coresample.noMineral");
				MineralMix mineral = CoresampleItem.getMix(coresample);
				if(mineral!=null)
				{
					String unloc = mineral.getTranslationKey();
					String loc = I18n.format(unloc);
					if(unloc.equals(loc))
						loc = mineral.getPlainName();
					overlay[0] = TextFormatting.GOLD+I18n.format(Lib.CHAT_INFO+"coresample.mineral", loc);
				}

				String s0 = (dimPos.x*16)+", "+(dimPos.z*16);
				String s1 = (dimPos.x*16+16)+", "+(dimPos.z*16+16);
				//TODO
				String name = dimPos.dimension.getRegistryName().getPath();
				if(name.toLowerCase(Locale.ENGLISH).startsWith("the_"))
					name = name.substring(4);
				overlay[1] = Utils.toCamelCase(name);
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

	private static final VoxelShape AABB_CORESAMPLE_X = VoxelShapes.create(0, 0, .28125f, 1, 1, .71875f);
	private static final VoxelShape AABB_CORESAMPLE_Z = VoxelShapes.create(.28125f, 0, 0, .71875f, 1, 1);

	@Override
	public VoxelShape getBlockBounds(@Nullable ISelectionContext ctx)
	{
		return getFacing().getAxis()==Axis.Z?AABB_CORESAMPLE_Z: AABB_CORESAMPLE_X;
	}
}