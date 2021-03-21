/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.items.CoresampleItem;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.state.Property;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CoresampleTileEntity extends IEBaseTileEntity implements IStateBasedDirectional, ITileDrop, IPlayerInteraction,
		IBlockOverlayText, IBlockBounds
{
	public ItemStack coresample = ItemStack.EMPTY;

	public CoresampleTileEntity()
	{
		super(IETileTypes.CORE_SAMPLE.get());
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
	public Property<Direction> getFacingProperty()
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
	public boolean canHammerRotate(Direction side, Vector3d hit, LivingEntity entity)
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
		ColumnPos coords = CoresampleItem.getCoords(coresample);
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
					if(mapData.dimension!=CoresampleItem.getDimension(coresample))
					{
						player.sendMessage(new TranslationTextComponent(Lib.CHAT_INFO+"coresample.mapDimension"), Util.DUMMY_UUID);
						return true;
					}

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

					double sampleX = coords.x+.5;
					double sampleZ = coords.z+.5;

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
						tagCompound.put("minerals", CoresampleItem.getSimplifiedMineralList(coresample));

						nbttaglist.add(tagCompound);
						mapTagCompound.put("Decorations", nbttaglist);
					}
					else
						player.sendMessage(new TranslationTextComponent(Lib.CHAT_INFO+"coresample.mapFail"), Util.DUMMY_UUID);
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

	private ITextComponent[] overlay = null;

	@Override
	public ITextComponent[] getOverlayText(PlayerEntity player, RayTraceResult mop, boolean hammer)
	{
		if(overlay==null)
		{
			List<ITextComponent> list = new ArrayList<>();
			CoresampleItem.getCoresampleInfo(coresample, list, TextFormatting.WHITE, world, false, false);
			overlay = list.toArray(new ITextComponent[0]);
		}
		return overlay;
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