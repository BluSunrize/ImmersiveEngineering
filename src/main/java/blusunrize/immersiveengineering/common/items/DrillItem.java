/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.client.render.IEOBJItemRenderer;
import blusunrize.immersiveengineering.common.fluids.IEItemFluidHandler;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class DrillItem extends DieselToolItem
{
	public static Material[] validMaterials = {Material.HEAVY_METAL, Material.CLAY, Material.GLASS, Material.GRASS, Material.DIRT,
			Material.ICE, Material.METAL, Material.ICE_SOLID, Material.PISTON, Material.STONE, Material.SAND, Material.TOP_SNOW};

	public DrillItem()
	{
		super(withIEOBJRender().stacksTo(1).setISTER(() -> () -> IEOBJItemRenderer.INSTANCE), "DRILL");
	}

	/* ------------- WORKBENCH & INVENTORY ------------- */
	@Override
	public int getSlotCount()
	{
		return 5;
	}

	@Override
	public Slot[] getWorkbenchSlots(AbstractContainerMenu container, ItemStack stack, Supplier<Level> getWorld, Supplier<Player> getPlayer)
	{
		IItemHandler inv = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
				.orElseThrow(RuntimeException::new);
		return new Slot[]
				{
						new IESlot.WithPredicate(inv, 0, 98, 22, (itemStack) -> itemStack.getItem() instanceof IDrillHead),
						new IESlot.Upgrades(container, inv, 1, 78, 52, "DRILL", stack, true, getWorld, getPlayer),
						new IESlot.Upgrades(container, inv, 2, 98, 52, "DRILL", stack, true, getWorld, getPlayer),
						new IESlot.Upgrades(container, inv, 3, 118, 52, "DRILL", stack, true, getWorld, getPlayer)
				};
	}

	@Override
	public void removeFromWorkbench(Player player, ItemStack stack)
	{
		LazyOptional<IItemHandler> invCap = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		invCap.ifPresent(inv -> {
			if(!inv.getStackInSlot(0).isEmpty()&&!inv.getStackInSlot(1).isEmpty()&&!inv.getStackInSlot(2).isEmpty()&&!inv.getStackInSlot(3).isEmpty())
				Utils.unlockIEAdvancement(player, "main/upgrade_drill");
		});
	}

	@Override
	public ItemStack getHead(ItemStack drill)
	{
		if(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY==null)
			return ItemStack.EMPTY;
		LazyOptional<IItemHandler> cap = drill.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		if(cap.isPresent())
		{
			ItemStack head = cap.map(handler -> handler.getStackInSlot(0)).orElse(ItemStack.EMPTY);
			return !head.isEmpty()&&head.getItem() instanceof IDrillHead?head: ItemStack.EMPTY;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void setHead(ItemStack drill, ItemStack head)
	{
		IItemHandler inv = drill.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(RuntimeException::new);
		((IItemHandlerModifiable)inv).setStackInSlot(0, head);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		list.add(IEItemFluidHandler.fluidItemInfoFlavor(getFluid(stack), getCapacity(stack, CAPACITY)));
		if(getHead(stack).isEmpty())
			list.add(TextUtils.applyFormat(
					new TranslatableComponent(Lib.DESC_FLAVOUR+"drill.noHead"),
					ChatFormatting.GRAY
			));
		else
		{
			int maxDmg = getMaxHeadDamage(stack);
			int dmg = maxDmg-getHeadDamage(stack);
			float quote = dmg/(float)maxDmg;
			ChatFormatting status = quote < .1?ChatFormatting.RED: quote < .3?ChatFormatting.GOLD: quote < .6?ChatFormatting.YELLOW: ChatFormatting.GREEN;
			list.add(TextUtils.applyFormat(new TranslatableComponent(Lib.DESC_FLAVOUR+"drill.headDamage"), ChatFormatting.GRAY)
					.append(" ")
					.append(TextUtils.applyFormat(
							new TranslatableComponent(Lib.DESC_INFO+"percent", (int)(quote*100)),
							status
					)));
		}
	}

	@Override
	protected double getAttackDamage(ItemStack stack, ItemStack head)
	{
		return ((IDrillHead)head.getItem()).getAttackDamage(head)+getUpgrades(stack).getInt("damage");
	}

	/* ------------- DIGGING ------------- */
	public boolean canToolBeUsed(ItemStack drill, @Nullable LivingEntity player)
	{
		if(player!=null&&player.isEyeInFluid(FluidTags.WATER)&&!getUpgrades(drill).getBoolean("waterproof"))
			return false;
		return getHeadDamage(drill) < getMaxHeadDamage(drill)&&!getFluid(drill).isEmpty();
	}

	@Override
	public int getMaxHeadDamage(ItemStack stack)
	{
		ItemStack head = getHead(stack);
		return !head.isEmpty()?((IDrillHead)head.getItem()).getMaximumHeadDamage(head): 0;
	}

	@Override
	public int getHeadDamage(ItemStack stack)
	{
		ItemStack head = getHead(stack);
		return !head.isEmpty()?((IDrillHead)head.getItem()).getHeadDamage(head): 0;
	}

	@Override
	public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity living)
	{
		if(state.getDestroySpeed(world, pos)!=0)
		{
			ItemStack head = getHead(stack);
			if(!head.isEmpty())
			{
				if(living instanceof Player)
				{
					if(((Player)living).abilities.instabuild)
						return true;
					((IDrillHead)head.getItem()).afterBlockbreak(stack, head, (Player)living);
				}
				consumeDurability(stack, world, state, pos, living);
			}
		}

		return true;
	}

	@Override
	protected void damageHead(ItemStack head, int amount, LivingEntity living)
	{
		((IDrillHead)head.getItem()).damageHead(head, amount);
	}

	@Override
	public int getHarvestLevel(ItemStack stack, @Nonnull ToolType tool, @Nullable Player player, @Nullable BlockState blockState)
	{
		ItemStack head = getHead(stack);
		if(!head.isEmpty()&&canToolBeUsed(stack, player))
			return ((IDrillHead)head.getItem()).getMiningLevel(head)+ItemNBTHelper.getInt(stack, "harvestLevel");
		return -1;
	}

	@Override
	public Set<ToolType> getToolTypes(ItemStack stack)
	{
		if(!getHead(stack).isEmpty()&&canToolBeUsed(stack, null))
			return ImmutableSet.of(ToolType.PICKAXE, ToolType.SHOVEL);
		return super.getToolTypes(stack);
	}

	@Override
	public boolean isEffective(ItemStack stack, Material mat)
	{
		for(Material m : validMaterials)
			if(m==mat)
				return true;
		return false;
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state)
	{
		ItemStack head = getHead(stack);
		if(!head.isEmpty()&&canToolBeUsed(stack, null))
			return ((IDrillHead)head.getItem()).getMiningSpeed(head)+getUpgrades(stack).getFloat("speed");
		return super.getDestroySpeed(stack, state);
	}

	public boolean canBreakExtraBlock(Level world, Block block, BlockPos pos, BlockState state, Player player, ItemStack drill, ItemStack head, boolean inWorld)
	{
		if(block.canHarvestBlock(state, world, pos, player)&&isEffective(drill, state.getMaterial())&&canToolBeUsed(drill, player))
		{
			if(inWorld)
				return !((IDrillHead)head.getItem()).beforeBlockbreak(drill, head, player);
			else
				return true;
		}
		return false;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack stack, BlockPos iPos, Player player)
	{
		Level world = player.level;
		if(player.isShiftKeyDown()||world.isClientSide||!(player instanceof ServerPlayer))
			return false;
		HitResult mop = getPlayerPOVHitResult(world, player, Fluid.NONE);
		ItemStack head = getHead(stack);
		if(mop==null||head.isEmpty()||!canToolBeUsed(stack, player))
			return false;
		ImmutableList<BlockPos> additional = ((IDrillHead)head.getItem()).getExtraBlocksDug(head, world, player, mop);
		for(BlockPos pos : additional)
		{
			if(!world.hasChunkAt(pos))
				continue;
			BlockState state = world.getBlockState(pos);
			Block block = state.getBlock();

			if(block!=null&&!block.isAir(state, world, pos)&&state.getDestroyProgress(player, world, pos)!=0)
			{
				if(!this.canBreakExtraBlock(world, block, pos, state, player, stack, head, true))
					continue;
				int xpDropEvent = ForgeHooks.onBlockBreakEvent(world, ((ServerPlayer)player).gameMode.getGameModeForPlayer(), (ServerPlayer)player, pos);
				if(xpDropEvent < 0)
					continue;

				if(player.abilities.instabuild)
				{
					block.playerWillDestroy(world, pos, state, player);
					if(block.removedByPlayer(state, world, pos, player, false, state.getFluidState()))
						block.destroy(world, pos, state);
				}
				else
				{
					block.playerWillDestroy(world, pos, state, player);
					BlockEntity te = world.getBlockEntity(pos);
					//implicitly damages head
					stack.mineBlock(world, state, pos, player);
					if(block.removedByPlayer(state, world, pos, player, true, state.getFluidState()))
					{
						block.destroy(world, pos, state);
						block.playerDestroy(world, player, pos, state, te, stack);
						if(world instanceof ServerLevel)
							block.popExperience((ServerLevel)world, pos, xpDropEvent);
					}
				}
				world.levelEvent(2001, pos, Block.getId(state));
				((ServerPlayer)player).connection.send(new ClientboundBlockUpdatePacket(world, pos));
			}
		}
		return false;
	}

	/* ------------- RENDERING ------------- */

	@OnlyIn(Dist.CLIENT)
	@Override
	public TextureAtlasSprite getTextureReplacement(ItemStack stack, String group, String material)
	{
		if(!"head".equals(material))
			return null;
		ItemStack head = this.getHead(stack);
		if(head.getItem() instanceof IDrillHead)
			return ((IDrillHead)head.getItem()).getDrillTexture(stack, this.getHead(stack));
		return null;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean shouldRenderGroup(ItemStack stack, String group)
	{
		if(group.equals("drill_frame")||group.equals("drill_grip"))
			return true;
		CompoundTag upgrades = this.getUpgrades(stack);
		if(group.equals("upgrade_waterproof"))
			return upgrades.getBoolean("waterproof");
		if(group.equals("upgrade_speed"))
			return upgrades.getBoolean("oiled");
		if(!this.getHead(stack).isEmpty())
		{
			if(group.equals("drill_head"))
				return true;

			if(group.equals("upgrade_damage0"))
				return upgrades.getInt("damage") > 0;
			if(group.equals("upgrade_damage1")||group.equals("upgrade_damage2"))
				return upgrades.getInt("damage") > 1;
			if(group.equals("upgrade_damage3")||group.equals("upgrade_damage4"))
				return upgrades.getInt("damage") > 2;
		}
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public Transformation applyTransformations(ItemStack stack, String group, Transformation transform)
	{
		CompoundTag upgrades = this.getUpgrades(stack);
		if(group.equals("drill_head")&&upgrades.getInt("damage") <= 0)
			return transform.compose(new Transformation(
					new Vector3f(-.25f, 0, 0), null, null, null
			));
		return transform;
	}

	private static final String[][] ROTATING = {
			{"drill_head", "upgrade_damage0"},
			{"upgrade_damage1", "upgrade_damage2"},
			{"upgrade_damage3", "upgrade_damage4"}
	};
	private static final String[][] FIXED = {
			{"upgrade_damage1", "upgrade_damage2", "upgrade_damage3", "upgrade_damage4"}
	};

	@Override
	@OnlyIn(Dist.CLIENT)
	public String[][] getSpecialGroups(ItemStack stack, TransformType transform, LivingEntity entity)
	{
		if(shouldRotate(entity, stack, transform))
			return ROTATING;
		else
			return FIXED;
	}

	@OnlyIn(Dist.CLIENT)
	private static Transformation matAugers;

	@Nonnull
	@Override
	@OnlyIn(Dist.CLIENT)
	public Transformation getTransformForGroups(ItemStack stack, String[] groups, TransformType transform, LivingEntity entity, float partialTicks)
	{
		if(matAugers==null)
			matAugers = new Transformation(new Vector3f(.441f, 0, 0), null, null, null);
		if(groups==FIXED[0])
			return matAugers;
		float angle = (entity.tickCount%60+partialTicks)/60f*(float)(2*Math.PI);
		Quaternion rotation = null;
		Vector3f translation = null;
		if("drill_head".equals(groups[0]))
			rotation = new Quaternion(angle, 0, 0, false);
		else if("upgrade_damage1".equals(groups[0]))
		{
			translation = new Vector3f(.441f, 0, 0);
			rotation = new Quaternion(0, angle, 0, false);
		}
		else if("upgrade_damage3".equals(groups[0]))
		{
			translation = new Vector3f(.441f, 0, 0);
			rotation = new Quaternion(0, 0, angle, false);
		}
		return new Transformation(translation, rotation, null, null);
	}
}
