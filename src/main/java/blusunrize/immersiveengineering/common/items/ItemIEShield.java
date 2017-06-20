package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.block.BlockDispenser;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemIEShield extends ItemIEBase implements IOBJModelCallback<ItemStack>
{
	public ItemIEShield()
	{
		super("shield", 1);
		this.addPropertyOverride(new ResourceLocation("blocking"), (stack, worldIn, entityIn) -> entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F);
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, ItemArmor.DISPENSER_BEHAVIOR);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
	}

	public void damageShield(ItemStack stack, EntityPlayer player, int damage, DamageSource source, float amount)
	{

	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 72000;
	}
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
	{
		ItemStack itemstack = playerIn.getHeldItem(handIn);
		playerIn.setActiveHand(handIn);
		return new ActionResult(EnumActionResult.SUCCESS, itemstack);
	}
	@Override
	public EnumAction getItemUseAction(ItemStack stack)
	{
		return EnumAction.BLOCK;
	}

	@Override
	public TextureAtlasSprite getTextureReplacement(ItemStack object, String material)
	{
		return null;
	}
	@Override
	public boolean shouldRenderGroup(ItemStack object, String group)
	{
		return true;
	}
	@Override
	public Matrix4 handlePerspective(ItemStack Object, TransformType cameraTransformType, Matrix4 perspective, EntityLivingBase entity)
	{
		if(entity!=null && entity.isHandActive())
			if((entity.getActiveHand()==EnumHand.MAIN_HAND) == (entity.getPrimaryHand()==EnumHandSide.RIGHT))
			{
				if(cameraTransformType==TransformType.FIRST_PERSON_RIGHT_HAND)
					perspective.rotate(-.15, 1, 0, 0).translate(-.25, .5, -.4375);
				else if(cameraTransformType==TransformType.THIRD_PERSON_RIGHT_HAND)
					perspective.rotate(0.52359, 1, 0, 0).rotate(0.78539, 0, 1, 0).translate(.40625, -.125, -.125);
			}
			else
			{
				if(cameraTransformType==TransformType.FIRST_PERSON_LEFT_HAND)
					perspective.rotate(.15, 1, 0, 0).translate(.25, .375, .4375);
				else if(cameraTransformType==TransformType.THIRD_PERSON_LEFT_HAND)
					perspective.rotate(-0.52359, 1, 0, 0).rotate(0.78539, 0, 1, 0).translate(.1875, .3125, .5625);
			}
		return perspective;
	}
}