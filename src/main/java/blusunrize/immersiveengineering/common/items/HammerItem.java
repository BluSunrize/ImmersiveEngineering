/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockAdvancementTrigger;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IConfigurableSides;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalBE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHammerInteraction;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.util.orientation.RotationUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HammerItem extends IEBaseItem
{
	public HammerItem()
	{
		super(new Properties().durability(100));// Value is overridden in getMaxDamage
	}

	@Override
	public int getMaxDamage(ItemStack stack)
	{
		return IEServerConfig.getOrDefault(IEServerConfig.TOOLS.hammerDurabiliy);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flagIn)
	{
		super.appendHoverText(stack, ctx, tooltip, flagIn);
		var permissions = stack.getOrDefault(IEDataComponents.MULTIBLOCK_RESTRICTION, MultiblockRestriction.DEFAULT);
		addInfo(tooltip, Lib.DESC_INFO+"multiblocksAllowed", permissions.allowed);
		addInfo(tooltip, Lib.DESC_INFO+"multiblockForbidden", permissions.forbidden);
	}

	private void addInfo(List<Component> tooltip, String titleKey, Optional<List<ResourceLocation>> list)
	{
		if(list.isEmpty())
			return;
		MutableComponent title = Component.translatable(titleKey);
		if(!Screen.hasShiftDown())
			tooltip.add(title.append(" ").append(Component.translatable(Lib.DESC_INFO+"holdShift")));
		else
		{
			tooltip.add(title);
			for(ResourceLocation mbName : list.get())
			{
				IMultiblock multiblock = MultiblockHandler.getByUniqueName(mbName);
				if(multiblock!=null)
					tooltip.add(TextUtils.applyFormat(multiblock.getDisplayName(), ChatFormatting.DARK_GRAY));
			}
		}
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
	{
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Player player = context.getPlayer();
		Direction side = context.getClickedFace();

		/*
			Multiblock Handling
		 */
		var restriction = stack.getOrDefault(IEDataComponents.MULTIBLOCK_RESTRICTION, MultiblockRestriction.DEFAULT);
		final Direction multiblockSide;
		if(side.getAxis()==Axis.Y&&player!=null)
			multiblockSide = Direction.fromYRot(player.getYRot()).getOpposite();
		else
			multiblockSide = side;
		for(MultiblockHandler.IMultiblock mb : MultiblockHandler.getMultiblocks())
			if(mb.isBlockTrigger(world.getBlockState(pos), multiblockSide, world))
			{
				boolean isAllowed;
				if(restriction.allowed.isPresent())
					isAllowed = restriction.allowed.get().contains(mb.getUniqueName());
				else if(restriction.forbidden.isPresent())
					isAllowed = !restriction.forbidden.get().contains(mb.getUniqueName());
				else
					isAllowed = true;
				if(!isAllowed)
					continue;
				if(MultiblockHandler.postMultiblockFormationEvent(player, mb, pos, stack).isCanceled())
					continue;
				if(mb.createStructure(world, pos, multiblockSide, player))
				{
					if(player instanceof ServerPlayer sPlayer)
						MultiblockAdvancementTrigger.INSTANCE.get().trigger(sPlayer, mb, stack);
					return InteractionResult.SUCCESS;
				}
			}

		/*
			Side Configs & Rotation Handling
		 */
		BlockEntity tile = world.getBlockEntity(pos);
		if(tile instanceof IConfigurableSides sideConfig)
		{
			Direction activeSide = ((player!=null)&&player.isShiftKeyDown())?side.getOpposite(): side;
			if(sideConfig.toggleSide(activeSide, player))
				return InteractionResult.SUCCESS;
			else
				return InteractionResult.FAIL;
		}
		else
		{
			boolean rotate = !(tile instanceof IDirectionalBE)&&!(tile instanceof IHammerInteraction);
			if(!rotate&&tile instanceof IDirectionalBE dirBE)
				rotate = dirBE.canHammerRotate(side, context.getClickLocation().subtract(Vec3.atLowerCornerOf(pos)), player);
			if(rotate&&RotationUtil.rotateBlock(world, pos, player!=null&&(player.isShiftKeyDown()!=side.equals(Direction.DOWN))))
				return InteractionResult.SUCCESS;
			else if(!rotate&&tile instanceof IHammerInteraction hammerInteraction)
			{
				if(hammerInteraction.hammerUseSide(side, player, context.getHand(), context.getClickLocation()))
					return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}

	@Nullable
	private static List<ResourceLocation> parseMultiblockNames(ListTag data, @Nullable Player player, String prefix)
	{
		List<ResourceLocation> result = new ArrayList<>();
		for(int i = 0; i < data.size(); ++i)
		{
			String listEntry = data.getString(i);
			ResourceLocation asRL = ResourceLocation.tryParse(listEntry);
			if(asRL==null||MultiblockHandler.getByUniqueName(asRL)==null)
			{
				if(player!=null&&!player.getCommandSenderWorld().isClientSide)
					player.displayClientMessage(Component.literal("Invalid "+prefix+" entry: "+listEntry), false);
				return null;
			}
			result.add(asRL);
		}
		return result;
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player)
	{
		return true;
	}

	@Nonnull
	@Override
	public ItemStack getCraftingRemainingItem(@Nonnull ItemStack stack)
	{
		ItemStack container = stack.copy();
		if(container.hurt(1, ApiUtils.RANDOM_SOURCE, null))
			return ItemStack.EMPTY;
		else
			return container;
	}

	@Override
	public boolean hasCraftingRemainingItem(@Nonnull ItemStack stack)
	{
		return true;
	}

	@Override
	public boolean isEnchantable(@Nonnull ItemStack stack)
	{
		return true;
	}

	@Override
	public int getEnchantmentValue()
	{
		return 14;
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book)
	{
		return EnchantmentHelper.getEnchantments(book).keySet().stream()
				.allMatch(enchantment -> canApplyAtEnchantingTable(stack, enchantment));
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
	{
		return enchantment==Enchantments.BLOCK_EFFICIENCY||enchantment==Enchantments.UNBREAKING||enchantment==Enchantments.MENDING;
	}

	@Override
	public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate)
	{
		return repairCandidate.is(Tags.Items.INGOTS_IRON);
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand)
	{
		if(!player.level().isClientSide&&RotationUtil.rotateEntity(entity, player))
			return InteractionResult.SUCCESS;
		else
			return InteractionResult.PASS;
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state)
	{
		if(isCorrectToolForDrops(stack, state))
			return 6;
		return super.getDestroySpeed(stack, state);
	}

	@Override
	public boolean isCorrectToolForDrops(ItemStack stack, BlockState state)
	{
		return state.is(IETags.hammerHarvestable);
	}

	public record MultiblockRestriction(
			Optional<List<ResourceLocation>> allowed,
			Optional<List<ResourceLocation>> forbidden
	)
	{
		public static final Codec<MultiblockRestriction> CODEC = RecordCodecBuilder.create(inst -> inst.group(
				ResourceLocation.CODEC.listOf().optionalFieldOf("allowed").forGetter(MultiblockRestriction::allowed),
				ResourceLocation.CODEC.listOf().optionalFieldOf("forbidden").forGetter(MultiblockRestriction::forbidden)
		).apply(inst, MultiblockRestriction::new));
		public static final StreamCodec<ByteBuf, MultiblockRestriction> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list())), MultiblockRestriction::allowed,
				ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list())), MultiblockRestriction::forbidden,
				MultiblockRestriction::new
		);
		public static final MultiblockRestriction DEFAULT = new MultiblockRestriction(
				Optional.empty(), Optional.empty()
		);

		public MultiblockRestriction
		{
			if(allowed.isPresent())
				allowed = Optional.of(List.copyOf(allowed.get()));
			if(forbidden.isPresent())
				forbidden = Optional.of(List.copyOf(forbidden.get()));
		}
	}
}
