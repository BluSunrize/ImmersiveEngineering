/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IConfigurableSides;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalBE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHammerInteraction;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.RotationUtil;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.advancements.IEAdvancements;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class HammerItem extends IEBaseItem implements ITool
{
	public HammerItem()
	{
		super(new Properties().defaultDurability(100));// Value is overridden in getMaxDamage
	}

	@Override
	public int getMaxDamage(ItemStack stack)
	{
		return IEServerConfig.TOOLS.hammerDurabiliy.getOrDefault();
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
	{
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		if(ItemNBTHelper.hasKey(stack, "multiblockPermission"))
		{
			ListTag tagList = stack.getOrCreateTag().getList("multiblockPermission", 8);
			String s = I18n.get(Lib.DESC_INFO+"multiblocksAllowed");
			addInfo(tooltip, s, tagList);
		}
		if(ItemNBTHelper.hasKey(stack, "multiblockInterdiction"))
		{
			ListTag tagList = stack.getOrCreateTag().getList("multiblockInterdiction", 8);
			String s = I18n.get(Lib.DESC_INFO+"multiblockForbidden");
			addInfo(tooltip, s, tagList);
		}
	}

	private void addInfo(List<Component> list, String s, ListTag tagList)
	{
		if(!Screen.hasShiftDown())
			list.add(new TranslatableComponent(Lib.DESC_INFO+"holdShift", s));
		else
		{
			list.add(new TextComponent(s));
			for(int i = 0; i < tagList.size(); i++)
				list.add(TextUtils.applyFormat(
						new TranslatableComponent(Lib.DESC_INFO+"multiblock."+tagList.getString(i)),
						ChatFormatting.DARK_GRAY
				));
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
		List<ResourceLocation> permittedMultiblocks = null;
		List<ResourceLocation> interdictedMultiblocks = null;
		if(ItemNBTHelper.hasKey(stack, "multiblockPermission"))
		{
			ListTag list = stack.getOrCreateTag().getList("multiblockPermission", NBT.TAG_STRING);
			Optional<List<ResourceLocation>> permittedMultiblocksResult = parseUserDefinedRLs(list, player, "permission");
			if(!permittedMultiblocksResult.isPresent())
				return InteractionResult.FAIL;
			else
				permittedMultiblocks = permittedMultiblocksResult.get();
		}
		if(ItemNBTHelper.hasKey(stack, "multiblockInterdiction"))
		{
			ListTag list = stack.getOrCreateTag().getList("multiblockInterdiction", NBT.TAG_STRING);
			Optional<List<ResourceLocation>> interdictedMultiblocksResult = parseUserDefinedRLs(list, player, "interdiction");
			if(!interdictedMultiblocksResult.isPresent())
				return InteractionResult.FAIL;
			else
				interdictedMultiblocks = interdictedMultiblocksResult.get();
		}
		final Direction multiblockSide;
		if(side.getAxis()==Axis.Y&&player!=null)
			multiblockSide = Direction.fromYRot(player.getYRot()).getOpposite();
		else
			multiblockSide = side;
		for(MultiblockHandler.IMultiblock mb : MultiblockHandler.getMultiblocks())
			if(mb.isBlockTrigger(world.getBlockState(pos), multiblockSide, world))
			{
				boolean isAllowed;
				if(permittedMultiblocks!=null)
					isAllowed = permittedMultiblocks.contains(mb.getUniqueName());
				else if(interdictedMultiblocks!=null)
					isAllowed = !interdictedMultiblocks.contains(mb.getUniqueName());
				else
					isAllowed = true;
				if(!isAllowed)
					continue;
				if(MultiblockHandler.postMultiblockFormationEvent(player, mb, pos, stack).isCanceled())
					continue;
				if(mb.createStructure(world, pos, multiblockSide, player))
				{
					if(player instanceof ServerPlayer)
						IEAdvancements.TRIGGER_MULTIBLOCK.trigger((ServerPlayer)player, mb, stack);
					return InteractionResult.SUCCESS;
				}
			}

		/*
			Side Configs & Rotation Handling
		 */
		BlockEntity tile = world.getBlockEntity(pos);
		if(tile instanceof IConfigurableSides)
		{
			Direction activeSide = ((player!=null)&&player.isShiftKeyDown())?side.getOpposite(): side;
			if(((IConfigurableSides)tile).toggleSide(activeSide, player))
				return InteractionResult.SUCCESS;
			else
				return InteractionResult.FAIL;
		}
		else
		{
			boolean rotate = !(tile instanceof IDirectionalBE)&&!(tile instanceof IHammerInteraction);
			if(!rotate&&tile instanceof IDirectionalBE)
				rotate = ((IDirectionalBE)tile).canHammerRotate(side, context.getClickLocation().subtract(Vec3.atLowerCornerOf(pos)), player);
			if(rotate&&RotationUtil.rotateBlock(world, pos, player!=null&&(player.isShiftKeyDown()!=side.equals(Direction.DOWN))))
				return InteractionResult.SUCCESS;
			else if(!rotate&&tile instanceof IHammerInteraction)
			{
				if(((IHammerInteraction)tile).hammerUseSide(side, player, context.getHand(), context.getClickLocation()))
					return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}

	private static Optional<List<ResourceLocation>> parseUserDefinedRLs(ListTag data, Player player, String prefix)
	{
		DataResult<List<ResourceLocation>> result = parseUserDefinedRLs(data);
		return result.resultOrPartial(err -> {
			if(player!=null&&!player.getCommandSenderWorld().isClientSide)
				player.displayClientMessage(
						new TextComponent("Invalid "+prefix+" entry: "+err), false
				);
		});
	}

	private static DataResult<List<ResourceLocation>> parseUserDefinedRLs(ListTag data)
	{
		return Codec.list(ResourceLocation.CODEC).parse(NbtOps.INSTANCE, data);
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player)
	{
		return true;
	}

	@Nonnull
	@Override
	public ItemStack getContainerItem(@Nonnull ItemStack stack)
	{
		ItemStack container = stack.copy();
		if(container.hurt(1, Utils.RAND, null))
			return ItemStack.EMPTY;
		else
			return container;
	}

	@Override
	public boolean hasContainerItem(@Nonnull ItemStack stack)
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
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
	{
		return enchantment==Enchantments.BLOCK_EFFICIENCY||enchantment==Enchantments.UNBREAKING||enchantment==Enchantments.MENDING;
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand)
	{
		if (!player.level.isClientSide&&RotationUtil.rotateEntity(entity, player))
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
	public boolean isTool(ItemStack item)
	{
		return true;
	}

	@Override
	public boolean isCorrectToolForDrops(ItemStack stack, BlockState state)
	{
		return state.is(IETags.hammerHarvestable);
	}
}
