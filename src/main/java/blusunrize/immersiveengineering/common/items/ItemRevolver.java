package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.shader.IShaderEquipableItem;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderCase.ShaderLayer;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.CommonProxy;
import blusunrize.immersiveengineering.common.entities.EntityRevolvershot;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IGuiItem;
import blusunrize.immersiveengineering.common.util.IEAchievements;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import blusunrize.immersiveengineering.common.util.network.MessageSpeedloaderSync;
import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

public class ItemRevolver extends ItemUpgradeableTool implements IShaderEquipableItem, IOBJModelCallback<ItemStack>, ITool, IGuiItem
{
	public ItemRevolver()
	{
		super("revolver", 1, "REVOLVER", "normal","speedloader");
	}
	public static UUID speedModUUID = Utils.generateNewUUID();
	public HashMap<String, TextureAtlasSprite> revolverIcons = new HashMap();
	public TextureAtlasSprite revolverDefaultTexture;
	public void stichRevolverTextures(TextureMap map)
	{
		revolverDefaultTexture = ApiUtils.getRegisterSprite(map, "immersiveengineering:revolvers/revolver");
		for(String key : specialRevolversByTag.keySet())
			if(!key.isEmpty() && !specialRevolversByTag.get(key).tag.isEmpty())
			{
				int split = key.lastIndexOf("_");
				if(split<0)
					split = key.length();
				revolverIcons.put(key, ApiUtils.getRegisterSprite(map, "immersiveengineering:revolvers/revolver_"+key.substring(0,split)));
			}
	}

	@Override
	public int getInternalSlots(ItemStack stack)
	{
		return 18+2+1;
	}
	@Override
	public Slot[] getWorkbenchSlots(Container container, ItemStack stack, IInventory invItem)
	{
		return new Slot[]
				{
						new IESlot.Upgrades(container, invItem,18+0, 80,32, "REVOLVER", stack, true),
						new IESlot.Upgrades(container, invItem,18+1,100,32, "REVOLVER", stack, true),
						new IESlot.Shader(container, invItem,20,130,32, stack)
				};
	}
	@Override
	public boolean canModify(ItemStack stack)
	{
		return stack.getItemDamage()!=1;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
	{
		return slotChanged|!ItemStack.areItemStacksEqual(getShaderItem(oldStack),getShaderItem(newStack))|super.shouldCauseReequipAnimation(oldStack,newStack,slotChanged);
	}

	@Override
	public void setShaderItem(ItemStack stack, ItemStack shader)
	{
		ItemStack[] contained = this.getContainedItems(stack);
		contained[20] =  shader;
		this.setContainedItems(stack, contained);
	}
	@Override
	public ItemStack getShaderItem(ItemStack stack)
	{
		ItemStack[] contained = this.getContainedItems(stack);
		return contained[20];
	}
	@Override
	public String getShaderType()
	{
		return "immersiveengineering:revolver";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list)
	{
		for(int i=0;i<2;i++)
			list.add(new ItemStack(this,1,i));
		//		for(Map.Entry<String, SpecialRevolver> e : specialRevolversByTag.entrySet())
		//		{
		//			ItemStack stack = new ItemStack(this,1,0);
		//			applySpecialCrafting(stack, e.getValue());
		//			this.recalculateUpgrades(stack);
		//			list.add(stack);
		//		}
	}
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		if(stack.getItemDamage()!=1)
		{
			String tag = getRevolverDisplayTag(stack);
			if(!tag.isEmpty())
				list.add(I18n.format(Lib.DESC_FLAVOUR+"revolver."+tag));
			else if(ItemNBTHelper.hasKey(stack, "flavour"))
				list.add(I18n.format(Lib.DESC_FLAVOUR+"revolver."+ItemNBTHelper.getString(stack, "flavour")));
			else if(stack.getItemDamage()==0)
				list.add(I18n.format(Lib.DESC_FLAVOUR+"revolver"));

			ItemStack shader = getShaderItem(stack);
			if(shader!=null)
			{
				list.add(TextFormatting.DARK_GRAY+shader.getDisplayName());
				ShaderCase sCase = ((IShaderItem)shader.getItem()).getShaderCase(shader, shader, getShaderType());
				for(ShaderLayer layer : sCase.getLayers())
				{
					TextureAtlasSprite sprite = ClientUtils.getSprite(layer.getTexture());
					list.add("  " + layer.getTexture().getResourcePath() + " ~ " +(sprite.getIconName()));
				}
			}
		}
	}
	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		if(stack.getItemDamage()!=1)
		{
			String tag = getRevolverDisplayTag(stack);
			if(!tag.isEmpty())
				return this.getUnlocalizedName()+"."+tag;
		}
		return super.getUnlocalizedName(stack);
	}
	@Override
	public boolean isFull3D()
	{
		return true;
	}

	@Override
	public Multimap getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack)
	{
		Multimap multimap = super.getAttributeModifiers(slot, stack);
		if(slot==EntityEquipmentSlot.MAINHAND)
		{
			double melee = getUpgrades(stack).getDouble("melee");
			if(melee != 0)
				multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getAttributeUnlocalizedName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", melee, 0));
			double speed = getUpgrades(stack).getDouble("speed");
			if(speed != 0)
				multimap.put(SharedMonsterAttributes.MOVEMENT_SPEED.getAttributeUnlocalizedName(), new AttributeModifier(speedModUUID, "Weapon modifier", speed, 1));
		}
		return multimap;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack p_77661_1_)
	{
		return EnumAction.BOW;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity ent, int slot, boolean inHand)
	{
		if(!world.isRemote && stack.getItemDamage()!=1 && ent!=null && ItemNBTHelper.hasKey(stack, "blocked"))
		{
			int l = ItemNBTHelper.handleDelayedSoundsForStack(stack, "casings", ent);
			if(l==0)
				ItemNBTHelper.setDelayedSoundsForStack(stack, "cylinderFill", "tile.piston.in",.3f,3, 1,6,1);
			l = ItemNBTHelper.handleDelayedSoundsForStack(stack, "cylinderFill", ent);
			if(l==0)
				ItemNBTHelper.setDelayedSoundsForStack(stack, "cylinderClose", "fire.ignite",.6f,5, 1,6,1);
			l = ItemNBTHelper.handleDelayedSoundsForStack(stack, "cylinderClose", ent);
			if(l==0)
				ItemNBTHelper.setDelayedSoundsForStack(stack, "cylinderSpin", "note.hat",.1f,5, 5,8,1);
			l = ItemNBTHelper.handleDelayedSoundsForStack(stack, "cylinderSpin", ent);
			if(l==0)
				ItemNBTHelper.remove(stack, "blocked");
		}
		if(!world.isRemote && ItemNBTHelper.hasKey(stack, "cooldown"))
		{
			int cooldown = ItemNBTHelper.getInt(stack, "cooldown")-1;
			if(cooldown<=0)
				ItemNBTHelper.remove(stack, "cooldown");
			else
				ItemNBTHelper.setInt(stack, "cooldown", cooldown);
		}
	}
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack revolver, World world, EntityPlayer player, EnumHand hand)
	{
		if(!world.isRemote)
		{
			if(player.isSneaking() || revolver.getItemDamage()==1)
			{
				CommonProxy.openGuiForItem(player, hand==EnumHand.MAIN_HAND? EntityEquipmentSlot.MAINHAND:EntityEquipmentSlot.OFFHAND);
				return new ActionResult(EnumActionResult.SUCCESS, revolver);
			}
			else if(this.getUpgrades(revolver).getBoolean("nerf"))
				world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1f, 0.6f);
			else
			{
				if(ItemNBTHelper.getInt(revolver, "cooldown")>0)
					return new ActionResult(EnumActionResult.PASS, revolver);

				ItemStack[] bullets = getBullets(revolver);

				if(isEmpty(revolver))
				{
					for(int i=0; i<player.inventory.getSizeInventory(); i++)
					{
						ItemStack loader = player.inventory.getStackInSlot(i);
						if(loader!=null && loader.getItem() == this && loader.getItemDamage()==1 && !isEmpty(loader))
						{
							int dc = 0;
							for(ItemStack b : bullets)
								if(b!=null)
								{
									world.spawnEntityInWorld(new EntityItem(world, player.posX,player.posY,player.posZ, b ));
									dc++;
								}
							world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.PLAYERS, .5f, 3f);
							ItemNBTHelper.setDelayedSoundsForStack(revolver, "casings", "random.successful_hit",.05f,5, dc/2, 8,2);
							setBullets(revolver, getBullets(loader));
							setBullets(loader, new ItemStack[8]);
							player.inventory.setInventorySlotContents(i, loader);
							player.inventory.markDirty();
							if (player instanceof EntityPlayerMP)
								ImmersiveEngineering.packetHandler.sendTo(new MessageSpeedloaderSync(i), (EntityPlayerMP)player);

							ItemNBTHelper.setBoolean(revolver, "blocked", true);
							return new ActionResult(EnumActionResult.SUCCESS, revolver);
						}
					}
				}

				if(!ItemNBTHelper.getBoolean(revolver, "blocked"))
				{
					if(bullets[0] != null && bullets[0].getItem() instanceof ItemBullet && ItemNBTHelper.hasKey(bullets[0], "bullet"))
					{
						String key = ItemNBTHelper.getString(bullets[0], "bullet");
						IBullet bullet = BulletHandler.getBullet(key);
						if(bullet != null)
						{
							Vec3d vec = player.getLookVec();
							boolean electro = getUpgrades(revolver).getBoolean("electro");
							int count = bullet.getProjectileCount(player, bullets[0]);
							if(count == 1)
							{
								Entity entBullet = getBullet(player, vec, vec, key, bullets[0], electro);
								player.worldObj.spawnEntityInWorld(bullet.getProjectile(player, bullets[0], entBullet, electro));
							} else
								for(int i = 0; i < count; i++)
								{
									Vec3d vecDir = vec.addVector(player.getRNG().nextGaussian() * .1, player.getRNG().nextGaussian() * .1, player.getRNG().nextGaussian() * .1);
									Entity entBullet = getBullet(player, vec, vecDir, key, bullets[0], electro);
									player.worldObj.spawnEntityInWorld(bullet.getProjectile(player, bullets[0], entBullet, electro));
								}
							bullets[0] = bullet.getCasing(bullets[0]);
							world.playSound(null, player.posX, player.posY, player.posZ, IESounds.revolverFire, SoundCategory.PLAYERS, 1f, 1f);
						} else
							world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_NOTE_HAT, SoundCategory.PLAYERS, 1f, 1f);
					}
					else
						world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_NOTE_HAT, SoundCategory.PLAYERS, 1f, 1f);

					ItemStack[] cycled = new ItemStack[getBulletSlotAmount(revolver)];
					for(int i=1; i<cycled.length; i++)
						cycled[i-1] = bullets[i];
					cycled[cycled.length-1] = bullets[0];
					setBullets(revolver, cycled);
					ItemNBTHelper.setInt(revolver, "cooldown", 10);
					return new ActionResult(EnumActionResult.SUCCESS, revolver);
				}
			}
		} else if(!player.isSneaking() && revolver.getItemDamage() == 0)
			return new ActionResult(ItemNBTHelper.getInt(revolver, "cooldown") > 0 ? EnumActionResult.PASS : EnumActionResult.SUCCESS, revolver);
		return new ActionResult(EnumActionResult.SUCCESS, revolver);
	}

	EntityRevolvershot getBullet(EntityPlayer player, Vec3d vecSpawn, Vec3d vecDir, String type, ItemStack stack, boolean electro)
	{
		EntityRevolvershot bullet = new EntityRevolvershot(player.worldObj, player, vecDir.xCoord * 1.5, vecDir.yCoord * 1.5, vecDir.zCoord * 1.5, type, stack);
		bullet.motionX = vecDir.xCoord;
		bullet.motionY = vecDir.yCoord;
		bullet.motionZ = vecDir.zCoord;
		bullet.bulletElectro = electro;
		return bullet;
	}

	public boolean isEmpty(ItemStack stack)
	{
		ItemStack[] bullets = getBullets(stack);
		boolean empty = true;
		for(ItemStack b : bullets)
			if(b != null && b.getItem() instanceof ItemBullet && ItemNBTHelper.hasKey(b, "bullet"))
				empty=false;
		return empty;
	}
	public ItemStack[] getBullets(ItemStack revolver)
	{
		ItemStack[] stackList = this.getContainedItems(revolver);
		ItemStack[] bullets = new ItemStack[getBulletSlotAmount(revolver)];
		System.arraycopy(stackList,0, bullets,0, bullets.length);
		return bullets;
	}
	public void setBullets(ItemStack revolver, ItemStack[] bullets)
	{
		ItemStack[] stackList = this.getContainedItems(revolver);
		for(int i=0; i<bullets.length; i++)
			stackList[i] = bullets[i];
		this.setContainedItems(revolver, stackList);
	}
	public int getBulletSlotAmount(ItemStack revolver)
	{
		return 8+this.getUpgrades(revolver).getInteger("bullets");
	}
	@Override
	public NBTTagCompound getUpgradeBase(ItemStack stack)
	{
		return ItemNBTHelper.getTagCompound(stack, "baseUpgrades");
	}

	public String getRevolverDisplayTag(ItemStack revolver)
	{
		String tag = ItemNBTHelper.getString(revolver, "elite");
		if(!tag.isEmpty())
		{
			int split = tag.lastIndexOf("_");
			if(split<0)
				split = tag.length();
			return tag.substring(0,split);
		}
		return "";
	}



	@SideOnly(Side.CLIENT)
	@Override
	public TextureAtlasSprite getTextureReplacement(ItemStack stack, String material)
	{
		String tag = ItemNBTHelper.getString(stack, "elite");
		if(!tag.isEmpty())
			return this.revolverIcons.get(tag);
		else
			return this.revolverDefaultTexture;
	}
	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldRenderGroup(ItemStack stack, String group)
	{
		if(group.equals("revolver_frame")||group.equals("barrel")||group.equals("cosmetic_compensator"))
			return true;

		HashSet<String> render = new HashSet<String>();
		String tag = ItemNBTHelper.getString(stack, "elite");
		String flavour = ItemNBTHelper.getString(stack, "flavour");
		if(tag!=null && !tag.isEmpty() && specialRevolversByTag.containsKey(tag))
		{
			SpecialRevolver r = specialRevolversByTag.get(tag);
			if(r!=null && r.renderAdditions!=null)
				for(String ss : r.renderAdditions)
					render.add(ss);
		}
		else if(flavour!=null && !flavour.isEmpty() && specialRevolversByTag.containsKey(flavour))
		{
			SpecialRevolver r = specialRevolversByTag.get(flavour);
			if(r!=null && r.renderAdditions!=null)
				for(String ss : r.renderAdditions)
					render.add(ss);
		}
		NBTTagCompound upgrades = this.getUpgrades(stack);
		if(upgrades.getInteger("bullets")>0 && !render.contains("dev_mag"))
			render.add("player_mag");
		if(upgrades.getDouble("melee")>0 && !render.contains("dev_bayonet"))
		{
			render.add("bayonet_attachment");
			render.add("player_bayonet");
		}
		if(upgrades.getBoolean("electro"))
		{
			render.add("player_electro_0");
			render.add("player_electro_1");
		}
		return render.contains(group);
	}
	@SideOnly(Side.CLIENT)
	@Override
	public Optional<TRSRTransformation> applyTransformations(ItemStack stack, String group, Optional<TRSRTransformation> transform)
	{
		return transform;
	}
	@SideOnly(Side.CLIENT)
	@Override
	public Matrix4 handlePerspective(ItemStack stack, TransformType cameraTransformType, Matrix4 perspective)
	{
		return perspective;
	}

	public String[] compileRender(ItemStack revolver)
	{
		HashSet<String> render = new HashSet<String>();
		render.add("revolver_frame");
		render.add("barrel");
		render.add("cosmetic_compensator");
		String tag = ItemNBTHelper.getString(revolver, "elite");
		String flavour = ItemNBTHelper.getString(revolver, "flavour");
		if(tag!=null && !tag.isEmpty() && specialRevolversByTag.containsKey(tag))
		{
			SpecialRevolver r = specialRevolversByTag.get(tag);
			if(r!=null && r.renderAdditions!=null)
				for(String ss : r.renderAdditions)
					render.add(ss);
		}
		else if(flavour!=null && !flavour.isEmpty() && specialRevolversByTag.containsKey(flavour))
		{
			SpecialRevolver r = specialRevolversByTag.get(flavour);
			if(r!=null && r.renderAdditions!=null)
				for(String ss : r.renderAdditions)
					render.add(ss);
		}
		NBTTagCompound upgrades = this.getUpgrades(revolver);
		if(upgrades.getInteger("bullets")>0 && !render.contains("dev_mag"))
			render.add("player_mag");
		if(upgrades.getDouble("melee")>0 && !render.contains("dev_bayonet"))
		{
			render.add("bayonet_attachment");
			render.add("player_bayonet");
		}
		if(upgrades.getBoolean("electro"))
		{
			render.add("player_electro_0");
			render.add("player_electro_1");
		}
		return render.toArray(new String[render.size()]);
	}


	@Override
	public void onCreated(ItemStack stack, World world, EntityPlayer player)
	{
		if(stack==null || player==null)
			return;

		if(stack.getItemDamage()==1)
			return;
		String uuid = player.getUniqueID().toString();
		if(specialRevolvers.containsKey(uuid))
		{
			ArrayList<SpecialRevolver> list = new ArrayList(specialRevolvers.get(uuid));
			if(!list.isEmpty())
			{
				list.add(null);
				String existingTag = ItemNBTHelper.getString(stack, "elite");
				if(existingTag.isEmpty())
					applySpecialCrafting(stack, list.get(0));
				else
				{
					int i=0;
					for(; i<list.size(); i++)
						if(list.get(i)!=null && existingTag.equals(list.get(i).tag))
							break;
					int next = (i+1)%list.size();
					applySpecialCrafting(stack, list.get(next));
				}
			}
		}
		this.recalculateUpgrades(stack);
	}
	public void applySpecialCrafting(ItemStack stack, SpecialRevolver r)
	{
		if(r==null)
		{
			ItemNBTHelper.remove(stack, "elite");
			ItemNBTHelper.remove(stack, "flavour");
			ItemNBTHelper.remove(stack, "baseUpgrades");
			return;
		}
		if(r.tag!=null && !r.tag.isEmpty())
			ItemNBTHelper.setString(stack, "elite", r.tag);
		if(r.flavour!=null && !r.flavour.isEmpty())
			ItemNBTHelper.setString(stack, "flavour", r.flavour);
		NBTTagCompound baseUpgrades = new NBTTagCompound();
		for(Map.Entry<String, Object> e : r.baseUpgrades.entrySet())
		{
			if(e.getValue() instanceof Boolean)
				baseUpgrades.setBoolean(e.getKey(), (Boolean)e.getValue());
			else if(e.getValue() instanceof Integer)
				baseUpgrades.setInteger(e.getKey(), (Integer)e.getValue());
			else if(e.getValue() instanceof Float)
				baseUpgrades.setDouble(e.getKey(), (Float)e.getValue());
			else if(e.getValue() instanceof Double)
				baseUpgrades.setDouble(e.getKey(), (Double)e.getValue());
			else if(e.getValue() instanceof String)
				baseUpgrades.setString(e.getKey(), (String)e.getValue());
		}
		ItemNBTHelper.setTagCompound(stack, "baseUpgrades", baseUpgrades);
	}
	@Override
	public void removeFromWorkbench(EntityPlayer player, ItemStack stack)
	{
		ItemStack[] contents = this.getContainedItems(stack);
		if(contents[18]!=null&&contents[19]!=null)
			player.addStat(IEAchievements.upgradeRevolver);
	}

	public static final ArrayListMultimap<String, SpecialRevolver> specialRevolvers = ArrayListMultimap.create();
	public static final Map<String, SpecialRevolver> specialRevolversByTag = new HashMap<String, SpecialRevolver>();

	@Override
	public int getGuiID(ItemStack stack)
	{
		return Lib.GUIID_Revolver;
	}


	public static class SpecialRevolver
	{
		public final String[] uuid;
		public final String tag;
		public final String flavour;
		public final HashMap<String, Object> baseUpgrades;
		public final String[] renderAdditions;
		public SpecialRevolver(String[] uuid, String tag, String flavour, HashMap<String, Object> baseUpgrades, String[] renderAdditions)
		{
			this.uuid=uuid;
			this.tag=tag;
			this.flavour=flavour;
			this.baseUpgrades=baseUpgrades;
			this.renderAdditions=renderAdditions;
		}
	}

	@Override
	public boolean isTool(ItemStack item)
	{
		return true;
	}
}