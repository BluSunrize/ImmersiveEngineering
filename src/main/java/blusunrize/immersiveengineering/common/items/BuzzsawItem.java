/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Item;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderRegistryEntry;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.client.render.IEOBJItemRenderer;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IAdvancedFluidItem;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IScrollwheel;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.items.IEItems.Tools;
import blusunrize.immersiveengineering.common.items.ToolUpgradeItem.ToolUpgrade;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.fluids.IEItemFluidHandler;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.play.server.SChangeBlockPacket;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

public class BuzzsawItem extends UpgradeableToolItem implements IAdvancedFluidItem, IOBJModelCallback<ItemStack>, ITool, IScrollwheel
{
	private static final int CAPACITY = 2*FluidAttributes.BUCKET_VOLUME;

	public static final Collection<SawbladeItem> SAWBLADES = new ArrayList<>(2);

	public BuzzsawItem()
	{
		super("buzzsaw", withIEOBJRender().maxStackSize(1).setISTER(() -> () -> IEOBJItemRenderer.INSTANCE), "BUZZSAW");
	}

	/* ------------- CORE ITEM METHODS ------------- */

	@Override
	public boolean isTool(ItemStack item)
	{
		return true;
	}

	@Override
	public int getItemEnchantability()
	{
		return 0;
	}

	@Nullable
	@Override
	public CompoundNBT getShareTag(ItemStack stack)
	{
		CompoundNBT ret = super.getShareTag(stack);
		if(ret==null)
			ret = new CompoundNBT();
		else
			ret = ret.copy();
		ItemStack sawblade = getSawblade(stack);
		if(!sawblade.isEmpty())
			ret.put("sawblade", sawblade.write(new CompoundNBT()));
		return ret;
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt)
	{
		if(!stack.isEmpty())
			return new IEItemStackHandler(stack)
			{
				LazyOptional<IEItemFluidHandler> fluids = CapabilityUtils.constantOptional(new IEItemFluidHandler(stack, CAPACITY));
				LazyOptional<ShaderWrapper_Item> shaders = CapabilityUtils.constantOptional(new ShaderWrapper_Item(new ResourceLocation(ImmersiveEngineering.MODID, "buzzsaw"), stack));

				@Nonnull
				@Override
				public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing)
				{
					if(capability==CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
						return fluids.cast();
					if(capability==CapabilityShader.SHADER_CAPABILITY)
						return shaders.cast();
					return super.getCapability(capability, facing);
				}
			};
		return null;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack)
	{
		return (double)getBladeDamage(stack)/(double)getMaxBladeDamage(stack);
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack)
	{
		return getBladeDamage(stack) > 0;
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
		final boolean hasQuiver = hasQuiverUpgrade(stack);
		final int mainHeadX = hasQuiver?88: 98;
		List<Slot> slots = new ArrayList<>(5);
		slots.add(new IESlot.WithPredicate(
				inv, 0, mainHeadX, 22, BuzzsawItem::isSawblade, newBlade -> setSawblade(stack, newBlade)
		));
		slots.add(new IESlot.Upgrades(container, inv, 1, 88, 52, "BUZZSAW", stack, true, getWorld, getPlayer));
		slots.add(new IESlot.Upgrades(container, inv, 2, 108, 52, "BUZZSAW", stack, true, getWorld, getPlayer));
		if(hasQuiverUpgrade(stack))
		{
			slots.add(new IESlot.WithPredicate(inv, 3, 108, 12, BuzzsawItem::isSawblade));
			slots.add(new IESlot.WithPredicate(inv, 4, 108, 32, BuzzsawItem::isSawblade));
		}
		return slots.toArray(new Slot[0]);
	}

	@Override
	public boolean canModify(ItemStack stack)
	{
		return true;
	}

	@Override
	public ItemStack removeUpgrade(ItemStack stack, PlayerEntity player, ItemStack upgrade)
	{
		if(upgrade.getItem()==Misc.toolUpgrades.get(ToolUpgrade.BUZZSAW_SPAREBLADES))
			for(int i = 1; i <= 2; i++)
			{
				ItemStack sawblade = getSawblade(stack, i);
				if(!sawblade.isEmpty())
				{
					ItemNBTHelper.setItemStack(upgrade, "sawblade"+i, sawblade);
					setSawblade(stack, ItemStack.EMPTY, i);
				}
			}
		return upgrade;
	}

	@Override
	public void removeFromWorkbench(PlayerEntity player, ItemStack stack)
	{
		LazyOptional<IItemHandler> invCap = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		invCap.ifPresent(inv -> {
			if(!inv.getStackInSlot(0).isEmpty()&&!inv.getStackInSlot(1).isEmpty()&&!inv.getStackInSlot(2).isEmpty())
				Utils.unlockIEAdvancement(player, "main/upgrade_buzzsaw");
		});
	}

	@Override
	public void recalculateUpgrades(ItemStack stack, World w, PlayerEntity player)
	{
		super.recalculateUpgrades(stack, w, player);
		LazyOptional<IItemHandler> invCap = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		invCap.ifPresent(inv -> {
			for(int iUpgrade = 1; iUpgrade <= 2; iUpgrade++)
			{
				ItemStack upgrade = inv.getStackInSlot(iUpgrade);
				if(upgrade.getItem()==Misc.toolUpgrades.get(ToolUpgrade.BUZZSAW_SPAREBLADES))
					for(int i = 1; i <= 2; i++)
						if(ItemNBTHelper.hasKey(upgrade, "sawblade"+i))
						{
							ItemStack sawblade = ItemNBTHelper.getItemStack(upgrade, "sawblade"+i);
							setSawblade(stack, sawblade, i);
							ItemNBTHelper.remove(upgrade, "sawblade"+i);
						}
			}
		});
		FluidStack fs = getFluid(stack);
		if(fs!=null&&fs.getAmount() > this.getCapacity(stack, CAPACITY))
		{
			fs.setAmount(this.getCapacity(stack, CAPACITY));
			ItemNBTHelper.setFluidStack(stack, "Fluid", fs);
		}
	}

	@Override
	public void finishUpgradeRecalculation(ItemStack stack)
	{
		FluidStack fs = getFluid(stack);
		if(fs!=null&&fs.getAmount() > getCapacity(stack, CAPACITY))
		{
			fs.setAmount(getCapacity(stack, CAPACITY));
			ItemNBTHelper.setFluidStack(stack, "Fluid", fs);
		}
	}

	public ItemStack getSawblade(ItemStack itemStack)
	{
		return getSawblade(itemStack, 0);
	}

	public ItemStack getSawblade(ItemStack itemStack, int spare)
	{
		if(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY==null)
			return ItemStack.EMPTY;
		ItemStack sawblade;
		boolean remote = EffectiveSide.get()==LogicalSide.CLIENT;
		LazyOptional<IItemHandler> cap = itemStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		// handle spares
		int slot = spare==0?0: 2+spare;
		String key = "sawblade"+(spare==0?"": ("_spare"+spare));
		if(!remote&&cap.map(h -> h.getStackInSlot(slot).isEmpty()).orElse(false))
			remote = true;
		else if(remote&&!ItemNBTHelper.hasKey(itemStack, key))
			remote = false;
		if(remote)
			sawblade = ItemStack.read(ItemNBTHelper.getTagCompound(itemStack, key));
		else
			sawblade = cap.orElseThrow(RuntimeException::new).getStackInSlot(slot);
		return !sawblade.isEmpty()&&isSawblade(sawblade)?sawblade: ItemStack.EMPTY;
	}

	public void setSawblade(ItemStack buzzsaw, ItemStack sawblade)
	{
		setSawblade(buzzsaw, sawblade, 0);
	}

	public void setSawblade(ItemStack buzzsaw, ItemStack sawblade, int spare)
	{
		int slot = spare==0?0: 2+spare;
		IItemHandler inv = buzzsaw.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElseThrow(RuntimeException::new);
		((IItemHandlerModifiable)inv).setStackInSlot(slot, sawblade);

		ListNBT enchants = null;
		if(sawblade.getItem() instanceof SawbladeItem)
			enchants = ((SawbladeItem)sawblade.getItem()).getSawbladeEnchants();
		if(enchants!=null)
			buzzsaw.getOrCreateTag().put("Enchantments", enchants);
		else
			buzzsaw.getOrCreateTag().remove("Enchantments");
	}

	/* ------------- NAME, TOOLTIP, SUB-ITEMS ------------- */

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
	{
		list.add(IEItemFluidHandler.fluidItemInfoFlavor(getFluid(stack), getCapacity(stack, CAPACITY)));
		if(getSawblade(stack).isEmpty())
			list.add(ClientUtils.applyFormat(
					new TranslationTextComponent(Lib.DESC_FLAVOUR+"buzzsaw.noBlade"),
					TextFormatting.GRAY
			));
		else
		{
			int maxDmg = getMaxBladeDamage(stack);
			int dmg = maxDmg-getBladeDamage(stack);
			float quote = dmg/(float)maxDmg;
			TextFormatting status = (quote < .1?TextFormatting.RED: quote < .3?TextFormatting.GOLD: quote < .6?TextFormatting.YELLOW: TextFormatting.GREEN);
			list.add(ClientUtils.applyFormat(new TranslationTextComponent(Lib.DESC_FLAVOUR+"buzzsaw.bladeDamage"), TextFormatting.GRAY)
					.appendString(" ")
					.append(ClientUtils.applyFormat(
							new TranslationTextComponent(Lib.DESC_INFO+"percent", (int)(quote*100)),
							status
					)));
		}
	}

	@Override
	public Rarity getRarity(ItemStack stack)
	{
		return Rarity.COMMON;
	}

	/* ------------- ATTRIBUTES ------------- */

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack)
	{
		Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		if(slot==EquipmentSlotType.MAINHAND)
		{
			ItemStack sawblade = getSawblade(stack);
			if(!sawblade.isEmpty()&&canBuzzsawBeUsed(stack, null))
			{
				builder.put(Attributes.ATTACK_DAMAGE,
						new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier",
								((SawbladeItem)sawblade.getItem()).getSawbladeDamage(), Operation.ADDITION));
				builder.put(Attributes.ATTACK_SPEED,
						new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", -2.5D, Operation.ADDITION));
			}
		}
		return builder.build();
	}

	@Override
	public UseAction getUseAction(ItemStack p_77661_1_)
	{
		return UseAction.BOW;
	}

	@Override
	public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity player)
	{
		consumeDurability(stack, target.getEntityWorld(), null, null, player);
		return true;
	}

	@Override
	public void onScrollwheel(ItemStack stack, PlayerEntity playerEntity, boolean forward)
	{
		if(hasQuiverUpgrade(stack))
		{
			ItemStack sawblade = getSawblade(stack);
			ItemStack spare1 = getSawblade(stack, 1);
			ItemStack spare2 = getSawblade(stack, 2);
			if(forward)
			{
				setSawblade(stack, spare2);
				setSawblade(stack, sawblade, 1);
				setSawblade(stack, spare1, 2);
			}
			else
			{
				setSawblade(stack, spare1);
				setSawblade(stack, spare2, 1);
				setSawblade(stack, sawblade, 2);
			}
		}
	}

	/* ------------- DIGGING ------------- */

	public boolean canBuzzsawBeUsed(ItemStack stack, @Nullable LivingEntity player)
	{
		if(getBladeDamage(stack) >= getMaxBladeDamage(stack))
			return false;
		return !getFluid(stack).isEmpty();
	}

	public int getMaxBladeDamage(ItemStack stack)
	{
		ItemStack sawblade = getSawblade(stack);
		return !sawblade.isEmpty()?sawblade.getMaxDamage(): 0;
	}

	public int getBladeDamage(ItemStack stack)
	{
		ItemStack sawblade = getSawblade(stack);
		return !sawblade.isEmpty()?sawblade.getDamage(): 0;
	}

	@Override
	public boolean onBlockDestroyed(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity living)
	{
		consumeDurability(stack, world, state, pos, living);
		if(!world.isRemote&&!living.isSneaking()&&living instanceof ServerPlayerEntity)
			if(canFellTree(stack)&&canBuzzsawBeUsed(stack, living)&&isTree(world, pos))
				fellTree(world, pos, (ServerPlayerEntity)living, stack);
		return true;
	}

	private void consumeDurability(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity living)
	{
		if(state==null||state.getBlockHardness(world, pos)!=0.0f)
		{
			int dmg = state==null||ForgeHooks.isToolEffective(world, pos, stack)||isEffective(stack, state.getMaterial())?1: 3;
			if(state.isIn(BlockTags.LEAVES) && Utils.RAND.nextInt(10)!=0)
				return;
			ItemStack sawblade = getSawblade(stack);
			if(!sawblade.isEmpty())
			{
				if(!getUpgrades(stack).getBoolean("oiled")||Utils.RAND.nextInt(4)==0)
					sawblade.damageItem(dmg, living, (entity) -> entity.sendBreakAnimation(Hand.MAIN_HAND));
				this.setSawblade(stack, sawblade);
				IFluidHandler handler = FluidUtil.getFluidHandler(stack).orElseThrow(RuntimeException::new);
				handler.drain(1, FluidAction.EXECUTE);

				Triple<ItemStack, ShaderRegistryEntry, ShaderCase> shader = ShaderRegistry.getStoredShaderAndCase(stack);
				if(shader!=null)
					shader.getMiddle().getEffectFunction().execute(world, shader.getLeft(), stack, shader.getRight().getShaderType().toString(), new Vector3d(pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5), null, .375f);
			}
		}
	}

	@Override
	public int getHarvestLevel(ItemStack stack, @Nonnull ToolType tool, @Nullable PlayerEntity player, @Nullable BlockState blockState)
	{
		ItemStack sawblade = getSawblade(stack);
		if(!sawblade.isEmpty())
			return 3;
		return -1;
	}

	@Override
	public Set<ToolType> getToolTypes(ItemStack stack)
	{
		if(!getSawblade(stack).isEmpty()&&canBuzzsawBeUsed(stack, null))
			return ImmutableSet.of(ToolType.AXE);
		return super.getToolTypes(stack);
	}

	public boolean isEffective(ItemStack stack, Material mat)
	{
		Material[] validMaterials = null;
		ItemStack sawblade = getSawblade(stack);
		if(sawblade.getItem() instanceof SawbladeItem)
			validMaterials = ((SawbladeItem)sawblade.getItem()).getSawbladeMaterials();

		if(validMaterials!=null)
			for(Material m : validMaterials)
				if(m==mat)
					return true;
		return false;
	}

	@Override
	public boolean canHarvestBlock(ItemStack stack, BlockState state)
	{
		return isEffective(stack, state.getMaterial())&&canBuzzsawBeUsed(stack, null);
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state)
	{
		if(isEffective(stack, state.getMaterial()))
		{
			ItemStack sawblade = getSawblade(stack);
			if(!sawblade.isEmpty()&&canBuzzsawBeUsed(stack, null))
				return ((SawbladeItem)sawblade.getItem()).getSawbladeSpeed();
		}
		return super.getDestroySpeed(stack, state);
	}

	/**
	 * Check if there is a tree sprouting from the given position.
	 * We define a tree as a vertical stack of logs, up to 32 blocks tall
	 * which can go diagonal by one block per level (acacia)
	 * and with a leaf block at its top
	 *
	 * @param world
	 * @param initialPos
	 * @return
	 */
	private boolean isTree(World world, BlockPos initialPos)
	{
		int logs = 0;
		boolean leafTop = false;
		BlockPos pos = initialPos;
		for(int y = 0; y < 32; y++)
		{
			pos = pos.up();
			BlockState state = world.getBlockState(pos);
			if(state.isIn(BlockTags.LOGS))
				logs++;
			else
			{
				if(state.isIn(BlockTags.LEAVES))
					leafTop = true;
				boolean foundLog = false;
				if(!leafTop)
				{
					// Yay, Acacia trees grow diagonally >_>
					boolean loop = true;
					for(int z = -1; z <= 1&&loop; z++)
						for(int x = -1; x <= 1&&loop; x++)
						{
							state = world.getBlockState(pos.add(x, 0, z));
							if(state.isIn(BlockTags.LOGS))
							{
								pos = pos.add(x, 0, z);
								foundLog = true;
								logs++;
								loop = false;
							}
						}
				}
				// If there is no diagonal growth, the tree ends
				if(!foundLog)
					break;
			}
		}
		return logs >= 3&&leafTop;
	}

	private boolean canFellTree(ItemStack stack)
	{
		ItemStack sawblade = getSawblade(stack);
		if(sawblade.getItem() instanceof SawbladeItem)
			return ((SawbladeItem)sawblade.getItem()).canSawbladeFellTree();
		return false;
	}

	/**
	 * The max distance a block can be from the initial hit
	 * to still be considered part of the tree
	 * This is based on the largest vanilla Jungle Trees
	 */
	private static final int MAX_HORIZONTAL_DISTANCE = 7;

	private boolean fellTree(World world, BlockPos initialPos, ServerPlayerEntity player, ItemStack stack)
	{
		int logs = 0;
		Deque<BlockPos> openList = new ArrayDeque<>();
		Deque<BlockPos> closedList = new ArrayDeque<>();
		openList.add(initialPos);
		while(!openList.isEmpty()&&closedList.size() < 512&&logs < 256)
		{
			BlockPos next = openList.pollFirst();

			// Ignore blocks too far away
			if(Math.abs(next.getX()-initialPos.getX()) > MAX_HORIZONTAL_DISTANCE
					||Math.abs(next.getZ()-initialPos.getZ()) > MAX_HORIZONTAL_DISTANCE)
				continue;

			if(!closedList.contains(next))
			{
				BlockState state = world.getBlockState(next);
				if(state.isIn(BlockTags.LOGS))
				{
					closedList.add(next);
					logs++;
					// Find all at same level or above, including diagonals
					for(int y = 0; y <= 1; y++)
						for(int z = -1; z <= 1; z++)
							for(int x = -1; x <= 1; x++)
								openList.add(next.add(x, y, z));
				}
				else if(state.isIn(BlockTags.LEAVES))
				{
					closedList.add(next);
					int trunkDist = state.getBlock() instanceof LeavesBlock?state.get(LeavesBlock.DISTANCE): 0;
					// Leaves only propagate in cardinal directions, and only to other leaves
					for(Direction dir : new Direction[]{Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST})
					{
						BlockPos adj = next.offset(dir);
						BlockState adjState = world.getBlockState(adj);
						if(adjState.isIn(BlockTags.LEAVES))
						{
							int adjDist = adjState.getBlock() instanceof LeavesBlock?adjState.get(LeavesBlock.DISTANCE): 0;
							if(adjDist < trunkDist) // We don't want to get closer
								continue;
						}
						openList.add(adj);
					}
				}
			}
		}

		if(closedList.size()==0)
			return false;
		// Register a Tick Handler to break the blocks, 5 at a time
		MinecraftForge.EVENT_BUS.register(new Object()
		{
			@SubscribeEvent
			public void onTick(TickEvent.WorldTickEvent event)
			{
				if(event.phase==Phase.START&&event.world==world)
				{
					breakFromList(closedList, 5, world, player, stack);
					if(closedList.isEmpty())
						MinecraftForge.EVENT_BUS.unregister(this);
				}
			}
		});
		return true;
	}

	private void breakFromList(Deque<BlockPos> closedList, int maxAmount, World world, ServerPlayerEntity player, ItemStack stack)
	{
		int count = 0;
		while(count++ < maxAmount&&!closedList.isEmpty())
		{
			BlockPos pos = closedList.pollFirst();

			int xpDropEvent = ForgeHooks.onBlockBreakEvent(world, player.interactionManager.getGameType(), player, pos);
			if(xpDropEvent < 0)
				continue;

			BlockState state = world.getBlockState(pos);
			Block block = state.getBlock();

			if(!block.isAir(state, world, pos)&&state.getPlayerRelativeBlockHardness(player, world, pos)!=0)
			{
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
					consumeDurability(stack, world, state, pos, player);
					if(block.removedByPlayer(state, world, pos, player, true, state.getFluidState()))
					{
						block.onPlayerDestroy(world, pos, state);
						block.harvestBlock(world, player, pos, state, te, stack);
						if (world instanceof ServerWorld)
							block.dropXpOnBlockBreak((ServerWorld)world, pos, xpDropEvent);
					}
				}
				world.playEvent(2001, pos, Block.getStateId(state));
				player.connection.sendPacket(new SChangeBlockPacket(world, pos));
			}
		}
	}

	/* ------------- FLUID ------------- */

	@Override
	public int getCapacity(ItemStack container, int baseCapacity)
	{
		return baseCapacity+getUpgrades(container).getInt("capacity");
	}

	@Override
	public boolean allowFluid(ItemStack container, FluidStack fluid)
	{
		return fluid!=null&&DieselHandler.isValidDrillFuel(fluid.getFluid());
	}

	/* ------------- RENDERING ------------- */

	public static HashMap<UUID, Integer> animationTimer = new HashMap<>();

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
	{
		if(slotChanged)
			return true;
		LazyOptional<ShaderWrapper> wrapperOld = oldStack.getCapability(CapabilityShader.SHADER_CAPABILITY);
		Optional<Boolean> sameShader = wrapperOld.map(wOld -> {
			LazyOptional<ShaderWrapper> wrapperNew = newStack.getCapability(CapabilityShader.SHADER_CAPABILITY);
			return wrapperNew.map(w -> ItemStack.areItemStacksEqual(wOld.getShaderItem(), w.getShaderItem()))
					.orElse(true);
		});
		if(!sameShader.orElse(true))
			return true;
		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entity)
	{
		if(canBuzzsawBeUsed(stack, entity))
		{
			if(!animationTimer.containsKey(entity.getUniqueID()))
				animationTimer.put(entity.getUniqueID(), 40);
			else if(animationTimer.get(entity.getUniqueID()) < 20)
				animationTimer.put(entity.getUniqueID(), 20);
		}
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public TextureAtlasSprite getTextureReplacement(ItemStack stack, String group, String material)
	{
		if("blade".equals(material))
		{
			int spare = "upgrade_blades1".equals(group)?1: "upgrade_blades2".equals(group)?2: 0;
			ItemStack sawblade = getSawblade(stack, spare);
			if(sawblade.getItem() instanceof SawbladeItem)
			{
				ResourceLocation rl = ((SawbladeItem)sawblade.getItem()).getSawbladeTexture();
				if(rl!=null)
					return ClientUtils.getSprite(rl);
			}
		}
		return null;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean shouldRenderGroup(ItemStack stack, String group)
	{
		if("body".equals(group))
			return true;
		if("blade".equals(group))
			return !this.getSawblade(stack).isEmpty();

		CompoundNBT upgrades = this.getUpgrades(stack);
		if("upgrade_lube".equals(group))
			return upgrades.getBoolean("oiled");
		if("upgrade_launcher".equals(group))
			return upgrades.getBoolean("launcher");
		if("upgrade_blades0".equals(group))
			return hasQuiverUpgrade(stack);
		if("upgrade_blades1".equals(group))
			return hasQuiverUpgrade(stack)&&!this.getSawblade(stack, 1).isEmpty();
		if("upgrade_blades2".equals(group))
			return hasQuiverUpgrade(stack)&&!this.getSawblade(stack, 2).isEmpty();
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public TransformationMatrix applyTransformations(ItemStack stack, String group, TransformationMatrix transform)
	{
//		CompoundNBT upgrades = this.getUpgrades(stack);
//		if(group.equals("drill_sawblade")&&upgrades.getInt("damage") <= 0)
//		{
//			Matrix4 mat = new Matrix4(transform.getMatrixVec());
//			mat.translate(-.25f, 0, 0);
//			return new TransformationMatrix(mat.toMatrix4f());
//		}
//		Matrix4 mat = new Matrix4(transform.getMatrixVec());
//		mat.translate(.25f, -0.25f, 0);
//		return new TransformationMatrix(mat.toMatrix4f());
		return transform;
	}


	private boolean shouldRotate(LivingEntity entity, ItemStack stack, TransformType transform)
	{
		return entity!=null&&canBuzzsawBeUsed(stack, entity)&&
				(entity.getHeldItem(Hand.MAIN_HAND)==stack||entity.getHeldItem(Hand.OFF_HAND)==stack)&&
				(transform==TransformType.FIRST_PERSON_RIGHT_HAND||transform==TransformType.FIRST_PERSON_LEFT_HAND||
						transform==TransformType.THIRD_PERSON_RIGHT_HAND||transform==TransformType.THIRD_PERSON_LEFT_HAND);
	}

	private static final String[][] GROUP_BLADE = {{"blade"}};

	@Override
	@OnlyIn(Dist.CLIENT)
	public String[][] getSpecialGroups(ItemStack stack, TransformType transform, LivingEntity entity)
	{
		return GROUP_BLADE;
	}

	@OnlyIn(Dist.CLIENT)
	private static TransformationMatrix MAT_FIXED;

	@Nonnull
	@Override
	@OnlyIn(Dist.CLIENT)
	public TransformationMatrix getTransformForGroups(ItemStack stack, String[] groups, TransformType transform, LivingEntity entity, float partialTicks)
	{
		if(MAT_FIXED==null)
			MAT_FIXED = new TransformationMatrix(new Vector3f(0.60945f, 0, 0), null, null, null);
		if(!shouldRotate(entity, stack, transform))
			return MAT_FIXED;
		float ticksPerRotation = 10f;
		float angle = (entity.ticksExisted%ticksPerRotation+partialTicks)/ticksPerRotation*(float)(2*Math.PI);
		return new TransformationMatrix(
				new Vector3f(0.60945f, 0, 0),
				new Quaternion(0, angle, 0, false),
				null, null);
	}

	public static boolean hasQuiverUpgrade(ItemStack stack)
	{
		return ((BuzzsawItem)Tools.buzzsaw).getUpgrades(stack).getBoolean("spareblades");
	}

	public static boolean isSawblade(ItemStack stack)
	{
		return SAWBLADES.contains(stack.getItem());
	}

}
