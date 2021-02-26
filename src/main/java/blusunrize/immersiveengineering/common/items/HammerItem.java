/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IConfigurableSides;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHammerInteraction;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.RotationUtil;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.advancements.IEAdvancements;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class HammerItem extends IEBaseItem implements ITool
{
	public static final ToolType HAMMER_TOOL = ToolType.get(ImmersiveEngineering.MODID+"_hammer");

	public HammerItem()
	{
		super("hammer", new Properties().defaultMaxDamage(100));// Value is overridden in getMaxDamage
	}

	@Override
	public int getMaxDamage(ItemStack stack)
	{
		return IEServerConfig.TOOLS.hammerDurabiliy.getOrDefault();
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
	{
		super.addInformation(stack, worldIn, tooltip, flagIn);
		if(ItemNBTHelper.hasKey(stack, "multiblockPermission"))
		{
			ListNBT tagList = stack.getOrCreateTag().getList("multiblockPermission", 8);
			String s = I18n.format(Lib.DESC_INFO+"multiblocksAllowed");
			addInfo(tooltip, s, tagList);
		}
		if(ItemNBTHelper.hasKey(stack, "multiblockInterdiction"))
		{
			ListNBT tagList = stack.getOrCreateTag().getList("multiblockInterdiction", 8);
			String s = I18n.format(Lib.DESC_INFO+"multiblockForbidden");
			addInfo(tooltip, s, tagList);
		}
	}

	private void addInfo(List<ITextComponent> list, String s, ListNBT tagList)
	{
		if(!Screen.hasShiftDown())
			list.add(new TranslationTextComponent(Lib.DESC_INFO+"holdShift", s));
		else
		{
			list.add(new StringTextComponent(s));
			for(int i = 0; i < tagList.size(); i++)
				list.add(ClientUtils.applyFormat(
						new TranslationTextComponent(Lib.DESC_INFO+"multiblock."+tagList.getString(i)),
						TextFormatting.DARK_GRAY
				));
		}
	}

	@Override
	public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context)
	{
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		PlayerEntity player = context.getPlayer();
		Direction side = context.getFace();

		/*
			Multiblock Handling
		 */
		List<ResourceLocation> permittedMultiblocks = null;
		List<ResourceLocation> interdictedMultiblocks = null;
		if(ItemNBTHelper.hasKey(stack, "multiblockPermission"))
		{
			ListNBT list = stack.getOrCreateTag().getList("multiblockPermission", NBT.TAG_STRING);
			Optional<List<ResourceLocation>> permittedMultiblocksResult = parseUserDefinedRLs(list, player, "permission");
			if(!permittedMultiblocksResult.isPresent())
				return ActionResultType.FAIL;
			else
				permittedMultiblocks = permittedMultiblocksResult.get();
		}
		if(ItemNBTHelper.hasKey(stack, "multiblockInterdiction"))
		{
			ListNBT list = stack.getOrCreateTag().getList("multiblockInterdiction", NBT.TAG_STRING);
			Optional<List<ResourceLocation>> interdictedMultiblocksResult = parseUserDefinedRLs(list, player, "interdiction");
			if(!interdictedMultiblocksResult.isPresent())
				return ActionResultType.FAIL;
			else
				interdictedMultiblocks = interdictedMultiblocksResult.get();
		}
		final Direction multiblockSide;
		if(side.getAxis()==Axis.Y&&player!=null)
			multiblockSide = Direction.fromAngle(player.rotationYaw).getOpposite();
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
					if(player instanceof ServerPlayerEntity)
						IEAdvancements.TRIGGER_MULTIBLOCK.trigger((ServerPlayerEntity)player, mb, stack);
					return ActionResultType.SUCCESS;
				}
			}

		/*
			Side Configs & Rotation Handling
		 */
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof IConfigurableSides)
		{
			Direction activeSide = ((player!=null)&&player.isSneaking())?side.getOpposite(): side;
			if(((IConfigurableSides)tile).toggleSide(activeSide, player))
				return ActionResultType.SUCCESS;
			else
				return ActionResultType.FAIL;
		}
		else
		{
			boolean rotate = !(tile instanceof IDirectionalTile)&&!(tile instanceof IHammerInteraction);
			if(!rotate&&tile instanceof IDirectionalTile)
				rotate = ((IDirectionalTile)tile).canHammerRotate(side, context.getHitVec().subtract(Vector3d.copy(pos)), player);
			if(rotate&&RotationUtil.rotateBlock(world, pos, player!=null&&(player.isSneaking()!=side.equals(Direction.DOWN))))
				return ActionResultType.SUCCESS;
			else if(!rotate&&tile instanceof IHammerInteraction)
			{
				if(((IHammerInteraction)tile).hammerUseSide(side, player, context.getHand(), context.getHitVec()))
					return ActionResultType.SUCCESS;
			}
		}
		return ActionResultType.PASS;
	}

	private static Optional<List<ResourceLocation>> parseUserDefinedRLs(ListNBT data, PlayerEntity player, String prefix)
	{
		DataResult<List<ResourceLocation>> result = parseUserDefinedRLs(data);
		return result.resultOrPartial(err -> {
			if(player!=null&&!player.getEntityWorld().isRemote)
				player.sendStatusMessage(
						new StringTextComponent("Invalid "+prefix+" entry: "+err), false
				);
		});
	}

	private static DataResult<List<ResourceLocation>> parseUserDefinedRLs(ListNBT data)
	{
		return Codec.list(ResourceLocation.CODEC).parse(NBTDynamicOps.INSTANCE, data);
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player)
	{
		return true;
	}

	@Nonnull
	@Override
	public ItemStack getContainerItem(@Nonnull ItemStack stack)
	{
		ItemStack container = stack.copy();
		if(container.attemptDamageItem(1, Utils.RAND, null))
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
	public int getItemEnchantability()
	{
		return 14;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
	{
		return enchantment==Enchantments.EFFICIENCY||enchantment==Enchantments.UNBREAKING||enchantment==Enchantments.MENDING;
	}

	@Override
	public ActionResultType itemInteractionForEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand)
	{
		if (!player.world.isRemote&&RotationUtil.rotateEntity(entity, player))
			return ActionResultType.SUCCESS;
		else
			return ActionResultType.PASS;
	}

	@Nonnull
	@Override
	public Set<ToolType> getToolTypes(ItemStack stack)
	{
		return ImmutableSet.of(HAMMER_TOOL);
	}

	@Override
	public int getHarvestLevel(ItemStack stack, @Nonnull ToolType tool, @Nullable PlayerEntity player, @Nullable BlockState blockState)
	{
		if(getToolTypes(stack).contains(tool))
			return 2;
		else
			return -1;
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state)
	{
		for(ToolType type : this.getToolTypes(stack))
			if(state.getBlock().isToolEffective(state, type))
				return 6;
		return super.getDestroySpeed(stack, state);
	}

	@Override
	public boolean isTool(ItemStack item)
	{
		return true;
	}

	@Override
	public boolean canHarvestBlock(ItemStack stack, BlockState state)
	{
		if(state.getBlock() instanceof IEBaseBlock)
		{
			return ((IEBaseBlock)state.getBlock()).allowHammerHarvest(state);
		}
		else return state.getBlock().isToolEffective(state, HAMMER_TOOL);
	}
}
