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
import blusunrize.immersiveengineering.common.items.CoresampleItem.ItemData;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.item.component.MapDecorations.Entry;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class CoresampleBlockEntity extends IEBaseBlockEntity implements IStateBasedDirectional, IBlockEntityDrop, IPlayerInteraction,
		IBlockOverlayText, IBlockBounds
{
	public CoresampleItem.ItemData containedSample;

	public CoresampleBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.CORE_SAMPLE.get(), pos, state);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		containedSample = ItemData.CODECS.codec().decode(NbtOps.INSTANCE, nbt.get("containedSample"))
				.result()
				.orElseThrow()
				.getFirst();
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		nbt.put("coresample", ItemData.CODECS.codec().encodeStart(NbtOps.INSTANCE, containedSample).getOrThrow());
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
	public InteractionResult interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(player.isShiftKeyDown())
		{
			if(!level.isClientSide)
			{
				ItemEntity entityitem = new ItemEntity(
						level, player.getX(), player.getY(), player.getZ(), makeSampleStack(), 0, 0, 0
				);
				level.removeBlock(worldPosition, false);
				level.addFreshEntity(entityitem);
			}
			return InteractionResult.sidedSuccess(getLevelNonnull().isClientSide);
		}
		else if(!heldItem.isEmpty()&&heldItem.getItem()==Items.FILLED_MAP)
		{
			if(!level.isClientSide)
			{
				MapItemSavedData mapData = MapItem.getSavedData(heldItem, player.getCommandSenderWorld());
				if(mapData!=null)
				{
					if(mapData.dimension!=containedSample.position().dimension())
					{
						player.sendSystemMessage(Component.translatable(Lib.CHAT_INFO+"coresample.mapDimension"));
						return InteractionResult.sidedSuccess(getLevelNonnull().isClientSide);
					}

					String ident = "ie:coresample_"+containedSample.position().position();
					MapDecorations oldDecorations = heldItem.getOrDefault(DataComponents.MAP_DECORATIONS, MapDecorations.EMPTY);
					if(oldDecorations.decorations().containsKey(ident))
					{
						HashMap<String, Entry> newMap = new HashMap<>(oldDecorations.decorations());
						newMap.remove(ident);
						heldItem.set(DataComponents.MAP_DECORATIONS, new MapDecorations(newMap));
					}
					else
					{
						double sampleX = containedSample.position().x()+.5;
						double sampleZ = containedSample.position().z()+.5;

						int mapScale = 1<<mapData.scale;
						float distX = (float)(sampleX-mapData.centerX)/(float)mapScale;
						float distZ = (float)(sampleZ-mapData.centerZ)/(float)mapScale;
						if(distX >= -63&&distX <= 63&&distZ >= -63&&distZ <= 63)
						{
							MapDecorations.Entry sampleEntry = new MapDecorations.Entry(
									MapDecorationTypes.TARGET_POINT, sampleX, sampleZ, 180
							);
							// TODO
							// tagCompound.put("minerals", CoresampleItem.getSimplifiedMineralList(level, coresample));
							heldItem.set(
									DataComponents.MAP_DECORATIONS, oldDecorations.withDecoration(ident, sampleEntry)
							);
						}
						else
							player.sendSystemMessage(Component.translatable(Lib.CHAT_INFO+"coresample.mapFail"));
					}
				}
			}
			return InteractionResult.sidedSuccess(getLevelNonnull().isClientSide);
		}
		return InteractionResult.PASS;
	}

	//TODO @Override
	// @Nullable
	// public Component getDisplayName()
	// {
	// 	if(coresample.has(DataComponents.CUSTOM_NAME))
	// 		return coresample.getHoverName();
	// 	else
	// 		return Component.translatable("item.immersiveengineering.coresample.name");
	// }

	@Override
	public void getBlockEntityDrop(LootContext context, Consumer<ItemStack> drop)
	{
		drop.accept(makeSampleStack());
	}

	@Override
	public void onBEPlaced(BlockPlaceContext ctx)
	{
		this.containedSample = ctx.getItemInHand().getOrDefault(IEDataComponents.CORESAMPLE, ItemData.EMPTY);
	}

	private Component[] overlay = null;

	@Override
	public Component[] getOverlayText(Player player, HitResult mop, boolean hammer)
	{
		if(overlay==null)
		{
			List<Component> list = new ArrayList<>();
			CoresampleItem.getCoresampleInfo(containedSample, list, ChatFormatting.WHITE, level, false, false);
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

	public ItemStack makeSampleStack()
	{
		ItemStack coresampleStack = Misc.CORESAMPLE.asItem().getDefaultInstance();
		coresampleStack.set(IEDataComponents.CORESAMPLE, this.containedSample);
		return coresampleStack;
	}
}