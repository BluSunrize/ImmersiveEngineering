/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.render.IEOBJItemRenderer;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.fluids.IEItemFluidHandler;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SChangeBlockPacket;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
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
	public static Material[] validMaterials = {Material.ANVIL, Material.CLAY, Material.GLASS, Material.ORGANIC, Material.EARTH,
			Material.ICE, Material.IRON, Material.PACKED_ICE, Material.PISTON, Material.ROCK, Material.SAND, Material.SNOW};

	public DrillItem()
	{
		super("drill", withIEOBJRender().maxStackSize(1).setISTER(() -> () -> IEOBJItemRenderer.INSTANCE), "DRILL");
	}

	/* ------------- WORKBENCH & INVENTORY ------------- */
	@Override
	public int getSlotCount()
	{
		return 5;
	}

	@Override
	public Slot[] getWorkbenchSlots(Container container, ItemStack stack, Supplier<World> getWorld, Supplier<PlayerEntity> getPlayer)
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
	public void removeFromWorkbench(PlayerEntity player, ItemStack stack)
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
		ItemStack head = cap.orElseThrow(RuntimeException::new).getStackInSlot(0);
		return !head.isEmpty()&&head.getItem() instanceof IDrillHead?head: ItemStack.EMPTY;
	}

	@Override
	public void setHead(ItemStack drill, ItemStack head)
	{
		IItemHandler inv = drill.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(RuntimeException::new);
		((IItemHandlerModifiable)inv).setStackInSlot(0, head);
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
	{
		list.add(IEItemFluidHandler.fluidItemInfoFlavor(getFluid(stack), getCapacity(stack, CAPACITY)));
		if(getHead(stack).isEmpty())
			list.add(ClientUtils.applyFormat(
					new TranslationTextComponent(Lib.DESC_FLAVOUR+"drill.noHead"),
					TextFormatting.GRAY
			));
		else
		{
			int maxDmg = getMaxHeadDamage(stack);
			int dmg = maxDmg-getHeadDamage(stack);
			float quote = dmg/(float)maxDmg;
			TextFormatting status = quote < .1?TextFormatting.RED: quote < .3?TextFormatting.GOLD: quote < .6?TextFormatting.YELLOW: TextFormatting.GREEN;
			list.add(ClientUtils.applyFormat(new TranslationTextComponent(Lib.DESC_FLAVOUR+"drill.headDamage"), TextFormatting.GRAY)
					.appendString(" ")
					.appendSibling(ClientUtils.applyFormat(
							new TranslationTextComponent(Lib.DESC_INFO+"percent", (int)(quote*100)),
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
		if(player!=null&&player.areEyesInFluid(FluidTags.WATER)&&!getUpgrades(drill).getBoolean("waterproof"))
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
	public boolean onBlockDestroyed(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity living)
	{
		if(state.getBlockHardness(world, pos)!=0)
		{
			ItemStack head = getHead(stack);
			if(!head.isEmpty())
			{
				if(living instanceof PlayerEntity)
				{
					if(((PlayerEntity)living).abilities.isCreativeMode)
						return true;
					((IDrillHead)head.getItem()).afterBlockbreak(stack, head, (PlayerEntity)living);
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
	public int getHarvestLevel(ItemStack stack, @Nonnull ToolType tool, @Nullable PlayerEntity player, @Nullable BlockState blockState)
	{
		ItemStack head = getHead(stack);
		if(!head.isEmpty())
			return ((IDrillHead)head.getItem()).getMiningLevel(head)+ItemNBTHelper.getInt(stack, "harvestLevel");
		return 0;
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

	public boolean canBreakExtraBlock(World world, Block block, BlockPos pos, BlockState state, PlayerEntity player, ItemStack drill, ItemStack head, boolean inWorld)
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
	public boolean onBlockStartBreak(ItemStack stack, BlockPos iPos, PlayerEntity player)
	{
		World world = player.world;
		if(player.isSneaking()||world.isRemote||!(player instanceof ServerPlayerEntity))
			return false;
		RayTraceResult mop = rayTrace(world, player, FluidMode.NONE);
		ItemStack head = getHead(stack);
		if(mop==null||head.isEmpty()||!canToolBeUsed(stack, player))
			return false;
		ImmutableList<BlockPos> additional = ((IDrillHead)head.getItem()).getExtraBlocksDug(head, world, player, mop);
		for(BlockPos pos : additional)
		{
			if(!world.isBlockLoaded(pos))
				continue;
			BlockState state = world.getBlockState(pos);
			Block block = state.getBlock();

			if(block!=null&&!block.isAir(state, world, pos)&&state.getPlayerRelativeBlockHardness(player, world, pos)!=0)
			{
				if(!this.canBreakExtraBlock(world, block, pos, state, player, stack, head, true))
					continue;
				int xpDropEvent = ForgeHooks.onBlockBreakEvent(world, ((ServerPlayerEntity)player).interactionManager.getGameType(), (ServerPlayerEntity)player, pos);
				if(xpDropEvent < 0)
					continue;

				if(player.abilities.isCreativeMode)
				{
					block.onBlockHarvested(world, pos, state, player);
					if(block.removedByPlayer(state, world, pos, player, false, state.getFluidState()))
						block.onPlayerDestroy(world, pos, state);
				}
				else
				{
					block.onBlockHarvested(world, pos, state, player);
					TileEntity te = world.getTileEntity(pos);
					//implicitly damages head
					stack.onBlockDestroyed(world, state, pos, player);
					if(block.removedByPlayer(state, world, pos, player, true, state.getFluidState()))
					{
						block.onPlayerDestroy(world, pos, state);
						block.harvestBlock(world, player, pos, state, te, stack);
						if (world instanceof ServerWorld)
							block.dropXpOnBlockBreak((ServerWorld)world, pos, xpDropEvent);
					}
				}
				world.playEvent(2001, pos, Block.getStateId(state));
				((ServerPlayerEntity)player).connection.sendPacket(new SChangeBlockPacket(world, pos));
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
		CompoundNBT upgrades = this.getUpgrades(stack);
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
	public TransformationMatrix applyTransformations(ItemStack stack, String group, TransformationMatrix transform)
	{
		CompoundNBT upgrades = this.getUpgrades(stack);
		if(group.equals("drill_head")&&upgrades.getInt("damage") <= 0)
			return transform.compose(new TransformationMatrix(
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
	private static TransformationMatrix matAugers;

	@Nonnull
	@Override
	@OnlyIn(Dist.CLIENT)
	public TransformationMatrix getTransformForGroups(ItemStack stack, String[] groups, TransformType transform, LivingEntity entity, float partialTicks)
	{
		if(matAugers==null)
			matAugers = new TransformationMatrix(new Vector3f(.441f, 0, 0), null, null, null);
		if(groups==FIXED[0])
			return matAugers;
		float angle = (entity.ticksExisted%60+partialTicks)/60f*(float)(2*Math.PI);
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
		return new TransformationMatrix(translation, rotation, null, null);
	}
}
