/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Item;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderRegistryEntry;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IAdvancedFluidItem;
import blusunrize.immersiveengineering.common.util.IEItemFluidHandler;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ItemDrill extends ItemUpgradeableTool implements IAdvancedFluidItem, IOBJModelCallback<ItemStack>, ITool
{
	public static Material[] validMaterials = {Material.ANVIL, Material.CLAY, Material.GLASS, Material.GRASS, Material.GROUND, Material.ICE, Material.IRON, Material.PACKED_ICE, Material.PISTON, Material.ROCK, Material.SAND, Material.SNOW};

	public ItemDrill()
	{
		super("drill", 1, "DRILL", "diesel");
	}

	@Override
	public int getSlotCount(ItemStack stack)
	{
		return 5;
	}

	@Override
	public Slot[] getWorkbenchSlots(Container container, ItemStack stack)
	{
		IItemHandler inv = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		return new Slot[]
				{
						new IESlot.DrillHead(inv, 0, 98, 22),
						new IESlot.Upgrades(container, inv, 1, 78, 52, "DRILL", stack, true),
						new IESlot.Upgrades(container, inv, 2, 98, 52, "DRILL", stack, true),
						new IESlot.Upgrades(container, inv, 3, 118, 52, "DRILL", stack, true)
				};
	}

	@Override
	public boolean canModify(ItemStack stack)
	{
		return true;
	}

	@Override
	public void recalculateUpgrades(ItemStack stack)
	{
		super.recalculateUpgrades(stack);
		FluidStack fs = getFluid(stack);
		if(fs!=null&&fs.amount > this.getCapacity(stack, 2000))
		{
			fs.amount = this.getCapacity(stack, 2000);
			ItemNBTHelper.setFluidStack(stack, "Fluid", fs);
		}
	}

	@Override
	public void finishUpgradeRecalculation(ItemStack stack)
	{
		FluidStack fs = getFluid(stack);
		if(fs!=null&&fs.amount > getCapacity(stack, 2000))
		{
			fs.amount = getCapacity(stack, 2000);
			ItemNBTHelper.setFluidStack(stack, "Fluid", fs);
		}
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag)
	{
		FluidStack fs = getFluid(stack);
		if(fs!=null)
			list.add(I18n.format(Lib.DESC_FLAVOUR+"drill.fuel")+" "+fs.amount+"/"+getCapacity(stack, 2000)+"mB");
		else
			list.add(I18n.format(Lib.DESC_FLAVOUR+"drill.empty"));
		if(getHead(stack).isEmpty())
			list.add(I18n.format(Lib.DESC_FLAVOUR+"drill.noHead"));
		else
		{
			int maxDmg = getMaxHeadDamage(stack);
			int dmg = maxDmg-getHeadDamage(stack);
			float quote = dmg/(float)maxDmg;
			String status = ""+(quote < .1?TextFormatting.RED: quote < .3?TextFormatting.GOLD: quote < .6?TextFormatting.YELLOW: TextFormatting.GREEN);
			list.add(I18n.format(Lib.DESC_FLAVOUR+"drill.headDamage")+" "+status+dmg+"/"+maxDmg);
		}
	}

	/*RENDER STUFF*/
	@Override
	public boolean isFull3D()
	{
		return true;
	}

	public static HashMap<String, Integer> animationTimer = new HashMap<String, Integer>();

	@Override
	public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack)
	{
		if(canDrillBeUsed(stack, entityLiving))
		{
			if(!animationTimer.containsKey(entityLiving.getName()))
				animationTimer.put(entityLiving.getName(), 40);
			else if(animationTimer.get(entityLiving.getName()) < 20)
				animationTimer.put(entityLiving.getName(), 20);
		}
		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public TextureAtlasSprite getTextureReplacement(ItemStack stack, String material)
	{
		if(material.equals("head")&&!this.getHead(stack).isEmpty()&&this.getHead(stack).getItem() instanceof IDrillHead)
		{
			TextureAtlasSprite spr = ((IDrillHead)this.getHead(stack).getItem()).getDrillTexture(stack, this.getHead(stack));
			return spr;
		}
		return null;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldRenderGroup(ItemStack stack, String group)
	{
		if(group.equals("drill_frame")||group.equals("drill_grip"))
			return true;
		NBTTagCompound upgrades = this.getUpgrades(stack);
		if(group.equals("upgrade_waterproof"))
			return upgrades.getBoolean("waterproof");
		if(group.equals("upgrade_speed"))
			return upgrades.getBoolean("oiled");
		if(!this.getHead(stack).isEmpty())
		{
			if(group.equals("drill_head"))
				return true;

			if(group.equals("upgrade_damage0"))
				return upgrades.getInteger("damage") > 0;
			if(group.equals("upgrade_damage1")||group.equals("upgrade_damage2"))
				return upgrades.getInteger("damage") > 1;
			if(group.equals("upgrade_damage3")||group.equals("upgrade_damage4"))
				return upgrades.getInteger("damage") > 2;
		}
		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Optional<TRSRTransformation> applyTransformations(ItemStack stack, String group, Optional<TRSRTransformation> transform)
	{
		if(transform.isPresent())
		{
			NBTTagCompound upgrades = this.getUpgrades(stack);
			if(group.equals("drill_head")&&upgrades.getInteger("damage") <= 0)
			{
				Matrix4 mat = new Matrix4(transform.get().getMatrix());
				mat.translate(-.25f, 0, 0);
				return Optional.of(new TRSRTransformation(mat.toMatrix4f()));
			}
		}
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

	private boolean shouldRotate(EntityLivingBase entity, ItemStack stack, TransformType transform)
	{
		return entity!=null&&canDrillBeUsed(stack, entity)&&
				(entity.getHeldItem(EnumHand.MAIN_HAND)==stack||entity.getHeldItem(EnumHand.OFF_HAND)==stack)&&
				(transform==TransformType.FIRST_PERSON_RIGHT_HAND||transform==TransformType.FIRST_PERSON_LEFT_HAND||
						transform==TransformType.THIRD_PERSON_RIGHT_HAND||transform==TransformType.THIRD_PERSON_LEFT_HAND);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String[][] getSpecialGroups(ItemStack stack, TransformType transform, EntityLivingBase entity)
	{
		if(shouldRotate(entity, stack, transform))
			return ROTATING;
		else
			return FIXED;
	}

	private static final Matrix4 matAugers = new Matrix4().translate(.441f, 0, 0);

	@Nonnull
	@Override
	public Matrix4 getTransformForGroups(ItemStack stack, String[] groups, TransformType transform, EntityLivingBase entity, Matrix4 mat, float partialTicks)
	{
		mat.setIdentity();
		if(groups==FIXED[0])
			return matAugers;
		//.069813f
		float angle = (entity.ticksExisted%60+partialTicks)/60f*(float)(2*Math.PI);
		if("drill_head".equals(groups[0]))
			mat.rotate(angle, 1, 0, 0);
		else if("upgrade_damage1".equals(groups[0]))
			mat.translate(.441f, 0, 0).rotate(angle, 0, 1, 0);
		else if("upgrade_damage3".equals(groups[0]))
			mat.translate(.441f, 0, 0).rotate(angle, 0, 0, 1);
		return mat;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack)
	{
		return (double)getHeadDamage(stack)/(double)getMaxHeadDamage(stack);
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack)
	{
		return getHeadDamage(stack) > 0;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack p_77661_1_)
	{
		return EnumAction.BOW;
	}

	@Override
	public void removeFromWorkbench(EntityPlayer player, ItemStack stack)
	{
		IItemHandler inv = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		if(inv!=null&&!inv.getStackInSlot(0).isEmpty()&&!inv.getStackInSlot(1).isEmpty()&&!inv.getStackInSlot(2).isEmpty()&&!inv.getStackInSlot(3).isEmpty())
			Utils.unlockIEAdvancement(player, "main/upgrade_drill");
	}

	/*INVENTORY STUFF*/
	public ItemStack getHead(ItemStack drill)
	{
		ItemStack head;
		boolean remote = FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT;
		IItemHandler cap = drill.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		if(!remote&&cap.getStackInSlot(0).isEmpty())
			remote = true;
		else if(remote&&!ItemNBTHelper.hasKey(drill, "head"))
			remote = false;
		if(remote)
			head = new ItemStack(ItemNBTHelper.getTagCompound(drill, "head"));
		else
			head = cap.getStackInSlot(0);
		return !head.isEmpty()&&head.getItem() instanceof IDrillHead?head: ItemStack.EMPTY;
	}

	public void setHead(ItemStack drill, ItemStack head)
	{
		IItemHandler inv = drill.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		((IItemHandlerModifiable)inv).setStackInSlot(0, head);
	}

	/*TOOL STUFF*/
	@Override
	public boolean isTool(ItemStack item)
	{
		return true;
	}

	public boolean canDrillBeUsed(ItemStack drill, EntityLivingBase player)
	{
		if(drill.getItemDamage()==0&&player.isInsideOfMaterial(Material.WATER)&&!getUpgrades(drill).getBoolean("waterproof"))
			return false;
		return !(drill.getItemDamage()==0&&getFluid(drill)==null);
	}

	public int getMaxHeadDamage(ItemStack stack)
	{
		ItemStack head = getHead(stack);
		return !head.isEmpty()?((IDrillHead)head.getItem()).getMaximumHeadDamage(head): 0;
	}

	public int getHeadDamage(ItemStack stack)
	{
		ItemStack head = getHead(stack);
		return !head.isEmpty()?((IDrillHead)head.getItem()).getHeadDamage(head): 0;
	}

	public boolean isDrillBroken(ItemStack stack)
	{
		return getHeadDamage(stack) >= getMaxHeadDamage(stack)||getFluid(stack)==null||getFluid(stack).amount < 1;
	}

	//	@Override
	//	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	//	{
	//		if(player.isSneaking())
	//			player.openGui(ImmersiveEngineering.instance, Lib.GUIID_Workbench, world, (int)player.posX,(int)player.posY,(int)player.posZ);
	//		return stack;
	//	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase player)
	{
		return true;
	}

	@Override
	public boolean onBlockDestroyed(ItemStack stack, World world, IBlockState state, BlockPos pos, EntityLivingBase living)
	{
		if((double)state.getBlockHardness(world, pos)!=0.0D)
		{
			int dmg = ForgeHooks.isToolEffective(world, pos, stack)?1: 3;
			ItemStack head = getHead(stack);
			if(!head.isEmpty())
			{
				if(living instanceof EntityPlayer)
				{
					if(((EntityPlayer)living).capabilities.isCreativeMode)
						return true;
					((IDrillHead)head.getItem()).afterBlockbreak(stack, head, (EntityPlayer)living);
				}
				if(!getUpgrades(stack).getBoolean("oiled")||Utils.RAND.nextInt(4)==0)
					((IDrillHead)head.getItem()).damageHead(head, dmg);
				this.setHead(stack, head);
				IFluidHandler handler = FluidUtil.getFluidHandler(stack);
				handler.drain(1, true);

				Triple<ItemStack, ShaderRegistryEntry, ShaderCase> shader = ShaderRegistry.getStoredShaderAndCase(stack);
				if(shader!=null)
					shader.getMiddle().getEffectFunction().execute(world, shader.getLeft(), stack, shader.getRight().getShaderType(), new Vec3d(pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5), null, .375f);
			}
		}

		return true;
	}

	@Override
	public int getItemEnchantability()
	{
		return 0;
	}

	@Override
	public Multimap getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack)
	{
		Multimap multimap = super.getAttributeModifiers(slot, stack);
		if(slot==EntityEquipmentSlot.MAINHAND)
		{
			ItemStack head = getHead(stack);
			if(!head.isEmpty())
			{
				multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", ((IDrillHead)head.getItem()).getAttackDamage(head)+getUpgrades(stack).getInteger("damage"), 0));
				multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", -2.5D, 0));
			}
		}
		return multimap;
	}

	@Override
	public int getHarvestLevel(ItemStack stack, String toolClass, @Nullable EntityPlayer player, @Nullable IBlockState blockState)
	{
		ItemStack head = getHead(stack);
		if(!head.isEmpty())
			return ((IDrillHead)head.getItem()).getMiningLevel(head)+ItemNBTHelper.getInt(stack, "harvestLevel");
		return 0;
	}

	@Override
	public Set<String> getToolClasses(ItemStack stack)
	{
		if(!getHead(stack).isEmpty()&&!isDrillBroken(stack))
			return ImmutableSet.of("pickaxe", "shovel");
		return super.getToolClasses(stack);
	}

	public boolean isEffective(Material mat)
	{
		for(Material m : validMaterials)
			if(m==mat)
				return true;
		return false;
	}

	@Override
	public boolean canHarvestBlock(IBlockState state, ItemStack stack)
	{
		return isEffective(state.getMaterial())&&!isDrillBroken(stack);
	}

	@Override
	public float getDestroySpeed(ItemStack stack, IBlockState state)
	{
		ItemStack head = getHead(stack);
		if(!head.isEmpty()&&!isDrillBroken(stack))
			return ((IDrillHead)head.getItem()).getMiningSpeed(head)+getUpgrades(stack).getFloat("speed");
		return super.getDestroySpeed(stack, state);
	}

	public boolean canBreakExtraBlock(World world, Block block, BlockPos pos, IBlockState state, EntityPlayer player, ItemStack drill, ItemStack head, boolean inWorld)
	{
		if(block.canHarvestBlock(world, pos, player)&&isEffective(state.getMaterial())&&!isDrillBroken(drill))
		{
			if(inWorld)
				return !((IDrillHead)head.getItem()).beforeBlockbreak(drill, head, player);
			else
				return true;
		}
		return false;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack stack, BlockPos iPos, EntityPlayer player)
	{
		World world = player.world;
		if(player.isSneaking()||world.isRemote||!(player instanceof EntityPlayerMP))
			return false;
		RayTraceResult mop = this.rayTrace(world, player, true);
		ItemStack head = getHead(stack);
		if(mop==null||head.isEmpty()||this.isDrillBroken(stack))
			return false;
		//		EnumFacing side = mop.sideHit;
		//		int diameter = ((IDrillHead)head.getItem()).getMiningSize(head)+getUpgrades(stack).getInteger("size");
		//		int depth = ((IDrillHead)head.getItem()).getMiningDepth(head)+getUpgrades(stack).getInteger("depth");
		//
		//		BlockPos startPos=iPos;
		//		if(diameter%2==0)//even numbers
		//		{
		//			float hx = (float)mop.hitVec.xCoord-iPos.getX();
		//			float hy = (float)mop.hitVec.yCoord-iPos.getY();
		//			float hz = (float)mop.hitVec.zCoord-iPos.getZ();
		//			if((side.getAxis()==Axis.Y&&hx<.5)||(side.getAxis()==Axis.Z&&hx<.5))
		//				startPos.add(-diameter/2,0,0);
		//			if(side.getAxis()!=Axis.Y&&hy<.5)
		//				startPos.add(0,-diameter/2,0);
		//			if((side.getAxis()==Axis.Y&&hz<.5)||(side.getAxis()==Axis.X&&hz<.5))
		//				startPos.add(0,0,-diameter/2);
		//		}
		//		else//odd numbers
		//		{
		//			startPos.add(-(side.getAxis()==Axis.X?0: diameter/2), -(side.getAxis()==Axis.Y?0: diameter/2), -(side.getAxis()==Axis.Z?0: diameter/2));
		//		}
		//
		//		for(int dd=0; dd<depth; dd++)
		//			for(int dw=0; dw<diameter; dw++)
		//				for(int dh=0; dh<diameter; dh++)
		//				{
		//					BlockPos pos = startPos.add((side.getAxis()==Axis.X?dd: dw), (side.getAxis()==Axis.Y?dd: dh), (side.getAxis()==Axis.Y?dh: side.getAxis()==Axis.X?dw: dd));
		//					if(pos.equals(iPos))
		//						continue;
		ImmutableList<BlockPos> additional = ((IDrillHead)head.getItem()).getExtraBlocksDug(head, world, player, mop);
		for(BlockPos pos : additional)
		{
			if(!world.isBlockLoaded(pos))
				continue;
			IBlockState state = world.getBlockState(pos);
			Block block = state.getBlock();

			if(block!=null&&!block.isAir(state, world, pos)&&state.getPlayerRelativeBlockHardness(player, world, pos)!=0)
			{
				if(!this.canBreakExtraBlock(world, block, pos, state, player, stack, head, true))
					continue;
				int xpDropEvent = ForgeHooks.onBlockBreakEvent(world, ((EntityPlayerMP)player).interactionManager.getGameType(), (EntityPlayerMP)player, pos);
				if(xpDropEvent < 0)
					continue;

				if(player.capabilities.isCreativeMode)
				{
					block.onBlockHarvested(world, pos, state, player);
					if(block.removedByPlayer(state, world, pos, player, false))
						block.onPlayerDestroy(world, pos, state);
				}
				else
				{
					block.onBlockHarvested(world, pos, state, player);
					TileEntity te = world.getTileEntity(pos);
					//implicitly damages head
					stack.onBlockDestroyed(world, state, pos, player);
					if(block.removedByPlayer(state, world, pos, player, true))
					{
						block.onPlayerDestroy(world, pos, state);
						block.harvestBlock(world, player, pos, state, te, stack);
						block.dropXpOnBlockBreak(world, pos, xpDropEvent);
					}
				}
				world.playEvent(2001, pos, Block.getStateId(state));
				((EntityPlayerMP)player).connection.sendPacket(new SPacketBlockChange(world, pos));
			}
		}
		return false;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
	{
		if(slotChanged)
			return true;
		if(oldStack.hasCapability(CapabilityShader.SHADER_CAPABILITY, null)&&newStack.hasCapability(CapabilityShader.SHADER_CAPABILITY, null))
		{
			ShaderWrapper wrapperOld = oldStack.getCapability(CapabilityShader.SHADER_CAPABILITY, null);
			ShaderWrapper wrapperNew = newStack.getCapability(CapabilityShader.SHADER_CAPABILITY, null);
			if(!ItemStack.areItemStacksEqual(wrapperOld.getShaderItem(), wrapperNew.getShaderItem()))
				return true;
		}
		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt)
	{
		if(!stack.isEmpty())
			return new IEItemStackHandler(stack)
			{
				IEItemFluidHandler fluids = new IEItemFluidHandler(stack, 2000);
				ShaderWrapper_Item shaders = new ShaderWrapper_Item("immersiveengineering:drill", stack);

				@Override
				public boolean hasCapability(Capability<?> capability, EnumFacing facing)
				{
					return capability==CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY||
							capability==CapabilityShader.SHADER_CAPABILITY||
							super.hasCapability(capability, facing);
				}

				@Override
				public <T> T getCapability(Capability<T> capability, EnumFacing facing)
				{
					if(capability==CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
						return (T)fluids;
					if(capability==CapabilityShader.SHADER_CAPABILITY)
						return (T)shaders;
					return super.getCapability(capability, facing);
				}
			};
		return null;
	}

	@Override
	public int getCapacity(ItemStack container, int baseCapacity)
	{
		return baseCapacity+getUpgrades(container).getInteger("capacity");
	}

	@Override
	public boolean allowFluid(ItemStack container, FluidStack fluid)
	{
		return fluid!=null&&DieselHandler.isValidDrillFuel(fluid.getFluid());
	}

	@Nullable
	@Override
	public NBTTagCompound getNBTShareTag(ItemStack stack)
	{
		NBTTagCompound ret = super.getNBTShareTag(stack);
		if(ret==null)
			ret = new NBTTagCompound();
		else
			ret = ret.copy();
		NBTTagCompound tmp = new NBTTagCompound();
		getHead(stack).writeToNBT(tmp);
		ret.setTag("head", tmp);
		return ret;
	}
}
