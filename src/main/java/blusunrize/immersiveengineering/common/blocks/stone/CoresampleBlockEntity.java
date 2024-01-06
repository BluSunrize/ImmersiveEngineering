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
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.items.CoresampleItem;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CoresampleBlockEntity extends IEBaseBlockEntity implements IStateBasedDirectional, IBlockEntityDrop, IPlayerInteraction,
		IBlockOverlayText, IBlockBounds
{
	public ItemStack coresample = ItemStack.EMPTY;

	public CoresampleBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.CORE_SAMPLE.get(), pos, state);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		coresample = ItemStack.of(nbt.getCompound("coresample"));
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		nbt.put("coresample", coresample.save(new CompoundTag()));
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
	public boolean interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		ColumnPos coords = CoresampleItem.getCoords(coresample);
		if(player.isShiftKeyDown())
		{
			if(!level.isClientSide)
			{
				ItemEntity entityitem = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), coresample, 0, 0, 0);
				level.removeBlock(worldPosition, false);
				level.addFreshEntity(entityitem);
			}
			return true;
		}
		else if(!heldItem.isEmpty()&&heldItem.getItem()==Items.FILLED_MAP&&coords!=null)
		{
			if(!level.isClientSide)
			{
				MapItemSavedData mapData = MapItem.getSavedData(heldItem, player.getCommandSenderWorld());
				if(mapData!=null)
				{
					if(mapData.dimension!=CoresampleItem.getDimension(coresample))
					{
						player.sendSystemMessage(Component.translatable(Lib.CHAT_INFO+"coresample.mapDimension"));
						return true;
					}

					String ident = "ie:coresample_"+coords;
					CompoundTag mapTagCompound = heldItem.getOrCreateTag();
					ListTag nbttaglist = mapTagCompound.getList("Decorations", 10);

					for(int i = 0; i < nbttaglist.size(); i++)
					{
						CompoundTag tagCompound = (CompoundTag)nbttaglist.get(i);
						if(ident.equalsIgnoreCase(tagCompound.getString("id")))
						{
							nbttaglist.remove(i);
							mapTagCompound.put("Decorations", nbttaglist);
							//TODO mapData.removeDecoration(ident);
							return true;
						}
					}

					double sampleX = coords.x()+.5;
					double sampleZ = coords.z()+.5;

					int mapScale = 1<<mapData.scale;
					float distX = (float)(sampleX-mapData.centerX)/(float)mapScale;
					float distZ = (float)(sampleZ-mapData.centerZ)/(float)mapScale;
					if(distX >= -63&&distX <= 63&&distZ >= -63&&distZ <= 63)
					{
						CompoundTag tagCompound = new CompoundTag();
						tagCompound.putString("id", ident);
						tagCompound.putByte("type", MapDecoration.Type.TARGET_POINT.getIcon());
						tagCompound.putDouble("x", sampleX);
						tagCompound.putDouble("z", sampleZ);
						tagCompound.putDouble("rot", 180.0);
						tagCompound.put("minerals", CoresampleItem.getSimplifiedMineralList(level, coresample));

						nbttaglist.add(tagCompound);
						mapTagCompound.put("Decorations", nbttaglist);
					}
					else
						player.sendSystemMessage(Component.translatable(Lib.CHAT_INFO+"coresample.mapFail"));
				}
			}
			return true;
		}
		return false;
	}

	//TODO @Override
	@Nullable
	public Component getDisplayName()
	{
		if(coresample.hasCustomHoverName())
			return coresample.getHoverName();
		else
			return Component.translatable("item.immersiveengineering.coresample.name");
	}

	@Override
	public void getBlockEntityDrop(LootContext context, Consumer<ItemStack> drop)
	{
		drop.accept(this.coresample);
	}

	@Override
	public void onBEPlaced(BlockPlaceContext ctx)
	{
		this.coresample = ctx.getItemInHand().copy();
	}

	private Component[] overlay = null;

	@Override
	public Component[] getOverlayText(Player player, HitResult mop, boolean hammer)
	{
		if(overlay==null)
		{
			List<Component> list = new ArrayList<>();
			CoresampleItem.getCoresampleInfo(coresample, list, ChatFormatting.WHITE, level, false, false);
			overlay = list.toArray(new Component[0]);
		}
		return overlay;
	}

	@Override
	public boolean useNixieFont(Player player, HitResult mop)
	{
		return false;
	}

	private static final VoxelShape AABB_CORESAMPLE_X = Shapes.box(0, 0, .28125f, 1, 1, .71875f);
	private static final VoxelShape AABB_CORESAMPLE_Z = Shapes.box(.28125f, 0, 0, .71875f, 1, 1);

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return getFacing().getAxis()==Axis.Z?AABB_CORESAMPLE_Z: AABB_CORESAMPLE_X;
	}
}