/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.blocks.BlockIETileProvider;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class BlockClothDevice extends BlockIETileProvider<BlockTypes_ClothDevice>
{
	public BlockClothDevice()
	{
		super("cloth_device", Material.CLOTH, PropertyEnum.create("type", BlockTypes_ClothDevice.class), ItemBlockClothDevice.class, IEProperties.FACING_ALL, IEProperties.BOOLEANS[0], IOBJModelCallback.PROPERTY, CapabilityShader.BLOCKSTATE_PROPERTY, IEProperties.CONNECTIONS);
		setHardness(0.8F);
		setHasColours();
		setMetaLightOpacity(BlockTypes_ClothDevice.BALLOON.getMeta(), 0);
		setMetaLightOpacity(BlockTypes_ClothDevice.STRIPCURTAIN.getMeta(), 0);
		setMetaLightOpacity(BlockTypes_ClothDevice.SHADER_BANNER.getMeta(), 0);
		setMetaBlockLayer(BlockTypes_ClothDevice.BALLOON.getMeta(), BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT);
		setNotNormalBlock(BlockTypes_ClothDevice.BALLOON.getMeta());
		setNotNormalBlock(BlockTypes_ClothDevice.STRIPCURTAIN.getMeta());
		setNotNormalBlock(BlockTypes_ClothDevice.SHADER_BANNER.getMeta());
		setMetaHidden(BlockTypes_ClothDevice.SHADER_BANNER.getMeta());
	}

	@SideOnly(Side.CLIENT)
	public int getRenderColor(IBlockState state)
	{
		return 16777215;
	}

	//    @Override
//	@SideOnly(Side.CLIENT)
//    public int colorMultiplier(IBlockAccess worldIn, BlockPos pos, int renderPass)
//    {
//        return 16777215;
//    }
	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag tooltipFlag)
	{
		if(ItemNBTHelper.hasKey(stack, "colour"))
		{
			String hexCol = Integer.toHexString(ItemNBTHelper.getInt(stack, "colour"));
			tooltip.add(I18n.translateToLocalFormatted(Lib.DESC_INFO+"colour", "<hexcol="+hexCol+":#"+hexCol+">"));
		}
	}

	@Override
	public boolean useCustomStateMapper()
	{
		return true;
	}

	@Override
	public String getCustomStateMapping(int meta, boolean itemBlock)
	{
		if(meta==2)
			return "stripcurtain";
		return null;
	}

	@Override
	public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance)
	{
//        if(entityIn.isSneaking())
//        {
//            super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
//        }
//        else
//        {
		entityIn.fall(fallDistance, 0.0F);
//        }
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		state = super.getExtendedState(state, world, pos);
		if(state instanceof IExtendedBlockState)
		{
			IExtendedBlockState ext = (IExtendedBlockState)state;
			TileEntity te = world.getTileEntity(pos);
			if(te instanceof TileEntityImmersiveConnectable)
				ext = ext.withProperty(IEProperties.CONNECTIONS, ((TileEntityImmersiveConnectable)te).genConnBlockstate());
			state = ext;
		}
		return state;
	}

	@Override
	public boolean allowHammerHarvest(IBlockState state)
	{
		return true;
	}
//	@Override
//	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
//	{
//		TileEntity tile = world.getTileEntity(x, y, z);
//		if(tile instanceof TileEntityBalloon)
//		{
//			ItemStack equipped = player.getCurrentEquippedItem();
//			if(Utils.isHammer(equipped))
//			{
//				((TileEntityBalloon)tile).style = ((TileEntityBalloon)tile).style==0?1:0;
//				world.addBlockEvent(x, y, z, this, 0, 0);
//				return true;
//			}
//			else if(equipped!=null && equipped.getItem() instanceof IShaderItem)
//			{
//				((TileEntityBalloon)tile).shader = equipped;
//				world.addBlockEvent(x, y, z, this, 0, 0);
//				return true;
//			}
//			else
//			{
//				int target = 0;
//				int style = ((TileEntityBalloon)tile).style;
//				if(side<2 && style==0)
//					target = (hitX<.375||hitX>.625)&&(hitZ<.375||hitZ>.625)?1:0;
//				else if(side>=2&&side<4)
//				{
//					if(style==0)
//						target = (hitX<.375||hitX>.625)?1:0;
//					else
//						target =(hitY>.5625&&hitY<.75)?1:0;
//				}
//				else if(side>=4)
//				{
//					if(style==0)
//						target = (hitZ<.375||hitZ>.625)?1:0;
//					else
//						target =(hitY>.5625&&hitY<.75)?1:0;
//				}
//				int heldDye = Utils.getDye(equipped);
//				if(heldDye==-1)
//					return false;
//				if(target==0)
//				{
//					if(((TileEntityBalloon)tile).colour0==heldDye)
//						return false;
//					((TileEntityBalloon)tile).colour0 = (byte)heldDye;
//				}
//				else
//				{
//					if(((TileEntityBalloon)tile).colour1==heldDye)
//						return false;
//					((TileEntityBalloon)tile).colour1 = (byte)heldDye;
//				}
//				world.addBlockEvent(x, y, z, this, 0, 0);
//				return true;
//			}
//		}
//		return false;
//	}


	@Override
	public TileEntity createBasicTE(World world, BlockTypes_ClothDevice type)
	{
		switch(type)
		{
			case CUSHION:
				return null;
			case BALLOON:
				return new TileEntityBalloon();
			case STRIPCURTAIN:
				return new TileEntityStripCurtain();
			case SHADER_BANNER:
				return new TileEntityShaderBanner();
		}
		return null;
	}
}