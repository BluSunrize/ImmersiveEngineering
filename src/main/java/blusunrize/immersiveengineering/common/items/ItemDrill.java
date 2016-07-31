package blusunrize.immersiveengineering.common.items;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import blusunrize.immersiveengineering.api.Lib;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.api.shader.IShaderEquipableItem;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.util.IEAchievements;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemDrill extends ItemUpgradeableTool implements IShaderEquipableItem, IFluidContainerItem, IOBJModelCallback<ItemStack>, ITool
{
	public static Material[] validMaterials = {Material.ANVIL,Material.CLAY,Material.GLASS,Material.GRASS,Material.GROUND,Material.ICE,Material.IRON,Material.PACKED_ICE,Material.PISTON,Material.ROCK,Material.SAND, Material.SNOW};

	public ItemDrill()
	{
		super("drill", 1, "DRILL", "diesel");
	}
	@Override
	public int getInternalSlots(ItemStack stack)
	{
		return 5;
	}
	@Override
	public Slot[] getWorkbenchSlots(Container container, ItemStack stack, IInventory invItem)
	{
		return new Slot[]
				{
						new IESlot.DrillHead(container, invItem,0, 98,22),
						new IESlot.Upgrades(container, invItem,1,  78,42, "DRILL", stack, true),
						new IESlot.Upgrades(container, invItem,2,  98,52, "DRILL", stack, true),
						new IESlot.Upgrades(container, invItem,3, 118,42, "DRILL", stack, true),
						new IESlot.Shader(container, invItem,4,150,32, stack)
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
		FluidStack fs = this.getFluid(stack);
		if(fs!=null && fs.amount>this.getCapacity(stack))
		{
			fs.amount = this.getCapacity(stack);
			ItemNBTHelper.setFluidStack(stack, "fuel",fs);
		}
	}
	@Override
	public void clearUpgrades(ItemStack stack)
	{
		super.clearUpgrades(stack);
		FluidStack fs = getFluid(stack);
		if(fs!=null && fs.amount > getCapacity(stack))
		{
			fs.amount = getCapacity(stack);
			ItemNBTHelper.setFluidStack(stack, "fuel", fs);
		}
	}
	@Override
	public void setShaderItem(ItemStack stack, ItemStack shader)
	{
		ItemStack[] contained = this.getContainedItems(stack);
		contained[4] =  shader;
		this.setContainedItems(stack, contained);
	}

	@Override
	public ItemStack getShaderItem(ItemStack stack)
	{
		ItemStack[] contained = this.getContainedItems(stack);
		return contained[4];
	}
	@Override
	public String getShaderType()
	{
		return "drill";
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		ItemStack shader = getShaderItem(stack);
		if(shader!=null)
			list.add(TextFormatting.DARK_GRAY+shader.getDisplayName());

		FluidStack fs = getFluid(stack);
		if(fs!=null)
			list.add(I18n.format(Lib.DESC_FLAVOUR+"drill.fuel")+" "+fs.amount+"/"+getCapacity(stack)+"mB");
		else
			list.add(I18n.format(Lib.DESC_FLAVOUR+"drill.empty"));
		if(getHead(stack)==null)
			list.add(I18n.format(Lib.DESC_FLAVOUR+"drill.noHead"));
		else
		{
			int maxDmg = getMaxHeadDamage(stack);
			int dmg = maxDmg-getHeadDamage(stack);
			float quote = dmg/(float)maxDmg;
			String status = ""+(quote<.1? TextFormatting.RED: quote<.3? TextFormatting.GOLD: quote<.6? TextFormatting.YELLOW: TextFormatting.GREEN);
			list.add(I18n.format(Lib.DESC_FLAVOUR+"headDamage")+" "+status+dmg+"/"+maxDmg);
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
			else if(animationTimer.get(entityLiving.getName())<20)
				animationTimer.put(entityLiving.getName(), 20);
		}
		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public TextureAtlasSprite getTextureReplacement(ItemStack stack, String material)
	{
		if(material.equals("head") && this.getHead(stack)!=null && this.getHead(stack).getItem() instanceof IDrillHead)
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
			return upgrades.getInteger("speed")>0;
			if(this.getHead(stack)!=null)
			{
				if(group.equals("drill_head"))
					return true;

				if(group.equals("upgrade_damage0"))
					return upgrades.getInteger("damage")>0;
					if(group.equals("upgrade_damage1")||group.equals("upgrade_damage2"))
						return upgrades.getInteger("damage")>1;
						if(group.equals("upgrade_damage3")||group.equals("upgrade_damage4"))
							return upgrades.getInteger("damage")>2;
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
			if(group.equals("drill_head") &&  upgrades.getInteger("damage")<=0)
			{
				Matrix4 mat = new Matrix4(transform.get().getMatrix());
				mat.translate(-.25f,0,0);
				return Optional.of(new TRSRTransformation(mat.toMatrix4f()));
			}
			if(group.equals("upgrade_damage1")||group.equals("upgrade_damage2")||group.equals("upgrade_damage3")||group.equals("upgrade_damage4"))
			{
				Matrix4 mat = new Matrix4(transform.get().getMatrix());
				mat.translate(.441f,0,0);
				return Optional.of(new TRSRTransformation(mat.toMatrix4f()));
			}
		}
		return transform;
	}
	@SideOnly(Side.CLIENT)
	@Override
	public Matrix4 handlePerspective(ItemStack stack, TransformType cameraTransformType, Matrix4 perspective)
	{
		return perspective;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack)
	{
		return (double)getHeadDamage(stack) / (double)getMaxHeadDamage(stack);
	}
	@Override
	public boolean showDurabilityBar(ItemStack stack)
	{
		return getHeadDamage(stack)>0;
	}
	@Override
	public EnumAction getItemUseAction(ItemStack p_77661_1_)
	{
		return EnumAction.BOW;
	}

	@Override
	public void removeFromWorkbench(EntityPlayer player, ItemStack stack)
	{
		ItemStack[] contents = this.getContainedItems(stack);
		if(contents[0]!=null&&contents[1]!=null&&contents[2]!=null&&contents[3]!=null)
			player.addStat(IEAchievements.upgradeDrill);
	}

	/*INVENTORY STUFF*/
	public ItemStack getHead(ItemStack drill)
	{
		ItemStack head = this.getContainedItems(drill)[0];
		return head!=null&&head.getItem() instanceof IDrillHead?head: null;
	}
	public void setHead(ItemStack drill, ItemStack head)
	{
		ItemStack[] inv = this.getContainedItems(drill);
		inv[0] = head;
		this.setContainedItems(drill, inv);
	}

	/*TOOL STUFF*/
	@Override
	public boolean isTool(ItemStack item)
	{
		return true;
	}

	public boolean canDrillBeUsed(ItemStack drill, EntityLivingBase player)
	{
		if(drill.getItemDamage()==0 && player.isInsideOfMaterial(Material.WATER) && !getUpgrades(drill).getBoolean("waterproof"))
			return false;
		return !(drill.getItemDamage() == 0 && getFluid(drill) == null);
	}

	public int getMaxHeadDamage(ItemStack stack)
	{
		ItemStack head = getHead(stack);
		return head!=null?((IDrillHead)head.getItem()).getMaximumHeadDamage(head): 0;
	}
	public int getHeadDamage(ItemStack stack)
	{
		ItemStack head = getHead(stack);
		return head!=null?((IDrillHead)head.getItem()).getHeadDamage(head): 0;
	}
	public boolean isDrillBroken(ItemStack stack)
	{
		return getHeadDamage(stack)>=getMaxHeadDamage(stack) || this.getFluid(stack)==null || this.getFluid(stack).amount<1;
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
		if((double)state.getBlockHardness(world, pos) != 0.0D)
		{
			int dmg = ForgeHooks.isToolEffective(world, pos, stack)?1:3;
			ItemStack head = getHead(stack);
			if(head!=null)
			{
				if(living instanceof EntityPlayer)
				{
					if(((EntityPlayer)living).capabilities.isCreativeMode)
						return true;
					((IDrillHead)head.getItem()).afterBlockbreak(stack, head, (EntityPlayer)living);
				}
				((IDrillHead)head.getItem()).damageHead(head, dmg);
				this.setHead(stack, head);
				this.drain(stack, 1, true);
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
		ItemStack head = getHead(stack);
		Multimap multimap = super.getAttributeModifiers(slot, stack);
		if(head!=null && slot==EntityEquipmentSlot.MAINHAND)
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getAttributeUnlocalizedName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", ((IDrillHead)head.getItem()).getAttackDamage(head)+getUpgrades(stack).getInteger("damage"), 0));
		return multimap;
	}
	@Override
	public int getHarvestLevel(ItemStack stack, String toolClass)
	{
		ItemStack head = getHead(stack);
		if(head!=null)
			return ((IDrillHead)head.getItem()).getMiningLevel(head)+ItemNBTHelper.getInt(stack, "harvestLevel");
		return 0;
	}
	@Override
	public Set<String> getToolClasses(ItemStack stack)
	{
		if(getHead(stack)!=null && !isDrillBroken(stack))
			return ImmutableSet.of("pickaxe");
		return super.getToolClasses(stack);
	}

	public boolean isEffective(Material mat)
	{
		for(Material m : validMaterials)
			if(m == mat)
				return true;
		return false;
	}
	@Override
	public boolean canHarvestBlock(IBlockState state, ItemStack stack)
	{
		return isEffective(state.getMaterial()) && !isDrillBroken(stack);
	}
	@Override
	public float getStrVsBlock(ItemStack stack, IBlockState state)
	{
		ItemStack head = getHead(stack);
		if(head!=null && !isDrillBroken(stack))
			return ((IDrillHead)head.getItem()).getMiningSpeed(head)+ItemNBTHelper.getInt(stack, "speed");
		return super.getStrVsBlock(stack, state);
	}
	public boolean canBreakExtraBlock(World world, Block block, BlockPos pos, IBlockState state, EntityPlayer player, ItemStack drill, ItemStack head, boolean inWorld)
	{
		if(block.canHarvestBlock(world, pos, player) && isEffective(state.getMaterial()) && !isDrillBroken(drill))
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
		World world = player.worldObj;
		if(player.isSneaking() || world.isRemote || !(player instanceof EntityPlayerMP))
			return false;
		RayTraceResult mop = this.rayTrace(world, player, true);
		ItemStack head = getHead(stack);
		if(mop==null || head==null || this.isDrillBroken(stack))
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

			if(block!=null && !block.isAir(state, world, pos) && state.getPlayerRelativeBlockHardness(player, world, pos) != 0)
			{
				if(!this.canBreakExtraBlock(world, block, pos, state, player, stack, head, true))
					continue;
				int xpDropEvent = ForgeHooks.onBlockBreakEvent(world, ((EntityPlayerMP)player).interactionManager.getGameType(), (EntityPlayerMP) player, pos);
				if(xpDropEvent<0)
					continue;

				if(player.capabilities.isCreativeMode)
				{
					block.onBlockHarvested(world, pos, state, player);
					if (block.removedByPlayer(state, world, pos, player, false))
						block.onBlockDestroyedByPlayer(world, pos, state);
				} 
				else
				{
					block.onBlockHarvested(world, pos, state, player);
					if(block.removedByPlayer(state, world, pos, player, true))
					{
						block.onBlockDestroyedByPlayer( world, pos, state);
						block.harvestBlock(world, player, pos, state, world.getTileEntity(pos), stack);
						block.dropXpOnBlockBreak(world, pos, xpDropEvent);
					}
					stack.onBlockDestroyed(world, state, pos, player);
				}
				world.playEvent(2001, pos, Block.getStateId(state));
				((EntityPlayerMP)player).connection.sendPacket(new SPacketBlockChange(world, pos));
			}
		}
		return false;
	}

	/*FLUID STUFF*/
	@Override
	public FluidStack getFluid(ItemStack container)
	{
		return ItemNBTHelper.getFluidStack(container, "fuel");
	}
	@Override
	public int getCapacity(ItemStack container)
	{
		return 2000+getUpgrades(container).getInteger("capacity");
	}
	@Override
	public int fill(ItemStack container, FluidStack resource, boolean doFill)
	{
		if(resource!=null && DieselHandler.isValidDrillFuel(resource.getFluid()))
		{
			FluidStack fs = getFluid(container);
			if(fs!=null && !fs.isFluidEqual(resource))
				return 0;
			int space = fs==null?getCapacity(container): getCapacity(container)-fs.amount;
			int accepted = Math.min(space, resource.amount);
			if(fs==null)
				fs = new FluidStack(resource, accepted);
			else
				fs.amount += accepted;
			if(doFill)
				ItemNBTHelper.setFluidStack(container, "fuel", fs);
			return accepted;
		}
		return 0;
	}
	@Override
	public FluidStack drain(ItemStack container, int maxDrain, boolean doDrain)
	{
		FluidStack fs = getFluid(container);
		if(fs == null)
			return null;
		int drained = Math.min(maxDrain, fs.amount);
		FluidStack stack = new FluidStack(fs, drained);
		if(doDrain)
		{
			fs.amount -= drained;
			if(fs.amount <= 0)
				ItemNBTHelper.remove(container, "fuel");
			else
				ItemNBTHelper.setFluidStack(container, "fuel", fs);
		}
		return stack;
	}
	/*===================================== FLUIDS END =================================*/
}